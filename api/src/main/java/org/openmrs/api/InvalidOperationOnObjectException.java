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
 * An instance of this exception is thrown if an operation is attempted on an object, typically this
 * should be thrown when any operation is made to an existing object and is considered detrimental
 * to the integrity of any existing associated data. This is a more generic exception that doesn't
 * convey any specifics on the actual operation that was attempted on the object.
 * 
 * @see UnchangeableObjectException
 * @see CannotDeleteObjectInUseException
 * @see CannotUpdateObjectInUseException
 * @since 2.1
 */
public class InvalidOperationOnObjectException extends APIException {
	
	public InvalidOperationOnObjectException(String message) {
		super(message);
	}
	
	public InvalidOperationOnObjectException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidOperationOnObjectException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
	/**
	 * @param clazz the type of the object on which the operation was attempted
	 */
	public InvalidOperationOnObjectException(Class clazz) {
		this("An invalid operation was attempted on an instance of " + clazz.getName());
	}
}
