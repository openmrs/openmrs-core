/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.logic.op.ComparisonOperator;

/**
 * Tests the {@link Duration} class
 */
public class DurationTest {

    /**
     * @verifies should get duration length
     * @see Duration#getDuration()
     */
    @Test
    public void getDuration_shouldReturnDurationLength() {
        final double seconds = 10;
        Duration duration1 = Duration.seconds(seconds);
        Assert.assertEquals(seconds, duration1.getDuration(), 0);

        final double minutes = 10;
        Duration duration2 = Duration.minutes(minutes);
        Assert.assertEquals(minutes, duration2.getDuration(), 0);

        final double hours = 10;
        Duration duration3 = Duration.hours(hours);
        Assert.assertEquals(hours, duration3.getDuration(), 0);

        final double days = 10;
        Duration duration4 = Duration.days(days);
        Assert.assertEquals(days, duration4.getDuration(), 0);

        final double weeks = 10;
        Duration duration5 = Duration.weeks(weeks);
        Assert.assertEquals(weeks, duration5.getDuration(), 0);

        final double months = 10;
        Duration duration6 = Duration.months(months);
        Assert.assertEquals(months, duration6.getDuration(), 0);

        final double years = 10;
        Duration duration7 = Duration.years(years);
        Assert.assertEquals(years, duration7.getDuration(), 0);
    }

    /**
     * @verifies get duration length if null
     * @see Duration#getDuration()
     */
    @Test
    public void getDuration_shouldNotFailIfNull() {
        Duration duration = Duration.seconds(null);
        Assert.assertNull(duration.getDuration());
    }

    /**
     * @verifies should get units
     * @see Duration#getUnits()
     */
    @Test
    public void getUnits_shouldReturnUnits() {
        final double value = 10;

        Duration duration1 = Duration.seconds(value);
        Assert.assertEquals(Duration.Units.SECONDS, duration1.getUnits());

        Duration duration2 = Duration.minutes(value);
        Assert.assertEquals(Duration.Units.MINUTES, duration2.getUnits());

        Duration duration3 = Duration.hours(value);
        Assert.assertEquals(Duration.Units.HOURS, duration3.getUnits());

        Duration duration4 = Duration.days(value);
        Assert.assertEquals(Duration.Units.DAYS, duration4.getUnits());

        Duration duration5 = Duration.weeks(value);
        Assert.assertEquals(Duration.Units.WEEKS, duration5.getUnits());

        Duration duration6 = Duration.months(value);
        Assert.assertEquals(Duration.Units.MONTHS, duration6.getUnits());

        Duration duration7 = Duration.years(value);
        Assert.assertEquals(Duration.Units.YEARS, duration7.getUnits());
    }

    /**
     * @verifies should convert duration to days
     * @see Duration#getDurationInDays()
     */
    @Test
    public void getDurationInDays_shouldReturnDays() {
        final double days = 1;

        final double secondsInDay = days * 86400;
        Duration duration1 = Duration.seconds(secondsInDay);
        Assert.assertEquals(days, duration1.getDurationInDays(), 0);

        final double minutesInDay = days * 1440;
        Duration duration2 = Duration.minutes(minutesInDay);
        Assert.assertEquals(days, duration2.getDurationInDays(), 0);

        final double hoursInDay = days * 24;
        Duration duration3 = Duration.hours(hoursInDay);
        Assert.assertEquals(days, duration3.getDurationInDays(), 0);

        Duration duration4 = Duration.days(days);
        Assert.assertEquals(days, duration4.getDurationInDays(), 0);

        final double weeks = 10;
        final double daysInWeeks = weeks * 7;
        Duration duration5 = Duration.weeks(weeks);
        Assert.assertEquals(daysInWeeks, duration5.getDurationInDays(), 0);

        final double months = 10;
        final double daysInMonths = months * 30;
        Duration duration6 = Duration.months(months);
        Assert.assertEquals(daysInMonths, duration6.getDurationInDays(), 0);

        final double years = 10;
        final double daysInYears = years * 365;
        Duration duration7 = Duration.years(years);
        Assert.assertEquals(daysInYears, duration7.getDurationInDays(), 0);
    }

