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
package org.openmrs.api.handler;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Order;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

/**
 * This class deals with {@link Order} objects when they are saved via a save* method in an Openmrs
 * Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP class. <br/>
 * 
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see Order
 * @since 1.5
 */
@Handler(supports = Order.class)
public class OrderSaveHandler implements SaveHandler<Order> {
	
	/**
	 * Used to store the last used order number
	 */
	private static Integer orderNumber;
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 * @should set the order number
	 * @should return the next available order number
	 * @should not assign an new order number to an existing order
	 */
	public void handle(Order order, User creator, Date dateCreated, String other) {
		if (order.getPatient() == null && order.getEncounter() != null)
			order.setPatient(order.getEncounter().getPatient());
		if (order.getOrderNumber() == null) {
			synchronized (OrderSaveHandler.class) {
				if (orderNumber == null) {
					orderNumber = 0;
					try {
						Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
						List<List<Object>> rowObjects = Context.getAdministrationService().executeSQL(
						    "SELECT max(order_id) FROM orders", true);
						if (CollectionUtils.isNotEmpty(rowObjects) && CollectionUtils.isNotEmpty(rowObjects.get(0))
						        && rowObjects.get(0).get(0) != null)
							orderNumber = (Integer) rowObjects.get(0).get(0);
					}
					finally {
						Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
					}
				}
				
				orderNumber++;
			}
			
			String nextOrderNumber = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GP_ORDER_ENTRY_ORDER_NUMBER_PREFIX, OpenmrsConstants.ORDER_NUMBER_DEFAULT_PERFIX)
			        + orderNumber.toString();
			order.setOrderNumber(nextOrderNumber);
		}
	}
}
