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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 *
 */
public class VerhoeffIdentifierValidatorTest {
	
	private VerhoeffIdentifierValidator validator = new VerhoeffIdentifierValidator();
	
	private String[] allowedIdentifiers = { "12345678", "87654321", "11111111", "64537218", "00000000" };
	
	private char[] allowedIdentifiersCheckDigits = { 'G', 'E', 'B', 'A', 'B' };
	
	private int[] allowedIdentifiersCheckDigitsInt = { 6, 4, 1, 0, 2 };
	
	private char unusedCheckDigit = 'C';
	
	private String[] invalidIdentifiers = { "", " ", "-", "adsfalasdf-adfasdf", "ABC DEF", "!234*", "++", " ABC", "def ",
	        "ab32kcdak3", "chaseisreallycoolyay", "1", "moose", "MOOSE", "MooSE", "adD3Eddf429daD999" };

	private static final int EXPECTED_RAW_IDENTIFIER_LENGTH = 8;

	/**
	 * @see VerhoeffIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldGetValidIdentifier() {

		//Make sure valid identifiers come back with the right check digit

		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertEquals(validator.getValidIdentifier(allowedIdentifiers[i]), allowedIdentifiers[i] + "-"
					+ allowedIdentifiersCheckDigits[i]);
		}
	}

	@Test
	public void getValidIdentifier_shouldFailWithInvalidIdentifiers() {
		//Make sure invalid identifiers throw an exception
		for (String invalidIdentifier: invalidIdentifiers) {
			try {
				validator.getValidIdentifier(invalidIdentifier);
				fail("Identifier " + invalidIdentifier + " should have failed.");
			} catch (Exception e) {
				if (invalidIdentifier.length() == 0){
					assertTrue(e.getMessage().matches("Identifier must contain at least one character\\."));
				} else if (invalidIdentifier.indexOf(' ') > -1) {
					assertTrue(e.getMessage().matches("Identifier may not contain white space\\."));
				} else if (invalidIdentifier.matches(".*[^\\d].*")) {
					assertTrue(e.getMessage().indexOf("is an invalid character") > 0);
				} else if (invalidIdentifier.length() != EXPECTED_RAW_IDENTIFIER_LENGTH) {
					assertTrue(e.getMessage().matches("Undecorated identifier must be 8 digits long\\."));
				} else {
					fail("Unexpected message '" + e.getMessage() + "' seen for invalid identifier '" + invalidIdentifier + "'");
				}
			}
		}
	}

	@Test
	public void isValid_shouldFailWithInvalidIdentifiers() {
		//Make sure invalid identifiers throw an exception
		for (String invalidIdentifier: invalidIdentifiers) {
			try {
				validator.isValid(invalidIdentifier);
				fail("Identifier " + invalidIdentifier + " should have failed.");
			} catch (Exception e) { /* Expected */ }
		}
	}

	@Test
	public void isValid_shouldFailWithInvalidSuffixes() {
		for (String allowedIdentifier : allowedIdentifiers) {
			try {
				validator.isValid(allowedIdentifier + "-X");
				fail("Identifier " + allowedIdentifier + " should have failed.");
			} catch (Exception e) { /* Expected */ }
			try {
				validator.isValid(allowedIdentifier + "-10");
				fail("Identifier " + allowedIdentifier + " should have failed.");
			} catch (Exception e) { /* Expected */ }
		}
	}

	@Test
	public void isValid_shouldPassWithValidSuffixes() {
		// Test allowed identifiers that have the right check digit.
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertTrue(validator.isValid(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigits[i]));
		}
	}

	@Test
	public void isValid_shouldFailWithIncorrectlyCalculatedSuffixes() {
		// Test allowed identifiers that just have the wrong check digit.
		for (String allowedIdentifier : allowedIdentifiers) {
			assertFalse(validator.isValid(allowedIdentifier + "-" + unusedCheckDigit));
		}
	}

	@Test
	public void isValid_shouldFailWithNumericSuffixes() {
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			try {
				validator.isValid(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigitsInt[i]);
				fail("Identifier " + allowedIdentifiers[i] + " should have failed.");
			}
			catch (Exception e) { /* Expected */ }
		}
	}

	@Test
	public void checkDigit_shouldChangeWhenAdjacentCharsAreTransposed() {
		VerhoeffIdentifierValidator validator = new VerhoeffIdentifierValidator();
		String test;
		int checkDigit;
		String pre;
		String post;
		int failures = 0;
		StringBuilder failureMsg = new StringBuilder();
		char c;
		char d;
		final String lineSeparator = System.getProperty("line.separator");
		for (String allowedIdentifier: allowedIdentifiers) {
			for (int i = 0; i < allowedIdentifier.length(); i++) {
				checkDigit = validator.getCheckDigit(allowedIdentifier);
				for (int j = 1; j < allowedIdentifier.length(); j++) {
					c = allowedIdentifier.charAt(j - 1);
					d = allowedIdentifier.charAt(j);
					// If 2 characters are not identical, shuffle them and then test that check digit is different.
					if (c != d) {
						pre = allowedIdentifier.substring(0, j - 1);
						post = allowedIdentifier.substring(j + 1);
						test = pre + d + c + post;
						if (checkDigit == validator.getCheckDigit(test)) {
							failureMsg.append("Check digits for '");
							failureMsg.append(allowedIdentifier);
							failureMsg.append("' and '");
							failureMsg.append(test);
							failureMsg.append("' should be different, both were ");
							failureMsg.append(checkDigit);
							failureMsg.append(lineSeparator);
							failures++;
						}
					}
				}
				// Shuffle the allowed identifier string around so that first character becomes last.
				allowedIdentifier = allowedIdentifier.substring(1, allowedIdentifier.length()) + allowedIdentifier.charAt(0);
			}
		}
		assertTrue(failures + " transposed digits were not detected:\n" + failureMsg, failures == 0);
	}

}
