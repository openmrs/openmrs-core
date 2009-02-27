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

import org.openmrs.Cohort;
import org.openmrs.report.EvaluationContext;

/**
 * PatientFilters may use an EvaluationContext for caching, but they ignore parameter values
 */
public interface PatientFilter extends ReportObject {
	
	/**
	 * Determine all patients in _input_ who also match some criteria. If input is null, then this
	 * should return all patients who match.
	 * 
	 * @param context
	 * @param input
	 * @return
	 */
	public Cohort filter(Cohort input, EvaluationContext context);
	
	/**
	 * Determine all patients in _input_ who do *not* match some criteria
	 * 
	 * @param context
	 * @param input
	 * @return
	 */
	public Cohort filterInverse(Cohort input, EvaluationContext context);
	
	/**
	 * @return Whether or not this filter has had enough parameters set to be run properly
	 */
	public boolean isReadyToRun();
}
