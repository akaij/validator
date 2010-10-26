package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.validator.impl.AbstractRule;
import org.springframework.stereotype.Component;

/**
 * Checks Degradation.conversionDirection value only "LEFT_TO_RIGHT"
 * 
 * @author rodche
 */
@Component
public class DegradationConversionDirectionRule extends AbstractRule<Degradation> {

	public boolean canCheck(Object thing) {
		return thing instanceof Degradation;
	}

	public void check(Degradation thing, boolean fix) {
		if(thing.getConversionDirection() != null 
			&& thing.getConversionDirection() != ConversionDirectionType.LEFT_TO_RIGHT) 
		{	
			if(!fix) {
				error(thing, "range.violated", "conversionDirection", 
						thing.getConversionDirection().name(), "",
					ConversionDirectionType.LEFT_TO_RIGHT.name()
					+ " (or empty)");
			} else {
				thing.setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
			}
		}
		
	}

}
