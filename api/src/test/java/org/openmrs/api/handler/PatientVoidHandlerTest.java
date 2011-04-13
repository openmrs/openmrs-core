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
package org.openmrs.api.handler;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class PatientVoidHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientVoidHandler#handle(Patient,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should void the encounters and observations for the patient", method = "handle(Patient,User,Date,String)")
	public void handle_shouldVoidTheEncountersAndObservationsForThePatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(7);
		Assert.assertFalse(patient.isVoided());
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		List<Obs> observations = Context.getObsService().getObservationsByPerson(patient);
		//check that fields to be set by the handler are initially null 
		for (Encounter encounter : encounters) {
			Assert.assertNull(encounter.getDateVoided());
			Assert.assertNull(encounter.getVoidedBy());
			Assert.assertNull(encounter.getVoidReason());
		}
		for (Obs obs : observations) {
			Assert.assertNull(obs.getDateVoided());
			Assert.assertNull(obs.getVoidedBy());
			Assert.assertNull(obs.getVoidReason());
		}
		//we should have some unvoided encounters and obs for the test to be concrete
		Assert.assertTrue(CollectionUtils.isNotEmpty(encounters));
		Assert.assertTrue(CollectionUtils.isNotEmpty(observations));
		Assert.assertFalse(patient.isVoided());
		
		patient.setVoided(true);
		new PatientVoidHandler().handle(patient, new User(1), new Date(), "voidReason");
		
		//all encounters void related fields should have been set
		for (Encounter encounter : encounters) {
			Assert.assertTrue(encounter.isVoided());
			Assert.assertNotNull(encounter.getDateVoided());
			Assert.assertNotNull(encounter.getVoidedBy());
			Assert.assertNotNull(encounter.getVoidReason());
		}
		//all obs void related fields should have been set
		for (Obs obs : observations) {
			Assert.assertTrue(obs.isVoided());
			Assert.assertNotNull(obs.getDateVoided());
			Assert.assertNotNull(obs.getVoidedBy());
			Assert.assertNotNull(obs.getVoidReason());
		}
		
		//refresh the lists and check that all encounters and obs were voided
		encounters = Context.getEncounterService().getEncountersByPatient(patient);
		observations = Context.getObsService().getObservationsByPerson(patient);
		Assert.assertTrue(CollectionUtils.isEmpty(encounters));
		Assert.assertTrue(CollectionUtils.isEmpty(observations));
	}
	
	/**
	 * @see {@link PatientVoidHandler#handle(Patient,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should ensure that the patient is voided too", method = "handle(Patient,User,Date,String)")
	public void handle_shouldEnsureThatThePatientIsVoidedToo() throws Exception {
		Patient patient = Context.getPatientService().getPatient(7);
		Assert.assertFalse(patient.isVoided());
		new PatientVoidHandler().handle(patient, new User(1), new Date(), "voidReason");
		Assert.assertTrue(patient.isVoided());
	}
}
