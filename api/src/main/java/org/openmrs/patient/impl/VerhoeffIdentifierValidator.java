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
	@Override
	protected int getCheckDigit(String undecoratedIdentifier) {
		int[] a = getBase(Integer.parseInt(undecoratedIdentifier), undecoratedIdentifier.length());
		insertCheck(a);
		return a[0];
	}
	
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
	 * @should get valid identifier
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
	
	private int[] getBase(int num, int length) {
		int[] a = new int[length + 1];
		int x = 1;
		for (int i = length; i-- > 0;) {
			int y = num / x;
			a[i + 1] = y % 10;
			x = x * 10;
		}
		return a;
	}
	
	private int insertCheck(int[] a) {
		int check = 0;
		for (int i = 1; i < a.length; i++) {
			check = op[check][F[i % 8][a[i]]];
		}
		a[0] = inv[check];
		return a[0];
	}
	
	public VerhoeffIdentifierValidator() {
		F[0] = F0;
		F[1] = F1;
		for (int i = 2; i < 8; i++) {
			F[i] = new int[10];
			for (int j = 0; j < 10; j++) {
				F[i][j] = F[i - 1][F[1][j]];
			}
		}
	}
	
	private int[][] F = new int[8][];
	
	private static final int[] F0 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	
	private static final int[] F1 = { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 };
	
	private static final int[][] op = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 },
	        { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
	        { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
	        { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };
	
	private static final int[] inv = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };
}
