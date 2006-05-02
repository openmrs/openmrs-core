package org.openmrs.reporting;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.context.Context;

public class NumericObsPatientFilter extends AbstractPatientFilter implements PatientFilter {

	Concept concept;
	PatientSetService.Modifier modifier;
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
		return input.intersect(service.getPatientsHavingNumericObs(concept, modifier, value));
	}
	
	public PatientSet filterInverse(Context context, PatientSet input) {
		PatientSetService service = context.getPatientSetService();
		return input.subtract(service.getPatientsHavingNumericObs(concept, modifier, value));
	}

	public String getDescription() {
		StringBuffer ret = new StringBuffer();
		ret.append("Patients with (concept #" + concept + ")");
		if (value != null && modifier != PatientSetService.Modifier.EXISTS) {
			ret.append(" " + modifier.getSqlRepresentation() + " " + value);
		}
		return ret.toString();
	}

	public String toString() {
		return "NumericObservationFilter with values [" + getConcept().getConceptId() + "] [" + getModifier() + "] [" + getValue() + "]";
	}
}