    /**
     * @verifies should convert duration to millis
     * @see Duration#getDurationInMillis()
     */
    @Test
    public void getDurationInMillis_shouldReturnMillis() {
        final double seconds = 10;
        final double millisInSeconds = seconds * 1000;
        Duration duration1 = Duration.seconds(seconds);
        Assert.assertEquals(millisInSeconds, duration1.getDurationInMillis(), 0);

        final double minutes = 10;
        final double millisInMinutes = minutes * 60000;
        Duration duration2 = Duration.minutes(minutes);
        Assert.assertEquals(millisInMinutes, duration2.getDurationInMillis(), 0);

        final double hours = 10;
        final double millisInHours = hours * 3600000;
        Duration duration3 = Duration.hours(hours);
        Assert.assertEquals(millisInHours, duration3.getDurationInMillis(), 0);

        final double days = 10;
        final double millisInDays = days * 86400000;
        Duration duration4 = Duration.days(days);
        Assert.assertEquals(millisInDays, duration4.getDurationInMillis(), 0);

        final double weeks = 10;
        final double millisInWeeks = weeks * 10080000;
        Duration duration5 = Duration.weeks(weeks);
        Assert.assertEquals(millisInWeeks, duration5.getDurationInMillis(), 0);

        final double months = 10;
        final double millisInMonths = months * 2628000000L;
        Duration duration6 = Duration.months(months);
        Assert.assertEquals(millisInMonths, duration6.getDurationInMillis(), 0);

        final double years = 10;
        final double millisInYears = years * 31536000000L;
        Duration duration7 = Duration.years(years);
        Assert.assertEquals(millisInYears, duration7.getDurationInMillis(), 0);
    }

    /**
     * @verifies should create seconds duration from double
     * @see Duration#seconds(Double)
     */
    @Test
    public void seconds_shouldReturnSecondsDurationFromDouble() {
        final double seconds = 10;
        Duration duration = Duration.seconds(seconds);
        Assert.assertEquals(Duration.Units.SECONDS, duration.getUnits());
        Assert.assertEquals(seconds, duration.getDuration(), 0);
    }

    /**
     * @verifies should create seconds duration from int
     * @see Duration#seconds(int)
     */
    @Test
    public void seconds_shouldReturnSecondsDurationFromInt() {
        final int seconds = 10;
        Duration duration = Duration.seconds(seconds);
        Assert.assertEquals(Duration.Units.SECONDS, duration.getUnits());
        Assert.assertEquals(seconds, duration.getDuration(), 0);
    }

    /**
     * @verifies should create minutes duration from double
     * @see Duration#minutes(Double)
     */
    @Test
    public void minutes_shouldReturnMinutesDurationFromDouble() {
        final double minutes = 10;
        Duration duration = Duration.minutes(minutes);
        Assert.assertEquals(Duration.Units.MINUTES, duration.getUnits());
        Assert.assertEquals(minutes, duration.getDuration(), 0);
    }

    /**
     * @verifies should create minutes duration from int
     * @see Duration#minutes(int)
     */
    @Test
    public void minutes_shouldReturnMinutesDurationFromInt() {
        final int minutes = 10;
        Duration duration = Duration.minutes(minutes);
        Assert.assertEquals(Duration.Units.MINUTES, duration.getUnits());
        Assert.assertEquals(minutes, duration.getDuration(), 0);
    }

    /**
     * @verifies should create hours duration from double
     * @see Duration#hours(Double)
     */
    @Test
    public void hours_shouldReturnHoursDurationFromDouble() {
        final double hours = 10;
        Duration duration = Duration.hours(hours);
        Assert.assertEquals(Duration.Units.HOURS, duration.getUnits());
        Assert.assertEquals(hours, duration.getDuration(), 0);
    }

    /**
     * @verifies should create hours duration from int
     * @see Duration#hours(int)
     */
    @Test
    public void hours_shouldReturnHoursDurationFromInt() {
        final int hours = 10;
        Duration duration = Duration.hours(hours);
        Assert.assertEquals(Duration.Units.HOURS, duration.getUnits());
        Assert.assertEquals(hours, duration.getDuration(), 0);
    }

