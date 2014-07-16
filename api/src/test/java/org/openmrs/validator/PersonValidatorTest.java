/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
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
	 * @verifies fail validation if birthdate is a future date
	 */
	
	@Test
	@Verifies(value = "should fail validation if birthdate is a future date", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfBirthdateIsAFutureDate() throws Exception {
		Patient pa = new Patient(1);
		Calendar birth = Calendar.getInstance();
		birth.setTime(new Date());
		birth.add(Calendar.YEAR, 20);
		pa.setBirthdate(birth.getTime());
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("birthdate"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 * @verifies fail validation if birthdate makes patient older that 120 years old
	 */
	@Test
	@Verifies(value = "should fail validation if birthdate makes patient older that 120 years old", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfBirthdateMakesPatientOlderThat120YearsOld() throws Exception {
		Patient pa = new Patient(1);
		Calendar birth = Calendar.getInstance();
		birth.setTime(new Date());
		birth.add(Calendar.YEAR, -125);
		pa.setBirthdate(birth.getTime());
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("birthdate"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 * @verifies fail validation if causeOfDeath is blank when patient is dead
	 */
	@Test
	@Verifies(value = "should fail validation if causeOfDeath is blank when patient is dead", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfCauseOfDeathIsBlankWhenPatientIsDead() throws Exception {
		Patient pa = new Patient(1);
		pa.setDead(true);
		
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("causeOfDeath"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 * @verifies fail validation if voidReason is blank when patient is voided
	 */
	@Test
	@Verifies(value = "should fail validation if voidReason is blank when patient is voided", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfVoidReasonIsBlankWhenPatientIsVoided() throws Exception {
		Patient pa = Context.getPatientService().getPatient(2);
		pa.setVoided(true);
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see PersonValidator#validate(Object,Errors)
	 * @verifies fail validation if person does not have at least one non voided name
	 */
	@Test
	@Verifies(value = "should fail validation if person does not have at least one non voided name", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPersonDoesNotHaveAtleastOneNonVoidedName() throws Exception {
		Patient pa = Context.getPatientService().getPatient(2);
		pa.getNames().clear();
		Errors errors = new BindException(pa, "patient");
		validator.validate(pa, errors);
		Assert.assertTrue(errors.hasFieldErrors("names"));
	}
	
	/**
	 * @see {@link org.openmrs.validator.PersonValidator#validate(Object,Errors)}
	 * @verifies pass validation if gender is blank for Persons
	 */
	@Test
	@Verifies(value = "should pass validation if gender is blank for Persons", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfGenderIsBlankForPersons() throws Exception {
		Person person = new Person(1);
		Errors errors = new BindException(person, "person");
		PersonValidator personValidator = new PersonValidator();
		personValidator.validate(person, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("gender"));
	}
	
}
