package org.openmrs.reporting;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;

public class TextObsPatientFilter extends AbstractReportObject implements PatientFilter {

	Concept concept;
	String value;
	
	public TextObsPatientFilter(Concept concept, String value) {
		this.concept = concept;
		this.value = value;
	}

	public TextObsPatientFilter() { }
	
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public PatientSet filter(Context context, PatientSet input) {
		PatientSetService service = context.getPatientSetService();
		return input.intersect(service.getPatientsHavingTextObs(concept, value));
	}
	
	public PatientSet filterInverse(Context context, PatientSet input) {
		PatientSetService service = context.getPatientSetService();
		return input.subtract(service.getPatientsHavingTextObs(concept, value));
	}

	public String getDescription() {
		StringBuffer ret = new StringBuffer();
		ret.append("Patients with (concept #" + concept + ")");
		if (value != null) {
			ret.append(" == " + value);
		}
		return ret.toString();
	}

}
