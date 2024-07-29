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
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());
		String criteria = "#set( $criteria = $patient.getAge() >= 1 && $patient.getAge() <= 10 )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsNoMatch() {
		calendar.add(Calendar.YEAR, -2);
		person.setBirthdate(calendar.getTime());
		String criteria = "#set( $criteria = $patient.getAge() >= 15 && $patient.getAge() <= 50 )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfCriteriaIsInvalid() {
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());
		String criteria = "invalidCriteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfCriteriaIsEmpty() {
		calendar.set(2019, Calendar.JUNE, 2);
		person.setBirthdate(calendar.getTime());
		String criteria = "";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfPersonIsNull() {
		String criteria = "#set( $criteria = $patient.getAge() >= 15 && $patient.getAge() <= 50 )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeOfAPersonIsNull() {
		String criteria = "#set( $criteria = $patient.getAge() >= 15 && $patient.getAge() <= 50 )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testGenderMatch_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");
		String criteria = "#set( $criteria = $patient.getGender().equals('M') )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testCriteriaWithPerson_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");
		String criteria = "#set( $criteria = $patient.getGender().equals('M') )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		person.setGender("F");
		String criteria = "#set( $criteria = $patient.getGender().equals('M') )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderIsNull() {
		String criteria = "#set( $criteria = $patient.getGender().equals('M') )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeAndGenderMatch() {
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M') )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeDoesNotMatch() {
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M') )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M') )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfPersonIsNull() {
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M') )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, null));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeOrGenderIsNull() {
		String criteria = "#set( $criteria = $patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M') )$criteria";
		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testObsValueMatch_shouldReturnTrueIfValueTextMatch() {
		person.setGender("F");
		
		Obs obs = buildObservation();
		obs.setPerson(person);
		obs.setValueText("PREGNANT");

		Mockito.when(obsService.getLatestObsByConceptId("5089")).thenReturn(obs);

		String criteria = "#set( $criteria = $patient.getGender().equals('F') && $fn.getLatestObsByConceptId('5089').getValueText().equals('PREGNANT') )$criteria";

		assertTrue(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testObsValueMatch_shouldReturnFalseIfValueMismatch() {
		person.setGender("M");

		String criteria = "#set( $criteria = $patient.getGender().equals('F') && $fn.getLatestObsByConceptId('5089') == true )$criteria";

		assertFalse(ConceptReferenceRangeUtility.evaluateCriteria(criteria, person));
	}

	@Test
	public void testTimeOfDay_shouldReturnTrueIfTimeMatches() {
		int hour = LocalTime.now().getHour();
		String criteria = "#set( $criteria = $fn.getTimeOfTheDay() == " + hour + " )$criteria";

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

