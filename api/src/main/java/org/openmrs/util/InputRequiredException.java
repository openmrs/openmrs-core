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

import java.util.Map;

/**
 * Used by the {@link DatabaseUpdater} to signal that a given update requires input from the user.
 * 
 * @since 1.5
 */
public class InputRequiredException extends Exception {
	
	public static final long serialVersionUID = 121994323413L;
	
	/**
	 * Required input will be in one of these forms
	 */
	public enum DATATYPE {
		STRING,
		INTEGER,
		DOUBLE,
		DATE
	}
	
	/**
	 * A mapping from user prompt to answer datatype
	 */
	private Map<String, DATATYPE> requiredInput;
	
	/**
	 * Common constructor taking in a message to give the user some context as to where/why the
	 * authentication failed.
	 * 
	 * @param requiredInput a list of questions that need to be answered in the form question, type
	 */
	public InputRequiredException(Map<String, DATATYPE> requiredInput) {
		super("Input is required before being able to update the database");
		
		this.requiredInput = requiredInput;
	}
	
	/**
	 * The user prompts and datatype for each question that the user has to provide input for
	 * 
	 * @return the requiredInput
	 */
	public Map<String, DATATYPE> getRequiredInput() {
		return requiredInput;
	}
	
}
