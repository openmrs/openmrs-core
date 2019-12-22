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
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the saving of orders as part of the OrderGroup
 */
public class HibernateOrderDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateOrderDAO dao;
	
	private static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";
	
	private static final String ORDER_GROUP = "org/openmrs/api/include/OrderServiceTest-createOrderGroup.xml";

	@Before
	public void setUp() {
		executeDataSet(ORDER_SET);
		executeDataSet(ORDER_GROUP);
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
	@Test(expected = APIException.class)
	public void getOrderGroupsByEncounter_shouldFailGivenNullEncounter() {
		dao.getOrderGroupsByEncounter(null);
	}
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByPatient(Patient)}
	 * @throws Exception
	 */
	@Test(expected = APIException.class)
	public void getOrderGroupsByPatient_shouldFailGivenNullPatient() {
		dao.getOrderGroupsByPatient(null);
	}
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByEncounter(Encounter)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByEncounter_shouldGetOrderGroupsFromAnEncounter() {
		Encounter existingEncounter = Context.getEncounterService().getEncounter(4);
		List<OrderGroup> ordergroups = Context.getOrderService().getOrderGroupsByEncounter(existingEncounter);
		assertEquals(1, ordergroups.size());
	}
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByPatient(Patient)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByPatient_shouldGetOrderGroupsGivenPatient() {
		Patient existingPatient = Context.getPatientService().getPatient(8);
		List<OrderGroup> ordergroups = Context.getOrderService().getOrderGroupsByPatient(existingPatient);
		assertEquals(1, ordergroups.size());
	}
}
