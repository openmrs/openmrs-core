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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the saving of orders as part of the OrderGroup
 */
public class HibernateOrderDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateOrderDAO dao;
	
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
}
