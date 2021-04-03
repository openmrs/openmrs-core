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
 * Represents often fatal errors that occur within the API infrastructure. All service methods
 * should be marked as possibly throwing this exception. The preferred methods to use in this
 * exception is the {@link #APIException(String, Throwable)} or the {@link #APIException(String)}
 */
public class APIException extends RuntimeException {
	
	public static final long serialVersionUID = 12121212L;
	
	/**
	 * Default empty constructor. If at all possible, don't use this one, but use the
	 * {@link #APIException(String)} constructor to specify a helpful message to the end user
	 */
	public APIException() {
	}
	
	/**
	 * General constructor to give the end user a helpful message that relates to why this error
	 * occurred.
	 * 
	 * @param message helpful message string for the end user
	 */
	public APIException(String message) {
		super(message);
	}
	
	/**
	 * General constructor to give the end user a helpful message and to also propagate the parent
	 * error exception message.
	 * 
	 * @param message helpful message string for the end user
	 * @param cause the parent exception cause that this APIException is wrapping around
	 */
	public APIException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor used to simply chain a parent exception cause to an APIException. Preference
	 * should be given to the {@link #APIException(String, Throwable)} constructor if at all
	 * possible instead of this one.
	 * 
	 * @param cause the parent exception cause that this APIException is wrapping around
	 */
	public APIException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructor to give the end user a helpful message that relates to why this error occurred.
	 * 
	 * @param messageKey message code to retrieve
	 * @param parameters message parameters
	 */
	public APIException(String messageKey, Object[] parameters) {
		super(Context.getMessageSourceService().getMessage(messageKey, parameters, Context.getLocale()));
	}
	
	/**
	 * Constructor to give the end user a helpful message and to also propagate the parent
	 * error exception message..
	 *
	 * @param messageKey message code to retrieve
	 * @param parameters message parameters
	 * @param cause the parent exception cause that this APIException is wrapping around   
	 */
	public APIException(String messageKey, Object[] parameters, Throwable cause) {
		super(Context.getMessageSourceService().getMessage(messageKey, parameters, Context.getLocale()), cause);
	}
}
