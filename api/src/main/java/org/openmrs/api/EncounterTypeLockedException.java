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
 * This exception is thrown when a encounter types are locked and the user tries to edit an encounter type
 * type, this is done by a global property being true/false.
 * 
 * @since 1.10 added to 1.8.5 and 1.9.4
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_ENCOUNTER_TYPES_LOCKED
 * @see EncounterService#checkIfEncounterTypesAreLocked()
 */
public class EncounterTypeLockedException extends APIException {
	
	private static final long serialVersionUID = 1223334444L;
	
	/**
	 * Generic constructor that gives a normal message about editing not being allowed to the user.
	 */
	public EncounterTypeLockedException() {
		this("Editing of encounter types is not allowed at this time since they are currently locked. ");
	}
	
	/**
	 * Convenience constructor to give the user a message other than normal default one
	 * 
	 * @param message the String to show to the user as to why the encounter types are locked
	 */
	public EncounterTypeLockedException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message other than the normal one and to chain
	 * this exception with a parent exception.
	 * 
	 * @param message the String to show to the user as to why the encounter types are locked
	 * @param cause the parent exception
	 */
	public EncounterTypeLockedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Convenience constructor used to only set the parent exception to chain with. This does not
	 * set the error message for the user as to why an exception is being thrown. The
	 * {@link #EncounterTypeLockedException(String, Throwable)} constructor is preferred over this
	 * one.
	 * 
	 * @param cause the parent exception
	 */
	public EncounterTypeLockedException(Throwable cause) {
		super(cause);
	}
}
