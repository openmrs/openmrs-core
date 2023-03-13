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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.ConceptAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptAttributeTypeValidator} class.
 */
public class ConceptAttributeTypeValidatorTest extends BaseContextSensitiveTest {

	protected static final String CONCEPT_ATTRIBUTE_TYPE_XML = "org/openmrs/api/include/ConceptServiceTest-conceptAttributeType.xml";
	
	private ConceptAttributeTypeValidator validator;

	private ConceptAttributeType type;
	
	private Errors errors;
	
	/**
	 * Run this before each unit test in this class. This adds a bit more data to the base data that
	 * is done in the "@Before" method in {@link BaseContextSensitiveTest} (which is run right
	 * before this method).
	 *
	 * @throws Exception
	 */
	@BeforeEach
	public void setUp() {
		executeDataSet(CONCEPT_ATTRIBUTE_TYPE_XML);
		
		validator = new ConceptAttributeTypeValidator();
		type = new ConceptAttributeType();
		errors = new BindException(type, "type");
	}
	
	@Test
	public void shouldFailValidationIfNameIsNull() {
		
		type.setName(null);
		type.setDescription("description");
		
		validator.validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}
	
	@Test
	public void validate_shouldFailValidationIfNameIsEmpty() {
		
		type.setName("");
		type.setDescription("description");
		
		validator.validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}
	
	@Test
	public void shouldFailValidationIfNameIsOnlyWhitespace() {
		
		type.setName(" ");
		type.setDescription("description");
		
		validator.validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptAttributeTypeNameIsDuplicate() {
		
		assertNotNull(Context.getConceptService().getConceptAttributeTypeByName("Audit Date"));
		type.setName("Audit Date");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		validator.validate(type, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("ConceptAttributeType.error.nameAlreadyInUse"));
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldPassEditingConceptAttributeTypeName() {
		
		ConceptAttributeType et = Context.getConceptService().getConceptAttributeTypeByName("Audit Date");
		assertNotNull(et);
		Errors errors = new BindException(et, "conceptAttributeType");
		
		validator.validate(et, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		
		final String stringOf256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		final String stringOf1025 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		type.setName(stringOf256);
		type.setDatatypeClassname(stringOf256);
		type.setDescription(stringOf1025);
		type.setPreferredHandlerClassname(stringOf256);
		type.setRetireReason(stringOf256);
		
		validator.validate(type, errors);
		
		List<String> errorFields = Arrays.asList("name", "datatypeClassname", "description", "preferredHandlerClassname",
		    "retireReason");
		errorFields.forEach(this::assertThatFieldExceedsMaxLength);
	}
	
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		
		type.setName("name");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		type.setDescription("description");
		type.setRetireReason("retireReason");
		
		validator.validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		
		type.setName("name");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		validator.validate(type, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	private void assertThatFieldExceedsMaxLength(String field) {
		assertTrue(errors.hasFieldErrors(field), String.format("Field '%s' has error(s)", field));
		assertThat(errors.getFieldErrors(field).get(0).getCode(), is("error.exceededMaxLengthOfField"));
	}
}
