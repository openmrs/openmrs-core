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
 * Superclass of all order entry related exceptions and should typically be thrown whenever there is
 * no more specific type can be thrown.
 * 
 * @since 2.1
 */
public class OrderEntryException extends APIException {
	
	public OrderEntryException(String message) {
		super(message);
	}
	
	public OrderEntryException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public OrderEntryException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
}
