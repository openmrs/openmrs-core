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
import org.openmrs.User;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link UserValidator} class.
 */
public class UserValidatorTest {
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should validate username with only alpha numerics", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithOnlyAlphaNumerics() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertTrue(userValidator.isUserNameValid("AB"));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should validate username with alpha dash and underscore", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithAlphaDashAndUnderscore() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertTrue(userValidator.isUserNameValid("A-_."));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should validate username with alpha dash underscore and dot", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithAlphaDashUnderscoreAndDot() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertTrue(userValidator.isUserNameValid("A-_.B"));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should validate username with exactly max size name", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithExactlyMaxSizeName() throws Exception {
		UserValidator userValidator = new UserValidator();
		String username = "12345678901234567890123456789012345678901234567890";
		Assert.assertEquals(50, username.length());
		
		Assert.assertTrue(userValidator.isUserNameValid(username));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should not validate username with less than minimumLength", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateUsernameWithLessThanMinimumLength() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertFalse(userValidator.isUserNameValid("A"));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should not validate username with invalid character", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateUsernameWithInvalidCharacter() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertFalse(userValidator.isUserNameValid("A*"));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should not validate username with more than maximum size", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateUsernameWithMoreThanMaximumSize() throws Exception {
		UserValidator userValidator = new UserValidator();
		String username = "12345678901234567890123456789012345678901AAAAABBBAABABABABA";
		Assert.assertTrue(username.length() > 50);
		Assert.assertFalse(userValidator.isUserNameValid(username));
	}	
	
	/**
	 * @see {@link UserValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if voided and voidReason is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfVoidedAndVoidReasonIsNullOrEmptyOrWhitespace() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setVoidReason(null);
		user.setVoided(true);
		
		Errors errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
		
		user.setVoidReason("");
		errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
		
		user.setVoidReason(" ");
		errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see {@link UserValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setVoided(true);
		user.setVoidReason("for the lulz");
		
		Errors errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
}
