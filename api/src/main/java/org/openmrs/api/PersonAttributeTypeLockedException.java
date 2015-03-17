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

import org.openmrs.util.OpenmrsConstants;

/**
 * This exception is thrown when a user tries manipulate of a person attribute type while person attribute types are locked
 * 
 * @see org.openmrs.util.OpenmrsConstants#GLOBAL_PROPERTY_PERSON_ATRIBUTE_TYPES_LOCKED
 * @see PersonService#checkIfPersonAttributeTypesAreLocked() 
 */
public class PersonAttributeTypeLockedException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Generic constructor that gives a normal reason why the user is not being allowed to manipulate of a person attribute type.
	 */
	public PersonAttributeTypeLockedException() {
		this("PersonAtttributeType.locked");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * 
	 * @param message the reason to show to the user as to why we cannot manipulate of a person attribute type
	 */
	public PersonAttributeTypeLockedException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the reason to show to the user as to why we cannot manipulate of a person attribute type
	 * @param cause the parent exception
	 */
	public PersonAttributeTypeLockedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with.
	 * This does not set the error message for the user as to why an exception is being thrown. 
	 * The {@link #PersonAttributeTypeLockedException(String, Throwable)} constructor is preferred over this one.
	 * 
	 * @param cause the parent exception
	 */
	public PersonAttributeTypeLockedException(Throwable cause) {
		super(cause);
	}
}
