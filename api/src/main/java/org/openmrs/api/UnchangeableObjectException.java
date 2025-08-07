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
 * An instance of this exception is thrown if an attempt is made to update an unchangeable object
 * 
 * @since 2.1
 */
public class UnchangeableObjectException extends InvalidOperationOnObjectException {
	
	public UnchangeableObjectException() {
		this("Cannot update an unchangeable object");
	}
	
	public UnchangeableObjectException(String message) {
		super(message);
	}
	
	public UnchangeableObjectException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnchangeableObjectException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
	/**
	 * @see InvalidOperationOnObjectException#InvalidOperationOnObjectException(Class)
	 */
	public UnchangeableObjectException(Class clazz) {
		super(clazz);
	}
}
