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
 * This exception is thrown whenever a concept stop word service failed.
 *
 * @since 1.8
 */

public class ConceptStopWordException extends APIException {
	
	private static final long serialVersionUID = 133352366232223L;
	
	/**
	 * Constructor to give the user a message
	 *
	 * @param message the String to show to the user as to why the concept stop word service failed
	 */
	public ConceptStopWordException(String message) {
		super(message);
	}
	
	/**
	 * Convenience constructor to give the user a message and to chain
	 * this exception with a parent exception.
	 *
	 * @param message the String to show to the user as to why the concept stop word service failed
	 * @param cause the parent exception
	 */
	public ConceptStopWordException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
