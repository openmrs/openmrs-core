/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.order;

import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains convenience methods for working with Orders.
 */
public class OrderUtil {
	
	private OrderUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(OrderUtil.class);
	
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
	 * @param order1 order to match
	 * @param order2 order to match
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
