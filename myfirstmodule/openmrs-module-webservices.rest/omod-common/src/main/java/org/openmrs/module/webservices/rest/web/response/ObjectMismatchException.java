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
 * This exception should be thrown from controllers when a subresource (like name) is fetched but
 * the uuid does not match the uuid of the main resource (like person).
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Subresource with given uuid doesn't match the uuid of the main resource")
public class ObjectMismatchException extends ResponseException {
	
	private static final long serialVersionUID = 1L;
	
	public ObjectMismatchException() {
	}
	
	public ObjectMismatchException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
