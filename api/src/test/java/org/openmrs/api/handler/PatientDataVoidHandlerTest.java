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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.CohortService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains the tests for the {@link PatientDataVoidHandler}
 */
public class PatientDataVoidHandlerTest extends BaseContextSensitiveTest {
	
	private final static String COHORT_UUID = "cohort-uuid";
	
	@Autowired
	private CohortService cohortService;
	
	/**
	 * @see PatientDataVoidHandler#handle(Patient,User,Date,String)
	 */
	@Test
	public void handle_shouldVoidTheOrdersEncountersAndObservationsAssociatedWithThePatient() {
		Patient patient = Context.getPatientService().getPatient(7);
		assertFalse(patient.getVoided());
		
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		List<Obs> observations = Context.getObsService().getObservationsByPerson(patient);
		List<Order> orders = Context.getOrderService().getAllOrdersByPatient(patient);
		
		//we should have some unvoided encounters, obs and orders for the test to be concrete
		assertTrue(CollectionUtils.isNotEmpty(encounters));
		assertTrue(CollectionUtils.isNotEmpty(observations));
		assertTrue(CollectionUtils.isNotEmpty(orders));
		
		//check that fields to be set by the handler are initially null 
		for (Encounter encounter : encounters) {
			assertNull(encounter.getDateVoided());
			assertNull(encounter.getVoidedBy());
			assertNull(encounter.getVoidReason());
		}
		for (Obs obs : observations) {
			assertNull(obs.getDateVoided());
			assertNull(obs.getVoidedBy());
			assertNull(obs.getVoidReason());
		}
		for (Order order : orders) {
			assertNull(order.getDateVoided());
			assertNull(order.getVoidedBy());
			assertNull(order.getVoidReason());
		}
		
		new PatientDataVoidHandler().handle(patient, new User(1), new Date(), "voidReason");
		
		//all encounters void related fields should have been set
		for (Encounter encounter : encounters) {
			assertTrue(encounter.getVoided());
			assertNotNull(encounter.getDateVoided());
			assertNotNull(encounter.getVoidedBy());
			assertNotNull(encounter.getVoidReason());
		}
		//all obs void related fields should have been set
		for (Obs obs : observations) {
			assertTrue(obs.getVoided());
			assertNotNull(obs.getDateVoided());
			assertNotNull(obs.getVoidedBy());
			assertNotNull(obs.getVoidReason());
		}
		//all order void related fields should have been set
		for (Order order : orders) {
			assertTrue(order.getVoided());
			assertNotNull(order.getDateVoided());
			assertNotNull(order.getVoidedBy());
			assertNotNull(order.getVoidReason());
		}
		
		//refresh the lists and check that all encounters, obs and orders were voided
		encounters = Context.getEncounterService().getEncountersByPatient(patient);
		observations = Context.getObsService().getObservationsByPerson(patient);
		
		assertTrue(CollectionUtils.isEmpty(encounters));
		assertTrue(CollectionUtils.isEmpty(observations));
	}
	
	@Test
	public void handle_shouldVoidCohortMemberships() throws Exception {
		// test a corner case by letting the same patient belong to the cohort for two separate periods
		CohortMembership membership1 = new CohortMembership(7, parseDate("2001-01-01", "yyyy-MM-dd"));
		membership1.setEndDate(parseDate("2001-12-31", "yyyy-MM-dd"));
		CohortMembership membership2 = new CohortMembership(7, parseDate("2017-01-01", "yyyy-MM-dd"));
		CohortMembership membership3 = new CohortMembership(8);

		Cohort cohort = new Cohort();
		cohort.setName("Cohort");
		cohort.setDescription("Description");
		cohort.setUuid(COHORT_UUID);
		cohort.addMembership(membership1);
		cohort.addMembership(membership2);
		cohort.addMembership(membership3);
		cohortService.saveCohort(cohort);
		
		PatientService patientService = Context.getPatientService();
		patientService.voidPatient(patientService.getPatient(7), "void reason");
		
		Collection<CohortMembership> memberships = cohortService.getCohortByUuid(COHORT_UUID).getMemberships(false);
		assertEquals(1, memberships.size());
		assertEquals(8, (int) memberships.iterator().next().getPatientId()); // patientId 7 was voided
	}
}
