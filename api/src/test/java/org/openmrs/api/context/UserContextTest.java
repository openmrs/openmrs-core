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
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */

package org.openmrs.api.context;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class UserContextTest extends BaseContextSensitiveTest {
	
	private ContextDAO dao = null;
	
	private UserContext uc = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	@Verifies(value = "should ", method = "authenticate(String,String)")
	public void runExtraSetup() throws Exception {
		executeDataSet("org/openmrs/api/context/include/userDAOTest.xml");
		
		if (dao == null) {
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (ContextDAO) applicationContext.getBean("contextDAO");
		}
		if (uc == null) {
			uc = (UserContext) Context.getUserContext();
		}
	}
	
	/**
	 * Methods in this class might authenticate with a different user, so log that user out after
	 * this whole junit class is done.
	 */
	@AfterClass
	public static void logOutAfterThisTest() {
		Context.logout();
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies authenticate given systemId and password
	 */
	@Test
	@Verifies(value = "should authenticateGivenSystemIdAndPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateGivenSystemIdAndPassword() throws Exception {
		User u = uc.authenticate("1-8", "test", dao);
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies authenticate given systemId without hyphen and password
	 */
	@Test
	@Verifies(value = "should authenticateGivenSystemIdWithoutHyphenAndPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateGivenSystemIdWithoutHyphenAndPassword() throws Exception {
		User u = uc.authenticate("18", "test", dao);
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies authenticate given username and password
	 */
	@Test
	@Verifies(value = "should authenticateGivenUsernameAndPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateGivenUsernameAndPassword() throws Exception {
		User u = uc.authenticate("admin", "test", dao);
		Assert.assertEquals("Should be the admin user", "admin", u.getUsername());
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies authenticateWithCorrectHashedPassword
	 */
	@Test
	@Verifies(value = "should authenticateWithCorrectHashedPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateWithCorrectHashedPassword() throws Exception {
		uc.authenticate("correct", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies authenticateWithIncorrectHashedPassword
	 */
	@Test
	@Verifies(value = "should authenticateWithIncorrectHashedPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateWithIncorrectHashedPassword() throws Exception {
		uc.authenticate("incorrect", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies give identical error messages between username and password mismatch
	 */
	@Test
	@Verifies(value = "should giveIdenticalErrorMessagesBetweenUsernameAndPasswordMismatch", method = "authenticate(String,String)")
	public void authenticate_shouldGiveIdenticalErrorMessagesBetweenUsernameAndPasswordMismatch() throws Exception {
		User user = uc.authenticate("admin", "test", dao);
		Assert.assertNotNull("This test depends on there being an admin:test user", user);
		
		String invalidUsernameErrorMessage = null;
		String invalidPasswordErrorMessage = null;
		
		try {
			uc.authenticate("some invalid username", "and an invalid password", dao);
		}
		catch (ContextAuthenticationException authException) {
			invalidUsernameErrorMessage = authException.getMessage();
			invalidUsernameErrorMessage = invalidUsernameErrorMessage.replace("some invalid username", "");
		}
		
		try {
			// a valid username but an invalid password for that user
			uc.authenticate("admin", "and an invalid password", dao);
		}
		catch (ContextAuthenticationException authException) {
			invalidPasswordErrorMessage = authException.getMessage();
			invalidPasswordErrorMessage = invalidPasswordErrorMessage.replace("admin", "");
		}
		
		Assert.assertEquals(invalidUsernameErrorMessage, invalidPasswordErrorMessage);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies lockout user after specified number of failed attempts
	 */
	@Test(expected = ContextAuthenticationLockoutException.class)
	@Verifies(value = "should lockout user after specified number of failed attempts", method = "authenticate(String,String)")
	public void authenticate_shouldLockoutUserAfterSpecifiedNumberOfFailedAttempts() throws Exception {
		// logout after the base setup
		Context.logout();
		
		// we rely on being able to log in as admin/test in this unittest
		// we must do the "improper" try/catch block here because the whole 
		// test is expected to throw and exception at the end
		try {
			uc.authenticate("admin", "test", dao);
		}
		catch (ContextAuthenticationException authException) {
			Assert.fail("There must be an admin:test user for this test to run properly");
		}
		Context.logout();
		
		for (int x = 1; x <= 8; x++) {
			// try to authenticate with a proper 
			try {
				uc.authenticate("admin", "not the right password", dao);
				Assert.fail("Not sure why this username/password combo worked");
			}
			catch (ContextAuthenticationException authException) {
				// pass
			}
		}
		
		// those were the first eight, now the ninth request 
		// (with the same user and right pw) should fail
		uc.authenticate("admin", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given empty login
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenEmptyLogin", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenEmptyLogin() throws Exception {
		uc.authenticate("", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given incorrect systemId
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenIncorrectSystemId", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenIncorrectSystemId() throws Exception {
		uc.authenticate("1-9", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given incorrect username
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenIncorrectUsername", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenIncorrectUsername() throws Exception {
		uc.authenticate("administrator", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given non null password when password in database is null
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		uc.authenticate("nullpassword", "password", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given null login
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenNullLogin", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenNullLogin() throws Exception {
		uc.authenticate(null, "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given null password when password in database is null
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		uc.authenticate("nullpassword", null, dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given systemId and incorrect password
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenSystemIdAndIncorrectPassword", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenSystemIdAndIncorrectPassword() throws Exception {
		uc.authenticate("1-8", "wrong", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate given username and incorrect password
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenUsernameAndIncorrectPassword", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenUsernameAndIncorrectPassword() throws Exception {
		uc.authenticate("admin", "wrong", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies not authenticate when password in database is empty
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateWhenPasswordInDatabaseIsEmpty", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsEmpty() throws Exception {
		uc.authenticate("emptypassword", "", dao);
	}
	
	/**
	 * #1580: If you type your password wrong, then log in correctly, the API will not lock you out
	 * after multiple login attempts in the future
	 * 
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies pass regression test for 1580
	 */
	@Test(expected = ContextAuthenticationLockoutException.class)
	@Verifies(value = "should pass regression test for 1580", method = "authenticate(String,String)")
	public void authenticate_shouldPassRegressionTestFor1580() throws Exception {
		// logout after the base setup
		Context.logout();
		
		// first we fail a login attempt
		try {
			uc.authenticate("admin", "not the right password", dao);
			Assert.fail("Not sure why this username/password combo worked");
		}
		catch (ContextAuthenticationException authException) {
			// pass
		}
		
		// next we log in correctly
		try {
			uc.authenticate("admin", "test", dao);
		}
		catch (ContextAuthenticationException authException) {
			Assert.fail("There must be an admin:test user for this test to run properly");
		}
		Context.logout();
		
		for (int x = 1; x <= 8; x++) {
			// now we fail several login attempts
			try {
				uc.authenticate("admin", "not the right password", dao);
				Assert.fail("Not sure why this username/password combo worked");
			}
			catch (ContextAuthenticationException authException) {
				// pass
			}
		}
		
		// those were the first eight, now the ninth request
		// (with the same user and right pw) should fail
		uc.authenticate("admin", "test", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies throw a ContextAuthenticationException if username is an empty string
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should throw a ContextAuthenticationException if username is an empty string", method = "authenticate(String,String)")
	public void authenticate_shouldThrowAContextAuthenticationExceptionIfUsernameIsAnEmptyString() throws Exception {
		//update a user with a username that is an empty string for this test
		UserService us = Context.getUserService();
		
		User u = us.getUser(1);
		u.setUsername("");
		u.getPerson().setGender("M");
		
		us.saveUser(u, "Openmr5xy");
		uc.authenticate("", "password", dao);
	}
	
	/**
	 * @see UserContext#authenticate(String,String,ContextDAO)
	 * @verifies throw a ContextAuthenticationException if username is white space
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should throw a ContextAuthenticationException if username is white space", method = "authenticate(String,String)")
	public void authenticate_shouldThrowAPIExceptionIfUsernameIsWhiteSpace() throws Exception {
		// it would be illegal to save this user (with a whitespace username) but we can get it in the db via xml
		User u = Context.getUserService().getUser(507);
		uc.authenticate(" ", "password", dao);
	}
}
