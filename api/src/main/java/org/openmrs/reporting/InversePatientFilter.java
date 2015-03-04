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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
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
	
	/**
	 * (The following at-should unit tests should really be distributed across many other classes,
	 * but it's much faster to put them all here.)
	 * 
	 * @should not fail with drug order filter
	 * @should not fail with drug order stop filter
	 * @should not fail with encounter patient filter
	 * @should not fail with location patient filter
	 * @should not fail with obs patient filter
	 * @should not fail with patient characteristic filter
	 * @should not fail with person attribute filter
	 * @should not fail with program state patient filter
	 * @should not fail with compound patient filter
	 * @should not fail with drug order patient filter
	 * @should not fail with program patient filter
	 */
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
		MessageSourceService msa = Context.getMessageSourceService();
		return msa.getMessage("reporting.not").toUpperCase() + " "
		        + (baseFilter == null ? "?" : baseFilter.getDescription());
	}
	
	/**
	 * @see org.openmrs.report.Parameterizable#getParameters()
	 */
	public List<Parameter> getParameters() {
		return new ArrayList<Parameter>();
	}
	
}
