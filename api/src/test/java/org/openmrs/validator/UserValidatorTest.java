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
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link UserValidator} class.
 */
public class UserValidatorTest extends BaseContextSensitiveTest {
	
	private static final String STRING_WITH_LENGTH_GREATER_THAN_50 = "too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text";

	@Autowired
	private UserValidator validator;
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should validate username with only alpha numerics", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithOnlyAlphaNumerics() throws Exception {
		Assert.assertTrue(validator.isUserNameValid("AB"));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should validate username with alpha dash and underscore", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithAlphaDashAndUnderscore() throws Exception {
		Assert.assertTrue(validator.isUserNameValid("A-_."));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should validate username with alpha dash underscore and dot", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithAlphaDashUnderscoreAndDot() throws Exception {
		Assert.assertTrue(validator.isUserNameValid("A-_.B"));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should validate username with exactly max size name", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateUsernameWithExactlyMaxSizeName() throws Exception {
		String username = "12345678901234567890123456789012345678901234567890";
		Assert.assertEquals(50, username.length());
		
		Assert.assertTrue(validator.isUserNameValid(username));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should not validate username with less than minimumLength", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateUsernameWithLessThanMinimumLength() throws Exception {
		Assert.assertFalse(validator.isUserNameValid("A"));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should not validate username with invalid character", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateUsernameWithInvalidCharacter() throws Exception {
		Assert.assertFalse(validator.isUserNameValid("A*"));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should not validate username with more than maximum size", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateUsernameWithMoreThanMaximumSize() throws Exception {
		String username = "12345678901234567890123456789012345678901AAAAABBBAABABABABA";
		Assert.assertTrue(username.length() > 50);
		Assert.assertFalse(validator.isUserNameValid(username));
	}

	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should fail validation if retired and retireReason is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredAndRetireReasonIsNull() throws Exception {
		String retireReason = null;
		invokeValidateAndAssertHasErrorRetireReason(retireReason);
	}

	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should fail validation if retired and retireReason is empty", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredAndRetireReasonIsEmpty() throws Exception {
		String retireReason = "";
		invokeValidateAndAssertHasErrorRetireReason(retireReason);
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "should fail validation if retired and retireReason is whitespace", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfRetiredAndRetireReasonIsWhitespace() throws Exception {
		String retireReason = "   ";
		invokeValidateAndAssertHasErrorRetireReason(retireReason);
	}
	
	private void invokeValidateAndAssertHasErrorRetireReason(String invalidRetireReason) {
		User user = new User();
		user.setUsername("test");
		user.setRetireReason(invalidRetireReason);
		user.setRetired(true);
		
		Errors errors = new BindException(user, "user");
		validator.validate(user, errors);
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}

	/**
	 * @see UserValidator#validate(Object,Errors)
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
		validator.validate(user, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should validate when username is null", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateWhenUsernameIsNull() throws Exception {
		Assert.assertTrue(validator.isUserNameValid(null));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should validate when username is the empty string", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldValidateWhenUsernameIsTheEmptyString() throws Exception {
		Assert.assertTrue(validator.isUserNameValid(""));
	}
	
	/**
	 * @see UserValidator#isUserNameValid(String)
	 */
	@Test
	@Verifies(value = "should not validate when username is whitespace only", method = "isUserNameValid(String)")
	public void isUserNameValid_shouldNotValidateWhenUsernameIsWhitespaceOnly() throws Exception {
		Assert.assertFalse(validator.isUserNameValid("  "));
	}
	
	/**
	 * @see UserValidator#isUserNameAsEmailValid(String)
	 * @verifies return false if email invalid
	 */
	@Test
	public void isUserNameAsEmailValid_shouldReturnFalseIfEmailInvalid() throws Exception {
		String[] invalids = new String[] { "mkyong", "mkyong123@.com", "my@kong", "my.kong", 
				"my.@kong", "@kong.my" };
		for (String email : invalids) {
			Assert.assertFalse(validator.isUserNameAsEmailValid(email));
		}
	}
	
	/**
	 * @see UserValidator#isUserNameAsEmailValid(String)
	 * @verifies return true if email valid
	 */
	@Test
	public void isUserNameAsEmailValid_shouldReturnTrueIfEmailValid() throws Exception {
		String[] valids = new String[] { "mkyong@yahoo.com", "mkyong-100@yahoo.com", "mkyong.100@yahoo.com",
		        "mkyong111@mkyong.com", "mkyong-100@mkyong.net", "mkyong.100@mkyong.com.au", "mkyong@1.com",
		        "mkyong@gmail.com.com", "mk@t-yong.de" };
		for (String email : valids) {
			Assert.assertTrue(validator.isUserNameAsEmailValid(email));
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
		validator.validate(user, errors);
		
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
		validator.validate(user, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("username"));
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@Test
	@Verifies(value = "not throw NPE when user is null", method = "validate(Object,Errors)")
	public void validate_shouldNotThrowNPEWhenUserIsNull() throws Exception {
		Errors errors = new BindException(new User(), "user");
		validator.validate(null, errors);
		Assert.assertTrue(true);
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@SuppressWarnings("deprecation")
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		User user = new User();
		user.setUsername("test");
		user.setSystemId("systemId");
		user.setRetireReason("retireReason");
		user.setPerson(new Person(999));
		user.getPerson().addName(new PersonName("Users", "Need", "People"));
		user.getPerson().setGender("F");
		
		Errors errors = new BindException(user, "user");
		validator.validate(user, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see UserValidator#validate(Object,Errors)
	 */
	@SuppressWarnings("deprecation")
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		User user = new User();
		user.setUsername(STRING_WITH_LENGTH_GREATER_THAN_50);
		user.setSystemId(STRING_WITH_LENGTH_GREATER_THAN_50);
		user.setRetireReason(STRING_WITH_LENGTH_GREATER_THAN_50);
		user.setPerson(new Person(999));
		user.getPerson().addName(new PersonName(STRING_WITH_LENGTH_GREATER_THAN_50, STRING_WITH_LENGTH_GREATER_THAN_50, STRING_WITH_LENGTH_GREATER_THAN_50));
		user.getPerson().setGender(STRING_WITH_LENGTH_GREATER_THAN_50);
		
		Errors errors = new BindException(user, "user");
		validator.validate(user, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("username"));
		Assert.assertTrue(errors.hasFieldErrors("systemId"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
		Assert.assertTrue(errors.hasFieldErrors("person.names[0].givenName"));
		Assert.assertTrue(errors.hasFieldErrors("person.names[0].middleName"));
		Assert.assertTrue(errors.hasFieldErrors("person.names[0].familyName"));
		Assert.assertTrue(errors.hasFieldErrors("person.gender"));
	}
}
