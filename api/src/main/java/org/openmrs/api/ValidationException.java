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

import org.springframework.validation.Errors;

/**
 * Represents often fatal errors that occur when an object fails validation
 * 
 * @since 1.10
 */
public class ValidationException extends APIException {
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * Spring Errors object associated with the validation failure
	 * @since 1.11
	 */
	
	private Errors errors;
	
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
	
	/**
	 * Constructor used to associate an Spring Errors object with a ValidationException
	 *
	 * @param errors
	 * @since 1.11
	 */
	public ValidationException(Errors errors) {
		this.errors = errors;
	}
	
	/**
	 * Constructor to give the end user a helpful message and to associate an Spring Errors object
	 * with a ValidationException
	 *
	 * @param errors
	 * @since 1.11
	 */
	public ValidationException(String message, Errors errors) {
		super(message);
		this.errors = errors;
	}
	
	/**
	 * @since 1.11
	 */
	public Errors getErrors() {
		return errors;
	}
	
	/**
	 * @since 1.11
	 */
	public void setErrors(Errors errors) {
		this.errors = errors;
	}
}
