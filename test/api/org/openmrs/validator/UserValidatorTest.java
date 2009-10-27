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
import org.openmrs.test.Verifies;

/**
 * Tests for {@link UserValidator}
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
	
}
