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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PatientIdentifierTypeValidator} class.
 */
public class PatientIdentifierTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PatientIdentifierTypeValidator#validate(Object,Errors)
	 * 
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName(null);
		type.setDescription("some text");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}
	
	/**
	 * @see PatientIdentifierTypeValidator#validate(Object,Errors)
	 * 
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("name");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see PatientIdentifierTypeValidator#validate(Object,Errors)
	 * 
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("restraining");
		type.setDescription(":(");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see	PatientIdentifierTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfRegExFieldLengthIsNotTooLong() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("Martin");
		type.setDescription("helps");
		String valid50charInput = "12345678901234567890123456789012345678901234567890";
		type.setFormat(valid50charInput);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see	PatientIdentifierTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRegExFieldLengthIsTooLong() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("Martin");
		type.setDescription("helps");
		String invalid255charInput = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456";
		type.setFormat(invalid255charInput);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasErrors());
		assertThat(errors.getAllErrors().get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertEquals(1, errors.getFieldErrorCount("format"));
	}
	
	/**
	 * @see	PatientIdentifierTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameFieldLengthIsTooLong() {
		PatientIdentifierType type = new PatientIdentifierType();
		String invalid51charInput = "123456789012345678901234567890123456789012345678901";
		type.setName(invalid51charInput);
		type.setDescription("helps");
		type.setFormat("format");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasErrors());
		assertThat(errors.getAllErrors().get(0).getCode(), is("error.exceededMaxLengthOfField"));
		assertEquals(1, errors.getFieldErrorCount("name"));
	}
	
	/**
	 * @see org.openmrs.validator.PatientIdentifierTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPatientIdentifierTypeNameAlreadyExist() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("OpenMRS Identification Number");
		type.setDescription("helps");
		String valid50charInput = "12345678901234567890123456789012345678901234567890";
		type.setFormat(valid50charInput);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasErrors());
		assertThat(errors.getAllErrors().get(0).getCode(), is("identifierType.duplicate.name"));
	}
	
	/**
	 * @see PatientIdentifierTypeValidator#validate(Object,Errors)
	 *
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("name");
		type.setFormat("format");
		type.setFormatDescription("formatDescription");
		type.setValidator("validator");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PatientIdentifierTypeValidator#validate(Object,Errors)
	 *
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		PatientIdentifierType type = new PatientIdentifierType();
		type
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setFormat("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setFormatDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setValidator("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("format"));
		assertThat(errors.getFieldErrors("format").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("formatDescription"));
		assertThat(errors.getFieldErrors("formatDescription").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("validator"));
		assertThat(errors.getFieldErrors("validator").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
