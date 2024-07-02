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
import org.openmrs.RelationshipType;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 *Tests methods on the {@link RelationshipTypeValidator} class.
 *
 * @since 1.10
 */
public class RelationshipTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfaIsToBIsNullOrEmptyOrWhitespace() {
		RelationshipType type = new RelationshipType();
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("aIsToB"));
		assertThat(errors.getFieldErrors("aIsToB").get(0).getCode(), is("RelationshipType.aIsToB.required"));
		
		type.setaIsToB("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("aIsToB"));
		assertThat(errors.getFieldErrors("aIsToB").get(0).getCode(), is("RelationshipType.aIsToB.required"));
		
		type.setaIsToB(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("aIsToB"));
		assertThat(errors.getFieldErrors("aIsToB").get(0).getCode(), is("RelationshipType.aIsToB.required"));
	}
	
	/**
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfbIsToAIsNullOrEmptyOrWhitespace() {
		RelationshipType type = new RelationshipType();
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("bIsToA"));
		assertThat(errors.getFieldErrors("bIsToA").get(0).getCode(), is("RelationshipType.bIsToA.required"));
		
		type.setbIsToA("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("bIsToA"));
		assertThat(errors.getFieldErrors("bIsToA").get(0).getCode(), is("RelationshipType.bIsToA.required"));
		
		type.setbIsToA(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("bIsToA"));
		assertThat(errors.getFieldErrors("bIsToA").get(0).getCode(), is("RelationshipType.bIsToA.required"));
	}
	
	/**
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		RelationshipType type = new RelationshipType();
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("RelationshipType.description.required"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("RelationshipType.description.required"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("RelationshipType.description.required"));
	}
	
	/**
	 * Test for all the field being set to some values
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsAreSet() {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("A is To B");
		type.setbIsToA("B is To A");
		type.setDescription("Description");
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see org.openmrs.validator.RelationshipTypeValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldPassEditingEncounterTypeName() {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("Doctor");
		type.setbIsToA("Patient");
		
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * Test for all the field being set to some values
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		RelationshipType type = new RelationshipType();
		type.setaIsToB("A is To B");
		type.setbIsToA("B is To A");
		type.setDescription("description");
		type.setRetireReason("retireReason");
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * Test for all the field being set to some values
	 * @see RelationshipTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		RelationshipType type = new RelationshipType();
		type
		        .setaIsToB("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setbIsToA("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		type
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(type, "type");
		new RelationshipTypeValidator().validate(type, errors);
		assertTrue(errors.hasFieldErrors("aIsToB"));
		assertThat(errors.getFieldErrors("aIsToB").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("bIsToA"));
		assertThat(errors.getFieldErrors("bIsToA").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("description"));
		assertThat(errors.getFieldErrors("description").get(0).getCode(), is("error.exceededMaxLengthOfField"));
		
		assertTrue(errors.hasFieldErrors("retireReason"));
		assertThat(errors.getFieldErrors("retireReason").get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
