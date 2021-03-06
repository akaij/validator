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

import org.biopax.paxtools.model.level3.Protein;
import org.biopax.validator.api.AbstractRule;
import org.biopax.validator.api.beans.Validation;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * Protein.entityReference is not null when its memberPhysicalEntity is null.
 * @author rodche
 */
@Component
public class ProteinEntityReferenceRule extends AbstractRule<Protein> {

	public boolean canCheck(Object thing) {
		return thing instanceof Protein;
	}

	public void check(final Validation validation, Protein p) {
		if(p.getEntityReference() == null) {
			if(p.getMemberPhysicalEntity().isEmpty()) {
				error(validation, p, "null.entity.reference", false, "ProteinReference");
			} 
		}
	} 
}
