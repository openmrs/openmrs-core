/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.reporting;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsUtil;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ObsPatientFilter extends CachingPatientFilter {
	
	private static final long serialVersionUID = 1L;
	
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
	
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append(".");
		if (getQuestion() != null)
			sb.append(getQuestion().getConceptId());
		sb.append(".");
		sb.append(getModifier()).append(".");
		sb.append(getTimeModifier()).append(".");
		sb.append(
		    OpenmrsUtil.fromDateHelper(null, getWithinLastDays(), getWithinLastMonths(), getUntilDaysAgo(),
		        getUntilMonthsAgo(), getSinceDate(), getUntilDate())).append(".");
		sb.append(
		    OpenmrsUtil.toDateHelper(null, getWithinLastDays(), getWithinLastMonths(), getUntilDaysAgo(),
		        getUntilMonthsAgo(), getSinceDate(), getUntilDate())).append(".");
		sb.append(getValue());
		return sb.toString();
	}
	
	public boolean isReadyToRun() {
		if (question == null)
			return value != null && (value instanceof Concept);
		if (question.getDatatype().getHl7Abbreviation().equals("NM")
		        || question.getDatatype().getHl7Abbreviation().equals("DT")
		        || question.getDatatype().getHl7Abbreviation().equals("TS")) {
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
		if (question.getDatatype().getHl7Abbreviation().equals("NM")
		        || question.getDatatype().getHl7Abbreviation().equals("DT")
		        || question.getDatatype().getHl7Abbreviation().equals("TS")) {
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
	
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		PatientSetService service = Context.getPatientSetService();
		return service.getPatientsHavingObs(question == null ? null : question.getConceptId(), timeModifier, modifier,
		    value, OpenmrsUtil.fromDateHelper(null, getWithinLastDays(), getWithinLastMonths(), getUntilDaysAgo(),
		        getUntilMonthsAgo(), getSinceDate(), getUntilDate()), OpenmrsUtil.toDateHelper(null, getWithinLastDays(),
		        getWithinLastMonths(), getUntilDaysAgo(), getUntilMonthsAgo(), getSinceDate(), getUntilDate()));
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
			ConceptName questionName = null;
			if (question == null)
				ret.append("CONCEPT");
			else if ((questionName = question.getName(locale, false)) != null)
				ret.append(questionName);
			else {
				question = Context.getConceptService().getConcept(question.getConceptId());
				questionName = question.getName(locale, false);
				ret.append(questionName);
			}
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
