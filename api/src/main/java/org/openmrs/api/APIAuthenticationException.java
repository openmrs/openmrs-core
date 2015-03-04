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
 * Represents often fatal errors that occur within the API infrastructure involving a user's lack of
 * privileges. In certain presentation environments, this exception is caught and the user is
 * redirected to the login page where they can provide new or higher credentials.
 */
public class APIAuthenticationException extends APIException {
	
	public static final long serialVersionUID = 12121213L;
	
	/**
	 * Default empty constructor. It is more common to use the
	 * {@link #APIAuthenticationException(String)} constructor to provide some context to the user
	 * as to where/why the authentication has failed
	 */
	public APIAuthenticationException() {
	}
	
	/**
	 * Common constructor taking in a message to give the user some context as to where/why the
	 * authentication failed.
	 * 
	 * @param message String describing where/why the authentication failed
	 */
	public APIAuthenticationException(String message) {
		super(message);
	}
	
	/**
	 * Common constructor taking in a message to give the user some context as to where/why the
	 * authentication failed.
	 * 
	 * @param message String describing where/why the authentication failed
	 * @param cause error further up the stream that caused this authentication failure
	 */
	public APIAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor giving the user a further cause exception reason that caused this authentication
	 * failure
	 * 
	 * @param cause error further up the stream that caused this authentication failure
	 */
	public APIAuthenticationException(Throwable cause) {
		super(cause);
	}
	
}
