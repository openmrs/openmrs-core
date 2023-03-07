/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openmrs.CareSetting;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;

public class OrderUtil {

	private OrderUtil(){}
	
	private static final String INACTIVE = "inactive";
	
	private static final String ANY = "any";
	
	/**
	 * Gets the inactive orders of the specified patient as of the specified date, defaults to
	 * current date if no date is specified
	 * 
	 * @param patient
	 * @param careSetting
	 * @param orderType
	 * @param asOfDate
	 * @return
	 */
	public static List<Order> getOrders(Patient patient, CareSetting careSetting, OrderType orderType, String status,
	        Date asOfDate, boolean includeVoided) {
		
		OrderService os = Context.getOrderService();
		if (!INACTIVE.equals(status) && !ANY.equals(status)) {
			return os.getActiveOrders(patient, orderType, careSetting, asOfDate);
		}
		
		if (INACTIVE.equals(status)) {
			includeVoided = false;
		}
		
		List<Order> orders = os.getOrders(patient, careSetting, orderType, includeVoided);
		if (INACTIVE.equals(status)) {
			removeActiveOrders(orders, asOfDate);
		}
		
		return orders;
	}
	
	private static void removeActiveOrders(List<Order> orders, final Date asOfDate) {
		
		CollectionUtils.filter(orders, new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				Order order = (Order) object;
				return order.isDiscontinued(asOfDate) || order.isExpired(asOfDate);
			}
			
		});
	}
}
