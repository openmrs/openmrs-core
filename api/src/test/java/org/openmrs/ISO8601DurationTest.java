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

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static org.openmrs.test.TestUtil.createDateTime;

import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
import org.openmrs.test.BaseContextSensitiveTest;

public class ISO8601DurationTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
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
	public void addToDate_shouldFailWhenUnitIsRecurringAndFrequencyIsUnknown() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(3, ISO8601Duration.RECURRING_INTERVAL_CODE); // 3 Times
		Date startDate = createDateTime("2014-07-01 10-00-00");
		OrderFrequency frequency = null;
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Matchers.is("Frequency can not be null when duration in Recurring Interval"));
		duration.addToDate(startDate, frequency);
	}
	
	@Test
	public void addToDate_shouldFailWhenUnitIsUnknown() throws Exception {
		ISO8601Duration duration = new ISO8601Duration(3, "J");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Matchers.is("Unknown code 'J' for ISO8601 duration units"));
		duration.addToDate(createDateTime("2014-07-01 10-00-00"), null);
	}
	
	private OrderFrequency createFrequency(double frequencyPerDay) {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setFrequencyPerDay(frequencyPerDay);
		return frequency;
	}
	
	/**
	 * @verifies return null if the concept has no mapping to the ISO8601 source
	 * @see ISO8601Duration#getCode(Concept)
	 */
	@Test
	public void getCode_shouldReturnNullIfTheConceptHasNoMappingToTheISO8601Source() throws Exception {
		final String daysCode = ISO8601Duration.DAYS_CODE;
		assertNull(ISO8601Duration.getCode(SimpleDosingInstructionsTest.createUnits("some-uuid", daysCode)));
	}
	
	/**
	 * @verifies return the code for the term of the mapping to the ISO8601 source
	 * @see ISO8601Duration#getCode(Concept)
	 */
	@Test
	public void getCode_shouldReturnTheCodeForTheTermOfTheMappingToTheISO8601Source() throws Exception {
		final String daysCode = ISO8601Duration.DAYS_CODE;
		Concept concept = SimpleDosingInstructionsTest.createUnits(daysCode);
		assertEquals(daysCode, ISO8601Duration.getCode(concept));
	}
}
