/**
 ** Copyright (c) 2010 Memorial Sloan-Kettering Cancer Center (MSKCC)
 ** and University of Toronto (UofT).
 **
 ** This is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** both UofT and MSKCC have no obligations to provide maintenance, 
 ** support, updates, enhancements or modifications.  In no event shall
 ** UofT or MSKCC be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** UofT or MSKCC have been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this software; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA;
 ** or find it at http://www.fsf.org/ or http://www.gnu.org.
 **/

package org.biopax.validator.utils;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.miriam.MiriamLink;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;

/**
 * BioPAX (Level 3) Normalizer, an advanced BioPAX utility 
 * to help pathway data integrating and linking.
 * 
 * @author rodche
 *
 */
public final class Normalizer {
	private static final Log log = LogFactory.getLog(Normalizer.class);
	
	private SimpleIOHandler biopaxReader;
	private ShallowCopy copier;
	private final Map<BioPAXElement,BioPAXElement> subs;
	private Model subsModel;
	private String description;
	private boolean fixDisplayName;
	private boolean inferPropertyOrganism;
	private boolean inferPropertyDataSource;
	private String xmlBase;
	//TODO add a "remove utility class duplicates/clones" option
	
	
	/**
	 * Constructor
	 */
	public Normalizer() {
		biopaxReader = new SimpleIOHandler(BioPAXLevel.L3);
		biopaxReader.mergeDuplicates(true);
		copier = new ShallowCopy(BioPAXLevel.L3);
		subs = new HashMap<BioPAXElement, BioPAXElement>();
		subsModel = biopaxReader.getFactory().createModel();
		fixDisplayName = true;
		inferPropertyOrganism = true;
		inferPropertyDataSource = true;
		xmlBase = "";
	}
	
	
	/**
	 * Normalizes BioPAX OWL data and returns
	 * the result as BioPAX OWL (string).
	 * 
	 * This public method is actually intended to use 
	 * outside the BioPAX Validator framework.
	 * 
	 * @param biopaxOwlData
	 * @return
	 */
	public String normalize(String biopaxOwlData) {
		
		if(biopaxOwlData == null || biopaxOwlData.length() == 0) 
			throw new IllegalArgumentException("no data. " + description);
		
		// quick-fix for older BioPAX L3 version (v0.9x) property 'taxonXref' (range: BioSource)
		biopaxOwlData = biopaxOwlData.replaceAll("taxonXref","xref");
		
		// build the model
		Model model = null;
		try {
			model = biopaxReader.convertFromOWL(
				new ByteArrayInputStream(biopaxOwlData.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Failed! " + description, e);
		}
		
		if(model == null) {
			throw new IllegalArgumentException("Failed to create Model! " 
					+ description);
		}
		
		// auto-convert to Level3 model
		if (model.getLevel() != BioPAXLevel.L3) {
			if (log.isInfoEnabled())
				log.info("Converting model to BioPAX Level3...");
			model = (new OneTwoThree()).filter(model);
		}
		
		normalize(model); // L3 only!
		
		// return as BioPAX OWL
		return convertToOWL(model);
	}
	

	private void normalizeXrefs(Model model) {
		// use a copy of the xrefs set (to avoid concurrent modif. exception)
		Set<? extends Xref> xrefs = new HashSet<Xref>(model.getObjects(Xref.class));
		for(Xref ref : xrefs) {
			String uri = uriForXref(getXmlBase(model), 
					ref.getDb(), ref.getId(), ref.getIdVersion(), (Class<? extends Xref>) ref.getModelInterface());				
			if(uri != null)
				addToReplacementMap(model, ref, uri);
		}		
		// update/replace xrefs now
		doSubs(model);
	}	


	/**
	 * Makes a Xref URI.
	 * 
	 * Miriam registry is used to get a standard db name
	 * and the identifiers.org URI, if possible.
	 * 
	 * @param xmlBase xml:base (common URI prefix for a BioPAX model)
	 * @param type Xref (sub-)class name
	 * @param dbName value for xref.db property
	 * @param id value for xref.id property
	 * @param other e.g., idVersion, dbVersion
	 * @return
	 */
	public static String uriForXref(final String xmlBase, 
		final String dbName, final String id, final String other, Class<? extends Xref> type) 
	{
		if(type == null)
			throw new IllegalArgumentException("null xref type");
		
		if(dbName == null || id == null)
			return null; //do not normalize (illegal/fake xref)

		String uri = null;
		String db = null;
			
		// try to find a standard URI, if exists, for a publication xref, 
		// or at least a standard name:
		try {
			//try to get the preferred name
			db = MiriamLink.getName(dbName);
			
			//a shortcut: try getting standard URI for a PublicationXref
			if(type.equals(PublicationXref.class)) 
			{
				return MiriamLink.getIdentifiersOrgURI(db, id);
			}
		} catch (IllegalArgumentException e) {
			if(log.isDebugEnabled())
				log.debug("generateURIForXref: Unknown db (not in Miriam collection): " 
						+ dbName + ". "  + e);
			db = dbName;
		}

		// if not returned above this point, then -
		// let's consistently build a new URI, anyway
		// (doing so ensures re-using of equivalent xrefs, i.e. no duplicate xrefs, for better data merging)
		StringBuilder sb = new StringBuilder(type.getSimpleName());
		sb.append(db).append(id);
		if(other != null)
			sb.append(other);
		
		
		uri = ((xmlBase!=null)?xmlBase:"") + ModelUtils.md5hex(sb.toString());
		
		return uri;
	}
	
	
	
	/*
	 * Replaces rdfid; removes the element from the model if (with new rdfid) it becomes duplicate
	 * Note: model loses its integrity (object properties fix is required after this)
	 */
	private void addToReplacementMap(Model model, BioPAXElement bpe, String newRdfid) 
	{
		// model has another object with the same (new) ID?
		if(model.containsID(newRdfid)) {
			// replace with the existing element (having new id)
			subs.put(bpe, model.getByID(newRdfid));
		} else if(subsModel.containsID(newRdfid)) {
			subs.put(bpe, subsModel.getByID(newRdfid));
		} else {
			// replace with its own copy that has new id
			BioPAXElement copy = copier.copy(bpe, newRdfid);
			subs.put(bpe, copy);
			subsModel.add(copy);
		}
	}

	
	/**
	 * Description of the model to normalize.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * Sets a new standard URI, if possible, 
	 * for a utility class object (except for Xref types).
	 * 
	 * @param model the BioPAX model
	 * @param bpe a utility class element, except for xref, to normalize
	 * @param uxref a unification xref to create new URI from its properties
	 * 
	 */
	private void normalizeID(Model model, UtilityClass bpe, UnificationXref uxref) 
	{	
		if(bpe instanceof Xref) {
			log.error("normalizeID is not supposed to " +
				"be called for Xrefs." + description);
			return;
		}
		
		final String db = uxref.getDb();
		final String id = uxref.getId();
		// not using dbVersion, idVersion prop...
		
		// get the standard ID
		String uri = null;
		try {
			// make a new ID for the element
			uri = MiriamLink.getIdentifiersOrgURI(db, id);

		} catch (Exception e) {
			log.error("Cannot get a Miriam standard ID for " + bpe 
				+ " (" + bpe.getModelInterface().getSimpleName()
				+ ") " + ", using " + db + ":" + id 
				+ ". " + e + ". " + description);
			return;
		}
		
		// if different id, edit the element
		if(uri != null)
			addToReplacementMap(model, bpe, uri);
	}
	
	
	private void fixDisplayName(Model model) {
		if (log.isInfoEnabled())
			log.info("Trying to auto-fix 'null' displayName...");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
					if (log.isInfoEnabled())
						log.info(e + " displayName auto-fix: "
								+ e.getDisplayName() + ". " + description);
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
					if (log.isInfoEnabled())
						log.info(e + " displayName auto-fix: " + dsp
							+ ". " + description);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
					}
				}
			}
		}
	}


	private String convertToOWL(Model model) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		(new SimpleIOHandler(model.getLevel())).convertToOWL(model, out);
		return out.toString();
	}


	private List<UnificationXref> getUnificationXrefsSorted(XReferrable referrable) {
		List<UnificationXref> urefs = new ArrayList<UnificationXref>(
			new ClassFilterSet<Xref,UnificationXref>(
				referrable.getXref(), UnificationXref.class)
		);
		
		for(UnificationXref ux : new ArrayList<UnificationXref>(urefs)) {
			if(ux.getDb() == null || ux.getId() == null) {
				// report error, try next xref
				log.warn("Won't consider the UnificationXref " +
					"having NULL 'db' or 'id' property: " + 
					ux + ", " + ux.getRDFId() + ". " + description);
				urefs.remove(ux);
			} 
		}
		
		Comparator<UnificationXref> comparator = new Comparator<UnificationXref>() {
			@Override
			public int compare(UnificationXref o1, UnificationXref o2) {
				String s1 = o1.getDb() + o1.getId();
				String s2 = o2.getDb() + o2.getId();
				return s1.compareTo(s2);
			}
		};
		
		Collections.sort(urefs, comparator);
		
		return urefs;
	}

	
	/**
	 * Gets the first preferred xref, if exists.
	 * Otherwise, - simply the first correct one.
	 * Preferred db values are:
	 * "entrez gene" - for NucleicAcidReference/NucleicAcidRegionReference;
	 * "uniprot" or "refseq" for ProteinReference;
	 * "chebi" - SMRs
	 * 
	 * @param bpe
	 * @return the "best" first unification xref 
	 */
	private UnificationXref getFirstUnificationXref(XReferrable bpe) 
	{
		UnificationXref toReturn = null;
		
		String preferredDb = null; 
		//use preferred db prefix for different type of ER
		if(bpe instanceof ProteinReference)
			preferredDb = "uniprot";
		else if(bpe instanceof SmallMoleculeReference)
			preferredDb = "chebi";
		else if(bpe instanceof NucleicAcidReference || bpe instanceof NucleicAcidRegionReference)
			preferredDb = "entrez";
		
		Collection<UnificationXref> orderedUrefs = getUnificationXrefsSorted(bpe);
		
		if(preferredDb == null && !orderedUrefs.isEmpty()) {
			toReturn = orderedUrefs.iterator().next(); 
			return toReturn;
		} else {
			for(UnificationXref uref : orderedUrefs) 
				if(uref.getDb().toLowerCase().startsWith(preferredDb)) {
					toReturn = uref;
					break;
				}
		}
		
		if(toReturn == null && bpe instanceof ProteinReference)
			for(UnificationXref uref : orderedUrefs) 
				if(uref.getDb().toLowerCase().startsWith("refseq")) {
					toReturn = uref;
					break;
				}
		
		return toReturn;
	}

	
	/**
	 * BioPAX normalization 
	 * (modifies the original Model)
	 * 
	 * @param model
	 * @throws NullPointerException if model is null
	 * @throws IllegalArgumentException if model is not Level3 BioPAX
	 */
	public void normalize(Model model) {
		
		if(model.getLevel() != BioPAXLevel.L3)
			throw new IllegalArgumentException("Not Level3 model. " +
				"Consider converting it first (e.g., with the PaxTools).");
		
		// clean/normalize xrefs first, because they gets used next!
		if(log.isInfoEnabled())
			log.info("Normalizing xrefs..." + description);
		normalizeXrefs(model);
		
		// fix displayName where possible
		if(fixDisplayName) {
			if(log.isInfoEnabled())
				log.info("Normalizing display names..." + description);
			fixDisplayName(model);
		}
			
		if(log.isInfoEnabled())
			log.info("Normalizing CVs and organisms..." + description);
		normalizeCVsAndBioSource(model);
		
//		if(log.isInfoEnabled())
//			log.info("Normalizing data sources (Provenance)..." + extraInfo());
//		normalizeProvenance(model);
		
		if(log.isInfoEnabled())
			log.info("Normalizing entity references..." + description);
		normalizeERs(model);
		
		// find/add lost (in replace) children
		if(log.isInfoEnabled())
			log.info("Repairing..." + description);
		model.repair(); // it does not remove dangling utility class objects (can be done separately, later, if needed)
		
		if(log.isInfoEnabled())
			log.info("Optional tasks (reasoning)..." + description);
		
		// auto-set dataSource property for all entities (top-down)
		if(inferPropertyDataSource) {
			ModelUtils.inferPropertyFromParent(model, "dataSource");//, Entity.class);
		}
		
		if(inferPropertyOrganism) {
			ModelUtils.inferPropertyFromParent(model, "organism");//, Gene.class, SequenceEntityReference.class, Pathway.class);
		}		 
		
		/* 
		 * We could also "fix" organism property, where it's null,
		 * a swell (e.g., using the value from the pathway);
		 * also - check those values in protein references actually
		 * correspond to what can be found in the UniProt by using
		 * unification xrefs's 'id'... But this, fortunately, 
		 * happens in the CPathMerger (a ProteinReference 
		 * comes from the Warehouse with organism property already set!)
		 */
	}

	
	private void normalizeCVsAndBioSource(Model model) {
		// process the rest of utility classes (selectively though)
		for(UtilityClass bpe : model.getObjects(UtilityClass.class)) 
		{
			if(bpe instanceof ControlledVocabulary || bpe instanceof BioSource) 
			{
				//note: it does not check/fix the CV term name if wrong or missing though...
				UnificationXref uref = getFirstUnificationXref((XReferrable) bpe);
				if (uref != null) 
					normalizeID(model, bpe, uref); // no idVersion for a CV or BS!
				else 
					if(log.isInfoEnabled())
						log.info("Cannot normalize " + bpe.getModelInterface().getSimpleName() 
							+ " : no unification xrefs found in " + bpe.getRDFId()
							+ ". " + description);
			} 
		}
		
		// replace/update elements in the model
		doSubs(model);
	}
	
	private void normalizeERs(Model model) {
		// process the rest of utility classes (selectively though)
		for (EntityReference bpe : model.getObjects(EntityReference.class)) {
			UnificationXref uref = getFirstUnificationXref(bpe);
			if (uref != null)
				normalizeID(model, bpe, uref);
			else if (log.isInfoEnabled())
				log.info("Cannot normalize EntityReference: "
						+ "no unification xrefs found in " + bpe.getRDFId()
						+ ". " + description);
		}
		
		// replace/update elements in the model
		doSubs(model);
	}
	

	/**
	 * Executes the batch replace/update 
	 * to the normalized equivalent objects.
	 * 
	 * @param model
	 */
	private void doSubs(Model model) {
		for(BioPAXElement e : subs.keySet()) {
			model.remove(e);
		}
		
		try {
			ModelUtils.replace(model, subs);
		} catch (Exception e) {
			log.error("Failed to replace IDs. " + description, e);
			return;
		}
		
		for(BioPAXElement e : subs.values()) {
			if(!model.contains(e))
				model.add(e);
		}
	
		for(BioPAXElement e : model.getObjects()) {
			ModelUtils.fixDanglingInverseProperties(e, model);
		}
		
		
		// clear internal tmp stuff
		subs.clear();
		subsModel = biopaxReader.getFactory().createModel();
	}

	
	/**
	 * Auto-generates standard and other names for the datasource
	 * from either its ID (if URN) or one of its existing names (preferably - standard name)
	 * 
	 * @param pro
	 */
	public static void autoName(Provenance pro) {
		if(!(pro.getRDFId().startsWith("urn:miriam:") || pro.getRDFId().startsWith("http://identifiers.org/"))
				&& pro.getName().isEmpty()) {
			if(log.isInfoEnabled())
				log.info("Skipping: cannot normalize Provenance: " + pro.getRDFId());
			
		}
		else { // i.e., 'name' is not empty or ID is the URN
			final SortedSet<String> names = new TreeSet<String>();
			
			String key = null;
			if(pro.getRDFId().startsWith("urn:miriam:") || pro.getRDFId().startsWith("http://identifiers.org/")) {
				key = pro.getRDFId();
			} else if (pro.getStandardName() != null) {
				key = pro.getStandardName();
			} else {
				key = pro.getDisplayName(); // can be null
			}
			
			if (key != null) {
				try {
					names.addAll(Arrays.asList(MiriamLink.getNames(key)));
					pro.setStandardName(MiriamLink.getName(key));
					// get the datasource description
					String description = MiriamLink.getDataTypeDef(pro.getStandardName());
					pro.addComment(description);
				} catch (IllegalArgumentException e) {
					// ignore (then, names is still empty...)
				}
			} 
			
			// when the above failed (no match in Miriam), or key was null -
			if(names.isEmpty()) {
				// finally, trying to find all valid names for each existing one
					for (String name : pro.getName()) {
						try {
							names.addAll(Arrays.asList(MiriamLink.getNames(name)));
						} catch (IllegalArgumentException e) {
							// ignore
						}
					}
					// pick up the first name, get the standard name
					if(!names.isEmpty())
						pro.setStandardName(MiriamLink
							.getName(names.iterator().next()));
			}
			
			// and add all the synonyms if any
			for(String name : names)
				pro.addName(name);
			
			//set display name if not set (standard name is set already)
			if(pro.getDisplayName() == null)
				pro.setDisplayName(pro.getStandardName());			
		}
	}
	
	/**
	 * Converts BioPAX L1 or L2 RDF/XML string data to BioPAX L3 string.
	 *
	 * @param biopaxData String
	 * @return
	 */
	public static String convertToLevel3(final String biopaxData) {
		String toReturn = "";
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is = new ByteArrayInputStream(biopaxData.getBytes());
			SimpleIOHandler io = new SimpleIOHandler();
			io.mergeDuplicates(true);
			Model model = io.convertFromOWL(is);
			if (model.getLevel() != BioPAXLevel.L3) {
				if (log.isInfoEnabled())
					log.info("Converting to BioPAX Level3... " + model.getXmlBase());
				model = (new OneTwoThree()).filter(model);
				if (model != null) {
					io.setFactory(model.getLevel().getDefaultFactory());
					io.convertToOWL(model, os);
					toReturn = os.toString();
				}
			} else {
				toReturn = biopaxData;
			}
		} catch(Exception e) {
			throw new RuntimeException(
				"Cannot convert to BioPAX Level3", e);
		}

		return toReturn;
	}
	
	
	/**
	 * Gets the xml:base to use with newly created BioPAX elements.
	 * 
	 * @param modelToNormalize
	 * @return
	 */
	private String getXmlBase(Model modelToNormalize) {
		if(xmlBase != null && !xmlBase.isEmpty())
			return xmlBase; //this one is preferred
		else
			return 
				(modelToNormalize.getXmlBase() != null) 
					? modelToNormalize.getXmlBase() : "";
	}
	
	
	public boolean isFixDisplayName() {
		return fixDisplayName;
	}
	public void setFixDisplayName(boolean fixDisplayName) {
		this.fixDisplayName = fixDisplayName;
	}
	
	
	public boolean isInferPropertyOrganism() {
		return inferPropertyOrganism;
	}
	public void setInferPropertyOrganism(boolean inferPropertyOrganism) {
		this.inferPropertyOrganism = inferPropertyOrganism;
	}
	
	
	public boolean isInferPropertyDataSource() {
		return inferPropertyDataSource;
	}
	public void setInferPropertyDataSource(boolean inferPropertyDataSource) {
		this.inferPropertyDataSource = inferPropertyDataSource;
	}
	
	
	public String getXmlBase() {
		return xmlBase;
	}
	public void setXmlBase(String xmlBase) {
		this.xmlBase = xmlBase;
	}
}