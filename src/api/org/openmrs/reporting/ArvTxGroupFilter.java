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
package org.openmrs.reporting;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;

public class ArvTxGroupFilter extends TextObsPatientFilter {
	
	public ArvTxGroupFilter() {
		super();
		super.setType("Patient Filter");
		super.setSubType("ARV Treatment Group Filter");
		Concept temp = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT GROUP");
		if (temp != null) {
			super.setConcept(temp);
			super.setTimeModifier(PatientSetService.TimeModifier.LAST);
		} else {
			throw new RuntimeException("Cannot find concept ANTIRETROVIRAL TREATMENT GROUP");
		}
	}
	
	public String getTreatmentGroup() {
		return super.getValue();
	}
	
	public void setTreatmentGroup(String treatmentGroup) {
		super.setValue(treatmentGroup);
	}
	
}
