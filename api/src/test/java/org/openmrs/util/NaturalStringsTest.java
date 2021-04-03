/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmrs.util.NaturalStrings.compareNatural;
import static org.openmrs.util.NaturalStrings.compareNaturalAscii;
import static org.openmrs.util.NaturalStrings.compareNaturalIgnoreCaseAscii;

import org.junit.jupiter.api.Test;

public class NaturalStringsTest {

	@Test
	public void compareNaturalAscii_twoEmptyStrings_shouldBeEqual() {
		assertThat(compareNaturalAscii("", ""), is(0));
	}

	@Test
	public void compareNaturalAscii_emptyStringComparedToNonEmptyString_shouldBeSmaller() {
		assertThat(compareNaturalAscii("", "a"), is(-1));
	}

	@Test
	public void compareNaturalAscii_nonEmptyStringComparedToEmptyString_shouldBeBigger() {
		assertThat(compareNaturalAscii("a", ""), is(1));
	}

	@Test
	public void compareNaturalAscii_twoSameOneLetterStrings_shouldBeEqual() {
		assertThat(compareNaturalAscii("a", "a"), is(0));
	}

	@Test
	public void compareNaturalAscii_twoSameTwoLettersStrings_shouldBeEqual() {
		assertThat(compareNaturalAscii("ab", "ab"), is(0));
	}

	@Test
	public void compareNaturalAscii_twoLetterStringComparedToItsOneLetterSubstring_shouldBeBigger() {
		assertThat(compareNaturalAscii("ab", "a"), is(1));
	}

	@Test
	public void compareNaturalAscii_oneLetterStringComparedToSameStringWithMoreLettersAtTheEnd_shouldBeSmaller() {
		assertThat(compareNaturalAscii("a", "ab"), is(-1));
	}

	@Test
	public void compareNaturalAscii_oneLetterStringComparedToHigherOneLetterString_shouldBeSmaller() {
		assertThat(compareNaturalAscii("a", "f"), is(-5)); // 'a' - 'f'
	}

	@Test
	public void compareNaturalAscii_higherOneLetterStringComparedToSmallerOneLetterString_shouldBeBigger() {
		assertThat(compareNaturalAscii("f", "a"), is(5)); // 'f' - 'a'
	}

	@Test
	public void compareNaturalAscii_bothStringsAreSingleZeroDigit_shouldBeEqual() {
		assertThat(compareNaturalAscii("0", "0"), is(0));
	}

	@Test
	public void compareNaturalAscii_bothStringsAreTwoZeros_shouldBeEqual() {
		assertThat(compareNaturalAscii("00", "00"), is(0));
	}

	@Test
	public void compareNaturalAscii_stringOfOnlyZerosComparedToNonEmptyDigitString_shouldBeSmaller() {
		assertThat(compareNaturalAscii("00000", "1"), is(-1));
	}

	@Test
	public void compareNaturalAscii_nonEmptyDigitStringComparedToStringWithOnlyZeros_shouldBeBigger() {
		assertThat(compareNaturalAscii("1", "00000"), is(1));
	}

	@Test
	public void compareNaturalAscii_stringOfOnlyZerosComparedToNonEmptyLetterString_shouldBeSmaller() {
		assertThat(compareNaturalAscii("00000", "a"), is('0' - 'a')); // -49
	}

	@Test
	public void compareNaturalAscii_stringWithDigitOneComparedToStringWithDigitTwo_shouldBeSmaller() {
		assertThat(compareNaturalAscii("1", "2"), is(-1));
	}

	@Test
	public void compareNaturalAscii_stringWithDigitsZeroOneComparedToStringWithDigitsZeroTwo_shouldBeSmaller() {
		assertThat(compareNaturalAscii("01", "02"), is(-1));
	}

	@Test
	public void compareNaturalAscii_stringWithDigitsZeroTwoComparedToStringWithDigitsZeroOne_shouldBeBigger() {
		assertThat(compareNaturalAscii("02", "01"), is(1));
	}

	@Test
	public void compareNaturalAscii_sameDigitPrefixedWithMoreZerosComparedToSameDigitPrefixedWithLessZeros_shouldBeBigger() {
		assertThat(compareNaturalAscii("0001", "01"), is(2)); // number of additional zeros
	}

	@Test
	public void compareNaturalAscii_sameDigitPrefixedWithLessZerosComparedToSameDigitPrefixedWithMoreZeros_shouldBeSmaller() {
		assertThat(compareNaturalAscii("01", "00001"), is(-3)); // number of additional zeros
	}

	@Test
	public void compareNaturalAscii_firstStringIsPrefixOfSecondString_shouldBeSmaller() {
		assertThat(compareNaturalAscii("1", "12"), is(-1));
	}

	@Test
	public void compareNaturalAscii_firstStringIsSameAsSecondStringButHasMoreDigitsAtTheEnd_shouldBeBigger() {
		assertThat(compareNaturalAscii("12", "1"), is(1));
	}

