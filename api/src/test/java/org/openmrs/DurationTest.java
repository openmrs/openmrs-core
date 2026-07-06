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

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openmrs.test.TestUtil.createDateTime;

public class DurationTest extends BaseContextSensitiveTest {

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
	public void addToDate_shouldAddMinutesWhenUnitIsTheCurrentSnomedCtMinuteCode() throws ParseException {
		Duration duration = new Duration(30, Duration.SNOMED_CT_MINUTES_CODE_2021);

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
	public void addToDate_shouldAddWeeksWhenUnitIsWeeks() throws ParseException {
		Duration duration = new Duration(3, Duration.SNOMED_CT_WEEKS_CODE);

		Date autoExpireDate = duration.addToDate(createDateTime("2014-07-01 10:00:00"), null);

		assertEquals(createDateTime("2014-07-22 10:00:00"), autoExpireDate);
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
	public void addToDate_shouldAddTimeForUcumTimeCodes() throws ParseException {
		Date startDate = createDateTime("2014-07-01 10:00:00");

		assertEquals(createDateTime("2014-07-01 10:00:30"),
		    new Duration(30, Duration.UCUM_SECONDS_CODE).addToDate(startDate, null));
		assertEquals(createDateTime("2014-07-01 10:30:00"),
		    new Duration(30, Duration.UCUM_MINUTES_CODE).addToDate(startDate, null));
		assertEquals(createDateTime("2014-07-01 20:00:00"),
		    new Duration(10, Duration.UCUM_HOURS_CODE).addToDate(startDate, null));
		assertEquals(createDateTime("2014-07-31 10:00:00"),
		    new Duration(30, Duration.UCUM_DAYS_CODE).addToDate(startDate, null));
		assertEquals(createDateTime("2014-07-22 10:00:00"),
		    new Duration(3, Duration.UCUM_WEEKS_CODE).addToDate(startDate, null));
		assertEquals(createDateTime("2014-10-01 10:00:00"),
		    new Duration(3, Duration.UCUM_MONTHS_CODE).addToDate(startDate, null));
		assertEquals(createDateTime("2017-07-01 10:00:00"),
		    new Duration(3, Duration.UCUM_YEARS_CODE).addToDate(startDate, null));
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

		APIException exception = assertThrows(APIException.class, () -> duration.addToDate(startDate, frequency));
		assertThat(exception.getMessage(),
		    is(Context.getMessageSourceService().getMessage("Duration.error.frequency.null")));
	}

	@Test
	public void addToDate_shouldFailWhenUnitIsUnknown() throws ParseException {
		Duration duration = new Duration(3, "J");

		APIException exception = assertThrows(APIException.class,
		    () -> duration.addToDate(createDateTime("2014-07-01 10:00:00"), null));
		assertThat(exception.getMessage(),
		    is(Context.getMessageSourceService().getMessage("Duration.unknown.code", new Object[] { "J" }, null)));
	}

	private OrderFrequency createFrequency(double frequencyPerDay) {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setFrequencyPerDay(frequencyPerDay);
		return frequency;
	}

	/**
	 * @see Duration#getCode(Concept)
	 */
	@Test
	public void getCode_shouldReturnNullIfTheConceptHasNoMappingToTheSNOMEDCTSource() {
		final String daysCode = Duration.SNOMED_CT_DAYS_CODE;
		assertNull(Duration.getCode(SimpleDosingInstructionsTest.createUnits("some-uuid", daysCode, null)));
	}

	/**
	 * @see Duration#getCode(Concept)
	 */
	@Test
	public void getCode_shouldReturnTheCodeForTheTermOfTheMappingToTheSNOMEDCTSource() {
		final String daysCode = Duration.SNOMED_CT_DAYS_CODE;
		Concept concept = SimpleDosingInstructionsTest.createUnits(daysCode);
		assertEquals(daysCode, Duration.getCode(concept));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldReturnTheCodeOfASnomedCtSameAsMappingWithAKnownCode() {
		Concept units = SimpleDosingInstructionsTest.createUnits(Duration.SNOMED_CT_DAYS_CODE);

		assertEquals(Duration.SNOMED_CT_DAYS_CODE, Duration.getKnownCode(units));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldReturnAMinutesCodeForAConceptCarryingBothLegacyAndCurrentSnomedCtMinuteCodes()
	        throws ParseException {
		Concept units = unitsWithMappings(sameAsMapping("SNOMED CT", "SCT", Duration.SNOMED_CT_MINUTES_CODE_2021),
		    sameAsMapping("SNOMED CT", "SCT", Duration.SNOMED_CT_MINUTES_CODE));

		String code = Duration.getKnownCode(units);

		assertNotNull(code);
		Date autoExpireDate = new Duration(30, code).addToDate(createDateTime("2014-07-01 10:00:00"), null);
		assertEquals(createDateTime("2014-07-01 10:30:00"), autoExpireDate);
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldReturnAUcumCodeWhenTheConceptHasOnlyAUcumMapping() {
		Concept units = unitsWithMappings(sameAsMapping("UCUM", null, Duration.UCUM_MINUTES_CODE));

		assertEquals(Duration.UCUM_MINUTES_CODE, Duration.getKnownCode(units));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldReturnNullIfNoSameAsMappingCarriesAKnownCode() {
		Concept units = SimpleDosingInstructionsTest.createUnits("SCT", "999999999", null);

		assertNull(Duration.getKnownCode(units));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldReturnNullIfTheKnownCodeIsMappedWithANonSameAsType() {
		Concept units = SimpleDosingInstructionsTest.createUnits("SCT", Duration.SNOMED_CT_DAYS_CODE, "some-map-type-uuid");

		assertNull(Duration.getKnownCode(units));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldReturnNullIfKnownCodesOfTheConceptDenoteDifferentUnits() {
		Concept units = unitsWithMappings(sameAsMapping("SNOMED CT", "SCT", Duration.SNOMED_CT_DAYS_CODE),
		    sameAsMapping("UCUM", null, Duration.UCUM_MONTHS_CODE));

		assertNull(Duration.getKnownCode(units));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldMatchTheUcumSourceNameCaseInsensitively() {
		Concept units = unitsWithMappings(sameAsMapping("ucum", null, Duration.UCUM_MINUTES_CODE));

		assertEquals(Duration.UCUM_MINUTES_CODE, Duration.getKnownCode(units));
	}

	/**
	 * @see Duration#getKnownCode(Concept)
	 */
	@Test
	public void getKnownCode_shouldIgnoreKnownCodesMappedToOtherSources() {
		Concept units = unitsWithMappings(sameAsMapping("Some Dictionary", "L", Duration.UCUM_MINUTES_CODE));

		assertNull(Duration.getKnownCode(units));
	}

	private static Concept unitsWithMappings(ConceptMap... mappings) {
		Concept concept = new Concept();
		for (ConceptMap mapping : mappings) {
			concept.addConceptMapping(mapping);
		}
		return concept;
	}

	private static ConceptMap sameAsMapping(String sourceName, String sourceHl7Code, String code) {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setName(sourceName);
		conceptSource.setHl7Code(sourceHl7Code);
		ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
		conceptReferenceTerm.setConceptSource(conceptSource);
		conceptReferenceTerm.setCode(code);
		ConceptMap conceptMapping = new ConceptMap();
		conceptMapping.setConceptReferenceTerm(conceptReferenceTerm);
		ConceptMapType conceptMapType = new ConceptMapType();
		conceptMapType.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
		conceptMapping.setConceptMapType(conceptMapType);
		return conceptMapping;
	}
}
