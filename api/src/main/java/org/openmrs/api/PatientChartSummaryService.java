/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.Date;

import org.openmrs.Patient;
import org.openmrs.PatientChartSummary;
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

/**
 * Service for aggregating a patient's clinical data into a unified summary.
 * <p>
 * This service provides a single entry point for retrieving a complete view of a patient's
 * clinical history, including encounters, observations, conditions, diagnoses, allergies,
 * orders, visits, and program enrollments. This is particularly useful for AI-powered
 * clinical decision support systems that need comprehensive patient context.
 *
 * @since 3.0.0
 */
public interface PatientChartSummaryService extends OpenmrsService {
	
	/**
	 * Generates a complete chart summary for the given patient, including all available
	 * clinical data.
	 *
	 * @param patient the patient to generate the summary for
	 * @return a summary of the patient's clinical data
	 * @throws APIException if an error occurs while generating the summary
	 * <strong>Should</strong> return a summary with all clinical data for the patient
	 * <strong>Should</strong> return a summary with empty lists if patient has no clinical data
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_CHART_SUMMARY })
	PatientChartSummary getChartSummary(Patient patient) throws APIException;
	
	/**
	 * Generates a chart summary for the given patient within a specific date range.
	 *
	 * @param patient the patient to generate the summary for
	 * @param fromDate the start of the date range (inclusive), or null for no lower bound
	 * @param toDate the end of the date range (inclusive), or null for no upper bound
	 * @return a summary of the patient's clinical data within the date range
	 * @throws APIException if an error occurs while generating the summary
	 * <strong>Should</strong> return only data within the specified date range
	 * <strong>Should</strong> return all data when fromDate and toDate are null
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_CHART_SUMMARY })
	PatientChartSummary getChartSummary(Patient patient, Date fromDate, Date toDate) throws APIException;
	
	/**
	 * Generates a plain-text narrative summary of the patient's chart suitable for use
	 * as context in AI/LLM prompts.
	 *
	 * @param patient the patient to generate the text summary for
	 * @return a structured plain-text narrative of the patient's clinical data
	 * @throws APIException if an error occurs while generating the summary
	 * <strong>Should</strong> return a non-empty string for a patient with clinical data
	 * <strong>Should</strong> return a minimal summary for a patient with no clinical data
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_CHART_SUMMARY })
	String getChartSummaryAsText(Patient patient) throws APIException;
}
