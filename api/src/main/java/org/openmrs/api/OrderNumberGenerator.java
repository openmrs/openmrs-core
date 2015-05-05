/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

/**
 * Will be implemented by classes that auto generate order numbers. This will let implementations or
 * modules create their own order number generation schemes.
 */
public interface OrderNumberGenerator {
	
	/**
	 * Generates a new order number. Note that this method is invoked in a non thread-safe way,
	 * therefore implementations need to be thread safe.
	 * 
	 * @return the new order number
	 * @should always return unique orderNumbers when called multiple times without saving orders
	 * @param orderContext
	 */
	public String getNewOrderNumber(OrderContext orderContext);
}
