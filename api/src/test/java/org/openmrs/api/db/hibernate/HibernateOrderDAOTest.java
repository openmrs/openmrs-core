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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.internal.SessionFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.builder.OrderBuilder;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the saving of orders as part of the OrderGroup
 */
public class HibernateOrderDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	private HibernateOrderDAO dao;
	
	private static final String ORDER_SET = "org/openmrs/api/include/OrderSetServiceTest-general.xml";
	
	private static final String ORDER_GROUP = "org/openmrs/api/include/OrderServiceTest-createOrderGroup.xml";
     
	private static final String  UUID = "9cf1b9de-d18e-11ea-87d0-0242ac130003";
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
	 * @see {@link HibernateOrderDAO#getOrderGroupAttributeTypeByUuid(String)}
	 * @throws Exception
	 */
	@Test
	public void getOrderGroupAttributeByUuid_shouldFailGivenNullUuid(){
		assertThrows(APIException.class,() ->dao.getOrderGroupAttributeByUuid(null));
			}
	/**
	 * @see {@link HibernateOrderDAO#getAllOrderGroupAttributeTypes()}
	 * @throws Exception
	 */
    @Test
	public void getAllOrderGroupAttributeTypes_shouldGetAllOrderGroupAttributeTypes(){
		List<OrderGroupAttributeType> orderGroupAttributeTypes = dao.getAllOrderGroupAttributeTypes();
		assertEquals(orderGroupAttributeTypes.size(),4);
	}
	@Test
	public void getOrderGroupAttributeType_shouldGetOrderGroupAttributeTypeGivenUuid(){
		final String UUID2 ="9cf1bdb2-d18e-11ea-87d0-0242ac130003";
    	OrderGroupAttributeType newOrderGroupAttributeType = dao.getOrderGroupAttributeTypeByUuid(UUID2);
		assertEquals(newOrderGroupAttributeType.getName(),dao.getOrderGroupAttributeTypeByUuid(UUID2).getName());
	}
	@Test
	public void getOrderGroupAttributeType_shouldReturnOrderGroupAttributeTypeGivenIntegerId(){
    	final Integer ID = 4;
		OrderGroupAttributeType newOrderGroupAttributeType = dao.getOrderGroupAttributeType(ID);
		assertEquals(4,newOrderGroupAttributeType.getId());
		
	}
	

	/**
	 * @see {@link HibernateOrderDAO#getOrderGroupAttributeTypeByName(String)}
	 * @throws Exception
	 */
    @Test
    public void getOrderGroupAttributeTypeByName_shouldGetOrderGroupAttributeTypeByName(){
		final String NAME = "ECG";
		final String UUID4="9cf1bdb2-d18e-11ea-87d0-0242ac130003";
		OrderGroupAttributeType newOrderGroupAttributeType = dao.getOrderGroupAttributeTypeByName(NAME);
		assertEquals(NAME,newOrderGroupAttributeType.getName());
		assertEquals(4,newOrderGroupAttributeType.getId());
		assertEquals(UUID4,newOrderGroupAttributeType.getUuid());
    }
<<<<<<< HEAD
    
//    @Test
//	public void deleteOrderGroupAttributeType_shouldDeleteOrderGroupAttributeTypeFromDatabase(){
//    	String uuid = "9cf1bdb2-d18e-11ea-87d0-0242ac130003";
//		OrderGroupAttributeType orderGroupAttributeType = dao.getOrderGroupAttributeTypeByUuid(uuid);
//		OrderGroupAttribute orderGroupAttribute=dao.getOrderGroupAttributeByUuid(uuid);
//		assertNotNull(orderGroupAttribute);
//		dao.deleteOrderGroupAttributeType(orderGroupAttributeType);
//		assertNull(dao.getOrderGroupAttributeByUuid(uuid));
//	}
=======
>>>>>>> TRUNK-5410 Created OrderGroupAttribute,OrderGroupAttributeType
}
