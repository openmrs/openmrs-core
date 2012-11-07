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
 * This exception is thrown when one attempts to retrieve a program by name while there accidentally
 * are more than one programs with the same name in the dB.
 * 
 * @see org.openmrs.api.ProgramWorkflowService#getProgramByName(java.lang.String)
 * @since 1.10
 */
public class ProgramNameDuplicatedException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor that takes the duplicated program name.
	 * 
	 * @param programName the name of the program
	 */
	public ProgramNameDuplicatedException(String programName) {
		super("Several programs exist in the database with the name " + programName);
	}
}
