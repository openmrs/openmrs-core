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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.openmrs.patient.UnallowedIdentifierException;

/**
 * Tests the {@link LuhnIdentifierValidator}
 */
public class LuhnIdentifierValidatorTest {
	
	private LuhnIdentifierValidator validator = new LuhnIdentifierValidator();
	
	private String[] allowedIdentifiers = { "a", "123456", "ab32kcdak3", "chaseisreallycoolyay", "1", "moose", "MOOSE",
	        "MooSE", "adD3Eddf429daD999" };
	
	private char[] allowedIdentifiersCheckDigits = { 'D', 'G', 'J', 'H', 'I', 'H', 'H', 'H', 'B' };
	
	private char unusedCheckDigit = 'E';
	
	private int unusedCheckDigitInt = 0;
	
	private int[] allowedIdentifiersCheckDigitsInts = { 3, 6, 9, 7, 8, 7, 7, 7, 1 };
	
	private String[] invalidIdentifiers = { "", " ", "-", "adsfalasdf-adfasdf", "ABC DEF", "!234*", "++", " ABC", "def " };
	
	/**
	 * @see LuhnIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldGetValidIdentifier() {
		
		//Make sure valid identifiers come back with the right check digit
		
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertEquals(validator.getValidIdentifier(allowedIdentifiers[i]), allowedIdentifiers[i] + "-"
			        + allowedIdentifiersCheckDigitsInts[i]);
		}
	}
	
	/**
	 * @see LuhnIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldFailWithInvalidIdentifiers() {
		//Make sure invalid identifiers throw an exception

		for (String invalidIdentifier : invalidIdentifiers) {
			assertThrows(UnallowedIdentifierException.class, () -> validator.getValidIdentifier(invalidIdentifier));
		}
	}
	
	/**
	 * Test the isValid method. TODO split this into multiple tests
	 */
	@Test
	public void shouldValidate() {
		//Make sure invalid identifiers throw an exception

		for (String invalidIdentifier1 : invalidIdentifiers) {
			assertThrows(UnallowedIdentifierException.class, () -> validator.isValid(invalidIdentifier1));
		}

		for (String invalidIdentifier : invalidIdentifiers) {
			assertThrows(UnallowedIdentifierException.class, () -> validator.isValid(invalidIdentifier + "-H"));
		}

		for (String allowedIdentifier1 : allowedIdentifiers) {
			assertThrows(UnallowedIdentifierException.class, () -> validator.isValid(allowedIdentifier1 + "-X"));
			assertThrows(UnallowedIdentifierException.class, () -> validator.isValid(allowedIdentifier1 + "-10"));
		}
		
		//Now test allowed identifiers that just have the wrong check digit.
		for (String allowedIdentifier : allowedIdentifiers) {
			assertFalse(validator.isValid(allowedIdentifier + "-" + unusedCheckDigit));
			assertFalse(validator.isValid(allowedIdentifier + "-" + unusedCheckDigitInt));
		}
		
		//Now test allowed identifiers that have the right check digit.  Test with both
		//chars and ints.
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertTrue(validator.isValid(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigits[i]));
			assertTrue(validator.isValid(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigitsInts[i]));
		}
	}
	
}
