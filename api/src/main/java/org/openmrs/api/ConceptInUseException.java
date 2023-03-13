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
 * This exception is thrown when concept is used/attached to an observation.
 * 
 * @see ConceptService#saveConcept(org.openmrs.Concept)
 * @since Version 1.7
 */
public class ConceptInUseException extends APIException {
	
	private static final long serialVersionUID = 132352321232223L;
	
	/**
	 * Generic constructor that gives a normal message about editing not being allowed to the user.
	 */
	public ConceptInUseException() {
		this("The concepts datatype cannot be changed if it is already used/associated to an observation");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * 
	 * @param message the String to show to the user as to why the concepts are locked
	 */
	public ConceptInUseException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than the normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the String to show to the user as to why the concepts are locked
	 * @param cause the parent exception
	 */
	public ConceptInUseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with. This does not
	 * set the error message for the user as to why an exception is being thrown. The
	 * {@link #ConceptInUseException(String, Throwable)} constructor is preferred over this one.
	 * 
	 * @param cause the parent exception
	 */
	public ConceptInUseException(Throwable cause) {
		super(cause);
	}
}
