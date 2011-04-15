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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderService.ORDER_STATUS;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

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
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);
		List<Order> orders = os.getOrders(Order.class, patients, null, ORDER_STATUS.ANY, null, null, null);
		Assert.assertTrue(CollectionUtils.isNotEmpty(orders));
		//all order void related fields should be null
		for (Order order : orders) {
			Assert.assertTrue(order.isVoided());
			Assert.assertNotNull(order.getDateVoided());
			Assert.assertNotNull(order.getVoidedBy());
			Assert.assertNotNull(order.getVoidReason());
		}
		
		new PatientDataUnvoidHandler().handle(patient, new User(1), patient.getDateVoided(), null);
		
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
		Patient patient = Context.getPatientService().getPatient(7);
		
		EncounterService es = Context.getEncounterService();
		OrderService os = Context.getOrderService();
		
		//void one of the unvoided encounters for testing purposes
		Encounter testEncounter = es.getEncountersByPatient(patient).get(0);
		//santy checks
		Assert.assertNotNull(testEncounter);
		Assert.assertNull(testEncounter.getDateVoided());
		Assert.assertNull(testEncounter.getVoidedBy());
		Assert.assertNull(testEncounter.getVoidReason());
		
		es.voidEncounter(testEncounter, "random reason");
		Assert.assertTrue(testEncounter.isVoided());
		
		//void one of the unvoided orders for testing purposes
		Order testOrder = os.getOrdersByPatient(patient).get(0);
		Assert.assertNotNull(testOrder);
		Assert.assertNull(testOrder.getDateVoided());
		Assert.assertNull(testOrder.getVoidedBy());
		Assert.assertNull(testOrder.getVoidReason());
		
		os.voidOrder(testOrder, "random reason");
		Assert.assertTrue(testOrder.isVoided());
		
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
