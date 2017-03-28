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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains methods for testing {@link EncounterValidator#validate(Object, Errors)}
 */
public class EncounterValidatorTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private EncounterValidator encounterValidator;
	
	private Encounter encounter;
	
	private Errors errors;
	
	@Before
	public void setUp() {
		encounterValidator = new EncounterValidator();
		
		encounter = new Encounter();
		
		errors = new BindException(encounter, "encounter");
	}
	
	@Test
	public void shouldFailIfGivenNull() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("The parameter obj should not be null and must be of type " + Encounter.class);
		encounterValidator.validate(null, errors);
	}
	
	@Test
	public void shouldFailIfGivenInstanceOfOtherClassThanEncounter() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("The parameter obj should not be null and must be of type " + Encounter.class);
		encounterValidator.validate(new Patient(), errors);
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
		
		Assert.assertEquals("Encounter.visit.patients.dontMatch", errors.getFieldError("visit").getCode());
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
		
		Assert.assertEquals("Encounter.visit.patients.dontMatch", errors.getFieldError("visit").getCode());
	}
	
	/**
	 * @see EncounterValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientIsNotSet() {
		
		encounterValidator.validate(encounter, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("patient"));
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
		
		Assert.assertEquals(true, errors.hasFieldErrors("encounterDatetime"));
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
		
		Assert.assertEquals(true, errors.hasFieldErrors("encounterDatetime"));
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
		
		Assert.assertEquals(true, errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterDateTimeIsNotSet() {
		
		encounterValidator.validate(encounter, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("encounterDatetime"));
	}
	
	/**
	 * @see EncounterValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterTypeIsNotSet() {
		
		encounterValidator.validate(encounter, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("encounterType"));
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
		
		Assert.assertFalse(errors.hasErrors());
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
		
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
