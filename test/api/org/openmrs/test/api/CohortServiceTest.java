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

	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		service = Context.getCohortService();
	}
	
	public void testShouldPatientSearchCohortDefinitionProvider() throws Exception {
		executeDataSet(CREATE_PATIENT_XML);
		CohortDefinition def = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
		Cohort result = service.evaluate(def, null);
		assertNotNull("Should not return null", result);
		assertEquals("Should return one member", 1, result.size());
	}
}
