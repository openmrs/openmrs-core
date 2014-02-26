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
 * Represents often fatal errors that occur when an object fails validation
 * 
 * @since 1.10
 */
public class ValidationException extends APIException {
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * Default empty constructor. If at all possible, don't use this one, but use the
	 */
	public ValidationException() {
	}
	
	/**
	 * General constructor to give the end user a helpful message that relates to why this error
	 * occurred.
	 * 
	 * @param message helpful message string for the end user
	 */
	public ValidationException(String message) {
		super(message);
	}
	
	/**
	 * General constructor to give the end user a helpful message and to also propagate the parent
	 * error exception message.
	 * 
	 * @param message helpful message string for the end user
	 * @param cause the parent exception cause that this ValidationException is wrapping around
	 */
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor used to simply chain a parent exception cause to an ValidationException.
	 * 
	 * @param cause the parent exception cause that this ValidationException is wrapping around
	 */
	public ValidationException(Throwable cause) {
		super(cause);
	}
}
