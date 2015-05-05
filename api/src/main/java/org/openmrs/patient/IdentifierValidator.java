/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
