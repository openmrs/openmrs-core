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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PatientIdentifierTypeValidator} class.
 */
public class PatientIdentifierTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should fail validation if name is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName(null);
		type.setDescription("some text");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should pass validation if description is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("name");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 * 
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("restraining");
		type.setDescription(":(");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see	PatientIdentifierTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "Should pass validation if regEx field length is not too long", method = "validate(Object, org.springframework.validation.Errors)")
	public void validate_shouldPassValidationIfRegExFieldLengthIsNotTooLong() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("Martin");
		type.setDescription("helps");
		String valid50charInput = "12345678901234567890123456789012345678901234567890";
		type.setFormat(valid50charInput);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see	PatientIdentifierTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "Should fail validation if regEx field length is too long", method = "validate(Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfRegExFieldLengthIsTooLong() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("Martin");
		type.setDescription("helps");
		String invalid255charInput = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456";
		type.setFormat(invalid255charInput);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals(1, errors.getFieldErrorCount("format"));
	}
	
	/**
	 * @see	PatientIdentifierTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "Should fail validation if name field length is too long", method = "validate(Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfNameFieldLengthIsTooLong() {
		PatientIdentifierType type = new PatientIdentifierType();
		String invalid51charInput = "123456789012345678901234567890123456789012345678901";
		type.setName(invalid51charInput);
		type.setDescription("helps");
		type.setFormat("format");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals(1, errors.getFieldErrorCount("name"));
	}
	
	/**
	 * @see {@link org.openmrs.validator.PatientIdentifierTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if patient identifier type name is already exist", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfPatientIdentifierTypeNameAlreadyExist() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("OpenMRS Identification Number");
		type.setDescription("helps");
		String valid50charInput = "12345678901234567890123456789012345678901234567890";
		type.setFormat(valid50charInput);
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 *
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName("name");
		type.setFormat("format");
		type.setFormatDescription("formatDescription");
		type.setValidator("validator");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "type");
		new PatientIdentifierTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientIdentifierTypeValidator#validate(Object,Errors)}
	 *
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
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
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("format"));
		Assert.assertTrue(errors.hasFieldErrors("formatDescription"));
		Assert.assertTrue(errors.hasFieldErrors("validator"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
