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
import org.openmrs.api.ObsService;
import org.openmrs.api.ValidationException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConceptReferenceRangeUtilityTest extends BaseContextSensitiveTest {
	private Calendar calendar;
	private Person person;
	
	@Mock
	private ObsService obsService;
	
	@BeforeEach
	public void setUp() {
		calendar = Calendar.getInstance();
		person = new Person();
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsWithinRange() {
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());
		String criteria = "$patient.getAge() >= 1 && $patient.getAge() <= 10";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsNoMatch() {
		calendar.add(Calendar.YEAR, -2);
		person.setBirthdate(calendar.getTime());
		String criteria = "$patient.getAge() >= 15 && $patient.getAge() <= 50";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInYears_shouldReturnTrueIfAgeIsWithinRange() {
		Person person = new Person();

		// Set birthdate to 5 years ago from today
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);

		String criteria = "$fn.getAge($patient, 'YEARS') > 1 && $fn.getAge($patient, 'YEARS') < 10";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInMonths_shouldReturnTrueIfAgeIsWithinRange() {
		Person person = new Person();

		// Set birthdate to 5 months ago from today
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);

		String criteria = "$fn.getAge($patient, 'MONTHS') > 1 && $fn.getAge($patient, 'MONTHS') < 12";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfCriteriaIsInvalid() {
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());
		String criteria = "invalidCriteria";

		assertThrows(ParseErrorException.class, () -> {
			ConceptReferenceRangeUtility.evaluateCriteria(criteria, person);
		});
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfCriteriaIsEmpty() {
		calendar.set(2019, Calendar.JUNE, 2);
		person.setBirthdate(calendar.getTime());
		String criteria = "";

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			ConceptReferenceRangeUtility.evaluateCriteria(criteria, person);
		});

		assertTrue(exception.getMessage().contains("criteria required"));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfPersonIsNull() {
		String criteria = "$patient.getAge() >= 15 && $patient.getAge() <= 50";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeOfAPersonIsNull() {
		String criteria = "$patient.getAge() >= 15 && $patient.getAge() <= 50";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testGenderMatch_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");
		String criteria = "$patient.getGender().equals('M')";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testCriteriaWithPerson_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");
		String criteria = "$patient.getGender().equals('M')";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		person.setGender("F");
		String criteria = "$patient.getGender().equals('M')";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderIsNull() {
		String criteria = "$patient.getGender().equals('M')";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeAndGenderMatch() {
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeDoesNotMatch() {
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfPersonIsNull() {
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";

		try {
			ConceptReferenceRangeUtility.evaluateCriteria(criteria, null);
		} catch (ValidationException e) {
			Assertions.assertEquals("Failed to validate with reason: patient is null", e.getMessage());
		}
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeOrGenderIsNull() {
		String criteria = "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')";
		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testObsValueMatch_shouldReturnTrueIfValueTextMatch() {
		person.setGender("F");
		
		Obs obs = buildObservation();
		obs.setPerson(person);
		obs.setValueText("PREGNANT");

		Mockito.when(obsService.getLatestObsByConceptId("5089")).thenReturn(obs);

		String criteria = "$patient.getGender().equals('F') && $fn.getLatestObsByConceptId('5089').getValueText().equals('PREGNANT')";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testObsValueMatch_shouldReturnFalseIfValueMismatch() {
		person.setGender("M");

		String criteria = "$patient.getGender().equals('F') && $fn.getLatestObsByConceptId('5089') == true";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testTimeOfDay_shouldReturnTrueIfTimeMatches() {
		int hour = LocalTime.now().getHour();
		String criteria = "$fn.getTimeOfTheDay() == " + hour + "";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	private Obs buildObservation() {
		Concept concept = new Concept(5089);
		concept.setDatatype(new ConceptDatatype(3));
		
		Encounter encounter = new Encounter(3);
		Date datetime = new Date();
		Location location = new Location(1);
		Integer valueGroupId = 7;
		Date valueDatetime = new Date();
		Concept valueCoded = new Concept(3);
		Double valueNumeric = 2.0;
		String valueModifier = "cc";

		Obs obs = new Obs();
		obs.setOrder(null);
		obs.setConcept(concept);
		obs.setEncounter(encounter);
		obs.setObsDatetime(datetime);
		obs.setLocation(location);
		obs.setValueGroupId(valueGroupId);
		obs.setValueDatetime(valueDatetime);
		obs.setValueCoded(valueCoded);
		obs.setValueNumeric(valueNumeric);
		obs.setValueModifier(valueModifier);

		return obs;
	}
}

