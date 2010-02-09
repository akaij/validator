import static org.junit.Assert.*;

import java.io.*;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.validator.Rule;
import org.biopax.validator.rules.*;
import org.biopax.validator.utils.*;
import org.junit.Test;


import org.biopax.paxtools.model.level3.Process;


/**
 * This test suite is also generates the examples (BioPAX L3 OWL files)
 * that illustrate the corresponding rule violation.
 * 
 * TODO Test all the L3 rules and generate OWL examples (for invalid cases).
 * TODO Also test valid use cases (look for 'false positives').
 * 
 * @author rodch
 */
public class Level3RulesUnitTest {

	static Level3Factory level3 = new Level3FactoryImpl(); // to create BioPAX objects
	static EditorMap editorMap = new SimpleEditorMap(BioPAXLevel.L3);
	static SimpleExporter exporter = new SimpleExporter(BioPAXLevel.L3);
	final static String TEST_DATA_DIR = "target";
	
	void writeExample(String file, Model model) {
    	try {
			exporter.convertToOWL(model, 
				new FileOutputStream(TEST_DATA_DIR + File.separator + file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
	
	
	@Test
	public void testBiochemicalPathwayStepProcessOnlyControlCRRule() 
		throws IOException
	{
		Rule rule = new BiochemicalPathwayStepProcessOnlyControlCRRule();	
		BiochemicalPathwayStep step = level3.createBiochemicalPathwayStep();
		step.setRDFId("step1");
		Conversion conv = level3.createBiochemicalReaction();
		conv.setRDFId("interaction1");
		conv.addComment("a conversion reaction (not Control)");
		step.addStepProcess((Process) conv);
		step.addComment("error: has not a Control type step process");
		try {
			rule.check(step); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			Model m = level3.createModel();
			m.add(conv);
			m.add(step);
			writeExample("testBiochemicalPathwayStepProcessOnlyControlCRRule.owl",m);
		}
		
	}
	
	
	@Test
	public void testCanCheckBiochemPathwayStepOneConversionRule() {
		Rule rule = new BiochemPathwayStepOneConversionRule();		
		BiochemicalPathwayStep bpstep = level3.createBiochemicalPathwayStep();
		BioPAXElement bpstepElement = level3.createBiochemicalPathwayStep();
		PathwayStep pstep = level3.createPathwayStep();
		BioPAXElement bpe = level3.createConversion(); // in real data, a subclass of Conversion should be used! 
		assertFalse(rule.canCheck(null));
		assertFalse(rule.canCheck(new Object()));
		assertFalse(rule.canCheck(pstep));
		assertFalse(rule.canCheck(bpe));
		assertTrue(rule.canCheck(bpstep));
		assertTrue(rule.canCheck(bpstepElement));
	}

	
	@Test
	public void testBiochemPathwayStepOneConversionRule() throws IOException {
		Rule rule = new BiochemPathwayStepOneConversionRule();	
		BiochemicalPathwayStep step = level3.createBiochemicalPathwayStep();
		step.setRDFId("step1");
		step.addComment("error: conversion cannot be a step process (only stepConversion)");
		
		//ok
		Conversion conv = level3.createBiochemicalReaction();
		conv.setRDFId("conversion1");
		step.setStepConversion(conv);
		rule.check(step); // shouldn't throw a BiopaxValidatorException
		
		//ok
		Catalysis catalysis = level3.createCatalysis();
		catalysis.setRDFId("catalysis1");
		catalysis.addComment("valid step process value");
		step.addStepProcess((Process)catalysis); //org.biopax.paxtools.model.level3.Process !!! :)
		rule.check(step);
		
		//illegal process
		step.addStepProcess((Process) conv);
		try {
			rule.check(step); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			// generate the example OWL
			Model m = level3.createModel();
			m.add(step);
			m.add(conv);
			m.add(catalysis);
			writeExample("testBiochemPathwayStepOneConversionRule.owl",m);
		}
	}

	//InteractionParticipantsLocationRule
	
	@Test
	public void testBiochemReactParticipantsLocationRule() throws IOException {
		Rule rule = new BiochemReactParticipantsLocationRule();
		BiochemicalReaction reaction = level3.createBiochemicalReaction();	
		reaction.setRDFId("#BiochemicalReaction");
		Dna left = level3.createDna(); left.setRDFId("#dna");
		Dna right = level3.createDna(); right.setRDFId("#modifiedDna");
		EntityFeature feature = level3.createFragmentFeature();
		feature.setRDFId("feature1");
		right.addFeature(feature);
		right.addComment("modified dna");
		
		DnaReference dnaReference = level3.createDnaReference();
		dnaReference.setRDFId("#dnaref");
		// set the same type (entity reference)
		left.setEntityReference(dnaReference);
		right.setEntityReference(dnaReference);

		CellularLocationVocabulary cl = level3.createCellularLocationVocabulary();
		CellularLocationVocabulary cr = level3.createCellularLocationVocabulary();
		cl.setRDFId("#cl"); cl.addTerm("cytoplasm");
		cr.setRDFId("#cr"); cr.addTerm("membrane");
		left.addName("dnaLeft");
		right.addName("dnaRight");
		reaction.addLeft(left);
		reaction.addRight(right);
		
		// ok
		left.setCellularLocation(cl); 
		right.setCellularLocation(cl); 
		rule.check(reaction);
		
		// test complex (rule cannot use entityReference to match "the same" entity)
		PhysicalEntity leftc = level3.createComplex(); 
		leftc.setRDFId("#complex");
		PhysicalEntity rightc = level3.createComplex(); 
		rightc.setRDFId("#modifiedCmplex");
		leftc.addName("cplx1");
		rightc.addName("cplx1"); 
		rightc.setCellularLocation(cl);
		reaction.addLeft(leftc);
		reaction.addRight(rightc);
		leftc.setCellularLocation(cr);
		leftc.addComment("location changed without transport?");
		try {
			rule.check(reaction);
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) {
		}
		
		// different complex, another location is ok for this rule,
		// but this is another problem (other rule will catch this)
		// for complexes, not ER but names are used to match...
		leftc.removeName("cplx1");
		leftc.addName("cplx2");
		rule.check(reaction);
		
		right.setCellularLocation(cr); 
		right.addComment("location changed without transport?");
		// check for: same entity (names intersection), diff. location
		try {
			rule.check(reaction); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			// generate the example OWL
			Model m = level3.createModel();
			m.add(reaction);
			m.add(left); m.add(right);
			m.add(dnaReference);
			m.add(cl); m.add(cr);
			m.add(feature);
			m.add(leftc);
			m.add(rightc);
			writeExample("testBiochemReactParticipantsLocationRule.owl",m);
		}
		
		//same locations is ok
		leftc.removeName("cplx2");
		leftc.addName("cplx1");
		leftc.setCellularLocation(cr);
		rightc.setCellularLocation(cr);
		right.setCellularLocation(cr); 
		left.setCellularLocation(cr); 
		rule.check(reaction);
	}
	
	
	@Test
	public void testBiochemReactParticipantsLocationRule_Transport() throws IOException {
		Rule rule = new BiochemReactParticipantsLocationRule();
		BiochemicalReaction reaction = level3.createTransportWithBiochemicalReaction();	
		reaction.setRDFId("#transportWithBiochemicalReaction");
		reaction.addComment("This Transport contains one Rna that did not change its " +
			"cellular location (error!) and another one that did not have any (which is now ok)");
		
		// to generate example
		Model m = level3.createModel();
		m.add(reaction);
		
		Rna left = level3.createRna(); left.setRDFId("#Rna1");
		Rna right = level3.createRna(); right.setRDFId("#modRna1");
		EntityFeature feature = level3.createModificationFeature();
		feature.setRDFId("feature1");
		right.addFeature(feature);
		right.addComment("modified");
		RnaReference rnaReference = level3.createRnaReference();
		rnaReference.setRDFId("#rnaRef");
		// set the same type (entity reference)
		left.setEntityReference(rnaReference);
		right.setEntityReference(rnaReference);
		CellularLocationVocabulary cl = level3.createCellularLocationVocabulary();
		CellularLocationVocabulary cr = level3.createCellularLocationVocabulary();
		cl.setRDFId("#cl"); cl.addTerm("nucleus");
		cr.setRDFId("#cr"); cr.addTerm("cytoplasm");
		left.setDisplayName("rnaLeft1");
		right.setDisplayName("rnaRight1");
		reaction.addLeft(left);
		reaction.addRight(right);

		// set different locations
		left.setCellularLocation(cl); 
		right.setCellularLocation(cr); 
		// make sure it's valid
		rule.check(reaction);
		
		// now set the same location on both sides and check	
		right.setCellularLocation(cl); 
		try {
			rule.check(reaction); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
			m.add(left); m.add(right);
			m.add(rnaReference);
			m.add(cl); m.add(cr);
			m.add(feature);
		}
		
		// Now check with location is null on one side
		right = level3.createRna(); right.setRDFId("#Rna2");
		left = level3.createRna(); left.setRDFId("#modRna2");
		left.addFeature(feature);
		left.addComment("modified");
		
		// set the same type (entity reference)
		left.setEntityReference(rnaReference);
		right.setEntityReference(rnaReference);
		left.setDisplayName("rnaLeft2");
		right.setDisplayName("rnaRight2");
		reaction.addLeft(left);
		reaction.addRight(right);
		right.setCellularLocation(cr); 
		m.add(left);
		m.add(right);
		
		// generate the example OWL
		writeExample("testBiochemReactParticipantsLocationRule_Transport.owl", m);

		// fix the error for the first Rna:
		right = (Rna) m.getByID("#modRna1");
		right.setCellularLocation(cr);
		
		// check again
		rule.check(reaction);
	}
	
	
	
	@Test
	public void testBiopaxElementIdRule() throws IOException {
		Rule<BioPAXElement> rule = new BiopaxElementIdRule();
		Level3Element bpe = level3.createUnificationXref();
		bpe.setRDFId("Taxonomy_UnificationXref_40674");
		bpe.addComment("This is a valid ID");
		rule.check(bpe);
		
		Model m = level3.createModel(); // to later generate examples
		m.add(bpe);
		
		bpe = level3.createUnificationXref();
		bpe.setRDFId("Taxonomy UnificationXref_40674");
		bpe.addComment("Invalid ID (has a space)");
		try { 
			rule.check(bpe); 
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) 
		{
			m.add(bpe);
		}
		
		bpe = level3.createUnificationXref();
		bpe.setRDFId("Taxonomy:40674");
		bpe.addComment("Invalid ID (contains a colon)");
		try { 
			rule.check(bpe); 
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) 
		{
			m.add(bpe);
		}
		
		bpe = level3.createUnificationXref();
		bpe.setRDFId("123_Taxonomy");
		bpe.addComment("Invalid ID (starts with a digit)");
		try { 
			rule.check(bpe); 
			fail("must throw BiopaxValidatorException");
		} catch (BiopaxValidatorException e) 
		{
			m.add(bpe);
		}
		
		writeExample("testBiopaxElementIdRule.owl", m);
	}
	
    @Test
    public void testDuplicateNamesByExporter() throws IOException {
    	Protein p = level3.createProtein();
    	String name = "aDisplayName";
    	p.setRDFId("myProtein");
    	p.setDisplayName(name);
    	p.addComment("Display Name should not be repeated again in the Name property!");
    	Model m = level3.createModel();
    	m.add(p);
    	writeExample("testDuplicateNamesByExporter.xml", m);
    	
    	// read back and tricky-test
    	BufferedReader in = new BufferedReader(new FileReader(
    			TEST_DATA_DIR + File.separator + "testDuplicateNamesByExporter.xml"));
    	char[] buf = new char[1000];
    	in.read(buf);
    	String xml = new String(buf);
    	if(xml.indexOf(name) != xml.lastIndexOf(name)) {
    		fail("displayName gets duplicated by the SimpleExporter!");
    	}	
    }
    
    
    @Test
    public void testProteinReferenceOrganismCRRule() throws IOException {
    	ProteinReferenceOrganismCRRule rule = new ProteinReferenceOrganismCRRule();
    	rule.setEditorMap(editorMap); // in real application it's set automatically at load time
    	
    	BioSource bioSource = level3.createBioSource();
    	bioSource.setRDFId("BioSource-Human");
    	bioSource.setDisplayName("Homo sapiens");
    	UnificationXref taxonXref = level3.createUnificationXref();
    	taxonXref.setDb("taxonomy");
    	taxonXref.setRDFId("Taxonomy_UnificationXref_9606");
    	taxonXref.setId("9606");
    	bioSource.setTaxonXref(taxonXref);
    	ProteinReference pr = level3.createProteinReference();
    	pr.setRDFId("ProteinReference1");
    	pr.setDisplayName("ProteinReference1");
    	pr.addComment("No value is set for the 'organism' property (must be exactly one)!");
    	
    	try {
    		rule.check(pr);
    		fail("must throw BiopaxValidatorException");
    	} catch (Exception e) {
        	// write the example
        	Model m = level3.createModel();
        	m.add(taxonXref);
        	m.add(bioSource);
        	m.add(pr);
        	writeExample("testProteinReferenceOrganismCRRule", m);
		}
    	
    	pr.setOrganism(bioSource);
    	rule.check(pr);
    	
    	// thnx to the API, it's impossible to add more than one 'organism' value, so - no more tests are required
    }

	@Test
	public void testControlTypeRule() throws IOException
	{
		Rule rule = new ControlTypeRule();	
		Catalysis ca = level3.createCatalysis();
		ca.setRDFId("catalysis1");
		rule.check(ca); // controlType==null, no error expected
		ca.setControlType(ControlType.ACTIVATION);
		rule.check(ca); // no error expected
		ca.setControlType(ControlType.INHIBITION);
		ca.addComment("error: illegal controlType");
		try {
			rule.check(ca); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
		}
		
		TemplateReactionRegulation tr = level3.createTemplateReactionRegulation();
		tr.setRDFId("regulation1");
		tr.setControlType(ControlType.INHIBITION);
		rule.check(tr); // no error...
		tr.setControlType(ControlType.ACTIVATION_ALLOSTERIC);
		tr.addComment("error: illegal controlType");
		try {
			rule.check(tr); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
		}
		
		// write the example XML
		Model m = level3.createModel();
		ca.setControlType(ControlType.INHIBITION); // set wrong
		tr.setControlType(ControlType.ACTIVATION_ALLOSTERIC); // set bad
		m.add(ca);
		m.add(tr);
		writeExample("testControlTypeRule.owl",m);
	}
	
    
	@Test
	public void testDegradationConversionDirectionRule() throws IOException
	{
		Rule rule = new DegradationConversionDirectionRule();
		Conversion dg = level3.createDegradation();
		dg.setRDFId("degradation-conversion-1");
		rule.check(dg); // direction is null, no error
		dg.setConversionDirection(ConversionDirectionType.REVERSIBLE);
		dg.addComment("error: illegal conversionDirection");
		try {
			rule.check(dg); 
			fail("must throw BiopaxValidatorException");
		} catch(BiopaxValidatorException e) {
		}
		
		// write the example
		Model m = level3.createModel();
		dg.setConversionDirection(ConversionDirectionType.REVERSIBLE);
		m.add(dg);
		writeExample("testDegradationConversionDirectionRule.owl",m);		
	}
    
}
