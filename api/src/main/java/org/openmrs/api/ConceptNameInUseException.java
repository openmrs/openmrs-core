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
 * This exception is thrown when one attempts to delete a concept that has a conceptName that is
 * being used by an observation
 * 
 * @see ObsService#getObservationCount(java.util.List, boolean)
 * @see ConceptService#saveConcept(org.openmrs.Concept)
 */
public class ConceptNameInUseException extends APIException {
	
	private static final long serialVersionUID = 1034355111901825174L;
	
	/**
	 * Generic constructor that gives a normal message about editing not being allowed to the user.
	 */
	public ConceptNameInUseException() {
		this("The conceptName cannot be changed if it is already used/associated to an observation");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * 
	 * @param message the String to show to the user as to why the conceptName's name can't be
	 *            changed
	 */
	public ConceptNameInUseException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than the normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the String to show to the user as to why the conceptName's name can't be
	 *            changed
	 * @param cause the parent exception
	 */
	public ConceptNameInUseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with. This does not
	 * set the error message for the user as to why an exception is being thrown. The
	 * {@link #ConceptNameInUseException(String, Throwable)} constructor is preferred over this one.
	 * 
	 * @param cause the parent exception
	 */
	public ConceptNameInUseException(Throwable cause) {
		super(cause);
	}
}
