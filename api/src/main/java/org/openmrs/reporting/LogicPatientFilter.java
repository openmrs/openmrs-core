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

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.report.Parameterizable;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class LogicPatientFilter extends AbstractPatientFilter implements PatientFilter, Parameterizable {
	
	private static final long serialVersionUID = 1L;
	
	private LogicCriteria criteria;
	
	public LogicPatientFilter() {
	}
	
	public LogicPatientFilter(LogicCriteria criteria) {
		this.criteria = criteria;
	}
	
	public LogicCriteria getCriteria() {
		return criteria;
	}
	
	public void setCriteria(LogicCriteria criteria) {
		this.criteria = criteria;
	}
	
	/**
	 * @see org.openmrs.reporting.PatientFilter#filter(org.openmrs.Cohort,
	 *      org.openmrs.report.EvaluationContext)
	 */
	public Cohort filter(Cohort input, EvaluationContext context) {
		try {
			Map<Integer, Result> results = Context.getLogicService().eval(input, criteria);
			// Assume these results are booleans
			Cohort matches = new Cohort();
			for (Map.Entry<Integer, Result> e : results.entrySet()) {
				if (e.getValue().latest().toBoolean()) {
					matches.addMember(e.getKey());
				}
			}
			return matches;
		}
		catch (LogicException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * @see org.openmrs.reporting.PatientFilter#filterInverse(org.openmrs.Cohort,
	 *      org.openmrs.report.EvaluationContext)
	 */
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		try {
			Map<Integer, Result> results = Context.getLogicService().eval(input, criteria);
			// Assume these results are booleans
			Cohort matches = new Cohort();
			for (Map.Entry<Integer, Result> e : results.entrySet()) {
				if (e.getValue().latest().toBoolean()) {
					matches.addMember(e.getKey());
				}
			}
			return Cohort.subtract(input, matches);
		}
		catch (LogicException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * @see org.openmrs.reporting.PatientFilter#isReadyToRun()
	 */
	public boolean isReadyToRun() {
		return criteria != null;
	}
	
	/**
	 * @see org.openmrs.report.Parameterizable#getParameters()
	 */
	public List<Parameter> getParameters() {
		return new Vector<Parameter>();
	}
	
	/**
	 * @see org.openmrs.reporting.AbstractReportObject#getDescription()
	 */
	public String getDescription() {
		if (getCriteria() == null) {
			return "criteria==NULL";
		}
		return getCriteria().toString();
	}
	
}
