/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static org.openmrs.test.TestUtil.createDateTime;

import java.text.ParseException;
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
		expectedException.expectMessage(Matchers.is("Duration.error.frequency.null"));
		duration.addToDate(startDate, frequency);
	}
	
	@Test
	public void addToDate_shouldFailWhenUnitIsUnknown() throws ParseException {
		Duration duration = new Duration(3, "J");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Matchers.is("Duration.unknown.code"));
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