	@Test
	public void compareNaturalAscii_firstStringHasBiggerDigitInTheMiddle_shouldBeBigger() {
		assertThat(compareNaturalAscii("133", "113"), is(2)); // 3-1
	}

	@Test
	public void compareNaturalAscii_firstStringHasLowerDigitInTheMiddle_shouldBeSmaller() {
		assertThat(compareNaturalAscii("123", "183"), is(-6)); // 2-8
	}

	@Test
	public void compareNaturalAscii_firstStringHasLowerDigitInTheMiddleAndSecondStringHasAdditionalDigitAtTheEnd_shouldBeSmaller() {
		assertThat(compareNaturalAscii("123", "1834"), is(-1));
	}

	@Test
	public void compareNaturalAscii_firstStringHasHigherDigitInTheMiddleAndHasMoreDigitsAtTheEndThanOtherString_shouldBeBigger() {
		assertThat(compareNaturalAscii("1834", "123"), is(1));
	}

	@Test
	public void compareNaturalAscii_firstStringHasHigherDigitInTheMiddleAndEndsWithAdditionalLetter_shouldBeBigger() {
		assertThat(compareNaturalAscii("183a", "123"), is(6)); // 8-2
	}

	@Test
	public void compareNaturalAscii_sameDigitStringsHaveSameLetterAtTheSamePlaceInTheMiddle_shouldBeEqual() {
		assertThat(compareNaturalAscii("1a3", "1a3"), is(0));
	}

	@Test
	public void compareNaturalAscii_sameDigitStringsButFirstHasSmallerLetterAtTheSamePlaceInTheMiddle_shouldBeSmaller() {
		assertThat(compareNaturalAscii("1a3", "1c3"), is(-2)); // 'a'-'c'
	}

	@Test
	public void compareNaturalAscii_sameDigitStringsButFirstHasHigherLetterAtTheSamePlaceInTheMiddle_shouldBeBigger() {
		assertThat(compareNaturalAscii("1c3", "1a3"), is(2)); // 'c'-'a'
	}

	@Test
	public void compareNaturalAscii_firstDigitStringHasSmallerNumberBeforeSameLetterInBothStrings_shouldBeSmaller() {
		assertThat(compareNaturalAscii("1a3", "3a3"), is(-2)); // 1-3
	}

	@Test
	public void compareNaturalAscii_firstDigitStringHasBiggerNumberBeforeSameLetterInBothStrings_shouldBeBigger() {
		assertThat(compareNaturalAscii("8a3", "3a3"), is(5)); // 8-3
	}

	@Test
	public void compareNaturalAscii_firstDigitStringHasDigitAtTheSamePositionThatOtherStringHasLetter_shouldBeBigger() {
		assertThat(compareNaturalAscii("11", "1d"), is(1));
	}

	@Test
	public void compareNaturalAscii_firstDigitStringHasLetterAtTheSamePositionThatOtherStringHasDigit_shouldBeSmaller() {
		assertThat(compareNaturalAscii("1z", "19"), is(-1));
	}

	@Test
	public void compareNaturalIgnoreCaseAscii_characterStringsWithDifferentCapitalization_shouldBeEqual() {
		assertThat(compareNaturalIgnoreCaseAscii("aA", "aa"), is(0));
	}

	@Test
	public void compareNaturalIgnoreCaseAscii_characterStringsWithLowerCapitalizedLetters_shouldBeSmaller() {
		assertThat(compareNaturalIgnoreCaseAscii("aA", "aZ"), is(-25)); // 'A' - 'Z'
	}

	@Test
	public void compareNaturalIgnoreCaseAscii_characterStringsWithLowerLetter_shouldBeSmaller() {
		assertThat(compareNaturalIgnoreCaseAscii("aa", "az"), is(-25)); // 'a' - 'z'
	}

	@Test
	public void compareNatural_firstStringHasLowerLetterComparedToSecondString_shouldBeSmaller() {
		assertThat(compareNatural("aa", "az"), is(-1));
	}

	@Test
	public void compareNatural_sameStringsWithOnlyLetters_shouldBeEqual() {
		assertThat(compareNatural("aa", "aa"), is(0));
	}

	@Test
	public void compareNatural_firstStringHasLowerLetterThanSecondStringAtTheSamePositionAfterDigit_shouldBeSmaller() {
		assertThat(compareNatural("a1a", "a1c"), is(-1));
	}

	@Test
	public void compareNatural_stringWithLettersComparedToStringStartingWithLetterAndHavingDigit_shouldBeBigger() {
		// When comparing using Collator, first only 'all-letters' part of the strings will be compared.
		// If, as in this case, comparing "aa" and "a" returns non-zero result, then remaining digits (and characters are ignored)
		assertThat(compareNatural("aa", "a5"), is(1));
	}
}
