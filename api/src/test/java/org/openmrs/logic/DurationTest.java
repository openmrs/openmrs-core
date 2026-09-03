/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DurationTest {

	@Test
	public void getDurationInMillis_shouldConvertSecondsCorrectly() {
		Duration duration = Duration.seconds(30.0);

		assertEquals(30000L, duration.getDurationInMillis());
	}

	@Test
	public void getDurationInMillis_shouldConvertMinutesCorrectly() {
		Duration duration = Duration.minutes(5.0);

		assertEquals(300000L, duration.getDurationInMillis());
	}

	@Test
	public void getDurationInMillis_shouldConvertHoursCorrectly() {
		Duration duration = Duration.hours(2.0);

		assertEquals(7200000L, duration.getDurationInMillis());
	}

	@Test
	public void getDurationInMillis_shouldConvertDaysCorrectly() {
		Duration duration = Duration.days(3.0);

		assertEquals(259200000L, duration.getDurationInMillis());
	}

	@Test
	public void getDurationInMillis_shouldConvertMonthsCorrectly() {
		Duration duration = Duration.months(1.0);

		assertEquals(2628000000L, duration.getDurationInMillis());
	}

	@Test
	public void getDurationInMillis_shouldConvertYearsCorrectly() {
		Duration duration = Duration.years(1.0);

		assertEquals(31536000000L, duration.getDurationInMillis());
	}

	@Test
	public void getDurationInMillis_shouldConvertOneWeekCorrectly() {
		Duration duration = Duration.weeks(1.0);

		assertEquals(604800000L, duration.getDurationInMillis());
	}
}
