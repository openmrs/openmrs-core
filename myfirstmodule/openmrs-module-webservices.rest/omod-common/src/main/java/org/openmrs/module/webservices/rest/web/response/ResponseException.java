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

/**
 * This is a marker class for all response exceptions. Controllers should throw these if an error
 * has occurred or a status needs to be shown to a user
 */
public abstract class ResponseException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public ResponseException() {
		super();
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public ResponseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param message
	 */
	public ResponseException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public ResponseException(Throwable cause) {
		super(cause);
	}
	
}
