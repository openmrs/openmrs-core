/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.openmrs.Cohort;
import org.openmrs.Program;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortUtil;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.report.EvaluationContext;

/**
 * Currently can only determine whether a patient was in a given program ever, or on a specific
 * date, or relative to dates
 * 
 * @deprecated Use @see org.openmrs.reporting.ProgramStatePatientFilter instead
 */
@Deprecated
public class ProgramPatientFilter extends AbstractPatientFilter implements PatientFilter {
	
	private Program program;
	
	private Date onDate;
	
	private Date fromDate;
	
	private Date toDate;
	
	public ProgramPatientFilter() {
		super.setType("Patient Filter");
		super.setSubType("Program Patient Filter");
	}
	
	public Cohort filter(Cohort input, EvaluationContext context) {
		if (!isReadyToRun())
			return null;
		PatientSetService service = Context.getPatientSetService();
		Cohort matches = null;
		if (onDate != null)
			matches = service.getPatientsInProgram(program, onDate, onDate);
		else
			matches = service.getPatientsInProgram(program, fromDate, toDate);
		return input == null ? matches : Cohort.intersect(input, matches);
	}
	
	public String getDescription() {
		MessageSourceService mss = Context.getMessageSourceService();
		if (!isReadyToRun())
			return "";
		Locale locale = Context.getLocale();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		StringBuilder ret = new StringBuilder();
		ret.append(mss.getMessage("reporting.patientsIn")).append(" ");
		ret.append(getConceptName(program.getConcept()));
		if (onDate != null)
			ret.append(" ").append(mss.getMessage("reporting.on", new Object[] { df.format(onDate) }, locale));
		else {
			if (fromDate != null)
				ret.append(" ").append(
				    mss.getMessage("reporting.anytimeAfter", new Object[] { df.format(fromDate) }, locale));
			if (toDate != null)
				ret.append(" ")
				        .append(mss.getMessage("reporting.anytimeBefore", new Object[] { df.format(toDate) }, locale));
		}
		return ret.toString();
	}
	
	public boolean isReadyToRun() {
		return program != null;
	}
	
	public Date getFromDate() {
		return fromDate;
	}
	
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	public Date getOnDate() {
		return onDate;
	}
	
	public void setOnDate(Date onDate) {
		this.onDate = onDate;
	}
	
	public Program getProgram() {
		return program;
	}
	
	public void setProgram(Program program) {
		this.program = program;
	}
	
	public Date getToDate() {
		return toDate;
	}
	
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
}
