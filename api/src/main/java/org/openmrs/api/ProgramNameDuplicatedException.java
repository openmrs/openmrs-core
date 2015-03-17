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
