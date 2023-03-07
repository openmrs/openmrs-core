/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalPropertyException extends ResponseException {
	
	private static final long serialVersionUID = 1L;
	
	public IllegalPropertyException() {
		super();
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public IllegalPropertyException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param message
	 */
	public IllegalPropertyException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public IllegalPropertyException(Throwable cause) {
		super(cause);
	}
	
}
