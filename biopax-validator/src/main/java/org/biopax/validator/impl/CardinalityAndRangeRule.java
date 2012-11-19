package org.biopax.validator.impl;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.level3.Level3Element;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * 
 * @author rodche
 *
 * @param <E> extends BioPAXElement
 */

@Configurable
public abstract class CardinalityAndRangeRule<E extends Level3Element> 
	extends AbstractCardinalityAndRangeRule<E>
{
	// Constructor with arguments
	public CardinalityAndRangeRule(Class<E> domain, String property,
			int min, int max, Class<?>... ranges) {
		super(domain, property, min, max, ranges);
		this.editorMap = SimpleEditorMap.L3;
	}	
}