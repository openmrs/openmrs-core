package org.openmrs.reporting;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.context.ContextFactory;

public class ArvTxGroupFilter extends TextObsPatientFilter {
	
	public ArvTxGroupFilter() {
		super();
		super.setType("Patient Filter");
		super.setSubType("ARV Treatment Group Filter");
		List<Concept> temp = ContextFactory.getContext().getConceptService().getConceptByName("ARV TREATMENT GROUP");
		if (temp != null && temp.size() > 0) {
			super.setConcept(temp.get(0));
		} else {
			throw new RuntimeException("Cannot find concept ARV TREATMENT GROUP");
		}
	}
	
	public String getTreatmentGroup() {
		return super.getValue();
	}
	
	public void setTreatmentGroup(String treatmentGroup) {
		super.setValue(treatmentGroup);
	}
	
}
