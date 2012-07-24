/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

import org.openmrs.Drug;
import org.openmrs.util.DrugsByNameComparator;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

public class DrugHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "drug.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		if (fieldGenTag != null) {
			String initialValue = "";
			checkEmptyVal((Drug) null);
			Drug d = (Drug) this.fieldGenTag.getVal();
			if (d != null)
				if (d.getDrugId() != null)
					initialValue = d.getDrugId().toString();
			String optionHeader = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if (optionHeader == null)
				optionHeader = "";
			
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			
			ConceptService cs = Context.getConceptService();
			List<Drug> drugs = cs.getAllDrugs();
			Collections.sort(drugs, new DrugsByNameComparator());
			
			if (drugs == null)
				drugs = new ArrayList<Drug>();
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("drugs", drugs);
		}
	}
}
