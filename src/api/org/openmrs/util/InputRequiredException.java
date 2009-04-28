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
		STRING, INTEGER, DOUBLE, DATE
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
