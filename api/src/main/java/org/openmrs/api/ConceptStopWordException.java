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
