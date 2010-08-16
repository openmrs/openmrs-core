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
