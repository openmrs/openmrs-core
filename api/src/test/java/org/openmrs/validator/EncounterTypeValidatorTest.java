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

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link EncounterTypeValidator} class.
 */
public class EncounterTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see EncounterTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNullOrEmptyOrWhitespace() {
		EncounterType type = new EncounterType();
		type.setName(null);
		type.setDescription("Aaaaah");
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName("");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
		type.setName(" ");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see EncounterTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfDescriptionIsNullOrEmptyOrWhitespace() {
		EncounterType type = new EncounterType();
		type.setName("CLOSE");
		type.setDescription(null);
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription("");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
		
		type.setDescription(" ");
		errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		Assert.assertFalse(errors.hasFieldErrors("description"));
	}
	
	/**
	 * @see EncounterTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		EncounterType type = new EncounterType();
		type.setName("CLOSE");
		type.setDescription("Aaaaah");
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see EncounterTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationWhenEditingAnExistingEncounterType() {
		EncounterType type = Context.getEncounterService().getEncounterType("Scheduled");
		Assert.assertNotNull(type);
		
		Errors errors = new BindException(type, "encounterType");
		new EncounterTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see EncounterTypeValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailIfEncounterTypeNameIsDuplicate() {
		
		Assert.assertNotNull(Context.getEncounterService().getEncounterType("Scheduled"));
		
		EncounterType newEncounterType = new EncounterType();
		newEncounterType.setName("Scheduled");
		Errors errors = new BindException(newEncounterType, "encounterType");
		new EncounterTypeValidator().validate(newEncounterType, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		
	}
	
	/**
	 * @see EncounterTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		EncounterType type = new EncounterType();
		type.setName("name");
		type.setDescription("some descriptin not exceeding the limit");
		type.setRetireReason("retireReason");
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see EncounterTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		EncounterType type = new EncounterType();
		type.setName(StringUtils.repeat("longer than 50 chars", 6));
		type.setDescription(StringUtils.repeat("longer than 1024 chars", 120));
		type.setRetireReason(StringUtils.repeat("longer than 255 chars", 27));
		
		Errors errors = new BindException(type, "type");
		new EncounterTypeValidator().validate(type, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
