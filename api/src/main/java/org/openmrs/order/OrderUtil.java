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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.util.OpenmrsUtil;

/**
 * Contains convenience methods for working with Orders.
 */
public class OrderUtil {
	
	private static final Log log = LogFactory.getLog(OrderUtil.class);
	
	/**
	 * Checks whether orderType2 matches or is a sub type of orderType1
	 * 
	 * @since 1.10
	 * @param orderType1
	 * @param orderType2
	 * @return true if orderType2 matches or is a sub type of orderType1
	 * @should true if orderType2 is the same or is a subtype of orderType1
	 * @should false if orderType2 is neither the same nor a subtype of orderType1
	 * @should return false if they are both null
	 * @should return false if any is null and the other is not
	 */
	public static boolean isType(OrderType orderType1, OrderType orderType2) {
		if (orderType1 != null && orderType2 != null) {
			if (orderType2.equals(orderType1)) {
				return true;
			}
			OrderType parentType = orderType2.getParent();
			while (parentType != null) {
				if (parentType.equals(orderType1)) {
					return true;
				}
				parentType = parentType.getParent();
			}
			
		}
		return false;
	}
	
	/**
	 * Checks if the schedules of the specified orders overlap, Note this only makes a check that is
	 * purely based on dates ignoring other properties like patient, voided, careSetting and the
	 * orderable
	 * 
	 * @since 1.10
	 * @param order1, order2 orders to match
	 * @return true if the schedules overlap otherwise false
	 * @should return true if order1 and order2 do not have end date
	 * @should return true if order1 and order2 have same start dates
	 * @should return false if order1 ends before order2 starts
	 * @should return false if order1 starts after order2
	 * @should return true if order1 stops after the order2 has already been activated
	 * @should return true if order1 starts when the order2 is active
	 * @should return true if order1 starts before order2 and ends after order2
	 * @should return true if order2 starts before order1 and ends after order1
	 * @should return true if order1 starts on the stop date of order2
	 * @should return true if order1 ends on the start date of order2
	 * @should return true if both orders start and end on same dates
	 */
	public static boolean checkScheduleOverlap(Order order1, Order order2) {
		if (order2.getEffectiveStopDate() == null && order1.getEffectiveStopDate() == null) {
			return true;
		}
		
		if (order2.getEffectiveStopDate() == null) {
			return OpenmrsUtil.compare(order1.getEffectiveStopDate(), order2.getEffectiveStartDate()) > -1;
		}
		
		if (order1.getEffectiveStopDate() == null) {
			return (OpenmrsUtil.compare(order1.getEffectiveStartDate(), order2.getEffectiveStartDate()) > -1)
			        && (OpenmrsUtil.compare(order1.getEffectiveStartDate(), order2.getEffectiveStopDate()) < 1);
		}
		
		return (OpenmrsUtil.compare(order1.getEffectiveStartDate(), order2.getEffectiveStopDate()) < 1)
		        && (OpenmrsUtil.compare(order1.getEffectiveStopDate(), order2.getEffectiveStartDate()) > -1);
	}
	
}
