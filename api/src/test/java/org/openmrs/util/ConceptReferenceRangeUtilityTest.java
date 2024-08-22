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
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
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

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10",
				obs)
		);
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10", 
				obs)
		);
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() >= 1 && $patient.getAge() <" + "= 10", 
				obs)
		);
	}

	@Test
	public void testAgeInMonths_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);

		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAgeInMonths() > 1 && $patient.getAgeInMonths() < 12",
				obs)
		);
	}

	@Test
	public void testAgeInRange_shouldThrowExceptionIfCriteriaIsInvalid() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class, () -> 
			conceptReferenceRangeUtility.evaluateCriteria("invalidCriteria", obs));
	}

	@Test
	public void testAgeInRange_shouldThrowAnExceptionIfCriteriaIsEmpty() {
		calendar = Calendar.getInstance();
		calendar.set(2019, Calendar.JUNE, 2);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			conceptReferenceRangeUtility.evaluateCriteria("", obs);
		});

		assertTrue(exception.getMessage().contains("criteria is empty"));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfPersonAgeIsNotSet() {
		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() >= 15 && $patient.getAge() <= 50",
				obs)
		);
	}

	@Test
	public void testGenderMatch_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getGender().equals('M')",
				obs)
		);
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getGender().equals('M')",
				obs)
		);
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderIsNull() {
		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
			"$patient.getGender().equals('M')",
			obs)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeAndGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				obs)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeOrGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"($patient.getAge() > 1 && $patient.getAge() < 3) || $patient.getGender().equals('M')",
				obs)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				obs)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				obs)
		);
	}

	@Test
	public void testAgeAndGenderMatch_shouldThrowExceptionIfPersonIsNull() {

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> 
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", 
				null)
		);

		assertEquals("Failed to evaluate criteria with reason: Obs is null", thrown.getMessage());
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeIsNull() {
		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAge() > 1 && $patient.getAge() < 10",
				obs)
		);
	}

	@Test
	public void testObsValueMatch_shouldReturnTrueIfValueTextMatch() {
		person.setGender("F");
		
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueText("PREGNANT");
		
		Concept concept = new Concept(4900);
		
		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person),
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
				"$patient.getGender().equals('F') " +
					"&& $fn.getLatestObs('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient).getValueText().equals('PREGNANT')", 
				obs)
		);
	}
	
	@Test
	public void testObsValueMatch_shouldReturnTrueIfPersonAttributeMatch() {
		PersonAttributeType personAttributeType = new PersonAttributeType();
		personAttributeType.setName("Race");
		personAttributeType.setSearchable(true);
		
		PersonAttribute personAttribute = new PersonAttribute();
		personAttribute.setAttributeType(personAttributeType);
		personAttribute.setValue("Maasai");
		
		person.setAttributes(Collections.singleton(personAttribute));
		
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueText("PREGNANT");
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$patient.getAttribute('Race').getValue() == 'Maasai'", 
				obs)
		);
	}

	@Test
	public void testObsValueMatch_shouldReturnFalseIfValueMismatch() {
		person.setId(1);

		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$fn.getLatestObs('CIEL:1234', $patient).getValueBoolean() == true",
				obs)
		);
	}

	@Test
	public void testTimeOfDay_shouldReturnTrueIfTimeOfDayMatches() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());

		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$fn.getCurrentHour() == " + LocalTime.now().getHourOfDay(),
				obs)
		);

		// Clean up: Reset time to system time
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void testTimeOfDay_shouldReturnFalseIfTimeOfDayDoesNotMatch() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());

		Obs obs = buildObs();
		obs.setPerson(person);
		
		assertFalse(
			conceptReferenceRangeUtility.evaluateCriteria(
				"$fn.getCurrentHour() == " + LocalTime.now().plusHours(1).getHourOfDay(),
				obs)
		);

		// Clean up: Reset time to system time
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void testRelevantObs_shouldReturnTrueIfCurrentObsHasValidNumericValue() {
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueNumeric(20.0);

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person),
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
				"$fn.getCurrentObs('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $obs).getValueNumeric() >= 20",
				obs)
		);
	}

	@Test
	public void testRelevantObs_shouldReturnTrueIfBMIIsInTheExpectedRange() {
		Obs obs = buildObs();
		obs.setPerson(person);
		
		Obs heightObservation = buildObs();
		heightObservation.setValueNumeric(170.0);

		Obs weightObservation = buildObs();
		weightObservation.setValueNumeric(70.0);

		Concept heightConcept = new Concept(5497);
		Concept weightConcept = new Concept(5089);

		Mockito.when(conceptService.getConceptByReference("a09ab2c5-878e-4905-b25d-5784167d0216")).thenReturn(heightConcept);
		Mockito.when(conceptService.getConceptByReference("c607c80f-1ea9-4da3-bb88-6276ce8868dd")).thenReturn(weightConcept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person),
				null,
				Collections.singletonList(heightConcept),
				null,
				null,
				null,
				Collections.singletonList("dateCreated"),
				1,
				null,
				null,
				null,
				false))
			.thenReturn(Collections.singletonList(heightObservation));

		Mockito.when(obsService.getObservations(Collections.singletonList(person),
				null,
				Collections.singletonList(weightConcept),
				null,
				null,
				null,
				Collections.singletonList("dateCreated"),
				1,
				null,
				null,
				null,
				false))
			.thenReturn(Collections.singletonList(weightObservation));

		assertTrue(
			conceptReferenceRangeUtility.evaluateCriteria(
				"($fn.getCurrentObs('c607c80f-1ea9-4da3-bb88-6276ce8868dd', $obs).getValueNumeric() " +
					"/ ( ($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100) * " +
					"($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100))) >= 18.5 && " +
					"($fn.getCurrentObs('c607c80f-1ea9-4da3-bb88-6276ce8868dd', $obs).getValueNumeric() / " +
					"( ($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100) * " +
					"($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100))) < 25",
				obs)
		);
	}
	
	private Obs buildObs() {
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

