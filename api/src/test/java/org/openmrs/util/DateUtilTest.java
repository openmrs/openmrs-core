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
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateUtilTest {

	@Test
	public void truncateToSeconds_shouldDropMilliseconds() {
		Date withMilliseconds = new Date(123L);
		Date withoutMilliseconds = new Date(0L);
		assertThat(DateUtil.truncateToSeconds(withMilliseconds), is(withoutMilliseconds));
		assertThat(DateUtil.truncateToSeconds(withoutMilliseconds), is(withoutMilliseconds));
	}

	@Test
	public void parseDatetimeString_shouldKeepSecondsInLegacyFormat() throws Exception {
		Date parsed = DateUtil.parseDatetimeString("2023-06-15 10:30:45");
		assertThat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parsed), is("2023-06-15 10:30:45"));
	}

	@Test
	public void parseDatetimeString_shouldParseLegacyFormatWithoutSeconds() throws Exception {
		Date parsed = DateUtil.parseDatetimeString("2023-06-15 10:30");
		assertThat(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(parsed), is("2023-06-15 10:30"));
	}

	@Test
	public void parseDatetimeString_shouldThrowParseExceptionForInvalidLegacyDate() {
		assertThrows(ParseException.class, () -> DateUtil.parseDatetimeString("2023-02-30 10:00"));
	}

	@Test
	public void parseDatetimeString_shouldThrowParseExceptionForTrailingCharactersInLegacyFormat() {
		assertThrows(ParseException.class, () -> DateUtil.parseDatetimeString("2023-06-15 10:30XYZ"));
	}

	@Test
	public void parseDatetimeString_shouldThrowForInvalidIsoDate() {
		assertThrows(DateTimeParseException.class, () -> DateUtil.parseDatetimeString("2023-02-30T10:00:00"));
	}
}
