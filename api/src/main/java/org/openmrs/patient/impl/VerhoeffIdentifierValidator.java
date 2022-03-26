/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.patient.impl;

import org.openmrs.patient.UnallowedIdentifierException;

/**
 * The Verhoeff Check Digit Validator catches all single errors and all adjacent transpositions.
 * See: http://www.cs.utsa.edu/~wagner/laws/verhoeff.html and Wagner, Neal.
 * "Verhoeff's Decimal Error Detection". The Laws of Cryptography with Java Code. p 54. San Antonio,
 * TX: 2003. http://www.cs.utsa.edu/~wagner/lawsbookcolor/laws.pdf
 */
public class VerhoeffIdentifierValidator extends BaseHyphenatedIdentifierValidator {

	private static final String ALLOWED_CHARS = "0123456789";
	private static final String VERHOEFF_NAME = "Verhoeff Check Digit Validator.";
	private static final int VERHOEFF_ID_LENGTH = 10;
	private static final int VERHOEFF_UNDECORATED_ID_LENGTH = VERHOEFF_ID_LENGTH - 2;
	getCheckDigitVerhoeffIdentifier obj = new getCheckDigitVerhoeffIdentifier();

	@Override
	protected int getCheckDigit(String undecoratedIdentifier) {
		return obj.getCheckDigit(undecoratedIdentifier);

	}

	/**
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getAllowedCharacters()
	 */
	@Override
	public String getAllowedCharacters() {
		return ALLOWED_CHARS;
	}

	/**
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getName()
	 */
	@Override
	public String getName() {
		return VERHOEFF_NAME;
	}

	/**
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getCheckDigit(java.lang.String)
	 */


	/**
	 * Override to disallow numeric check digits and identifiers that are not exactly
	 * VERHOEFF_ID_LENGTH long.
	 *
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String identifier) throws UnallowedIdentifierException {

		boolean result = super.isValid(identifier);

		if (Character.isDigit(identifier.charAt(identifier.length() - 1))) {
			throw new UnallowedIdentifierException("Check digit can not be numeric.");
		}
		if (identifier.length() != VERHOEFF_ID_LENGTH) {
			throw new UnallowedIdentifierException("Identifier must be " + VERHOEFF_ID_LENGTH + " digits long.");
		}

		return result;
	}

	/**
	 * Override to disallow identifiers that are not exactly VERHOEFF_UNDECORATED_ID_LENGTH long.
	 *
	 * @see org.openmrs.patient.impl.BaseHyphenatedIdentifierValidator#getValidIdentifier(java.lang.String)
	 * <strong>Should</strong> get valid identifier
	 */
	@Override
	public String getValidIdentifier(String undecoratedIdentifier) throws UnallowedIdentifierException {

		String result = super.getValidIdentifier(undecoratedIdentifier);

		if (undecoratedIdentifier.length() != VERHOEFF_UNDECORATED_ID_LENGTH) {
			throw new UnallowedIdentifierException("Undecorated identifier must be " + VERHOEFF_UNDECORATED_ID_LENGTH
				+ " digits long.");
		}

		return result;
	}


}
	
