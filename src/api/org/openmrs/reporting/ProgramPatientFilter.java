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
import org.openmrs.Program;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
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
	
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		if (!isReadyToRun())
			return null;
		PatientSetService service = Context.getPatientSetService();
		Cohort matches = null;
		if (onDate != null)
			matches = service.getPatientsInProgram(program, onDate, onDate);
		else
			matches = service.getPatientsInProgram(program, fromDate, toDate);
		return Cohort.subtract(input, matches);
	}
	
	public String getDescription() {
		if (!isReadyToRun())
			return "";
		Locale locale = Context.getLocale();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		StringBuffer ret = new StringBuffer();
		ret.append("Patients in ");
		ret.append(getConceptName(program.getConcept()));
		if (onDate != null)
			ret.append(" on " + df.format(onDate));
		else {
			if (fromDate != null)
				ret.append(" anytime after " + df.format(fromDate));
			if (toDate != null)
				ret.append(" anytime before " + df.format(toDate));
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
