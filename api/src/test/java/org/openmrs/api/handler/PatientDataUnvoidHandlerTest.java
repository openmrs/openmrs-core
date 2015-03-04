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
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.test.Verifies;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains the tests for the {@link PatientDataUnvoidHandler}
 */
public class PatientDataUnvoidHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientDataUnvoidHandler#handle(Patient,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unvoid the orders and encounters associated with the patient", method = "handle(Patient,User,Date,String)")
	public void handle_shouldUnvoidTheOrdersAndEncountersAssociatedWithThePatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(7);
		patient = Context.getPatientService().voidPatient(patient, "Void Reason");
		Assert.assertTrue(patient.isVoided());
		
		EncounterService es = Context.getEncounterService();
		List<Encounter> encounters = es.getEncounters(patient, null, null, null, null, null, null, true);
		Assert.assertTrue(CollectionUtils.isNotEmpty(encounters));
		//all encounters void related fields should be null
		for (Encounter encounter : encounters) {
			Assert.assertTrue(encounter.isVoided());
			Assert.assertNotNull(encounter.getDateVoided());
			Assert.assertNotNull(encounter.getVoidedBy());
			Assert.assertNotNull(encounter.getVoidReason());
		}
		
		OrderService os = Context.getOrderService();
		List<Order> orders = os.getAllOrdersByPatient(patient);
		Assert.assertFalse(orders.isEmpty());
		//all order void related fields should be null
		for (Order order : orders) {
			Assert.assertTrue(order.isVoided());
			Assert.assertNotNull(order.getDateVoided());
			Assert.assertNotNull(order.getVoidedBy());
			Assert.assertNotNull(order.getVoidReason());
		}
		
		User user = Context.getUserService().getUser(1);
		new PatientDataUnvoidHandler().handle(patient, user, patient.getDateVoided(), null);
		
		//check that the voided related fields were set null 
		for (Encounter encounter : encounters) {
			Assert.assertFalse(encounter.isVoided());
			Assert.assertNull(encounter.getDateVoided());
			Assert.assertNull(encounter.getVoidedBy());
			Assert.assertNull(encounter.getVoidReason());
		}
		for (Order order : orders) {
			Assert.assertFalse(order.isVoided());
			Assert.assertNull(order.getDateVoided());
			Assert.assertNull(order.getVoidedBy());
			Assert.assertNull(order.getVoidReason());
		}
	}
	
	/**
	 * @see {@link PatientDataUnvoidHandler#handle(Patient,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not unvoid the orders and encounters that never got voided with the patient", method = "handle(Patient,User,Date,String)")
	public void handle_shouldNotUnvoidTheOrdersAndEncountersThatNeverGotVoidedWithThePatient() throws Exception {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherEncounters.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		
		EncounterService es = Context.getEncounterService();
		OrderService os = Context.getOrderService();
		
		Encounter testEncounter = es.getEncountersByPatient(patient).get(0);
		//santy checks
		Assert.assertFalse(testEncounter.isVoided());
		Assert.assertNull(testEncounter.getDateVoided());
		Assert.assertNull(testEncounter.getVoidedBy());
		Assert.assertNull(testEncounter.getVoidReason());
		
		//void one of the encounter orders be voided at a different time for testing purposes
		Assert.assertFalse(testEncounter.getOrders().isEmpty());
		Order testOrder = testEncounter.getOrders().iterator().next();
		Assert.assertFalse(testOrder.isVoided());
		Context.getOrderService().voidOrder(testOrder, "testing");
		Assert.assertTrue(testOrder.isVoided());
		TestUtil.waitForClockTick();
		
		//void one of the unvoided encounters for testing purposes
		es.voidEncounter(testEncounter, "random reason");
		Assert.assertTrue(testEncounter.isVoided());
		Assert.assertTrue(testOrder.isVoided());
		
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);
		
		//wait a bit so that the patient isn't voided on the same millisecond
		TestUtil.waitForClockTick();
		
		//now void the patient for testing purposes
		patient = Context.getPatientService().voidPatient(patient, "Void Reason");
		Assert.assertTrue(patient.isVoided());
		new PatientDataUnvoidHandler().handle(patient, patient.getVoidedBy(), patient.getDateVoided(), null);
		//the encounter that was initially voided separately should still be voided
		testEncounter = es.getEncounter(testEncounter.getId());
		Assert.assertTrue(testEncounter.isVoided());
		Assert.assertNotNull(testEncounter.getDateVoided());
		Assert.assertNotNull(testEncounter.getVoidedBy());
		Assert.assertNotNull(testEncounter.getVoidReason());
		
		//the order that was initially voided separately should still be voided
		Assert.assertTrue(testOrder.isVoided());
		Assert.assertNotNull(testOrder.getDateVoided());
		Assert.assertNotNull(testOrder.getVoidedBy());
		Assert.assertNotNull(testOrder.getVoidReason());
		
	}
}
