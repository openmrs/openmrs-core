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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.report.Parameterizable;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class InversePatientFilter extends AbstractPatientFilter implements PatientFilter, Parameterizable {
	
	private static final long serialVersionUID = -3206254139003809474L;
	
	private PatientFilter baseFilter;
	
	public InversePatientFilter() {
	}
	
	public InversePatientFilter(PatientFilter baseFilter) {
		this();
		this.baseFilter = baseFilter;
	}
	
	public PatientFilter getBaseFilter() {
		return baseFilter;
	}
	
	public void setBaseFilter(PatientFilter baseFilter) {
		this.baseFilter = baseFilter;
	}
	
	public Cohort filter(Cohort input, EvaluationContext context) {
		return baseFilter.filterInverse(input, context);
	}
	
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		return baseFilter.filter(input, context);
	}
	
	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return baseFilter != null;
	}
	
	public String getDescription() {
		return "NOT " + (baseFilter == null ? "?" : baseFilter.getDescription());
	}
	
	/**
	 * @see org.openmrs.report.Parameterizable#getParameters()
	 */
	public List<Parameter> getParameters() {
		return new ArrayList<Parameter>();
	}
	
}
