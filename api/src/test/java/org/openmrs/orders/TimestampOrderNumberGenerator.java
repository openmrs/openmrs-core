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
package org.openmrs.orders;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.OrderContext;
import org.openmrs.api.OrderNumberGenerator;
import org.springframework.stereotype.Component;

/**
 * An OrderNumberGenerator used for testing purposes, returns the order number set as a context
 * attribute otherwise returns one matching the current time stamp and prepends a prefix to it
 */
@Component("orderEntry.OrderNumberGenerator")
public class TimestampOrderNumberGenerator implements OrderNumberGenerator {
	
	public static final String NEXT_ORDER_NUMBER = "nextOrderNumber";
	
	public static final String ORDER_NUMBER_PREFIX = "TEST-PREFIX-";
	
	@Override
	public String getNewOrderNumber(OrderContext orderContext) {
		String nextOrderNumber = null;
		if (orderContext != null) {
			nextOrderNumber = (String) orderContext.getAttribute(NEXT_ORDER_NUMBER);
		}
		if (StringUtils.isBlank(nextOrderNumber)) {
			nextOrderNumber = ORDER_NUMBER_PREFIX + System.currentTimeMillis();
		}
		return nextOrderNumber;
	}
}
