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
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.api;

import org.openmrs.util.OpenmrsConstants;

/**
 * This exception is thrown the user tries to edit a person attribute type when person attribute types are locked
 * this is done by a global property being true/false.
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_PERSON_ATTRIBUTE_TYPES_LOCKED
 * @see PersonService#checkIfPersonAttributeTypesAreLocked()
 * @since 1.10
 */
public class PersonAttributeTypeLockedException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Generic constructor that gives a normal message about editing not being allowed to the user.
	 */
	public PersonAttributeTypeLockedException() {
		this("PersonAttributeType.locked.exception");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * 
	 * @param message the String to show to the user as to why person attribute types are locked
	 */
	public PersonAttributeTypeLockedException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than the normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the String to show to the user as to why person attribute types are locked
	 * @param cause the parent exception
	 */
	public PersonAttributeTypeLockedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with. This does not
	 * set the error message for the user as to why an exception is being thrown. The
	 * {@link #PersonAttributeTypeLockedException(String, Throwable)} constructor is preferred over
	 * this one.
	 * 
	 * @param cause the parent exception
	 */
	public PersonAttributeTypeLockedException(Throwable cause) {
		super(cause);
	}
}
