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
 * An instance of this exception is thrown if an update operation is attempted on an object that is
 * referenced by others, typically this should be thrown when altering an existing object is
 * detrimental to the integrity of objects referencing it or any other existing associated data.
 *
 * @since 2.1
 */
public class CannotUpdateObjectInUseException extends InvalidOperationOnObjectException {
	
	public CannotUpdateObjectInUseException(String message) {
		super(message);
	}
	
	public CannotUpdateObjectInUseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CannotUpdateObjectInUseException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
	/**
	 * @see InvalidOperationOnObjectException#InvalidOperationOnObjectException(Class)
	 */
	public CannotUpdateObjectInUseException(Class clazz) {
		super(clazz);
	}
}
