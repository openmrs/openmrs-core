package org.openmrs.reporting;

import org.openmrs.Concept;
import org.openmrs.api.context.ContextFactory;

public class ArvTxGroupFilter extends TextObsPatientFilter {
	
	public ArvTxGroupFilter() {
		super();
		super.setType("Patient Filter");
		super.setSubType("ARV Treatment Group Filter");
		Concept temp = ContextFactory.getContext().getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT GROUP");
		if (temp != null) {
			super.setConcept(temp);
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
