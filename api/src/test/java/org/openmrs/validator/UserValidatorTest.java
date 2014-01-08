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
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link UserValidator} class.
 */
public class UserValidatorTest extends BaseContextSensitiveTest {
	
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
	@Verifies(value = "should fail validation if retired and retireReason is null or empty or whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredAndRetireReasonIsNullOrEmptyOrWhitespace() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setRetireReason(null);
		user.setRetired(true);
		
		Errors errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
		
		user.setRetireReason("");
		errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
		
		user.setRetireReason(" ");
		errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see {@link UserValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if all required fields have proper values", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setRetired(true);
		user.setRetireReason("for the lulz");
		user.setPerson(new Person(999));
		user.getPerson().addName(new PersonName("Users", "Need", "People"));
		user.getPerson().setGender("F");
		
		Errors errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should validate when username is null", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateWhenUsernameIsNull() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertTrue(userValidator.isUserNameValid(null));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should validate when username is the empty string", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateWhenUsernameIsTheEmptyString() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertTrue(userValidator.isUserNameValid(""));
	}
	
	/**
	 * @see {@link UserValidator#isUserNameValid(String)}
	 */
	@Test
	@Verifies(value = "should not validate when username is whitespace only", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateWhenUsernameIsWhitespaceOnly() throws Exception {
		UserValidator userValidator = new UserValidator();
		Assert.assertFalse(userValidator.isUserNameValid("  "));
	}
	
	/**
	 * @see UserValidator#isUserNameAsEmailValid(String)
	 * @verifies return false if email invalid
	 */
	@Test
	public void isUserNameAsEmailValid_shouldReturnFalseIfEmailInvalid() throws Exception {
		UserValidator userValidator = new UserValidator();
		String[] invalids = new String[] { "mkyong", "mkyong@.com.my", "mkyong123@gmail.a", "mkyong123@.com",
		        "mkyong123@.com.com", ".mkyong@mkyong.com", "mkyong()*@gmail.com", "mkyong@%*.com",
		        "mkyong..2002@gmail.com", "mkyong.@gmail.com", "mkyong@mkyong@gmail.com", "mkyong@gmail.com.1a" };
		for (String email : invalids) {
			Assert.assertFalse(userValidator.isUserNameAsEmailValid(email));
		}
	}
	
	/**
	 * @see UserValidator#isUserNameAsEmailValid(String)
	 * @verifies return true if email valid
	 */
	@Test
	public void isUserNameAsEmailValid_shouldReturnTrueIfEmailValid() throws Exception {
		UserValidator userValidator = new UserValidator();
		String[] valids = new String[] { "mkyong@yahoo.com", "mkyong-100@yahoo.com", "mkyong.100@yahoo.com",
		        "mkyong111@mkyong.com", "mkyong-100@mkyong.net", "mkyong.100@mkyong.com.au", "mkyong@1.com",
		        "mkyong@gmail.com.com" };
		for (String email : valids) {
			Assert.assertTrue(userValidator.isUserNameAsEmailValid(email));
		}
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 * @verifies fail validation if email as username enabled and email invalid
	 */
	@Test
	public void validate_shouldFailValidationIfEmailAsUsernameEnabledAndEmailInvalid() throws Exception {
		User user = new User();
		user.setUsername("test@example.com");
		
		AdministrationService as = Context.getAdministrationService();
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME, "true"));
		
		Errors errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("username"));
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 * @verifies fail validation if email as username disabled and email provided
	 */
	@Test
	public void validate_shouldFailValidationIfEmailAsUsernameDisabledAndEmailProvided() throws Exception {
		User user = new User();
		user.setUsername("test@example.com");
		
		AdministrationService as = Context.getAdministrationService();
		as.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_USER_REQUIRE_EMAIL_AS_USERNAME, "false"));
		
		Errors errors = new BindException(user, "user");
		new UserValidator().validate(user, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("username"));
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "not throw NPE when user is null", method = "validate(Object,Errors)")
	public void validate_shouldNotThrowNPEWhenUserIsNull() throws Exception {
		UserValidator userValidator = new UserValidator();
		Errors errors = new BindException(new User(), "user");
		userValidator.validate(null, errors);
		Assert.assertTrue(true);
	}
}
