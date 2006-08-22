package org.openmrs.reporting;

import java.util.Locale;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

public class TextObsPatientFilter extends AbstractReportObject implements PatientFilter {

	Concept concept;
	String value;
	Boolean suggestFromDatabase = false;
	
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

	public Boolean getSuggestFromDatabase() {
		return suggestFromDatabase;
	}

	public void setSuggestFromDatabase(Boolean suggestFromDatabase) {
		this.suggestFromDatabase = suggestFromDatabase;
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
		// TODO: get the right locale
		StringBuffer ret = new StringBuffer();
		Locale locale = OpenmrsConstants.OPENMRS_LOCALES().iterator().next();
		ret.append("Patients with " + concept.getName(locale, false)); 
		if (value != null) {
			ret.append(" of " + value);
		}
		return ret.toString();
	}

}
