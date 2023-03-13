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
 * Exception thrown for situations of an invalid file type  
 * 
 * @see FormService
 */
public class InvalidFileTypeException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default empty constructor. If at all possible, don't use this one, but use the
	 * {@link #InvalidFileTypeException(String)} constructor to specify a helpful
	 * message to the end user
	 */
	public InvalidFileTypeException() {
		super();
	}
	
	/**
	 * General constructor to give a helpful message that relates to why this error occured
	 * 
	 * @param message helpful message string 
	 */
	public InvalidFileTypeException(String message) {
		super(message);
	}
	
	/**
	 * General constructor to give the end user a helpful message and to also propagate the parent
	 * error exception message.
	 *  
	 * @param message helpful message string for the end user
	 * @param cause the parent exception cause that this exception is wrapping around
	 */
	public InvalidFileTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}
