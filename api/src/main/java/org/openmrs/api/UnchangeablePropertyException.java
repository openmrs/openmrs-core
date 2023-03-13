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
 * An instance of this exception is thrown if an attempt is made to update an unchangeable property
 * on an object
 *
 * @since 2.1
 */
public class UnchangeablePropertyException extends APIException {
	
	public UnchangeablePropertyException(String message) {
		super(message);
	}
	
	public UnchangeablePropertyException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnchangeablePropertyException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
	/**
	 * @param clazz the class of the object on which it was changed
	 * @param property the name of the unchangeable property
	 */
	public UnchangeablePropertyException(Class clazz, String property) {
		this(clazz.getName() + "." + property + " cannot be changed");
	}
}
