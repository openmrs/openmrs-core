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

import java.text.ParseException;
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

public class DurationTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void addToDate_shouldAddSecondsWhenUnitIsSeconds() throws ParseException {
		Duration duration = new Duration(30, Duration.SNOMED_CT_SECONDS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
		
		assertEquals(createDateTime("2014-07-01 10:00:30"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddMinutesWhenUnitIsMinutes() throws ParseException {
		Duration duration = new Duration(30, Duration.SNOMED_CT_MINUTES_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
		
		assertEquals(createDateTime("2014-07-01 10:30:00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddHoursWhenUnitIsHours() throws ParseException {
		Duration duration = new Duration(10, Duration.SNOMED_CT_HOURS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
		
		assertEquals(createDateTime("2014-07-01 20:00:00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddDaysWhenUnitIsDays() throws ParseException {
		Duration duration = new Duration(30, Duration.SNOMED_CT_DAYS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
		
		assertEquals(createDateTime("2014-07-31 10:00:00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddMonthsWhenUnitIsMonths() throws ParseException {
		Duration duration = new Duration(3, Duration.SNOMED_CT_MONTHS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
		
		assertEquals(createDateTime("2014-10-01 10:00:00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddYearsWhenUnitIsYears() throws ParseException {
		Duration duration = new Duration(3, Duration.SNOMED_CT_YEARS_CODE);
		
		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
		
		assertEquals(createDateTime("2017-07-01 10:00:00"), autoExpireDate);
	}
	
	@Test
	public void addToDate_shouldAddTimeBasedOnFrequencyWhenUnitIsRecurringInterval() throws ParseException {
		Duration duration = new Duration(3, Duration.SNOMED_CT_RECURRING_INTERVAL_CODE); // 3 Times
		Date startDate = createDateTime("2014-07-01 10:00:00");
		OrderFrequency onceAWeek = createFrequency(1 / 7.0);
		
		assertEquals(createDateTime("2014-07-22 10:00:00"), duration.addToDate(startDate, onceAWeek));
	}
	
	@Test
	public void addToDate_shouldFailWhenUnitIsRecurringAndFrequencyIsUnknown() throws ParseException {
		Duration duration = new Duration(3, Duration.SNOMED_CT_RECURRING_INTERVAL_CODE); // 3 Times
		Date startDate = createDateTime("2014-07-01 10:00:00");
		OrderFrequency frequency = null;
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Matchers.is("Frequency can not be null when duration in Recurring Interval"));
		duration.addToDate(startDate, frequency);
	}
	
	@Test
	public void addToDate_shouldFailWhenUnitIsUnknown() throws ParseException {
		Duration duration = new Duration(3, "J");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Matchers.is("Unknown code 'J' for SNOMED CT duration units"));
		duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);
	}
	
	private OrderFrequency createFrequency(double frequencyPerDay) {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setFrequencyPerDay(frequencyPerDay);
		return frequency;
	}
	
	/**
	 * @verifies return null if the concept has no mapping to the SNOMED CT source
	 * @see Duration#getCode(Concept)
	 */
	@Test
	public void getCode_shouldReturnNullIfTheConceptHasNoMappingToTheSNOMEDCTSource() {
		final String daysCode = Duration.SNOMED_CT_DAYS_CODE;
		assertNull(Duration.getCode(SimpleDosingInstructionsTest.createUnits("some-uuid", daysCode, null)));
	}
	
	/**
	 * @verifies return the code for the term of the mapping to the SNOMED CT source
	 * @see Duration#getCode(Concept)
	 */
	@Test
	public void getCode_shouldReturnTheCodeForTheTermOfTheMappingToTheSNOMEDCTSource() {
		final String daysCode = Duration.SNOMED_CT_DAYS_CODE;
		Concept concept = SimpleDosingInstructionsTest.createUnits(daysCode);
		assertEquals(daysCode, Duration.getCode(concept));
	}
}
