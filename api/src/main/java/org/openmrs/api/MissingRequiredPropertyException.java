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
 * An instance of this exception is thrown when a required property has not been set on an Object.
 *
 * @since 2.1
 */
public class MissingRequiredPropertyException extends APIException {
	
	public MissingRequiredPropertyException(String message) {
		super(message);
	}
	
	public MissingRequiredPropertyException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public MissingRequiredPropertyException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
	/**
	 * @param clazz the class of the object on which the property is required
	 * @param property the name of the missing required property
	 */
	public MissingRequiredPropertyException(Class clazz, String property) {
		this(clazz.getName() + "." + property + " is required");
	}
}
