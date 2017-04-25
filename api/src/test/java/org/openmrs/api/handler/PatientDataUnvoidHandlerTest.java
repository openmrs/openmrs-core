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

import static org.apache.commons.lang3.time.DateUtils.parseDate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.CohortService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.TestUtil;

/**
 * Contains the tests for the {@link PatientDataUnvoidHandler}
 */
public class PatientDataUnvoidHandlerTest extends BaseContextSensitiveTest {
	
	private static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";
	
	/**
	 * @see PatientDataUnvoidHandler#handle(Patient,User,Date,String)
	 */
	@Test
	public void handle_shouldUnvoidTheOrdersAndEncountersAssociatedWithThePatient() {
		Patient patient = Context.getPatientService().getPatient(7);
		patient = Context.getPatientService().voidPatient(patient, "Void Reason");
		assertTrue(patient.getVoided());
		
		EncounterService es = Context.getEncounterService();
		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
			.setPatient(patient)
			.setIncludeVoided(true)
			.createEncounterSearchCriteria();
		List<Encounter> encounters = es.getEncounters(encounterSearchCriteria);
		assertTrue(CollectionUtils.isNotEmpty(encounters));
		//all encounters void related fields should be null
		for (Encounter encounter : encounters) {
			assertTrue(encounter.getVoided());
			assertNotNull(encounter.getDateVoided());
			assertNotNull(encounter.getVoidedBy());
			assertNotNull(encounter.getVoidReason());
		}
		
		OrderService os = Context.getOrderService();
		List<Order> orders = os.getAllOrdersByPatient(patient);
		assertFalse(orders.isEmpty());
		//all order void related fields should be null
		for (Order order : orders) {
			assertTrue(order.getVoided());
			assertNotNull(order.getDateVoided());
			assertNotNull(order.getVoidedBy());
			assertNotNull(order.getVoidReason());
		}
		
		User user = Context.getUserService().getUser(1);
		new PatientDataUnvoidHandler().handle(patient, user, patient.getDateVoided(), null);
		
		//check that the voided related fields were set null 
		for (Encounter encounter : encounters) {
			assertFalse(encounter.getVoided());
			assertNull(encounter.getDateVoided());
			assertNull(encounter.getVoidedBy());
			assertNull(encounter.getVoidReason());
		}
		for (Order order : orders) {
			assertFalse(order.getVoided());
			assertNull(order.getDateVoided());
			assertNull(order.getVoidedBy());
			assertNull(order.getVoidReason());
		}
	}
	
	/**
	 * @see PatientDataUnvoidHandler#handle(Patient,User,Date,String)
	 */
	@Test
	public void handle_shouldNotUnvoidTheOrdersAndEncountersThatNeverGotVoidedWithThePatient() {
		executeDataSet("org/openmrs/api/include/OrderServiceTest-otherEncounters.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		
		EncounterService es = Context.getEncounterService();
		OrderService os = Context.getOrderService();
		
		Encounter testEncounter = es.getEncountersByPatient(patient).get(0);
		//santy checks
		assertFalse(testEncounter.getVoided());
		assertNull(testEncounter.getDateVoided());
		assertNull(testEncounter.getVoidedBy());
		assertNull(testEncounter.getVoidReason());
		
		//void one of the encounter orders be voided at a different time for testing purposes
		assertFalse(testEncounter.getOrders().isEmpty());
		Order testOrder = testEncounter.getOrders().iterator().next();
		assertFalse(testOrder.getVoided());
		Context.getOrderService().voidOrder(testOrder, "testing");
		assertTrue(testOrder.getVoided());
		TestUtil.waitForClockTick();
		
		//void one of the unvoided encounters for testing purposes
		es.voidEncounter(testEncounter, "random reason");
		assertTrue(testEncounter.getVoided());
		assertTrue(testOrder.getVoided());
		
		List<Patient> patients = new ArrayList<>();
		patients.add(patient);
		
		//wait a bit so that the patient isn't voided on the same millisecond
		TestUtil.waitForClockTick();
		
		//now void the patient for testing purposes
		patient = Context.getPatientService().voidPatient(patient, "Void Reason");
		assertTrue(patient.getVoided());
		new PatientDataUnvoidHandler().handle(patient, patient.getVoidedBy(), patient.getDateVoided(), null);
		//the encounter that was initially voided separately should still be voided
		testEncounter = es.getEncounter(testEncounter.getId());
		assertTrue(testEncounter.getVoided());
		assertNotNull(testEncounter.getDateVoided());
		assertNotNull(testEncounter.getVoidedBy());
		assertNotNull(testEncounter.getVoidReason());
		
		//the order that was initially voided separately should still be voided
		assertTrue(testOrder.getVoided());
		assertNotNull(testOrder.getDateVoided());
		assertNotNull(testOrder.getVoidedBy());
		assertNotNull(testOrder.getVoidReason());
		
	}
	
	/**
	 * @verifies unvoid the members associated with the patient
	 * @see PatientDataUnvoidHandler#handle(Patient,User,Date,String)
	 */
	@Test
	public void handle_shouldUnvoidMembersAssociatedWithThePatient() throws Exception {
		executeDataSet(COHORT_XML);
		CohortService cs = Context.getCohortService();
		Cohort cohort = cs.getCohort(2);
		CohortMembership otherMembership = cohort.getMemberships().iterator().next();
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMembership membership = new CohortMembership(patient.getPatientId());
		membership.setStartDate(parseDate("2001-01-01", "yyyy-MM-dd"));
		membership.setEndDate(parseDate("2001-12-31", "yyyy-MM-dd"));
		cohort.addMembership(membership);
		
		cs.saveCohort(cohort);
		
		patient = Context.getPatientService().voidPatient(patient, "Void Reason");
		
		assertTrue(membership.getVoided());
		assertFalse(otherMembership.getVoided());
		
		Context.getPatientService().unvoidPatient(patient);

		assertFalse(membership.getVoided());
		assertNull(membership.getDateVoided());
		assertNull(membership.getVoidedBy());
		assertNull(membership.getVoidReason());
	}
}
