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
