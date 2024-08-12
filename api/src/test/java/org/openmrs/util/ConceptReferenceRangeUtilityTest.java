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

import org.apache.velocity.exception.ParseErrorException;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConceptReferenceRangeUtilityTest extends BaseContextSensitiveTest {
	private Calendar calendar;
	private Person person;
	private ConceptReferenceRangeUtility conceptReferenceRangeUtility;
	
	@Mock
	private ObsService obsService;
	
	@Mock
	private ConceptService conceptService;
	
	@BeforeEach
	public void setUp() {
		person = new Person();
		conceptReferenceRangeUtility = new ConceptReferenceRangeUtility();
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());
		final String CRITERIA = "$patient.getAge() >= 1 && $patient.getAge() <= 10";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsNoMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -2);
		person.setBirthdate(calendar.getTime());
		final String CRITERIA = "$patient.getAge() >= 15 && $patient.getAge() <= 50";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInYears_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);
		person.setId(1);

		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInMonths_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);

		final String CRITERIA = "$patient.getAgeInMonths() > 1 && $patient.getAgeInMonths() < 12";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInRange_shouldThrowExceptionIfCriteriaIsInvalid() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());
		final String CRITERIA = "invalidCriteria";

		assertThrows(RuntimeException.class, () -> conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInRange_shouldThrowAnExceptionIfCriteriaIsEmpty() {
		calendar = Calendar.getInstance();
		calendar.set(2019, Calendar.JUNE, 2);
		person.setBirthdate(calendar.getTime());
		final String CRITERIA = "";

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person);
		});

		assertTrue(exception.getMessage().contains("criteria required"));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfPersonIsNull() {
		final String CRITERIA = "$patient.getAge() >= 15 && $patient.getAge() <= 50";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeOfAPersonIsNull() {
		final String CRITERIA = "$patient.getAge() >= 15 && $patient.getAge() <= 50";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testGenderMatch_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");
		final String CRITERIA = "$patient.getGender().equals('M')";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testCriteriaWithPerson_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");
		final String CRITERIA = "$patient.getGender().equals('M')";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		person.setGender("F");
		final String CRITERIA = "$patient.getGender().equals('M')";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderIsNull() {
		final String CRITERIA = "$patient.getGender().equals('M')";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeAndGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeOrGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		final String CRITERIA = "($patient.getAge() > 1 && $patient.getAge() < 10) || $patient.getGender().equals('M')";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldThrowExceptionIfPersonIsNull() {
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		try {
			conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, null);
		} catch (IllegalArgumentException e) {
			Assertions.assertEquals("Failed to validate with reason: patient is null", e.getMessage());
		}
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeOrGenderIsNull() {
		final String CRITERIA = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testObsValueMatch_shouldReturnTrueIfValueTextMatch() {
		person.setGender("F");
		
		Obs obs = buildObservation();
		obs.setPerson(person);
		obs.setValueText("PREGNANT");
		
		Concept concept = new Concept(4900);
		
		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(null,
				null,
				Collections.singletonList(concept),
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				false))
			.thenReturn(Collections.singletonList(obs));

		final String CRITERIA = "$patient.getGender().equals('F') && $fn.getLatestObsByConcept('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce').getValueText().equals('PREGNANT')";

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testObsValueMatch_shouldReturnFalseIfValueMismatch() {
		person.setGender("M");

		final String CRITERIA = "$patient.getGender().equals('F') && $fn.getLatestObsByConcept('CIEL:1234') == true";

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));
	}

	@Test
	public void testTimeOfDay_shouldReturnTrueIfTimeMatches() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());

		int currentHour = LocalTime.now().getHourOfDay();
		final String CRITERIA = "$fn.getTimeOfTheDay() == " + currentHour;

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(CRITERIA, person));

		// Clean up: Reset time to system time
		DateTimeUtils.setCurrentMillisSystem();
	}

	private Obs buildObservation() {
		Concept concept = new Concept(5089);
		concept.setDatatype(new ConceptDatatype(3));

		Date date = new Date();

		Obs obs = new Obs();
		obs.setOrder(null);
		obs.setConcept(concept);
		obs.setEncounter(new Encounter(3));
		obs.setObsDatetime(date);
		obs.setLocation(new Location(1));
		obs.setValueGroupId(7);
		obs.setValueDatetime(date);
		obs.setValueCoded(new Concept(3));
		obs.setValueNumeric(2.0);
		obs.setValueModifier("cc");

		return obs;
	}
}

