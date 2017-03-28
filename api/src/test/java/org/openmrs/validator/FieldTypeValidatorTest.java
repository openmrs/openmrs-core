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
import org.openmrs.FieldType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link FieldTypeValidator} class.
 */
public class FieldTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see FieldTypeValidator#validate(Object,Errors)
	 * 
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		FieldType type = new FieldType();
		type.setName(null);
		type.setDescription("Humba humba humba ...");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see FieldTypeValidator#validate(Object,Errors)
	 * 
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		FieldType type = new FieldType();
		type.setName("soccer");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see org.openmrs.validator.FieldTypeValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailIfFieldTypeNameIsDuplicate() {
		FieldType type = new FieldType();
		type.setName("some field type");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see FieldTypeValidator#validate(Object,Errors)
	 *
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		FieldType type = new FieldType();
		type.setName("soccer");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see FieldTypeValidator#validate(Object,Errors)
	 *
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		FieldType type = new FieldType();
		type.setName("too long text too long text too long text too long text");
		
		Errors errors = new BindException(type, "type");
		new FieldTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
}
