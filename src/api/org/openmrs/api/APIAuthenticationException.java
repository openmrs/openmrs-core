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
 * Represents often fatal errors that occur within the API
 * infrastructure involving a user's lack of privileges.
 *  

 * @version 1.0
 */
public class APIAuthenticationException extends APIException {

	public static final long serialVersionUID = 12121213L;
	
	public APIAuthenticationException() {
	}

	public APIAuthenticationException(String message) {
		super(message);
	}

	public APIAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public APIAuthenticationException(Throwable cause) {
		super(cause);
	}

}
