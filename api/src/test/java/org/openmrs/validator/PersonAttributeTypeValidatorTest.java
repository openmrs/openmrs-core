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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PersonAttributeTypeValidator} class.
 */
public class PersonAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNull() {
		PersonAttributeType type = new PersonAttributeType();
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("PersonAttributeType.error.nameEmpty"));
	}
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameAlreadyInUse() {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("Birthplace");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("PersonAttributeType.error.nameAlreadyInUse"));
	}
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorreect() {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("Zodiac");
		type.setFormat("java.lang.String");
		type.setDescription("Zodiac Description");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFormatIsEmpty() {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("Zodiac");
		type.setDescription("Zodiac Description");
		type.setFormat("");
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("format"));
		assertThat(errors.getFieldErrors("format").get(0).getCode(), is("PersonAttributeType.error.formatEmpty"));
	}
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("name");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new PersonAttributeTypeValidator().validate(type, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new PersonAttributeTypeValidator().validate(type, errors);
		assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new PersonAttributeTypeValidator().validate(type, errors);
		assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("name");
		type.setFormat("java.lang.String");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see PersonAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		PersonAttributeType type = new PersonAttributeType();
		type
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setFormat("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("format"));
		assertThat(errors.getFieldErrors("format").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
