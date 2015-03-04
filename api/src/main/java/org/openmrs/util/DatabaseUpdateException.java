/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

/**
 * Used by the {@link DatabaseUpdater} to show that an error occurred while updating to the latest
 * database setup.
 * 
 * @since 1.5
 */
public class DatabaseUpdateException extends Exception {
	
	public static final long serialVersionUID = 23413L;
	
	/**
	 * Generic constructor
	 */
	public DatabaseUpdateException() {
		super();
	}
	
	/**
	 * Generic exception class constructor
	 * 
	 * @param message the string message to pass on
	 * @param cause the error that occurred
	 */
	public DatabaseUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Generic exception class constructor
	 * 
	 * @param message the string message to pass on to the user
	 */
	public DatabaseUpdateException(String message) {
		super(message);
	}
	
	/**
	 * Generic exception class constructor
	 * 
	 * @param cause the error that occurred
	 */
	public DatabaseUpdateException(Throwable cause) {
		super(cause);
	}
	
}
