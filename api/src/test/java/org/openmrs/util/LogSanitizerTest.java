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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

class LogSanitizerTest {

	@Test
	void sanitize_shouldReturnNullStringForNullInput() {
		assertThat(LogSanitizer.sanitize((String) null), is("null"));
	}

	@Test
	void sanitize_shouldReturnNullStringForNullObject() {
		assertThat(LogSanitizer.sanitize((Object) null), is("null"));
	}

	@Test
	void sanitize_shouldReturnSameStringWhenNoSpecialChars() {
		assertThat(LogSanitizer.sanitize("hello world"), is("hello world"));
	}

	@Test
	void sanitize_shouldReplaceNewlineWithUnderscore() {
		assertThat(LogSanitizer.sanitize("line1\nline2"), is("line1_line2"));
	}

	@Test
	void sanitize_shouldReplaceCarriageReturnWithUnderscore() {
		assertThat(LogSanitizer.sanitize("line1\rline2"), is("line1_line2"));
	}

	@Test
	void sanitize_shouldReplaceTabWithUnderscore() {
		assertThat(LogSanitizer.sanitize("col1\tcol2"), is("col1_col2"));
	}

	@Test
	void sanitize_shouldReplaceCRLFWithUnderscores() {
		assertThat(LogSanitizer.sanitize("line1\r\nline2"), is("line1__line2"));
	}

	@Test
	void sanitize_shouldHandleMultipleNewlines() {
		assertThat(LogSanitizer.sanitize("a\nb\nc"), is("a_b_c"));
	}

	@Test
	void sanitize_shouldPreventLogForging() {
		// Simulates an attacker injecting a fake log entry
		String malicious = "innocent data\nINFO  2026-02-11 Forged log entry - user admin logged in";
		String sanitized = LogSanitizer.sanitize(malicious);
		assertFalse(sanitized.contains("\n"), "Sanitized output must not contain newlines");
		assertThat(sanitized, is("innocent data_INFO  2026-02-11 Forged log entry - user admin logged in"));
	}

	@Test
	void sanitize_shouldHandleEmptyString() {
		assertThat(LogSanitizer.sanitize(""), is(""));
	}

	@Test
	void sanitize_shouldSanitizeObjectToString() {
		Object obj = new Object() {
			@Override
			public String toString() {
				return "value\ninjected";
			}
		};
		assertThat(LogSanitizer.sanitize(obj), is("value_injected"));
	}

	@Test
	void sanitize_shouldHandleStringWithOnlySpecialChars() {
		assertThat(LogSanitizer.sanitize("\r\n\t"), is("___"));
	}
}
