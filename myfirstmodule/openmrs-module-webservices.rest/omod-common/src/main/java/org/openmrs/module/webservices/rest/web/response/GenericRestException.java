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

/**
 * Generic exception, to be thrown if nothing more specific can be thrown. (Please try to throw a
 * more specif exception whenever possible.)
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class GenericRestException extends ResponseException {
	
	private static final long serialVersionUID = 1L;
	
	public GenericRestException() {
		super();
	}
	
	public GenericRestException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GenericRestException(String message) {
		super(message);
	}
	
	public GenericRestException(Throwable cause) {
		super(cause);
	}
}
