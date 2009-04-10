/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link FormValidator} class.
 */
public class FormValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link FormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfNameIsNull() throws Exception {
		Form form = new Form();
		form.setVersion("1.0");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertFalse(errors.hasFieldErrors("version"));
	}
	
	/**
	 * @see {@link FormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if version is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfVersionIsNull() throws Exception {
		Form form = new Form();
		form.setName("test");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("version"));
	}
	
	/**
	 * @see {@link FormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if version does not match regex", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfVersionDoesNotMatchRegex() throws Exception {
		Form form = new Form();
		form.setName("test");
		form.setVersion("first");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("version"));
	}
	
	/**
	 * @see {@link FormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if retiredReason is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredReasonIsNull() throws Exception {
		Form form = new Form();
		form.setName("test");
		form.setVersion("1.0");
		form.setRetired(true);
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("name"));
		Assert.assertFalse(errors.hasFieldErrors("version"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see {@link FormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() throws Exception {
		Form form = new Form();
		form.setName("test");
		form.setVersion("1.0");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link FormValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if retiredReason is empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredReasonIsEmpty() throws Exception {
		Form form = new Form();
		form.setName("test");
		form.setVersion("1.0");
		form.setRetired(true);
		form.setRetireReason("");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
