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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.junit.Test;
import org.openmrs.order.OrderUtilTest;
import org.openmrs.util.Reflect;

/**
 * This class tests all methods that are not getter or setters in the Order java object TODO: finish
 * this test class for Order
 * 
 * @see Order
 */
public class OrderTest {
	
	protected static void assertThatAllFieldsAreCopied(Order original, String methodName, String... otherfieldsToExclude)
	        throws Exception {
		if (methodName == null) {
			methodName = "copy";
		}
		List<String> fieldsToExclude = new ArrayList<String>();
		fieldsToExclude.addAll(Arrays.asList("log", "serialVersionUID", "orderId", "uuid"));
		if (otherfieldsToExclude != null) {
			fieldsToExclude.addAll(Arrays.asList(otherfieldsToExclude));
		}
		List<Field> fields = Reflect.getAllFields(original.getClass());
		for (Field field : fields) {
			if (fieldsToExclude.contains(field.getName())) {
				continue;
			}
			field.setAccessible(true);
			Object fieldValue = null;
			
			if (field.getType().isEnum()) {
				fieldValue = field.getType().getEnumConstants()[0];
			} else if (field.getType().equals(Boolean.class)) {
				fieldValue = true;
			} else if (field.getType().equals(Integer.class)) {
				fieldValue = 10;
			} else if (field.getType().equals(Double.class)) {
				fieldValue = 5.0;
			} else {
				fieldValue = field.getType().newInstance();
			}
			field.set(original, fieldValue);
		}
		
		Order copy = (Order) MethodUtils.invokeExactMethod(original, methodName, null);
		for (Field field : fields) {
			Object copyValue = field.get(copy);
			if (fieldsToExclude.contains(field.getName())) {
				continue;
			}
			assertNotNull("Order." + methodName + " should set " + field.getName() + " on the new Order", copyValue);
			assertEquals("Order." + methodName + " should set " + field.getName() + " on the new Order",
			    field.get(original), copyValue);
		}
	}
	
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
		
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));
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
		
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and dateStopped", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after dateStopped", o.isCurrent(ymd.parse("2007-11-26")));
		
		OrderUtilTest.setDateStopped(o, ymd.parse("2007-11-01"));
		assertFalse("shouldn't be current before startDate", o.isCurrent(ymd.parse("2006-10-26")));
		assertTrue("should be current between startDate and dateStopped", o.isCurrent(ymd.parse("2007-10-26")));
		assertFalse("shouldn't be current after dateStopped", o.isCurrent(ymd.parse("2007-11-26")));
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see Order#cloneForDiscontinuing()
	 */
	@Test
	public void cloneForDiscontinuing_shouldSetAllTheRelevantFields() throws Exception {
		
		Order anOrder = new Order();
		anOrder.setPatient(new Patient());
		anOrder.setCareSetting(new CareSetting());
		anOrder.setConcept(new Concept());
		anOrder.setOrderType(new OrderType());
		
		Order orderThatCanDiscontinueTheOrder = anOrder.cloneForDiscontinuing();
		
		assertEquals(anOrder.getPatient(), orderThatCanDiscontinueTheOrder.getPatient());
		
		assertEquals(anOrder.getConcept(), orderThatCanDiscontinueTheOrder.getConcept());
		
		assertEquals("should set previous order to anOrder", anOrder, orderThatCanDiscontinueTheOrder.getPreviousOrder());
		
		assertEquals("should set new order action to new", orderThatCanDiscontinueTheOrder.getAction(),
		    Order.Action.DISCONTINUE);
		
		assertEquals(anOrder.getCareSetting(), orderThatCanDiscontinueTheOrder.getCareSetting());
		
		assertEquals(anOrder.getOrderType(), orderThatCanDiscontinueTheOrder.getOrderType());
	}
	
	/**
	 * @verifies copy all fields
	 * @see Order#copy()
	 */
	@Test
	public void copy_shouldCopyAllFields() throws Exception {
		assertThatAllFieldsAreCopied(new Order(), null);
		assertThatAllFieldsAreCopied(new TestOrder(), null);
	}
	
	/**
	 * @verifies set all the relevant fields
	 * @see Order#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetAllTheRelevantFields() throws Exception {
		assertThatAllFieldsAreCopied(new Order(), "cloneForRevision", "creator", "dateCreated", "action", "changedBy",
		    "dateChanged", "voided", "dateVoided", "voidedBy", "voidReason", "encounter", "orderNumber", "orderer",
		    "previousOrder", "startDate", "dateStopped");
	}
	
	/**
	 * @verifies true if it is the same or is a subtype
	 * @see Order#isType(OrderType)
	 */
	@Test
	public void isType_shouldTrueIfItIsTheSameOrIsASubtype() throws Exception {
		Order order = new Order();
		OrderType orderType = new OrderType();
		OrderType subType1 = new OrderType();
		OrderType subType2 = new OrderType();
		subType2.setParent(subType1);
		subType1.setParent(orderType);
		order.setOrderType(subType2);
		
		assertTrue(order.isType(subType2));
		assertTrue(order.isType(subType1));
		assertTrue(order.isType(orderType));
	}
	
	/**
	 * @verifies false if it neither the same nor a subtype
	 * @see Order#isType(OrderType)
	 */
	@Test
	public void isType_shouldFalseIfItNeitherTheSameNorASubtype() throws Exception {
		Order order = new Order();
		order.setOrderType(new OrderType());
		
		assertFalse(order.isType(new OrderType()));
	}
	
	/**
	 * @verifies set the relevant fields for a DC order
	 * @see Order#cloneForRevision()
	 */
	@Test
	public void cloneForRevision_shouldSetTheRelevantFieldsForADCOrder() throws Exception {
		Order order = new Order();
		order.setAction(Order.Action.DISCONTINUE);
		Date date = new Date();
		order.setStartDate(date);
		order.setPreviousOrder(new Order());
		OrderUtilTest.setDateStopped(order, date);
		
		Order clone = order.cloneForRevision();
		assertEquals(Order.Action.DISCONTINUE, clone.getAction());
		assertEquals(order.getStartDate(), clone.getStartDate());
		assertEquals(order.getPreviousOrder(), clone.getPreviousOrder());
		assertNull(clone.getDateStopped());
	}
}
