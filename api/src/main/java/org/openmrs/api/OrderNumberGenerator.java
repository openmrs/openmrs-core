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
