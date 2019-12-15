/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the saving of orders as part of the OrderGroup
 */
public class HibernateOrderDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateOrderDAO dao;
	
	@Autowired
	private EncounterService encounterService;

	@Autowired
	private PatientService patientService;
	
	private static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";
	
	@Before
	public void setUp() {
		executeDataSet(ORDER_SET);
	}
	
	/**
	 * @see {@link HibernateOrderDAO#saveOrderGroup(OrderGroup)}
	 * @throws Exception
	 */
	@Test
	public void saveOrderGroup_shouldSaveOrderGroup() {
		OrderGroup newOrderGroup = new OrderGroup();
		
		final Order order = new OrderBuilder().withAction(Order.Action.NEW).withPatient(7).withConcept(1000)
		        .withCareSetting(1).withOrderer(1).withEncounter(3).withDateActivated(new Date()).withOrderType(17)
		        .withUrgency(Order.Urgency.ON_SCHEDULED_DATE).withScheduledDate(new Date()).build();
		
		newOrderGroup.setOrders(new ArrayList<Order>() {
			
			{
				add(order);
			}
		});
		
		OrderGroup savedOrderGroup = dao.saveOrderGroup(newOrderGroup);
		assertNotNull("OrderGroup gets saved", savedOrderGroup.getOrderGroupId());
		
		for (Order savedOrder : savedOrderGroup.getOrders()) {
			assertNull("Order is not saved as a part of Order Group", savedOrder.getOrderId());
		}
		
	}

	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByEncounter(Encounter)} 
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByEncounter_shouldGetOrderGroupsGivenMultipleEncounters() {
		OrderGroup firstOrderGroup = new OrderGroup();
		OrderGroup secondOrderGroup = new OrderGroup();
		OrderGroup thirdOrderGroup = new OrderGroup();

		Encounter existingEncounter1 = encounterService.getEncounter(3);
		Encounter existingEncounter2 = encounterService.getEncounter(4);

		firstOrderGroup.setEncounter(existingEncounter1);
		secondOrderGroup.setEncounter(existingEncounter1);
		thirdOrderGroup.setEncounter(existingEncounter2);

		assertEquals(0, dao.getOrderGroupsByEncounter(existingEncounter1).size());
		assertEquals(0, dao.getOrderGroupsByEncounter(existingEncounter2).size());
		
		dao.saveOrderGroup(firstOrderGroup);
		dao.saveOrderGroup(secondOrderGroup);
		dao.saveOrderGroup(thirdOrderGroup);

		assertEquals(2, dao.getOrderGroupsByEncounter(existingEncounter1).size());
		assertEquals(1, dao.getOrderGroupsByEncounter(existingEncounter2).size());
	}

	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByEncounter(Encounter)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByEncounter_shouldGetOrderGroupsGivenFewEncounters() {
		OrderGroup orderGroup = new OrderGroup();
		
		Encounter existingEncounter1 = encounterService.getEncounter(3);
		Encounter existingEncounter2 = encounterService.getEncounter(4);

		orderGroup.setEncounter(existingEncounter1);

		assertEquals(0, dao.getOrderGroupsByEncounter(existingEncounter2).size());
		assertEquals(0, dao.getOrderGroupsByEncounter(existingEncounter1).size());
		
		dao.saveOrderGroup(orderGroup);

		assertEquals(0, dao.getOrderGroupsByEncounter(existingEncounter2).size());
		assertEquals(1, dao.getOrderGroupsByEncounter(existingEncounter1).size());
	}

	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByEncounter(Encounter)}
	 * @throws Exception
	 */
	@Test(expected = APIException.class)
	public void getOrderGroupsByEncounter_shouldFailGivenNullEncounter() {
		dao.getOrderGroupsByEncounter(null);
	}

	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByPatient(Patient)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByPatient_shouldGetOrderGroupsGivenMultiplePatients() {
		OrderGroup firstOrderGroup = new OrderGroup();
		OrderGroup secondOrderGroup = new OrderGroup();
		OrderGroup thirdOrderGroup = new OrderGroup();
		
		Patient existingPatient1 = patientService.getPatient(7);
		Patient existingPatient2 = patientService.getPatient(8);

		firstOrderGroup.setPatient(existingPatient1);
		secondOrderGroup.setPatient(existingPatient1);
		thirdOrderGroup.setPatient(existingPatient2);

		assertEquals(0, dao.getOrderGroupsByPatient(existingPatient1).size());
		assertEquals(0, dao.getOrderGroupsByPatient(existingPatient2).size());

		dao.saveOrderGroup(firstOrderGroup);
		dao.saveOrderGroup(secondOrderGroup);
		dao.saveOrderGroup(thirdOrderGroup);

		assertEquals(2, dao.getOrderGroupsByPatient(existingPatient1).size());
		assertEquals(1, dao.getOrderGroupsByPatient(existingPatient2).size());
		
	}

	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByPatient(Patient)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByPatient_shouldGetOrderGroupsGivenFewPatients() {
		OrderGroup orderGroup = new OrderGroup();

		Patient existingPatient1 = patientService.getPatient(7);
		Patient existingPatient2 = patientService.getPatient(8);

		orderGroup.setPatient(existingPatient1);

		assertEquals(0, dao.getOrderGroupsByPatient(existingPatient2).size());
		assertEquals(0, dao.getOrderGroupsByPatient(existingPatient1).size());
		
		dao.saveOrderGroup(orderGroup);

		assertEquals(0, dao.getOrderGroupsByPatient(existingPatient2).size());
		assertEquals(1, dao.getOrderGroupsByPatient(existingPatient1).size());
	}
	
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByPatient(Patient)}
	 * @throws Exception
	 */
	@Test(expected = APIException.class)
	public void getOrderGroupsByPatient_shouldFailGivenNullPatient() {
		dao.getOrderGroupsByPatient(null);
	}
}
