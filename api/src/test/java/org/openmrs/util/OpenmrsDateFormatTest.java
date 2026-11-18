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
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OpenmrsDateFormat}.
 * <p>
 * {@code OpenmrsDateFormat} is a strict-mode wrapper around {@link SimpleDateFormat} whose central
 * reason for existing is to reject two-digit years when the pattern explicitly asks for four. Under
 * plain {@code SimpleDateFormat}, parsing {@code "12/25/25"} against pattern {@code "MM/dd/yyyy"}
 * silently produces the year 25 AD — the kind of clinical-timestamp bug that only surfaces months
 * later in reports. This test pins that guarantee down so a future refactor of the regex check in
 * {@link OpenmrsDateFormat#parse(String)} cannot weaken it without a test failing.
 */
public class OpenmrsDateFormatTest {

	/**
	 * @see OpenmrsDateFormat#parse(String)
	 */
	@Test
	public void parse_shouldRejectTwoDigitYearWhenPatternEndsInYyyy() {
		OpenmrsDateFormat fmt = new OpenmrsDateFormat(new SimpleDateFormat("MM/dd/yyyy", Locale.US), Locale.US);

		ParseException ex = assertThrows(ParseException.class, () -> fmt.parse("12/25/25"));

		// The error message is part of the contract: a downstream UI may rely on it to explain
		// the rejection to a user typing a short-form date. Asserting the message guards both the
		// rejection itself and the human-facing wording.
		assertTrue(ex.getMessage().contains("year must have 4 digits"),
		    "Expected message to call out the 4-digit-year requirement, got: " + ex.getMessage());
	}
}
