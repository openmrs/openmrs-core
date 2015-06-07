/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

/**
 * Represents an Exception that has occurred during object Serialization or Deserialization within
 * OpenMRS
 */
public class SerializationException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public SerializationException() {
		super();
	}
	
	/**
	 * Inherited Constructor
	 */
	public SerializationException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Inherited Constructor
	 */
	public SerializationException(String message) {
		super(message);
	}
	
	/**
	 * Inherited Constructor
	 */
	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
