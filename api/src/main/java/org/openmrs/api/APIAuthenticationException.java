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
