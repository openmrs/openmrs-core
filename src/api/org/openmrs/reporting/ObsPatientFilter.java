package org.openmrs.reporting;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;

public class ObsPatientFilter extends AbstractPatientFilter implements PatientFilter {

	private Concept question;
	private PatientSetService.Modifier modifier;
	private PatientSetService.TimeModifier timeModifier;
	private Object value;
	private Integer withinLastDays;
	private Integer withinLastMonths;
	private Integer untilDaysAgo;
	private Integer untilMonthsAgo;
	private Date sinceDate;
	private Date untilDate;

	public ObsPatientFilter() {
		super.setType("Patient Filter");
		super.setSubType("Observation Patient Filter");
	}
	
	public boolean isReadyToRun() {
		if (question == null)
			return value != null && (value instanceof Concept);
		if (question.getDatatype().getHl7Abbreviation().equals("NM")) {
			if (getTimeModifier() == TimeModifier.ANY || getTimeModifier() == TimeModifier.NO)
				return true;
			else
				return getValue() != null && getModifier() != null;
		} else if (question.getDatatype().getHl7Abbreviation().equals("ST")) {
			if (getTimeModifier() == TimeModifier.ANY || getTimeModifier() == TimeModifier.NO)
				return true;
			else
				return getValue() != null;
		} else if (question.getDatatype().getHl7Abbreviation().equals("CWE")) {
			if (getTimeModifier() == TimeModifier.ANY || getTimeModifier() == TimeModifier.NO)
				return true;
			else
				return getValue() != null;
		} else {
			return false;
		}		
	}
	
	public boolean checkConsistancy() {
		if (!isReadyToRun())
			return false;
		if (question == null)
			return value != null && (value instanceof Concept);
		if (question.getDatatype().getHl7Abbreviation().equals("NM")) {
			return true;
		} else if (question.getDatatype().getHl7Abbreviation().equals("ST")) {
			TimeModifier tm = getTimeModifier();
			return tm == TimeModifier.ANY || tm == TimeModifier.NO || tm == TimeModifier.FIRST || tm == TimeModifier.LAST;
		} else if (question.getDatatype().getHl7Abbreviation().equals("CWE")) {
			TimeModifier tm = getTimeModifier();
			return tm == TimeModifier.ANY || tm == TimeModifier.NO || tm == TimeModifier.FIRST || tm == TimeModifier.LAST;
		} else {
			return false;
		}
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
		if (sinceDate != null && (ret == null || sinceDate.after(ret)))
			ret = sinceDate;
		return ret;
	}
	
	private Date toDateHelper() {
		Date ret = null;
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			Calendar gc = new GregorianCalendar();
			if (untilDaysAgo != null)
				gc.add(Calendar.DAY_OF_MONTH, -untilDaysAgo);
			if (untilMonthsAgo != null)
				gc.add(Calendar.MONTH, -untilMonthsAgo);
			ret = gc.getTime();
		}
		if (untilDate != null && (ret == null || untilDate.before(ret)))
			ret = untilDate;
		return ret;
	}

	public PatientSet filter(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.intersect(service.getPatientsHavingObs(question == null ? null : question.getConceptId(), timeModifier, modifier, value, fromDateHelper(), toDateHelper()));
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.subtract(service.getPatientsHavingObs(question == null ? null : question.getConceptId(), timeModifier, modifier, value, fromDateHelper(), toDateHelper()));
	}
	
	public String getDescription() {
		Locale locale = Context.getLocale();
		StringBuffer ret = new StringBuffer();
		if (question == null) {
			if (getValue() != null)
				ret.append("Patients with " + timeModifier + " obs with value " + ((Concept) value).getName().getName());
			else
				ret.append("question and value are both null");
		} else {
			ret.append("Patients with ");
			ret.append(timeModifier + " ");
			ret.append(question == null ? "CONCEPT" : question.getName(locale, false));
			if (value != null && modifier != null) {
				ret.append(" " + modifier.getSqlRepresentation() + " ");
				if (value instanceof Concept)
					ret.append(((Concept) value).getName(locale));
				else
					ret.append(value);
			}
		}
		if (withinLastDays != null || withinLastMonths != null) {
			ret.append(" within last");
			if (withinLastMonths != null)
				ret.append(" " + withinLastMonths + " months");
			if (withinLastDays != null)
				ret.append(" " + withinLastDays + " days");
		}
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			ret.append(" until");
			if (untilMonthsAgo != null)
				ret.append(" " + untilMonthsAgo + " months");
			if (untilDaysAgo != null)
				ret.append(" " + untilDaysAgo + " months");
			ret.append(" ago");
		}
		DateFormat df = null;
		if (sinceDate != null || untilDate != null)
			df = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
		if (sinceDate != null)
			ret.append(" since " + df.format(sinceDate));
		if (untilDate != null)
			ret.append(" until " + df.format(untilDate));
		return ret.toString();
	}

	
	public PatientSetService.Modifier getModifier() {
		return modifier;
	}

	public void setModifier(PatientSetService.Modifier modifier) {
		this.modifier = modifier;
	}

	public Concept getQuestion() {
		return question;
	}

	public void setQuestion(Concept question) {
		this.question = question;
	}

	public Date getSinceDate() {
		return sinceDate;
	}

	public void setSinceDate(Date sinceDate) {
		this.sinceDate = sinceDate;
	}

	public PatientSetService.TimeModifier getTimeModifier() {
		return timeModifier;
	}

	public void setTimeModifier(PatientSetService.TimeModifier timeModifier) {
		this.timeModifier = timeModifier;
	}

	public Date getUntilDate() {
		return untilDate;
	}

	public void setUntilDate(Date untilDate) {
		this.untilDate = untilDate;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
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

	public Integer getUntilDaysAgo() {
		return untilDaysAgo;
	}

	public void setUntilDaysAgo(Integer untilDaysAgo) {
		this.untilDaysAgo = untilDaysAgo;
	}

	public Integer getUntilMonthsAgo() {
		return untilMonthsAgo;
	}

	public void setUntilMonthsAgo(Integer untilMonthsAgo) {
		this.untilMonthsAgo = untilMonthsAgo;
	}
	
}
