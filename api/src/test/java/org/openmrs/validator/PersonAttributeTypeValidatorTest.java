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
import org.openmrs.PersonAttributeType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link PersonAttributeTypeValidator} class.
 */
public class PersonAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PersonAttributeTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNull() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link PersonAttributeTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name already in use", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameAlreadyInUse() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("Birthplace");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link PersonAttributeTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorreect() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("Zodiac");
		type.setDescription("Zodiac Description");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link PersonAttributeTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if description is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfDescriptionIsNull() throws Exception {
		PersonAttributeType type = new PersonAttributeType();
		type.setName("Zodiac");
		
		Errors errors = new BindException(type, "patObj");
		new PersonAttributeTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("description"));
	}
}
