/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.validation;

import org.openmrs.api.APIException;
import org.springframework.validation.Errors;

/**
 * Exception that represents a Spring validation error. Includes the Spring Errors object associated
 * This is just a copy of the ValidationException class in 1.10.x, with the Spring Errors object
 * added. The Spring Errors object will be also added to the ValidationException in core as part of
 * TRUNK-4296 Once REST-WS only supports versions of core that have this change, this class (and
 * ValidateUtil) can be removed, and we will not have to explicitly call the "validate" within the
 * DelegatingCrudResource, as we can rely on the underlying API validation to do the right thing,
 * and return a ValidationException with Errors which we can trap in the BaseRestController
 */
public class ValidationException extends APIException {
	
	public static final long serialVersionUID = 1L;
	
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
	 * Constructor used to associate an Spring Errors object with a ValidationException
	 * 
	 * @param errors
	 */
	public ValidationException(Errors errors) {
		this.errors = errors;
	}
	
	/**
	 * Constructor to give the end user a helpful message and to associate an Spring Errors object
	 * with a ValidationException
	 * 
	 * @param errors
	 */
	public ValidationException(String message, Errors errors) {
		super(message);
		this.errors = errors;
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
	
	public Errors getErrors() {
		return errors;
	}
	
	public void setErrors(Errors errors) {
		this.errors = errors;
	}
}
