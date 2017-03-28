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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitAttributeType;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;

/**
 * Tests {@link BaseAttributeTypeValidator}.
 */
public class BaseAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	private CustomVisitAttributeTypeValidator validator;
	
	private VisitAttributeType attributeType;
	
	private BindException errors;
	
	@Before
	public void before() {
		validator = new CustomVisitAttributeTypeValidator();
		attributeType = new VisitAttributeType();
		errors = new BindException(attributeType, "attributeType");
	}
	
	/**
	 * Needed so we can test the abstract {@link BaseAttributeTypeValidator} without interference of
	 * an existing concrete implementation like a VisitAttributeTypeValidator.
	 */
	private class CustomVisitAttributeTypeValidator extends BaseAttributeTypeValidator<VisitAttributeType> {
		
		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(VisitAttributeType.class);
		}
	}
	
	@Test
	public void shouldFailIfGivenNull() {
		
		validator.validate(null, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("error.general", errors.getAllErrors().get(0).getCode());
	}
	
	@Test
	public void shouldFailIfNameIsNull() {
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}
	
	@Test
	public void shouldFailIfNameIsEmpty() {
		
		attributeType.setName("");
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}

	@Test
	public void shouldFailIfNameIsOnlyWhitespaces() {
		
		attributeType.setName("  ");
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		assertThat(errors.getFieldErrors("name").get(0).getCode(), is("error.name"));
	}
	
	@Test
	public void validate_shouldRequireMinOccurs() {
		
		attributeType.setMinOccurs(null);
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("minOccurs"));
		assertThat(errors.getFieldErrors("minOccurs").get(0).getCode(), is("error.null"));
	}
	
	@Test
	public void shouldFailIfMinOccursIsLessThanZero() {
		
		attributeType.setMinOccurs(-1);
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("minOccurs"));
		assertThat(errors.getFieldErrors("minOccurs").get(0).getCode(),
		    is("AttributeType.minOccursShouldNotBeLessThanZero"));
	}
	
	@Test
	public void validate_shouldNotAllowMaxOccursLessThan1() {
		
		attributeType.setMaxOccurs(0);
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("maxOccurs"));
		assertThat(errors.getFieldErrors("maxOccurs").get(0).getCode(), is("AttributeType.maxOccursShouldNotBeLessThanOne"));
	}
	
	@Test
	public void validate_shouldNotAllowMaxOccursLessThanMinOccurs() {
		
		attributeType.setMinOccurs(3);
		attributeType.setMaxOccurs(2);
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("maxOccurs"));
		assertThat(errors.getFieldErrors("maxOccurs").get(0).getCode(),
		    is("AttributeType.maxOccursShouldNotBeLessThanMinOccurs"));
	}
	
	@Test
	public void validate_shouldRequireDatatypeClassname() {
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("datatypeClassname"));
		assertThat(errors.getFieldErrors("datatypeClassname").get(0).getCode(), is("error.null"));
	}
	
	@Test
	public void shouldFailIfDatatypeConfigurationIsBlankIfDatatypeEqualsRegexValidatedText() {
		
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("");
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("datatypeConfig"));
		assertThat(errors.getFieldErrors("datatypeConfig").get(0).getCode(), is("error.null"));
	}
	
	@Test
	public void shouldFailIfDatatypeConfigurationIsInvalidIfDatatypeEqualsRegexValidatedText() {
		
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig(null);
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("datatypeConfig"));
		assertThat(errors.getFieldErrors("datatypeConfig").get(0).getCode(), is("AttributeType.datatypeConfig.invalid"));
	}

	@Test
	public void shouldFailIfPreferredHandlerClassIsOfWrongDatatype() {
		
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("some valid config");
		attributeType.setPreferredHandlerClassname("org.openmrs.attribute.handler.DateDatatypeHandler");
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("preferredHandlerClassname"));
		assertThat(errors.getFieldErrors("preferredHandlerClassname").get(0).getCode(),
		    is("AttributeType.preferredHandlerClassname.wrongDatatype"));
	}
	
	@Test
	public void shouldFailIfPreferredHandlerClassIsInvalid() {
		
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("some valid config");
		attributeType.setPreferredHandlerClassname("uncompatible class");
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("handlerConfig"));
		assertThat(errors.getFieldErrors("handlerConfig").get(0).getCode(),
		    is("AttributeType.handlerConfig.invalid"));
	}
	
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		
		attributeType.setName("name");
		attributeType.setMinOccurs(1);
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig(new String(new char[66000]));
		attributeType.setHandlerConfig(new String(new char[66000]));
		
		validator.validate(attributeType, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("datatypeConfig"));
		Assert.assertTrue(errors.hasFieldErrors("handlerConfig"));
	}
	
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		
		attributeType.setName("name");
		attributeType.setMinOccurs(1);
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("[a-z]+");
		attributeType.setHandlerConfig("HandlerConfig");
		
		validator.validate(attributeType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassValidationIfAllRequiredValuesAreSet() {
		
		attributeType.setName("name");
		attributeType.setMinOccurs(1);
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("[a-z]+");
		
		validator.validate(attributeType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
