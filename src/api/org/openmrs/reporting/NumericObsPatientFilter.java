package org.openmrs.reporting;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.context.Context;

public class NumericObsPatientFilter extends AbstractReportObject implements PatientFilter {

	Concept concept;
	PatientSetService.Modifier modifier;
	Number value;
	
	public NumericObsPatientFilter(Concept concept, Modifier modifier, Number value) {
		this.concept = concept;
		this.modifier = modifier;
		this.value = value;
	}

	public NumericObsPatientFilter() { }
	
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

	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
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

}
