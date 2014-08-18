/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static org.openmrs.test.TestUtil.createDateTime;

public class ISO8601DurationTest extends BaseContextSensitiveTest {
	
	@Test
	public void addToDate_shouldAddSecondsWhenUnitIsSeconds() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(30, ISO8601Duration.SECONDS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
		
		assertEquals(createDateTime("2014-07-01 10-00-30"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddMinutesWhenUnitIsMinutes() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(30, ISO8601Duration.MINUTES_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
		
		assertEquals(createDateTime("2014-07-01 10-30-00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddHoursWhenUnitIsHours() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(10, ISO8601Duration.HOURS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
		
		assertEquals(createDateTime("2014-07-01 20-00-00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddDaysWhenUnitIsDays() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(30, ISO8601Duration.DAYS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
		
		assertEquals(createDateTime("2014-07-31 10-00-00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddMonthsWhenUnitIsMonths() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(3, ISO8601Duration.MONTHS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
		
		assertEquals(createDateTime("2014-10-01 10-00-00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddYearsWhenUnitIsYears() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(3, ISO8601Duration.YEARS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
		
		assertEquals(createDateTime("2017-07-01 10-00-00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddTimeBasedOnFrequencyWhenUnitIsRecurringInterval() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(3, ISO8601Duration.RECURRING_INTERVAL_CODE); // 3 Times
		Date startDate = createDateTime("2014-07-01 10-00-00");
		OrderFrequency onceAWeek = createFrequency(1 / 7.0);
		
		assertEquals(createDateTime("2014-07-22 10-00-00"), duration.addToDate(startDate, onceAWeek));
	}
	
	@Test
	public void addToDate_shouldReturnNullWhenUnitIsUnknown() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(3, "UNKNOWN");
		
		assertEquals(null, duration.addToDate(createDateTime("2014-07-01 10-00-00"), null));
	}
	
	private OrderFrequency createFrequency(double frequencyPerDay) {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setFrequencyPerDay(frequencyPerDay);
		return frequency;
	}
}
