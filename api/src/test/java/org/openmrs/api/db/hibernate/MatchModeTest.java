/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MatchModeTest {

	static Stream<Object[]> data() {
		return Arrays.stream(new Object[][]{
			{MatchMode.START, "TEST", true, "test%"},
			{MatchMode.END, "TeST", true, "%test"},
			{MatchMode.ANYWHERE, "test", true, "%test%"},
			{MatchMode.EXACT, "TEst", true, "test"},
			{MatchMode.START, "TEST", false, "TEST%"},
			{MatchMode.END, "TeST", false, "%TeST"},
			{MatchMode.ANYWHERE, "test", false, "%test%"},
			{MatchMode.EXACT, "TEst", false, "TEst"},
		});
	}

	@ParameterizedTest
	@MethodSource("data")
	public void shouldMatchPatternCorrectly(MatchMode matchMode, String input, boolean caseInsensitive, String expectedPattern) {
		if (caseInsensitive) {
			assertEquals(expectedPattern, matchMode.toLowerCasePattern(input));
		} else {
			assertEquals(expectedPattern, matchMode.toCaseSensitivePattern(input));
		}
	}
}
