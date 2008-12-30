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
package org.openmrs.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO clean up and test all methods in OrderService
 */
public class OrderServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * Adds then updates an order
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldOrderCreateUpdateDelete() throws Exception {
		
	}
	
	/**
	 * Adds then updates a drug order
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldDrugOrderCreateUpdateDelete() throws Exception {
		
	}
	
	/**
	 * TODO finish and activate this test method
	 * 
	 * @throws Exception
	 */
	public void xtestOrderType() throws Exception {
		
		OrderService orderService = Context.getOrderService();
		
		//testing creation
		
		OrderType orderType = new OrderType();
		
		orderType.setName("testing");
		orderType.setDescription("desc");
		
		orderService.saveOrderType(orderType);
		assertNotNull(orderType.getOrderTypeId());
		
		List<OrderType> orderTypes = orderService.getAllOrderTypes();
		
		//make sure we get a list
		assertNotNull(orderTypes);
		
		boolean found = false;
		for (Iterator<OrderType> i = orderTypes.iterator(); i.hasNext();) {
			OrderType orderType2 = i.next();
			assertNotNull(orderType);
			//check .equals function
			assertTrue(orderType.equals(orderType2) == (orderType.getOrderTypeId().equals(orderType2.getOrderTypeId())));
			//mark found flag
			if (orderType.equals(orderType2))
				found = true;
		}
		
		//assert that the new orderType was returned in the list
		assertTrue(found);
		
		//check update
		orderType.setName("another test");
		orderService.saveOrderType(orderType);
		
		OrderType newerOrderType = orderService.getOrderType(orderType.getOrderTypeId());
		assertTrue(newerOrderType.getName().equals(orderType.getName()));
		
		//check deletion
		
		// TODO must create this method before testing it!
		//as.deleteOrderType(orderType.getOrderTypeId());
		
		assertNull(orderService.getOrderType(orderType.getOrderTypeId()));
		
	}
	
}
