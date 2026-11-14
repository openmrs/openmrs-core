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

import org.junit.jupiter.api.Test;
import org.openmrs.patient.UnallowedIdentifierException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the shared framework logic in {@link BaseHyphenatedIdentifierValidator}.
 * <p>
 * The base class is abstract and is normally only exercised indirectly through its concrete
 * subclasses ({@link LuhnIdentifierValidator} and {@link VerhoeffIdentifierValidator}), where its
 * behaviour is entangled with each subclass's own check-digit algorithm. These tests instead use a
 * tiny stub subclass whose check digit is supplied by the caller, so the base-class contract - the
 * hyphen/check-digit parsing, the {@code 0-9 <-> A-J} mapping, and the allowed-character rules -
 * can be verified in isolation, independent of any real check-digit math.
 */
public class BaseHyphenatedIdentifierValidatorTest {

	/**
	 * Minimal concrete implementation whose check digit is fixed by the caller. This lets the tests
	 * drive the base-class logic deterministically without relying on a real check-digit algorithm.
	 */
	private static class FixedCheckDigitValidator extends BaseHyphenatedIdentifierValidator {

		private final int checkDigit;

		FixedCheckDigitValidator(int checkDigit) {
			this.checkDigit = checkDigit;
		}

		@Override
		protected int getCheckDigit(String undecoratedIdentifier) {
			return checkDigit;
		}

		@Override
		public String getAllowedCharacters() {
			return "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		}

		@Override
		public String getName() {
			return "Fixed Check Digit Validator";
		}
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldAppendHyphenAndMapDigitsZeroThroughNineToLettersAToJ() {
		String[] expectedCheckLetters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
		for (int digit = 0; digit <= 9; digit++) {
			BaseHyphenatedIdentifierValidator validator = new FixedCheckDigitValidator(digit);
			assertEquals("identifier-" + expectedCheckLetters[digit], validator.getValidIdentifier("identifier"));
		}
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldUseXAsCheckCharacterWhenCheckDigitIsOutOfRange() {
		assertEquals("identifier-X", new FixedCheckDigitValidator(10).getValidIdentifier("identifier"));
		assertEquals("identifier-X", new FixedCheckDigitValidator(-1).getValidIdentifier("identifier"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldReturnTrueWhenCheckDigitMatchesAsBothLetterAndInteger() {
		BaseHyphenatedIdentifierValidator validator = new FixedCheckDigitValidator(3);
		assertTrue(validator.isValid("identifier-D"));
		assertTrue(validator.isValid("identifier-3"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldReturnFalseWhenCheckDigitDoesNotMatch() {
		BaseHyphenatedIdentifierValidator validator = new FixedCheckDigitValidator(3);
		assertFalse(validator.isValid("identifier-A"));
		assertFalse(validator.isValid("identifier-5"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldAcceptCheckLetterCaseInsensitively() {
		assertTrue(new FixedCheckDigitValidator(0).isValid("identifier-a"));
		assertTrue(new FixedCheckDigitValidator(7).isValid("identifier-h"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldThrowWhenIdentifierHasNoHyphen() {
		assertThrows(UnallowedIdentifierException.class, () -> new FixedCheckDigitValidator(0).isValid("identifier"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldThrowWhenIdentifierEndsWithHyphen() {
		assertThrows(UnallowedIdentifierException.class, () -> new FixedCheckDigitValidator(0).isValid("identifier-"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldThrowWhenCheckDigitIsNotASingleCharacter() {
		assertThrows(UnallowedIdentifierException.class, () -> new FixedCheckDigitValidator(0).isValid("identifier-10"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#isValid(String)
	 */
	@Test
	public void isValid_shouldThrowWhenCheckDigitIsNeitherAToJNorADigit() {
		assertThrows(UnallowedIdentifierException.class, () -> new FixedCheckDigitValidator(0).isValid("identifier-K"));
	}

	/**
	 * @see BaseHyphenatedIdentifierValidator#getValidIdentifier(String)
	 */
	@Test
	public void getValidIdentifier_shouldThrowForNullEmptyWhitespaceOrDisallowedCharacters() {
		BaseHyphenatedIdentifierValidator validator = new FixedCheckDigitValidator(0);
		assertThrows(UnallowedIdentifierException.class, () -> validator.getValidIdentifier(null));
		assertThrows(UnallowedIdentifierException.class, () -> validator.getValidIdentifier(""));
		assertThrows(UnallowedIdentifierException.class, () -> validator.getValidIdentifier("ab cd"));
		assertThrows(UnallowedIdentifierException.class, () -> validator.getValidIdentifier("ab!cd"));
	}
}
