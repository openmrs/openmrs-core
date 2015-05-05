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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;

/**
 * Exception thrown when a {@link Patient} or {@link PatientIdentifier} is being saved with an empty
 * or null {@link PatientIdentifier#getIdentifier()}
 * 
 * @see PatientService
 * @see PatientIdentifierException
 */
public class BlankIdentifierException extends PatientIdentifierException {
	
	private static final long serialVersionUID = -3404483383593320184L;
	
	/**
	 * Default empty constructor. If at all possible, don't use this one, but use the
	 * {@link #BlankIdentifierException(String, PatientIdentifier)} constructor to specify a helpful
	 * message to the end user
	 */
	public BlankIdentifierException() {
	}
	
	/**
	 * General constructor to give the end user a helpful message that relates to why this error
	 * occurred. Preference should be given to
	 * {@link #BlankIdentifierException(String, PatientIdentifier)} over this constructor
	 * 
	 * @param message helpful message string for the end user
	 */
	public BlankIdentifierException(String message) {
		super(message);
	}
	
	/**
	 * This is the preferred constructor. This gives the end user both a message as to why this
	 * error occurred and the identifier which is blank.
	 * 
	 * @param message helpful message string for the end user
	 * @param identifier the identifier that is blank
	 */
	public BlankIdentifierException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}
	
	/**
	 * Convenience constructor used to simply wrap around a different error <code>cause</code>
	 * 
	 * @param message helpful message string for the end user
	 * @param cause parent exception cause that this exception is wrapping around
	 */
	public BlankIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor used to only wrap around a parent cause. Preference should be given to the
	 * {@link #BlankIdentifierException(String, Throwable)} constructor before this one.
	 * 
	 * @param cause the parent wrapping cause
	 */
	public BlankIdentifierException(Throwable cause) {
		super(cause);
	}
}
