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
package org.openmrs.attribute;

import org.openmrs.api.APIException;

/**
 * Exception thrown when trying to convert between a serialized attribute value and its typed value, if the
 * serialized String is not a legal value for the given handler.
 * @since 1.9
 */
public class InvalidAttributeValueException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param message
	 */
	public InvalidAttributeValueException(String message) {
		super(message);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public InvalidAttributeValueException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
