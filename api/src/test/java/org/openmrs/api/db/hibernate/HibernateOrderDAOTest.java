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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.OrderGroupAttributeType;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the saving of orders as part of the OrderGroup
 */
public class HibernateOrderDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateOrderDAO dao;
	
	private static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";
	
	private static final String ORDER_GROUP = "org/openmrs/api/include/OrderServiceTest-createOrderGroup.xml";
	
	@BeforeEach
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
		assertNotNull(savedOrderGroup.getOrderGroupId(), "OrderGroup gets saved");
		
		for (Order savedOrder : savedOrderGroup.getOrders()) {
			assertNull(savedOrder.getOrderId(), "Order is not saved as a part of Order Group");
		}
	}
	
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByEncounter(Encounter)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByEncounter_shouldFailGivenNullEncounter() {
		assertThrows(APIException.class, () -> dao.getOrderGroupsByEncounter(null));
	}
	
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupsByPatient(Patient)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupsByPatient_shouldFailGivenNullPatient() {
		assertThrows(APIException.class, () -> dao.getOrderGroupsByPatient(null));
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
	
	/**
	 * @see {@link HibernateOrderDAO#getAllOrderGroupAttributeTypes()}
	 * @throws Exception
	 */
	@Test
	public void getAllOrderGroupAttributeTypes_shouldGetAllOrderGroupAttributeTypes() {
		List<OrderGroupAttributeType> orderGroupAttributeTypes = dao.getAllOrderGroupAttributeTypes();
		assertEquals(orderGroupAttributeTypes.size(), 4);
	}
	
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupAttributeTypeByUuid(String)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupAttributeTypeByUuid_shouldGetOrderGroupAttributeTypeGivenUuid() {
		OrderGroupAttributeType orderGroupAttributeType = dao
		        .getOrderGroupAttributeTypeByUuid("9cf1bce0-d18e-11ea-87d0-0242ac130003");
		assertEquals("Bacteriology", orderGroupAttributeType.getName());
	}
	
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupAttributeType(Integer)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupAttributeType_shouldReturnOrderGroupAttributeType() {
		OrderGroupAttributeType orderGroupAttributeType = dao.getOrderGroupAttributeType(4);
		assertEquals("ECG", orderGroupAttributeType.getName());
	}
	
	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupAttributeTypeByName(String)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupAttributeTypeByName_shouldGetOrderGroupAttributeTypeByName() {
		final String NAME = "ECG";
		OrderGroupAttributeType OrderGroupAttributeType = dao.getOrderGroupAttributeTypeByName(NAME);
		assertEquals(NAME, OrderGroupAttributeType.getName());
		assertEquals(4, OrderGroupAttributeType.getId());
		assertEquals("9cf1bdb2-d18e-11ea-87d0-0242ac130003", OrderGroupAttributeType.getUuid());
	}
	
	/**
	 * @see {@link HibernateOrderDAO#deleteOrderGroupAttributeType(OrderGroupAttributeType)}
	 * @throws Exception
	 */
	@Test
	public void deleteOrderGroupAttributeType_shouldDeleteOrderGroupAttributeTypeFromDatabase() {
		final String UUID = "9cf1bdb2-d18e-11ea-87d0-0242ac130003";
		OrderGroupAttributeType orderGroupAttributeType = dao.getOrderGroupAttributeTypeByUuid(UUID);
		assertNotNull(orderGroupAttributeType);
		dao.deleteOrderGroupAttributeType(orderGroupAttributeType);
		assertNull(dao.getOrderGroupAttributeByUuid(UUID));
	}
}
