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
import org.openmrs.Form;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link FormValidator} class.
 */
public class FormValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfNameIsNull() {
		Form form = new Form();
		form.setVersion("1.0");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertFalse(errors.hasFieldErrors("version"));
	}
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfVersionIsNull() {
		Form form = new Form();
		form.setName("test");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("version"));
	}
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfVersionDoesNotMatchRegex() {
		Form form = new Form();
		form.setName("test");
		form.setVersion("first");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("version"));
	}
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRetiredReasonIsNull() {
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
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllFieldsAreCorrect() {
		Form form = new Form();
		form.setName("test");
		form.setVersion("1.0");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfRetiredReasonIsEmpty() {
		Form form = new Form();
		form.setName("test");
		form.setVersion("1.0");
		form.setRetired(true);
		form.setRetireReason("");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Form form = new Form();
		form.setName("name");
		form.setVersion("1.0");
		form.setDescription("description");
		form.setRetireReason("retireReason");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see FormValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Form form = new Form();
		form
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		form.setVersion("1111111111111111111111111111111111111111111111111111");
		form
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		form
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(form, "form");
		new FormValidator().validate(form, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("version"));
		Assert.assertTrue(errors.hasFieldErrors("description"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
