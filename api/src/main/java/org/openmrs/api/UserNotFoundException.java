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

import org.openmrs.api.context.Context;

/**
 * Represents often fatal errors that occur when a user cannot be found due to incorrect, expired
 * token or wrong email and or username.
 */
public class UserNotFoundException extends APIException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UserNotFoundException() {
		
	}
	
	public UserNotFoundException(String message) {
		super(message);
	}
	
	public UserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UserNotFoundException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor to give the end user a helpful message that relates to why this error occurred.
	 * 
	 * @param messageKey message code to retrieve
	 * @param parameters message parameters
	 */
	public UserNotFoundException(String messageKey, Object[] parameters) {
		super(Context.getMessageSourceService().getMessage(messageKey, parameters, Context.getLocale()));
	}
	
}
