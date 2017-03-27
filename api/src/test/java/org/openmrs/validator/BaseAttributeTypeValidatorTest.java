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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitAttributeType;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class BaseAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	VisitAttributeTypeValidator validator;
	
	VisitAttributeType attributeType;
	
	BindException errors;
	
	@Before
	public void before() {
		validator = new VisitAttributeTypeValidator();
		attributeType = new VisitAttributeType();
		errors = new BindException(attributeType, "attributeType");
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldNotAllowMaxOccursLessThan1() {
		attributeType.setMaxOccurs(0);
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("maxOccurs").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldNotAllowMaxOccursLessThanMinOccurs() {
		attributeType.setMinOccurs(3);
		attributeType.setMaxOccurs(2);
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("maxOccurs").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRequireDatatypeClassname() {
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("datatypeClassname").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRequireMinOccurs() {
		attributeType.setMinOccurs(null);
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("minOccurs").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRequireName() {
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("name").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRequireDatatypeConfigurationIfDatatypeEqualsRegexValidatedText() {
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("datatypeConfig").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredValuesAreSet() {
		attributeType.setName("name");
		attributeType.setMinOccurs(1);
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("[a-z]+");
		validator.validate(attributeType, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
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
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 */
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
}
