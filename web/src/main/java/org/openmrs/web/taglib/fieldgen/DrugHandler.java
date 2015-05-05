/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.DrugsByNameComparator;

public class DrugHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "drug.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		if (fieldGenTag != null) {
			String initialValue = "";
			checkEmptyVal((Drug) null);
			Drug d = (Drug) this.fieldGenTag.getVal();
			if (d != null && d.getDrugId() != null) {
				initialValue = d.getDrugId().toString();
			}
			String optionHeader = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if (optionHeader == null) {
				optionHeader = "";
			}
			
			ConceptService cs = Context.getConceptService();
			List<Drug> drugs = cs.getAllDrugs();
			Collections.sort(drugs, new DrugsByNameComparator());
			
			if (drugs == null) {
				drugs = new ArrayList<Drug>();
			}
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("drugs", drugs);
		}
	}
}
