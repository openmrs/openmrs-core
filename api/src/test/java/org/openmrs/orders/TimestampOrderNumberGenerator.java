/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.orders;

import org.apache.commons.lang3.StringUtils;
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
