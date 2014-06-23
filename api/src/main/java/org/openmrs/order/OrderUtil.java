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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.OrderType;
import org.openmrs.Patient;

/**
 * Contains convenience methods for working with Orders.
 *
 */
public class OrderUtil {
	
	private static final Log log = LogFactory.getLog(OrderUtil.class);
	
	/**
	 * Discontinues all current orders for the given <code>patient</code>
	 *
	 * @param patient
	 * @param discontinueReason
	 * @param discontinueDate
	 * @should discontinue all orders for the given patient if none are yet discontinued
	 * @should not affect orders that were already discontinued on the specified date
	 * @should not affect orders that end before the specified date
	 * @should not affect orders that start after the specified date
	 */
	public static void discontinueAllOrders(Patient patient, Concept discontinueReason, Date discontinueDate) {
		if (log.isDebugEnabled()) {
			log.debug("In discontinueAll with patient " + patient + " and concept " + discontinueReason + " and date "
			        + discontinueDate);
		}
		
		//TODO discontinue all active drug orders for a patient
		//See https://tickets.openmrs.org/browse/TRUNK-4185
	}
	
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
	
}
