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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OpenmrsDateFormat}.
 * <p>
 * {@code OpenmrsDateFormat} is a strict-mode wrapper around {@link SimpleDateFormat} whose only job
 * is to defend against two specific real-world parsing mistakes that the JDK default allows: silent
 * two-digit-year coercion (e.g. "25" turning into 1925 or 2025 unpredictably) and silent
 * date-rollover under lenient parsing (e.g. day 45 of month 13 turning into a date in the following
 * year). These tests pin down both guarantees plus the supporting length validation.
 */
public class OpenmrsDateFormatTest {

	private static OpenmrsDateFormat formatFor(String pattern) {
		return new OpenmrsDateFormat(new SimpleDateFormat(pattern, Locale.US), Locale.US);
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldAcceptFourDigitYearWhenPatternEndsInYyyy() throws ParseException {
		Date parsed = formatFor("MM/dd/yyyy").parse("12/25/2025");

		Calendar cal = new GregorianCalendar(Locale.US);
		cal.setTime(parsed);
		assertEquals(2025, cal.get(Calendar.YEAR));
		assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
		assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldRejectTwoDigitYearWhenPatternEndsInYyyy() {
		// This is the central correctness guarantee of OpenmrsDateFormat: under plain
		// SimpleDateFormat, "12/25/25" with pattern "MM/dd/yyyy" would silently coerce to the year
		// 25 AD, producing wildly wrong clinical timestamps. The wrapper must reject it outright.
		ParseException ex = assertThrows(ParseException.class, () -> formatFor("MM/dd/yyyy").parse("12/25/25"));

		assertTrue(ex.getMessage().contains("year must have 4 digits"),
		    "Expected message to call out the 4-digit-year requirement, got: " + ex.getMessage());
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldAcceptTextEqualLengthToPatternWhenPatternDoesNotEndInYyyy() throws ParseException {
		// Pattern starts with yyyy but does not end with it, so the length-check branch runs.
		Date parsed = formatFor("yyyy-MM-dd").parse("2025-12-25");

		Calendar cal = new GregorianCalendar(Locale.US);
		cal.setTime(parsed);
		assertEquals(2025, cal.get(Calendar.YEAR));
		assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
		assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldRejectTextShorterThanPatternWhenPatternDoesNotEndInYyyy() {
		// "2025-12-5" is one character shorter than "yyyy-MM-dd"; default SimpleDateFormat would
		// happily parse it as Dec 5, but the wrapper enforces fixed-width inputs.
		ParseException ex = assertThrows(ParseException.class, () -> formatFor("yyyy-MM-dd").parse("2025-12-5"));

		assertTrue(ex.getMessage().contains("length of date string doesn't match"),
		    "Expected length-mismatch message, got: " + ex.getMessage());
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldRejectTextLongerThanPatternWhenPatternDoesNotEndInYyyy() {
		// Symmetric check: a longer-than-pattern input is also rejected, not silently truncated.
		assertThrows(ParseException.class, () -> formatFor("yyyy-MM-dd").parse("2025-12-250"));
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldEnforceStrictParsingViaSetLenientFalse() {
		// The constructor sets lenient=false. Under lenient=true, "13/45/2025" would silently roll
		// over to Feb 14, 2026. Strict parsing must reject it. We use a pattern ending in yyyy so
		// both the regex check and the length check pass and the request reaches super.parse,
		// where the lenient=false flag is what catches the bad month/day.
		assertThrows(ParseException.class, () -> formatFor("MM/dd/yyyy").parse("13/45/2025"));
	}

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldThrowNullPointerExceptionWhenTextIsNull() {
		// Pins down the current contract: parse(null) bubbles up an NPE from the regex matcher
		// rather than wrapping it in a ParseException. Callers must null-check before invoking.
		assertThrows(NullPointerException.class, () -> formatFor("MM/dd/yyyy").parse(null));
	}

	/**
	 * @see OpenmrsDateFormat#OpenmrsDateFormat(SimpleDateFormat, Locale)
	 */
	@Test
	public void constructor_shouldPreserveSourcePatternAndApplyLocale() {
		OpenmrsDateFormat fmt = new OpenmrsDateFormat(new SimpleDateFormat("dd-MMM-yyyy", Locale.US), Locale.US);

		assertEquals("dd-MMM-yyyy", fmt.toPattern());
		assertNotNull(fmt);
	}
}
