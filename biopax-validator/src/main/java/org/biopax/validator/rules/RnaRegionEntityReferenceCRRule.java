package org.biopax.validator.rules;

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

import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.validator.impl.CardinalityAndRangeRule;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * RnaRegion.entityReference cardinality/range.
 * @author rodche
 */
@Component
public class RnaRegionEntityReferenceCRRule extends CardinalityAndRangeRule<RnaRegion> {
	public RnaRegionEntityReferenceCRRule() {
		super(RnaRegion.class, "entityReference", 0, 1, RnaRegionReference.class);
	}
}
