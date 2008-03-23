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
package org.openmrs.module;

/**
 * Represents often fatal errors that occur within the module package
 * \r\n * @version 1.0
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
		super (message + " Module: " + moduleName);
	}
	
	public ModuleException(String message, String moduleName, Throwable cause) {
		super (message + " Module: " + moduleName, cause);
	}

}
