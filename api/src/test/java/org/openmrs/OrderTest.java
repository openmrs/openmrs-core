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
package org.openmrs;

import org.junit.Test;
import org.openmrs.test.Verifies;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests all methods that are not getter or setters in the Order java object TODO: finish
 * this test class for Order
 * 
 * @see Order
 */
public class OrderTest {
	
	/**
	 * Tests the {@link Order#isDiscontinuedRightNow()} method TODO this should be split into many
	 * different tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldIsDiscontinued() throws Exception {
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Order o = new Order();
		assertFalse("order without dates shouldn't be discontinued", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setStartDate(ymd.parse("2007-01-01"));
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("order without no end dates shouldn't be discontinued", o.isDiscontinued(ymd.parse("2007-10-26")));
		
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be discontinued before autoExpireDate", o.isDiscontinued(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be discontinued after autoExpireDate", o.isDiscontinued(ymd.parse("2008-10-26")));
		
		o.setDateStopped(ymd.parse("2007-11-01"));
		assertFalse("shouldn't be discontinued before start date", o.isDiscontinued(ymd.parse("2006-10-26")));
		assertFalse("shouldn't be discontinued before dateStopped", o.isDiscontinued(ymd.parse("2007-10-26")));
		assertTrue("should be discontinued after dateStopped", o.isDiscontinued(ymd.parse("2007-11-26")));
		
	}
	
	/**
	 * Tests the {@link Order#isCurrent()} method TODO this should be split into many different
	 * tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldIsCurrent() throws Exception {
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		
		Order o = new Order();
		assertTrue("startDate==null && no end date should always be current", o.isCurrent(ymd.parse("2007-10-26")));
		
		o.setStartDate(ymd.parse("2007-01-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current after startDate", o.isCurrent(ymd.parse("2007-10-26")));
		
		o.setAutoExpireDate(ymd.parse("2007-12-31"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and autoExpireDate", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after autoExpireDate", o.isCurrent(ymd.parse("2008-10-26")));
		
		o.setDateStopped(ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and dateStopped", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after dateStopped", o.isCurrent(ymd.parse("2007-11-26")));
		
		o.setDateStopped(ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and dateStopped", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after dateStopped", o.isCurrent(ymd.parse("2007-11-26")));
	}
	
	/**
	 * @see {@link Order#cloneForDiscontinuing()}
	 */
	@Test
	@Verifies(value = "set previousOrder on new order", method = "cloneForDiscontinuing(Order)")
	public void cloneForDiscontinuing_shouldSetPreviousOrderOnNewOrder() throws Exception {
		Order anOrder = new Order();
		anOrder.setUuid(UUID.randomUUID().toString());
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals("should set previous order to anOrder", orderThatCanDiscontinueTheOrder.getPreviousOrder(), anOrder);
	}
	
	/**
	 * @see {@link Order#cloneForDiscontinuing()}
	 */
	@Test
	@Verifies(value = "set action to discontinue on new order", method = "cloneForDiscontinuing(Order)")
	public void cloneForDiscontinuing_shouldSetActionToDiscontinueOnNewOrder() throws Exception {
		Order anOrder = new Order();
		anOrder.setUuid(UUID.randomUUID().toString());
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals("should set new order action to new", orderThatCanDiscontinueTheOrder.getAction(),
		    Order.Action.DISCONTINUE);
	}
	
	/**
	 * @see {@link Order#cloneForDiscontinuing()}
	 */
	@Test
	@Verifies(value = "set this care setting to new order", method = "cloneForDiscontinuing(Order)")
	public void cloneForDiscontinuing_shouldSetThisCareSettingToNewOrder() throws Exception {
		Order anOrder = new Order();
		CareSetting careSetting = new CareSetting();
		anOrder.setCareSetting(careSetting);
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals(anOrder.getCareSetting(), orderThatCanDiscontinueTheOrder.getCareSetting());
	}
	
	/**
	 * @verifies return an instance of the same type
	 * @see Order#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldReturnAnInstanceOfTheSameType() throws Exception {
		Order order = new TestOrder();
		assertEquals(order.getClass(), order.cloneForDiscontinuing().getClass());
	}
}
