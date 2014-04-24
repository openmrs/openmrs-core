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
package org.openmrs.order;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.OrderType;

/**
 * Contains test for OrderUtil
 */
public class OrderUtilTest {
	
	public static boolean isActiveOrder(Order order, Date asOfDate) {
		return order.isCurrent(asOfDate) && order.getAction() != Order.Action.DISCONTINUE;
	}
	
	public static void setDateStopped(Order targetOrder, Date dateStopped) throws Exception {
		Method method = null;
		Boolean isMethodAccessible = null;
		try {
			method = Order.class.getDeclaredMethod("setDateStopped", Date.class);
			isMethodAccessible = method.isAccessible();
			if (!isMethodAccessible) {
				method.setAccessible(true);
			}
			method.invoke(targetOrder, dateStopped);
		}
		finally {
			if (method != null && isMethodAccessible != null) {
				method.setAccessible(isMethodAccessible);
			}
		}
	}
	
	/**
	 * @verifies true if orderType2 is the same or is a subtype of orderType1
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldTrueIfOrderType2IsTheSameOrIsASubtypeOfOrderType1() throws Exception {
		OrderType orderType = new OrderType();
		OrderType subType1 = new OrderType();
		OrderType subType2 = new OrderType();
		subType2.setParent(subType1);
		subType1.setParent(orderType);
		assertTrue(OrderUtil.isType(subType2, subType2));
		assertTrue(OrderUtil.isType(subType1, subType2));
		assertTrue(OrderUtil.isType(orderType, subType2));
	}
	
	/**
	 * @verifies return false if they are both null
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldReturnFalseIfTheyAreBothNull() throws Exception {
		assertFalse(OrderUtil.isType(null, null));
	}
	
	/**
	 * @verifies return false if any is null and the other is not
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldReturnFalseIfAnyIsNullAndTheOtherIsNot() throws Exception {
		assertFalse(OrderUtil.isType(new OrderType(), null));
		assertFalse(OrderUtil.isType(null, new OrderType()));
	}
	
	/**
	 * @verifies false if orderType2 is neither the same nor a subtype of orderType1
	 * @see OrderUtil#isType(org.openmrs.OrderType, org.openmrs.OrderType)
	 */
	@Test
	public void isType_shouldFalseIfOrderType2IsNeitherTheSameNorASubtypeOfOrderType1() throws Exception {
		assertFalse(OrderUtil.isType(new OrderType(), new OrderType()));
	}
}
