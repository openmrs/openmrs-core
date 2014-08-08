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
	 * Checks whether schedule of this order overlaps with schedule of other order
	 *
	 * @since 1.10
	 * @param order1, order2 orders to match
	 * @should return false if any of the orders is voided
	 * @should return true if order1 and order2 do not have end date
	 * @should return true if order1 and order2 have same dates
	 * @should return false if order1 ends before order2 starts
	 * @should return false if order1 ends when order2 starts
	 * @should return false if order1 starts after order2
	 * @should return false if order1 starts when order2 ends
	 * @should return true if order1 stops after the order2 has already been activated
	 * @should return true if order1 starts when the order2 is active
	 * @should return true if order1 starts before order2 and ends after order2
	 */
	public static boolean checkScheduleOverlap(Order order1, Order order2) {
		if (order1.getVoided() == true || order2.getVoided() == true) {
			return false;
		}
		if (order2.getEffectiveStopDate() == null && order1.getEffectiveStopDate() == null) {
			return true;
		}
		
		if (order2.getEffectiveStopDate() == null) {
			return order1.getEffectiveStopDate().after(order2.getEffectiveStartDate());
		}
		
		if (order1.getEffectiveStopDate() == null) {
			return order1.getEffectiveStartDate().after(order2.getEffectiveStartDate())
			        && order1.getEffectiveStartDate().before(order2.getEffectiveStopDate());
		}
		
		return order1.getEffectiveStartDate().before(order2.getEffectiveStopDate())
		        && order1.getEffectiveStopDate().after(order2.getEffectiveStartDate());
	}
	
}
