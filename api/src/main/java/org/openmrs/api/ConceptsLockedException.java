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
package org.openmrs.api;

import org.openmrs.util.OpenmrsConstants;

/**
 * This exception is thrown when a specific implementation has chosen to lock down their concepts
 * and prevent editing. Currently this is simply done through a global property being true/false.
 * Any call that will manipulate a concept or concept_* table should throw this.
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_CONCEPTS_LOCKED
 * @see ConceptService#checkIfLocked()
 * @see ConceptService#saveConcept(org.openmrs.Concept)
 */
public class ConceptsLockedException extends APIException {
	
	private static final long serialVersionUID = 132352321232223L;
	
	/**
	 * Generic constructor that gives a normal message about editing not being allowed to the user.
	 */
	public ConceptsLockedException() {
		this("The concepts are currently locked. Editing of concepts is not allowed at this time.");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * 
	 * @param message the String to show to the user as to why the concepts are locked
	 */
	public ConceptsLockedException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than the normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the String to show to the user as to why the concepts are locked
	 * @param cause the parent exception
	 */
	public ConceptsLockedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with. This does not
	 * set the error message for the user as to why an exception is being thrown. The
	 * {@link #ConceptsLockedException(String, Throwable)} constructor is preferred over this one.
	 * 
	 * @param cause the parent exception
	 */
	public ConceptsLockedException(Throwable cause) {
		super(cause);
	}
}
