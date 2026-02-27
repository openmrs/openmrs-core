/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.PatientChartSummary;
import org.openmrs.api.PatientChartSummaryService;
import org.openmrs.api.PatientService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link PatientChartSummaryServiceImpl}.
 */
public class PatientChartSummaryServiceImplTest extends BaseContextSensitiveTest {
	
	@Autowired
	private PatientChartSummaryService patientChartSummaryService;
	
	@Autowired
	private PatientService patientService;
	
	/**
	 * @see PatientChartSummaryService#getChartSummary(Patient)
	 */
	@Test
	public void getChartSummary_shouldReturnSummaryForPatient() {
		Patient patient = patientService.getPatient(2);
		PatientChartSummary summary = patientChartSummaryService.getChartSummary(patient);
		
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getPatient(), is(patient));
		assertThat(summary.getGeneratedDate(), is(notNullValue()));
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummary(Patient)
	 */
	@Test
	public void getChartSummary_shouldReturnEncountersForPatient() {
		Patient patient = patientService.getPatient(7);
		PatientChartSummary summary = patientChartSummaryService.getChartSummary(patient);
		
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getEncounters(), is(not(empty())));
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummary(Patient)
	 */
	@Test
	public void getChartSummary_shouldReturnObservationsForPatient() {
		Patient patient = patientService.getPatient(7);
		PatientChartSummary summary = patientChartSummaryService.getChartSummary(patient);
		
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getObservations(), is(not(empty())));
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummary(Patient)
	 */
	@Test
	public void getChartSummary_shouldReturnEmptyListsForPatientWithNoData() {
		Patient patient = patientService.getPatient(2);
		PatientChartSummary summary = patientChartSummaryService.getChartSummary(patient);
		
		assertThat(summary, is(notNullValue()));
		assertThat(summary.getConditions(), is(notNullValue()));
		assertThat(summary.getDiagnoses(), is(notNullValue()));
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummaryAsText(Patient)
	 */
	@Test
	public void getChartSummaryAsText_shouldReturnNonEmptyTextForPatient() {
		Patient patient = patientService.getPatient(2);
		String text = patientChartSummaryService.getChartSummaryAsText(patient);
		
		assertThat(text, is(notNullValue()));
		assertThat(text.contains("PATIENT CHART SUMMARY"), is(true));
		assertThat(text.contains("ALLERGIES"), is(true));
		assertThat(text.contains("CONDITIONS"), is(true));
	}
}
