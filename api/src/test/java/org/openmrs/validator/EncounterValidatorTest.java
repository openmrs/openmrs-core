/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains methods for testing {@link EncounterValidator#validate(Object, Errors)}
 */
public class EncounterValidatorTest extends BaseContextSensitiveTest {
	
	
	private EncounterValidator encounterValidator;
	
	private Encounter encounter;
	
	private Errors errors;
	
	@BeforeEach
	public void setUp() {
		encounterValidator = new EncounterValidator();
		
		encounter = new Encounter();
		
		errors = new BindException(encounter, "encounter");
	}
	
	@Test
	public void shouldFailIfGivenNull() { 
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> encounterValidator.validate(null, errors));
		assertThat(exception.getMessage(), is("The parameter obj should not be null and must be of type " + Encounter.class));
	}
	
	@Test
	public void shouldFailIfGivenInstanceOfOtherClassThanEncounter() { 
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> encounterValidator.validate(new Patient(), errors));
		assertThat(exception.getMessage(), is("The parameter obj should not be null and must be of type " + Encounter.class));
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfThePatientsForTheVisitAndTheEncounterDontMatch() {
		
		encounter.setPatient(new Patient(2));
		Visit visit = new Visit();
		visit.setPatient(new Patient(3));
		encounter.setVisit(visit);
		
		encounterValidator.validate(encounter, errors);
		
		assertEquals("Encounter.visit.patients.dontMatch", errors.getFieldError("visit").getCode());
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheVisitHasNoPatient() {
		
		encounter.setPatient(new Patient(2));
		Visit visit = new Visit();
		encounter.setVisit(visit);
		
		encounterValidator.validate(encounter, errors);
		
		assertEquals("Encounter.visit.patients.dontMatch", errors.getFieldError("visit").getCode());
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientIsNotSet() {
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterDateTimeIsBeforeVisitStartDateTime() {
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		Visit visit = Context.getVisitService().getVisit(1);
		visit.setPatient(encounter.getPatient());
		encounter.setVisit(visit);
		//Set encounter dateTime to before the visit startDateTime.
		Date date = new Date(visit.getStartDatetime().getTime() - 1);
		encounter.setEncounterDatetime(date);
		errors = new BindException(encounter, "encounter");
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterDateTimeIsAfterVisitStopDateTime() {
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		Visit visit = Context.getVisitService().getVisit(1);
		visit.setPatient(encounter.getPatient());
		encounter.setVisit(visit);
		//Set encounter dateTime to after the visit stopDateTime.
		visit.setStopDatetime(new Date());
		Date date = new Date(visit.getStopDatetime().getTime() + 1);
		encounter.setEncounterDatetime(date);
		errors = new BindException(encounter, "encounter");
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterDateTimeIsAfterCurrentDateTime() {
		
		Encounter encounter = Context.getEncounterService().getEncounter(3);
		//Set encounter dateTime after the current dateTime.
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date tomorrowDate = calendar.getTime();
		encounter.setEncounterDatetime(tomorrowDate);
		errors = new BindException(encounter, "encounter");
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterDateTimeIsNotSet() {
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterTypeIsNotSet() {
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("encounterType"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		
		encounter.setEncounterType(new EncounterType());
		encounter.setPatient(new Patient());
		encounter.setEncounterDatetime(new Date());
		encounter.setVoidReason("voidReason");
		
		encounterValidator.validate(encounter, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		
		encounter.setEncounterType(new EncounterType());
		encounter.setPatient(new Patient());
		encounter.setEncounterDatetime(new Date());
		encounter
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		encounterValidator.validate(encounter, errors);
		
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
