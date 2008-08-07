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
package org.openmrs.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests methods in the CohortService class
 * 
 * TODO add all the rest of the tests
 */
public class CohortServiceTest extends BaseContextSensitiveTest {

	protected static final String CREATE_PATIENT_XML = "org/openmrs/test/api/include/PatientServiceTest-createPatient.xml";
	protected static CohortService service;

	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		service = Context.getCohortService();
	}
	
	@Test
	public void shouldPatientSearchCohortDefinitionProvider() throws Exception {
		executeDataSet(CREATE_PATIENT_XML);
		CohortDefinition def = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
		Cohort result = service.evaluate(def, null);
		assertNotNull("Should not return null", result);
		assertEquals("Should return one member", 1, result.size());
	}
	

	@Test
	public void shouldOnlyGetNonVoidedCohortsByName() throws Exception {
		executeDataSet("org/openmrs/test/api/include/CohortServiceTest-cohort.xml");
		
		// make sure we have two cohorts with the same name and the first is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).isVoided());
		assertFalse(allCohorts.get(1).isVoided());
		
		// now do the actual test: getCohort by name and expect a non voided cohort
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertEquals(2, exampleCohort.size());
		assertFalse(exampleCohort.isVoided());
	}
}
