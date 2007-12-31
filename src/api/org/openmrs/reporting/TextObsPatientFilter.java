package org.openmrs.reporting;

import java.util.Locale;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

public class TextObsPatientFilter extends AbstractReportObject implements PatientFilter {

	private Concept concept;
	private String value;
	private Boolean suggestFromDatabase = false; // not yet implemented: should we suggest with a (dropdown?) of known values from the database?
	private PatientSetService.TimeModifier timeModifier;
	
	public TextObsPatientFilter(Concept concept, String value) {
		this.concept = concept;
		this.value = value;
	}

	public TextObsPatientFilter() { }
	
	public boolean isReadyToRun() {
		return getConcept() != null && getValue() != null;
	}
	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof TextObsPatientFilter) {
			TextObsPatientFilter other = (TextObsPatientFilter) o;
			if (getReportObjectId() != null && getReportObjectId().equals(other.getReportObjectId()))
				return true;
			return ( getConcept().equals(other.getConcept())
					&& getValue().equals(other.getValue())
					&& getTimeModifier().equals(other.getTimeModifier()) );
		} else {
			return false;
		}
	}
	
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

	public PatientSet filter(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		PatientSet ps = service.getPatientsHavingTextObs(concept, value, timeModifier);
		return input == null ? ps : input.intersect(ps);
	}
	
	public PatientSet filterInverse(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.subtract(service.getPatientsHavingTextObs(concept, value, timeModifier));
	}

	public String getDescription() {
		if (concept == null)
			return "TextObsPatientFilter with no concept specified";

		// TODO: get the right locale
		StringBuffer ret = new StringBuffer();
		Locale locale = OpenmrsConstants.OPENMRS_LOCALES().iterator().next();
		ret.append("Patients with " + concept.getName(locale, false)); 
		if (value != null) {
			ret.append(" of " + value);
		}
		return ret.toString();
	}

	public PatientSetService.TimeModifier getTimeModifier() {
		return timeModifier;
	}

	public void setTimeModifier(PatientSetService.TimeModifier timeModifier) {
		this.timeModifier = timeModifier;
	}
}
