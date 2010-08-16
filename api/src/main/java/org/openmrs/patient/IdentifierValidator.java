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
package org.openmrs.patient;

/**
 * Interface for patient identifier validators. Patient identifier validators check whether a
 * patient identifier is valid. They also can be used to create a valid identifier from an
 * identifier that has not yet been given a check digit or other form of validation. Each validator
 * should also have a name, e.g. "Luhn Algorithm."
 */
public interface IdentifierValidator {
	
	/**
	 * @return The name of this validator. E.g. "Luhn Algorithm"
	 */
	public String getName();
	
	/**
	 * @param identifier The Identifier to check.
	 * @return Whether this identifier is valid according to the validator.
	 * @throws UnallowedIdentifierException if the identifier contains unallowed characters or is
	 *             otherwise not appropriate for this validator.
	 */
	public boolean isValid(String identifier) throws UnallowedIdentifierException;
	
	/**
	 * @param undecoratedIdentifier The identifier prior to being given a check digit or other form
	 *            of validation.
	 * @return The identifier after the check digit or other form of validation has been applied.
	 * @throws UnallowedIdentifierException if the identifier contains unallowed characters or is
	 *             otherwise not appropriate for this validator.
	 */
	public String getValidIdentifier(String undecoratedIdentifier) throws UnallowedIdentifierException;
	
	/**
	 * @return A string containing all the characters allowed in this type of identifier validation.
	 */
	public String getAllowedCharacters();
}
