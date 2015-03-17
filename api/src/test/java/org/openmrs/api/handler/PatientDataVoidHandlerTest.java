/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.Date;
import java.util.List;

/**
 * Contains the tests for the {@link PatientDataVoidHandler}
 */
public class PatientDataVoidHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientDataVoidHandler#handle(Patient,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should void the orders encounters and observations associated with the patient", method = "handle(Patient,User,Date,String)")
	public void handle_shouldVoidTheOrdersEncountersAndObservationsAssociatedWithThePatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(7);
		Assert.assertFalse(patient.isVoided());
		
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		List<Obs> observations = Context.getObsService().getObservationsByPerson(patient);
		List<Order> orders = Context.getOrderService().getAllOrdersByPatient(patient);
		
		//we should have some unvoided encounters, obs and orders for the test to be concrete
		Assert.assertTrue(CollectionUtils.isNotEmpty(encounters));
		Assert.assertTrue(CollectionUtils.isNotEmpty(observations));
		Assert.assertTrue(CollectionUtils.isNotEmpty(orders));
		
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
		for (Order order : orders) {
			Assert.assertNull(order.getDateVoided());
			Assert.assertNull(order.getVoidedBy());
			Assert.assertNull(order.getVoidReason());
		}
		
		new PatientDataVoidHandler().handle(patient, new User(1), new Date(), "voidReason");
		
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
		//all order void related fields should have been set
		for (Order order : orders) {
			Assert.assertTrue(order.isVoided());
			Assert.assertNotNull(order.getDateVoided());
			Assert.assertNotNull(order.getVoidedBy());
			Assert.assertNotNull(order.getVoidReason());
		}
		
		//refresh the lists and check that all encounters, obs and orders were voided
		encounters = Context.getEncounterService().getEncountersByPatient(patient);
		observations = Context.getObsService().getObservationsByPerson(patient);
		
		Assert.assertTrue(CollectionUtils.isEmpty(encounters));
		Assert.assertTrue(CollectionUtils.isEmpty(observations));
	}
}
