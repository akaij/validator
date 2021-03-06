package org.biopax.validator.impl;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.*;

import javax.annotation.PostConstruct;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.CvRestriction;
import org.biopax.validator.api.CvRule;
import org.biopax.validator.api.CvValidator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An abstract class for CV terms checks.
 * 
 * @author rodche
 *
 * @param <D> property domain
 */
public abstract class AbstractCvRule<D extends BioPAXElement> extends AbstractRule<D> implements CvRule<D> {
    
    @Autowired
	protected CvValidator ontologyManager;
    
    protected final Class<D> domain;
    protected final String property; // helps validate generic ControlledVocabulary instances
    protected final Set<CvRestriction> restrictions;
	private Set<String> validTerms;
	protected PropertyEditor<? super D, ?> editor;
  
    /**
     * Constructor.
     * 
     * @param domain a BioPAX class for which the CV terms restrictions apply
     * @param property the name of the BioPAX property to get controlled vocabularies or null
     * @param restrictions a list of beans, each defining names (a subtree of an ontology) that 
     * is either to include or exclude (when 'not' flag is set) from the valid names set.
     */
    public AbstractCvRule(Class<D> domain, String property, CvRestriction... restrictions)
    {
    	this.domain = domain;
    	this.property = property;
        this.restrictions = new HashSet<CvRestriction>(restrictions.length);
    	for(CvRestriction c: restrictions) {
        	this.restrictions.add(c);
        }    	
    }
	
    @PostConstruct
    public void init() {
    	if(ontologyManager != null) {
    		setValidTerms(ontologyManager.getValidTermNames(this));
    	} else {
    		throw new IllegalStateException("ontologyManager is NULL!");
    	}
    };
    
    
	public boolean canCheck(Object thing) {
		return domain.isInstance(thing);
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getValidTerms()
	 */
	public Set<String> getValidTerms() {
		return validTerms;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#setValidTerms(java.util.Set)
	 */
	public void setValidTerms(Set<String> validTerms) {
		this.validTerms = validTerms;
	}
	
	// for unit testing

	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getRestrictions()
	 */
	public Set<CvRestriction> getRestrictions() {
		return restrictions;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getDomain()
	 */
	public Class<D> getDomain() {
		return domain;
	}
	
	/* (non-Javadoc)
	 * @see org.biopax.validator.impl.CvRule#getProperty()
	 */
	public String getProperty() {
		return property;
	}
	
	/**
	 * Gets the internal BiopaxOntologyManager instance
	 * @return
	 */
	public CvValidator getBiopaxOntologyManager() {
		return ontologyManager;
	}
	
	/**
	 * Gets the corresponding CV property editor.
	 * Returns null if either the 'domain' itself is of CV type
	 * or the 'property' is null.
	 * 
	 * @return
	 */
	public PropertyEditor<? super D, ?> getEditor() {
		return editor;
	}
	
	/**
	 * OntologyAccess IDs used to check this CV rule.
	 * These can be extracted from the CV rescrictions 
	 * used to define the rule.
	 * (other ontologies are not used).
	 * 
	 * @return
	 */
	protected Set<String> getOntologyIDs() {
		Set<String> ids = new HashSet<String>();
		for(CvRestriction restriction : restrictions)
			ids.add(restriction.getOntologyId());
		return ids;
	}
}
