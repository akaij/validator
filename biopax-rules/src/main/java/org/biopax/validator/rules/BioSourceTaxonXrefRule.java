package org.biopax.validator.rules;

import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.validator.impl.AbstractRule;
import org.biopax.validator.utils.XrefHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks:
 * BioSource taxon xref points to a taxonomy db.
 *
 *
 * @author rodche
 */
@Component
public class BioSourceTaxonXrefRule extends AbstractRule<BioSource> {

    @Autowired
    XrefHelper xrefHelper;

    public boolean canCheck(Object thing) {
		return thing instanceof BioSource;
	}  
    
	public void check(BioSource bioSource, boolean fix) {
		for (Xref x : bioSource.getXref()) {
			if (x instanceof UnificationXref) {
				String db = x.getDb();
				if (db != null) {
					if (!xrefHelper.isSynonyms(db, "taxonomy")) {
						if(!fix) {
							error(bioSource, "not.taxon.db", db);
						} else {
							x.setDb("Taxonomy");
						}
					}
				}
			}
		}
	}

}
