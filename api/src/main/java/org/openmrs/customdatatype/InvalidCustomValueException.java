/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import org.openmrs.api.APIException;

/**
 * Exception thrown when trying to convert between a serialized attribute value and its typed value, if the
 * serialized String is not a legal value for the given handler.
 * @since 1.9
 */
public class InvalidCustomValueException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param message
	 */
	public InvalidCustomValueException(String message) {
		super(message);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public InvalidCustomValueException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
