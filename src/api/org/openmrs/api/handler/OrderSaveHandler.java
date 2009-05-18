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

import org.openmrs.Order;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

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
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	public void handle(Order order, User creator, Date dateCreated, String other) {
		if (order.getPatient() == null && order.getEncounter() != null)
			order.setPatient(order.getEncounter().getPatient());
	}
}
