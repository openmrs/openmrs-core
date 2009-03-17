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
package org.openmrs.api.db;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.hibernate.HibernateContextDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * This class tests the {@link ContextDAO} linked to from the Context. Currently that file is the
 * {@link HibernateContextDAO}.<br/>
 * <br/>
 * So far we have thoroughly analyzed:
 * <ul>
 * <li>public User authenticate(String, String) on 21/Aug/2008</li>
 * </ul>
 */
public class ContextDAOTest extends BaseContextSensitiveTest {
	
	private ContextDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	@Verifies(value = "should ", method = "authenticate(String,String)")
	public void runExtraSetup() throws Exception {
		executeDataSet("org/openmrs/api/db/include/contextDAOTest.xml");
		
		if (dao == null) {
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (ContextDAO) applicationContext.getBean("contextDAO");
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
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test
	@Verifies(value = "should authenticateGivenUsernameAndPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateGivenUsernameAndPassword() throws Exception {
		User u = dao.authenticate("admin", "test");
		Assert.assertEquals("Should be the admin user", "admin", u.getUsername());
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test
	@Verifies(value = "should authenticateGivenSystemIdAndPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateGivenSystemIdAndPassword() throws Exception {
		User u = dao.authenticate("1-8", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}
	
	/**
	 * Fixed bug #982
	 * 
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test
	@Verifies(value = "should authenticateGivenSystemIdWithoutHyphenAndPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateGivenSystemIdWithoutHyphenAndPassword() throws Exception {
		User u = dao.authenticate("18", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenUsernameAndIncorrectPassword", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenUsernameAndIncorrectPassword() throws Exception {
		dao.authenticate("admin", "wrong");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenSystemIdAndIncorrectPassword", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenSystemIdAndIncorrectPassword() throws Exception {
		dao.authenticate("1-8", "wrong");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenIncorrectUsername", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenIncorrectUsername() throws Exception {
		dao.authenticate("administrator", "test");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenIncorrectSystemId", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenIncorrectSystemId() throws Exception {
		dao.authenticate("1-9", "test");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenNullLogin", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenNullLogin() throws Exception {
		dao.authenticate(null, "test");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenEmptyLogin", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenEmptyLogin() throws Exception {
		dao.authenticate("", "test");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateWhenPasswordInDatabaseIsNull", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("admin", null);
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("nullpassword", "password");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("nullpassword", null);
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should notAuthenticateWhenPasswordInDatabaseIsEmpty", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsEmpty() throws Exception {
		dao.authenticate("emptypassword", "");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test()
	@Verifies(value = "should giveIdenticalErrorMessagesBetweenUsernameAndPasswordMismatch", method = "authenticate(String,String)")
	public void authenticate_shouldGiveIdenticalErrorMessagesBetweenUsernameAndPasswordMismatch() throws Exception {
		User user = dao.authenticate("admin", "test");
		Assert.assertNotNull("This test depends on there being an admin:test user", user);
		
		String invalidUsernameErrorMessage = null;
		String invalidPasswordErrorMessage = null;
		
		try {
			dao.authenticate("some invalid username", "and an invalid password");
		}
		catch (ContextAuthenticationException authException) {
			invalidUsernameErrorMessage = authException.getMessage();
			invalidUsernameErrorMessage = invalidUsernameErrorMessage.replace("some invalid username", "");
		}
		
		try {
			// a valid username but an invalid password for that user
			dao.authenticate("admin", "and an invalid password");
		}
		catch (ContextAuthenticationException authException) {
			invalidPasswordErrorMessage = authException.getMessage();
			invalidPasswordErrorMessage = invalidPasswordErrorMessage.replace("admin", "");
		}
		
		Assert.assertEquals(invalidUsernameErrorMessage, invalidPasswordErrorMessage);
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should lockoutUserAfterFiveFailedAttempts", method = "authenticate(String,String)")
	public void authenticate_shouldLockoutUserAfterFiveFailedAttempts() throws Exception {
		// logout after the base setup
		Context.logout();
		
		// we rely on being able to log in as admin/test in this unittest
		// we must do the "improper" try/catch block here because the whole 
		// test is expected to throw and exception at the end
		try {
			dao.authenticate("admin", "test");
		}
		catch (ContextAuthenticationException authException) {
			Assert.fail("There must be an admin:test user for this test to run properly");
		}
		Context.logout();
		
		for (int x = 1; x <= 5; x++) {
			// try to authenticate with a proper 
			try {
				dao.authenticate("admin", "not the right password");
				Assert.fail("Not sure why this username/password combo worked");
			}
			catch (ContextAuthenticationException authException) {
				// pass
			}
		}
		
		// those were the first five, now the sixth request 
		// (with the same user and right pw) should fail
		dao.authenticate("admin", "test");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test
	@Verifies(value = "should authenticateWithCorrectHashedPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateWithCorrectHashedPassword() throws Exception {
		dao.authenticate("correct", "test");
	}
	
	/**
	 * @see {@link ContextDAO#authenticate(String,String)}
	 */
	@Test
	@Verifies(value = "should authenticateWithIncorrectHashedPassword", method = "authenticate(String,String)")
	public void authenticate_shouldAuthenticateWithIncorrectHashedPassword() throws Exception {
		dao.authenticate("incorrect", "test");
	}
	
}
