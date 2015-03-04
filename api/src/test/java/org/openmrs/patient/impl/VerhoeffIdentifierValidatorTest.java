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
import org.openmrs.test.Verifies;

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
	
	/**
	 * @see {@link VerhoeffIdentifierValidator#getValidIdentifier(String)}
	 */
	@Test
	@Verifies(value = "should get valid identifier", method = "getValidIdentifier(String)")
	public void getValidIdentifier_shouldGetValidIdentifier() throws Exception {
		
		//Make sure valid identifiers come back with the right check digit
		
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertEquals(validator.getValidIdentifier(allowedIdentifiers[i]), allowedIdentifiers[i] + "-"
			        + allowedIdentifiersCheckDigits[i]);
		}
		
		//Make sure invalid identifiers throw an exception
		
		for (int j = 0; j < invalidIdentifiers.length; j++) {
			try {
				validator.getValidIdentifier(invalidIdentifiers[j]);
				fail("Identifier " + invalidIdentifiers[j] + " should have failed.");
			}
			catch (Exception e) {}
		}
	}
	
	/**
	 * Test the is valid method. TODO split this into multiple tests.
	 */
	@Test
	public void shouldIsValid() {
		//Make sure invalid identifiers throw an exception
		
		for (int j = 0; j < invalidIdentifiers.length; j++) {
			try {
				validator.isValid(invalidIdentifiers[j]);
				fail("Identifier " + invalidIdentifiers[j] + " should have failed.");
			}
			catch (Exception e) {}
		}
		
		for (int j = 0; j < invalidIdentifiers.length; j++) {
			try {
				validator.isValid(invalidIdentifiers[j] + "-H");
				fail("Identifier " + invalidIdentifiers[j] + " should have failed.");
			}
			catch (Exception e) {}
		}
		
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			try {
				validator.isValid(allowedIdentifiers[i] + "-X");
				fail("Identifier " + allowedIdentifiers[i] + " should have failed.");
			}
			catch (Exception e) {}
			try {
				validator.isValid(allowedIdentifiers[i] + "-10");
				fail("Identifier " + allowedIdentifiers[i] + " should have failed.");
			}
			catch (Exception e) {}
		}
		
		//Make sure check digit can't be numeric
		for (int j = 0; j < invalidIdentifiers.length; j++) {
			try {
				validator.isValid(allowedIdentifiers[j] + "-" + allowedIdentifiersCheckDigitsInt[j]);
				fail("Identifier " + allowedIdentifiers[j] + " should have failed.");
			}
			catch (Exception e) {}
		}
		
		//Now test allowed identifiers that just have the wrong check digit.
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertFalse(validator.isValid(allowedIdentifiers[i] + "-" + unusedCheckDigit));
		}
		
		//Now test allowed identifiers that have the right check digit.  Test with both
		//chars and ints.
		for (int i = 0; i < allowedIdentifiers.length; i++) {
			assertTrue(validator.isValid(allowedIdentifiers[i] + "-" + allowedIdentifiersCheckDigits[i]));
		}
		
	}
	
}
