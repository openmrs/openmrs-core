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
				if (e.getValue().latest().toBoolean())
					matches.addMember(e.getKey());
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
				if (e.getValue().latest().toBoolean())
					matches.addMember(e.getKey());
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof LogicPatientFilter) {
			LogicPatientFilter other = (LogicPatientFilter) o;
			return equals(criteria, other.getCriteria());
		} else {
			return false;
		}
	}
	
	/**
	 * @see org.openmrs.reporting.AbstractReportObject#getDescription()
	 */
	public String getDescription() {
		if (getCriteria() == null)
			return "criteria==NULL";
		return getCriteria().toString();
	}
	
}
