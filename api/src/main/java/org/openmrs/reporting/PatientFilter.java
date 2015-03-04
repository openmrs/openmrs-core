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

import org.openmrs.Cohort;
import org.openmrs.report.EvaluationContext;

/**
 * PatientFilters may use an EvaluationContext for caching, but they ignore parameter values
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public interface PatientFilter extends ReportObject {
	
	/**
	 * Determine all patients in _input_ who also match some criteria. If input is null, then this
	 * should return all patients who match.
	 * 
	 * @param input Cohort of the patients to search
	 * @param context EvaluationContext specifying filtering criteria
	 * @return Cohort of the patients matching the criteria
	 */
	public Cohort filter(Cohort input, EvaluationContext context);
	
	/**
	 * Determine all patients in _input_ who do *not* match some criteria
	 * 
	 * @param input Cohort of the patients to search
	 * @param context EvaluationContext specifying filtering criteria
	 * @return Cohort of patients in <code>input</code> who do <b>not</b> meet specified criteria
	 */
	public Cohort filterInverse(Cohort input, EvaluationContext context);
	
	/**
	 * Check whether this filter has had enough parameters set to be run properly
	 * 
	 * @return true if the filter has enough parameters set, false otherwise
	 */
	public boolean isReadyToRun();
}
