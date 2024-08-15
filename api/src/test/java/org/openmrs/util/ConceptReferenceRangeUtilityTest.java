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

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalTime;
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
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10", 
				person)
		);
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10", 
				person)
		);
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() >= 1 && $patient.getAge() <" +
					"= 10", person)
		);
	}

	@Test
	public void testAgeInMonths_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAgeInMonths() > 1 && $patient.getAgeInMonths() < 12",
				person)
		);
	}

	@Test
	public void testAgeInRange_shouldThrowExceptionIfCriteriaIsInvalid() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());

		assertThrows(APIException.class, () -> 
			conceptReferenceRangeUtility.evaluateCriteria("invalidCriteria", person));
	}

	@Test
	public void testAgeInRange_shouldThrowAnExceptionIfCriteriaIsEmpty() {
		calendar = Calendar.getInstance();
		calendar.set(2019, Calendar.JUNE, 2);
		person.setBirthdate(calendar.getTime());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			conceptReferenceRangeUtility.evaluateCriteria("", person);
		});

		assertTrue(exception.getMessage().contains("criteria is empty"));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfPersonAgeIsNotSet() {
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() >= 15 && $patient.getAge() <= 50",
				person)
		);
	}

	@Test
	public void testGenderMatch_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getGender().equals('M')",
				person)
		);
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		person.setGender("F");

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getGender().equals('M')",
				person)
		);
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderIsNull() {
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
			"$patient.getGender().equals('M')",
			person)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeAndGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				person)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeOrGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"($patient.getAge() > 1 && $patient.getAge() < 3) || $patient.getGender().equals('M')",
				person)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				person)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				person)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldThrowExceptionIfPersonIsNull() {

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> 
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				null)
		);

		assertEquals("Failed to evaluate criteria with reason: patient is null", thrown.getMessage());
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeIsNull() {
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10",
				person)
		);
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
				Collections.singletonList("dateCreated"),
				1,
				null,
				null,
				null,
				false))
			.thenReturn(Collections.singletonList(obs));
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getGender().equals('F') && $fn.getLatestObsByConcept('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce').getValueText().equals('PREGNANT')", 
				person)
		);
	}

	@Test
	public void testObsValueMatch_shouldReturnFalseIfValueMismatch() {
		person.setGender("M");
		
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$fn.getLatestObsByConcept('CIEL:1234') == true",
				person)
		);
	}

	@Test
	public void testTimeOfDay_shouldReturnTrueIfTimeOfDayMatches() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$fn.getTimeOfTheDay() == " + LocalTime.now().getHourOfDay(), 
				person)
		);

		// Clean up: Reset time to system time
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void testTimeOfDay_shouldReturnFalseIfTimeOfDayDoesNotMatch() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$fn.getTimeOfTheDay() == " + LocalTime.now().plusHours(1).getHourOfDay(),
				person)
		);

		// Clean up: Reset time to system time
		DateTimeUtils.setCurrentMillisSystem();
	}

	private Obs buildObservation() {
		Concept concept = new Concept(5089);
		concept.setDatatype(new ConceptDatatype(3));

		Date date = new Date();

		Obs obs = new Obs();
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

