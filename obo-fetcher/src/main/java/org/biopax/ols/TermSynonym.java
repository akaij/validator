package org.biopax.ols;

/*
 * #%L
 * Ontologies Access
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

import java.util.Collection;

/**
 * @author R. Cote
 * @version $Id: TermSynonym.java,v 1.6 2006/03/23 12:34:17 rglcote Exp $
 */
public interface TermSynonym {
    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public String getSynonym();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Term getParentTerm();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Term getSynonymType();

    /**
     * <p>Does ...</p>
     *
     * @return
     */
    public Collection<DbXref> getSynonymXrefs();
}









