package org.openmrs.reporting;

import java.util.Locale;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

public class NumericObsPatientFilter extends AbstractPatientFilter implements PatientFilter {

	Concept concept;
	PatientSetService.Modifier modifier;
	PatientSetService.TimeModifier timeModifier;
	// this used to be java.lang.Number, but changed to Double because difficult to make prop editors for abstract classes
	Double value;
	
	public NumericObsPatientFilter(Concept concept, Modifier modifier, Double value) {
		super.setType("Patient Filter");
		super.setSubType("Numeric Observation Patient Filter");
		this.concept = concept;
		this.modifier = modifier;
		this.value = value;
	}

	public NumericObsPatientFilter() {
		super.setType("Patient Filter");
		super.setSubType("Numeric Observation Patient Filter");		
	}
	
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public PatientSetService.TimeModifier getTimeModifier() {
		return timeModifier;
	}

	public void setTimeModifier(PatientSetService.TimeModifier timeModifier) {
		this.timeModifier = timeModifier;
	}

	public PatientSetService.Modifier getModifier() {
		return modifier;
	}

	public void setModifier(PatientSetService.Modifier modifier) {
		this.modifier = modifier;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public PatientSet filter(Context context, PatientSet input) {
		PatientSetService service = context.getPatientSetService();
		return input.intersect(service.getPatientsHavingNumericObs(concept.getConceptId(), timeModifier, modifier, value));
	}
	
	public PatientSet filterInverse(Context context, PatientSet input) {
		PatientSetService service = context.getPatientSetService();
		return input.subtract(service.getPatientsHavingNumericObs(concept.getConceptId(), timeModifier, modifier, value));
	}

	public String getDescription() {
		// TODO: get the right locale
		Locale locale = OpenmrsConstants.OPENMRS_LOCALES().iterator().next();
		StringBuffer ret = new StringBuffer();
		//ret.append("Patients with (concept #" + concept + ")");
		ret.append("Patients with " + (concept == null ? "CONCEPT" : concept.getName(locale, false)));
		if (value != null && modifier != PatientSetService.Modifier.EXISTS) {
			ret.append(" " + modifier.getSqlRepresentation() + " " + value);
		}
		return ret.toString();
	}

	public String toString() {
		return "NumericObservationFilter with values [" + getConcept().getConceptId() + "] [" + getModifier() + "] [" + getValue() + "]";
	}
}
