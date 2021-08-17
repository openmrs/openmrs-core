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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PersonValidatorTest extends BaseContextSensitiveTest {
	
	protected Validator validator;
	
	/**
	 * @param validator the validator to set
	 */
	@Autowired
	@Qualifier("personValidator")
	public void setValidator(Validator validator) {
		this.validator = validator;
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 */
	
	@Test
	public void validate_shouldFailValidationIfBirthdateIsAFutureDate() {
		Patient pa = new Patient(1);
		Calendar birth = Calendar.getInstance();
		birth.setTime(new Date());
		birth.add(Calendar.YEAR, 20);
		pa.setBirthdate(birth.getTime());
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("birthdate"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 */
	
	@Test
	public void validate_shouldFailValidationIfDeathDateIsAFutureDate() {
		Patient pa = new Patient(1);
		Calendar death = Calendar.getInstance();
		death.setTime(new Date());
		death.add(Calendar.YEAR, 20);
		pa.setDeathDate(death.getTime());
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("deathDate"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfBirthdateMakesPatientOlderThat140YearsOld() {
		Patient pa = new Patient(1);
		Calendar birth = Calendar.getInstance();
		birth.setTime(new Date());
		birth.add(Calendar.YEAR, -145);
		pa.setBirthdate(birth.getTime());
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("birthdate"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfCauseOfDeathIsBlankWhenPatientIsDead() {
		Patient pa = new Patient(1);
		pa.setDead(true);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("causeOfDeath"));
		assertEquals("Person.dead.causeOfDeathAndCauseOfDeathNonCodedNull", errors.getFieldError("causeOfDeath").getCode());
		
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfVoidReasonIsBlankWhenPatientIsVoided() {
		Patient pa = Context.getPatientService().getPatient(2);
		pa.setVoided(true);
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonDoesNotHaveAtleastOneNonVoidedName() {
		Patient pa = Context.getPatientService().getPatient(2);
		pa.getNames().clear();
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		assertTrue(errors.hasFieldErrors("names"));
	}
	
	/**
	 * @see org.openmrs.validator.PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfGenderIsBlankForPersons() {
		Person person = new Person(1);
		Errors errors = new BindException(person, "person");
		PersonValidator personValidator = new PersonValidator();
		personValidator.validate(person, errors);
		
		assertFalse(errors.hasFieldErrors("gender"));
	}
	
	/**
	 * @see org.openmrs.validator.PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Person person = new Person(1);
		person.setBirthdate(new Date());
		person.setGender("g");
		person.setPersonVoided(true);
		person.setPersonVoidReason("voidReason");
		
		Errors errors = new BindException(person, "person");
		PersonValidator personValidator = new PersonValidator();
		personValidator.validate(person, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see org.openmrs.validator.PersonValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Person person = new Person(1);
		person.setBirthdate(new Date());
		person.setGender("too long text too long too long text too long text  too long text");
		person.setPersonVoided(true);
		person
		        .setPersonVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(person, "person");
		PersonValidator personValidator = new PersonValidator();
		personValidator.validate(person, errors);
		
		assertTrue(errors.hasFieldErrors("gender"));
		assertTrue(errors.hasFieldErrors("personVoidReason"));
	}


	/**
 	 * @see org.openmrs.validator.PersonValidator#validate(Object,Errors)
     */
    @Test
	public void shouldNotSetDeathBeforeBirth() {
		Patient pa = new Patient(1);
		Calendar birth = Calendar.getInstance();
		birth.setTime(new Date());
		birth.add(Calendar.YEAR, +5);
		pa.setBirthdate(birth.getTime());
  		Calendar death = Calendar.getInstance();
		death.setTime(new Date());
		pa.setDeathDate(death.getTime());
  
  		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("deathDate"));
	}

	@Test
	public void validate_shouldFailValidationWhenDeathCauseAndDeathCauseNonCodedAreSet() {
		Patient pa = new Patient(1);
		pa.setDead(true);
		
		pa.setCauseOfDeathNonCoded("Some text describing Cause of Death");
		pa.setCauseOfDeath(new Concept());
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		assertTrue(errors.hasFieldErrors("causeOfDeath"));
		assertEquals("Person.dead.shouldHaveOnlyOneCauseOfDeathOrCauseOfDeathNonCodedSet", errors.getFieldError("causeOfDeath").getCode());
	}
	
	@Test
	public void validate_shouldNotFailWhenDeathCauseNotCodedIsSet() {
		Person person = new Patient(1);
		
		person.setDead(true);
		person.setCauseOfDeathNonCoded("Some text describing Cause of Death");
		person.setPersonVoided(true);
		person.setPersonVoidReason("voidReason");
		person.setGender("g");
		
		Errors errors = new BindException(person, "patient");
		PersonValidator personValidator = new PersonValidator();
		personValidator.validate(person, errors);

		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldThrowExceptionWhenAddressIsNull() {
		Person person = new Patient(1);
		Set<PersonAddress> addresses =  new HashSet<>();
		addresses.add(null);
		
		person.setDead(true);
		person.setCauseOfDeathNonCoded("Some text describing Cause of Death");
		person.setPersonVoided(true);
		person.setPersonVoidReason("voidReason");
		person.setGender("g");
		person.setAddresses(addresses);
		
		Errors errors = new BindException(person, "patient");
		PersonValidator personValidator = new PersonValidator();
		
		assertThrows(IllegalArgumentException.class, () ->  personValidator.validate(person, errors));
	}

}