    /**
     * @verifies should create days duration from double
     * @see Duration#days(Double)
     */
    @Test
    public void days_shouldReturnDaysDurationFromDouble() {
        final double days = 10;
        Duration duration = Duration.days(days);
        Assert.assertEquals(Duration.Units.DAYS, duration.getUnits());
        Assert.assertEquals(days, duration.getDuration(), 0);
    }

    /**
     * @verifies should create days duration from int
     * @see Duration#days(int)
     */
    @Test
    public void days_shouldReturnDaysDurationFromInt() {
        final int days = 10;
        Duration duration = Duration.days(days);
        Assert.assertEquals(Duration.Units.DAYS, duration.getUnits());
        Assert.assertEquals(days, duration.getDuration(), 0);
    }

    /**
     * @verifies should create weeks duration from double
     * @see Duration#weeks(Double)
     */
    @Test
    public void weeks_shouldReturnWeeksDurationFromDouble() {
        final double weeks = 10;
        Duration duration = Duration.weeks(weeks);
        Assert.assertEquals(Duration.Units.WEEKS, duration.getUnits());
        Assert.assertEquals(weeks, duration.getDuration(), 0);
    }

    /**
     * @verifies should create weeks duration from int
     * @see Duration#weeks(int)
     */
    @Test
    public void weeks_shouldReturnWeeksDurationFromInt() {
        final int weeks = 10;
        Duration duration = Duration.weeks(weeks);
        Assert.assertEquals(Duration.Units.WEEKS, duration.getUnits());
        Assert.assertEquals(weeks, duration.getDuration(), 0);
    }

    /**
     * @verifies should create months duration from double
     * @see Duration#months(Double)
     */
    @Test
    public void months_shouldReturnMonthsDurationFromDouble() {
        final double months = 10;
        Duration duration = Duration.months(months);
        Assert.assertEquals(Duration.Units.MONTHS, duration.getUnits());
        Assert.assertEquals(months, duration.getDuration(), 0);
    }

    /**
     * @verifies should create months duration from int
     * @see Duration#months(int)
     */
    @Test
    public void months_shouldReturnMonthsDurationFromInt() {
        final int months = 10;
        Duration duration = Duration.months(months);
        Assert.assertEquals(Duration.Units.MONTHS, duration.getUnits());
        Assert.assertEquals(months, duration.getDuration(), 0);
    }

    /**
     * @verifies should create years duration from double
     * @see Duration#years(Double)
     */
    @Test
    public void years_shouldReturnYearsDurationFromDouble() {
        final double years = 10;
        Duration duration = Duration.years(years);
        Assert.assertEquals(Duration.Units.YEARS, duration.getUnits());
        Assert.assertEquals(years, duration.getDuration(), 0);
    }

    /**
     * @verifies should create years duration from int
     * @see Duration#years(int)
     */
    @Test
    public void years_shouldReturnYearsDurationFromInt() {
        final int years = 10;
        Duration duration = Duration.years(years);
        Assert.assertEquals(Duration.Units.YEARS, duration.getUnits());
        Assert.assertEquals(years, duration.getDuration(), 0);
    }

    /**
     * @verifies should return false unless ComparisonOperator.WITHIN
     * @see Duration#supports(ComparisonOperator)
     */
    @Test
    public void supports_shouldReturnBool() {
        final double dur = 10;
        Duration duration = Duration.seconds(dur);

        Assert.assertFalse(duration.supports(ComparisonOperator.AFTER));
        Assert.assertFalse(duration.supports(ComparisonOperator.BEFORE));
        Assert.assertFalse(duration.supports(ComparisonOperator.CONTAINS));
        Assert.assertFalse(duration.supports(ComparisonOperator.EQUALS));
        Assert.assertFalse(duration.supports(ComparisonOperator.GT));
        Assert.assertFalse(duration.supports(ComparisonOperator.GTE));
        Assert.assertFalse(duration.supports(ComparisonOperator.IN));
        Assert.assertFalse(duration.supports(ComparisonOperator.LT));
        Assert.assertFalse(duration.supports(ComparisonOperator.LTE));
        Assert.assertTrue(duration.supports(ComparisonOperator.WITHIN));
    }
}