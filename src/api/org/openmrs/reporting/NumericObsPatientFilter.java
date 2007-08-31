package org.openmrs.reporting;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
	Integer withinLastDays;
	Integer withinLastMonths;

	public NumericObsPatientFilter() {
		super.setType("Patient Filter");
		super.setSubType("Numeric Observation Patient Filter");		
	}
	
	public NumericObsPatientFilter(Concept concept, Modifier modifier, Double value) {
		this();
		this.concept = concept;
		this.modifier = modifier;
		this.value = value;
	}
	
	public boolean isReadyToRun() {
		return getConcept() != null &&
			(getTimeModifier() == PatientSetService.TimeModifier.ANY || getTimeModifier() == PatientSetService.TimeModifier.NO ||
					(getModifier() != null && getValue() != null));
	}
	
	public Integer getWithinLastDays() {
		return withinLastDays;
	}

	public void setWithinLastDays(Integer withinLastDays) {
		this.withinLastDays = withinLastDays;
	}

	public Integer getWithinLastMonths() {
		return withinLastMonths;
	}

	public void setWithinLastMonths(Integer withinLastMonths) {
		this.withinLastMonths = withinLastMonths;
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
	
	private Date fromDateHelper() {
		Date ret = null;
		if (withinLastDays != null || withinLastMonths != null) {
			Calendar gc = new GregorianCalendar();
			if (withinLastDays != null)
				gc.add(Calendar.DAY_OF_MONTH, -withinLastDays);
			if (withinLastMonths != null)
				gc.add(Calendar.MONTH, -withinLastMonths);
			ret = gc.getTime();
		}
		return ret;
	}

	public PatientSet filter(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		PatientSet ps = service.getPatientsHavingNumericObs(concept.getConceptId(), timeModifier, modifier, value, fromDateHelper(), null);
		return input == null ? ps : input.intersect(ps);
	}
	
	public PatientSet filterInverse(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.subtract(service.getPatientsHavingNumericObs(concept.getConceptId(), timeModifier, modifier, value, fromDateHelper(), null));
	}

	public String getDescription() {
		Locale locale = Context.getLocale();
		StringBuffer ret = new StringBuffer();
		//ret.append("Patients with (concept #" + concept + ")");
		ret.append("Patients with ");
		ret.append(timeModifier + " ");
		ret.append(concept == null ? "CONCEPT" : concept.getName(locale, false));
		if (value != null && modifier != null)
			ret.append(modifier.getSqlRepresentation() + " " + value);
		if (withinLastDays != null || withinLastMonths != null) {
			ret.append(" within last");
			if (withinLastMonths != null)
				ret.append(" " + withinLastMonths + " months");
			if (withinLastDays != null)
				ret.append(" " + withinLastDays + " days");
		}
		return ret.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("NumericObservationFilter");
		if (concept != null)
			sb.append(" concept " + concept);
		if (timeModifier != null)
			sb.append(" timeModifier " + timeModifier);
		if (modifier != null)
			sb.append(" modifier " + modifier);
		if (value != null)
			sb.append(" value " + value);
		if (withinLastMonths != null)
			sb.append(" withinLastMonths " + withinLastMonths);
		if (withinLastDays != null)
			sb.append(" withinLastDays " + withinLastDays);
		return sb.toString();
	}
}
