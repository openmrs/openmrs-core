/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.patient.impl;

import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.UnallowedIdentifierException;

/**
 * An abstract class for identifier validators for identifiers that have a hyphen before a single
 * check digit. Identifiers can not be null, must have at least one character before the check
 * digit, and can not contain white space. Integers 0-9 or characters A-J are allowed for the check
 * digit. A character is used by default.
 */
public abstract class BaseHyphenatedIdentifierValidator implements IdentifierValidator {
	
	protected abstract int getCheckDigit(String undecoratedIdentifier);
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#getAllowedCharacters()
	 */
	@Override
	public abstract String getAllowedCharacters();
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#getName()
	 */
	@Override
	public abstract String getName();
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#getValidIdentifier(java.lang.String)
	 */
	@Override
	public String getValidIdentifier(String undecoratedIdentifier) throws UnallowedIdentifierException {
		
		checkAllowedIdentifier(undecoratedIdentifier);
		
		char checkLetter = convertCheckDigitToChar(getCheckDigit(undecoratedIdentifier));

		return undecoratedIdentifier + "-" + checkLetter;
	}
	
	/**
	 * @see org.openmrs.patient.IdentifierValidator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String identifier) throws UnallowedIdentifierException {
		
		if (identifier.indexOf("-") < 1) {
			throw new UnallowedIdentifierException(
					"Identifier \"" + identifier + "\" must contain a hyphen followed by a check digit character (A-J).");
		}
		if (identifier.endsWith("-")) {
			throw new UnallowedIdentifierException(
					"Identifier \""
							+ identifier
							+ "\" must not end with a hyphen - a check digit character (A-J) must follow.");
		}
		
		String idWithoutCheckDigit = identifier.substring(0, identifier.indexOf("-"));
		
		checkAllowedIdentifier(idWithoutCheckDigit);
		
		int computedCheckDigit = getCheckDigit(idWithoutCheckDigit);
		
		String checkDigit = identifier.substring(identifier.indexOf("-") + 1);
		
		if (checkDigit.length() != 1) {
			throw new UnallowedIdentifierException("Identifier must have a check digit of length 1.");
		}
		
		if ("A".equalsIgnoreCase(checkDigit)) {
			checkDigit = "0";
		}
		if ("B".equalsIgnoreCase(checkDigit)) {
			checkDigit = "1";
		}
		if ("C".equalsIgnoreCase(checkDigit)) {
			checkDigit = "2";
		}
		if ("D".equalsIgnoreCase(checkDigit)) {
			checkDigit = "3";
		}
		if ("E".equalsIgnoreCase(checkDigit)) {
			checkDigit = "4";
		}
		if ("F".equalsIgnoreCase(checkDigit)) {
			checkDigit = "5";
		}
		if ("G".equalsIgnoreCase(checkDigit)) {
			checkDigit = "6";
		}
		if ("H".equalsIgnoreCase(checkDigit)) {
			checkDigit = "7";
		}
		if ("I".equalsIgnoreCase(checkDigit)) {
			checkDigit = "8";
		}
		if ("J".equalsIgnoreCase(checkDigit)) {
			checkDigit = "9";
		}
		
		int givenCheckDigit;
		
		try {
			givenCheckDigit = Integer.valueOf(checkDigit);
		}
		catch (NumberFormatException e) {
			throw new UnallowedIdentifierException(
			        "Check digit must either be a character from A to J or a single digit integer.");
		}
		
		return (computedCheckDigit == givenCheckDigit);
	}
	
	/**
	 * @param undecoratedIdentifier
	 * @throws UnallowedIdentifierException if identifier contains unallowed characters or is
	 *             otherwise invalid.
	 */
	protected void checkAllowedIdentifier(String undecoratedIdentifier) throws UnallowedIdentifierException {
		if (undecoratedIdentifier == null) {
			throw new UnallowedIdentifierException("Identifier can not be null.");
		}
		if (undecoratedIdentifier.length() == 0) {
			throw new UnallowedIdentifierException("Identifier must contain at least one character.");
		}
		if (undecoratedIdentifier.contains(" ")) {
			throw new UnallowedIdentifierException("Identifier may not contain white space.");
		}
		for (int i = 0; i < undecoratedIdentifier.length(); i++) {
			if (getAllowedCharacters().indexOf(undecoratedIdentifier.charAt(i)) == -1) {
				throw new UnallowedIdentifierException("\"" + undecoratedIdentifier.charAt(i)
				        + "\" is an invalid character.");
			}
		}
	}
	
	/**
	 * Not doing this with ASCII math to be extra careful.
	 *
	 * @param checkDigit
	 * @return
	 */
	private char convertCheckDigitToChar(int checkDigit) {
		switch (checkDigit) {
			case 0:
				return 'A';
			case 1:
				return 'B';
			case 2:
				return 'C';
			case 3:
				return 'D';
			case 4:
				return 'E';
			case 5:
				return 'F';
			case 6:
				return 'G';
			case 7:
				return 'H';
			case 8:
				return 'I';
			case 9:
				return 'J';
			default:
				return 'X';
		}
	}
}
