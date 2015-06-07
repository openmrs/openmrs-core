/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

/**
 * Represents often fatal errors that occur within the module package
 * 
 * @version 1.0
 */
public class ModuleException extends RuntimeException {
	
	public static final long serialVersionUID = 236472665L;
	
	public ModuleException(String message) {
		super(message);
	}
	
	public ModuleException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ModuleException(String message, String moduleName) {
		super(message + " Module: " + moduleName);
	}
	
	public ModuleException(String message, String moduleName, Throwable cause) {
		super(message + " Module: " + moduleName, cause);
	}
	
}
