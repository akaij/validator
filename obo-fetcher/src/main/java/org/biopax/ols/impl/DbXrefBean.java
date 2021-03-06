package org.biopax.ols.impl;

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


import org.biopax.ols.DbXref;

/**
 * <p></p>
 *
 * @author R. Cote
 * @version $Id: DbXrefBean.java,v 1.1 2006/03/23 12:32:41 rglcote Exp $
 */
public class DbXrefBean implements DbXref {

    private String parentObjectPk;
    private long dbXrefId;
    private String dbName = null;
    private String accession = null;
    private String description = null;
    private int xrefType;

    /**
     * @return
     */
    public long getDbXrefId() {
        return dbXrefId;
    }

    /**
     * @param _dbXrefId
     */
    public void setDbXrefId(final long _dbXrefId) {
        dbXrefId = _dbXrefId;
    }

    /**
     * @return
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param _dbName
     */
    public void setDbName(final String _dbName) {
        dbName = _dbName;
    }

    /**
     * @return
     */
    public String getAccession() {
        return accession;
    }

    /**
     * @param _accession
     */
    public void setAccession(final String _accession) {
        accession = _accession;
    }

    /**
     * @return
     */
    public int getXrefType() {
        return xrefType;
    }

    /**
     * @param _version
     */
    public void setXrefType(final int _version) {
        xrefType = _version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentObjectPk() {
        return parentObjectPk;
    }

    public void setParentObjectPk(String parentObjectPk) {
        this.parentObjectPk = parentObjectPk;
    }

    public String toString() {
        return "DbName: " + dbName + " Acc: " + accession + " type: " + xrefType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DbXrefBean) {
            if (o != null) {
                DbXrefBean dx = (DbXrefBean) o;
                if (xorNull(dbName, dx.getDbName()) ||
                        xorNull(accession, dx.getAccession())) {
                    return false;
                } else {
                    boolean retval = true;

                    //if still here, dbName is either null or not null in both DbXrefBeans
                    //if not null, compare for equality
                    if (dbName != null)
                        retval = retval && dbName.equals(dx.getDbName());

                    //same for dbXrefs
                    if (accession != null)
                        retval = retval && accession.equals(dx.getAccession());

                    return retval;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

	private boolean xorNull(Object a, Object b) {
		return (a == null) ? (b != null) : (b == null) ;
	}

 }
