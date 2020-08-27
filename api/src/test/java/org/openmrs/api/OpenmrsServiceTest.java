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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.SerializationException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Contains methods to test behavior of OpenmrsService methods
 */
public class OpenmrsServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * Tests that if two service methods are called (one from inside the other) the first one will
	 * be rolled back if an exception is thrown inside the second one. <pre>
	 * We are testing with the merge patient method since it is transactional and calls multiple other 
	 * transactional methods
	 * </pre>
	 * 
	 * @throws SerializationException
	 */
	@Test
	@Disabled
	public void shouldCheckThatAMethodIsNotRolledBackInCaseOfAnErrorInAnotherInvokedInsideIt()
	        throws SerializationException {
		//TODO FIx why this test fails when run with other tests
		PatientService patientService = Context.getPatientService();
		EncounterService encounterService = Context.getEncounterService();
		ProgramWorkflowService programService = Context.getProgramWorkflowService();
		Patient prefPatient = patientService.getPatient(6);
		Patient notPrefPatient = patientService.getPatient(7);
		Collection<Program> programs = programService.getAllPrograms(false);
		
		int originalPrefEncounterCount = encounterService.getEncountersByPatient(prefPatient).size();
		int originalNotPrefEncounterCount = encounterService.getEncountersByPatient(notPrefPatient).size();
		assertTrue(originalNotPrefEncounterCount > 0);
		
		Cohort notPreferredCohort = new Cohort(notPrefPatient.getPatientId().toString());
		List<PatientProgram> notPrefPrograms = programService.getPatientPrograms(notPreferredCohort, programs);
		assertTrue(notPrefPrograms.size() > 0);
		
		//Set the program to null so that the patient program is rejected on validation with
		//an APIException, since it is a RuntimeException, all transactions should be rolled back
		notPrefPrograms.get(0).setProgram(null);
		
		boolean failed = false;
		try {
			patientService.mergePatients(prefPatient, notPrefPatient);
		}
		catch (APIException e) {
			failed = true;//should have failed to force a rollback
		}
		assertTrue(failed);
		
		//Since the encounters are moved first, that logic should have been rolled back
		assertEquals(originalPrefEncounterCount, encounterService.getEncountersByPatient(prefPatient).size());
	}
}
