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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.hibernate.HibernateContextDAO;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests the {@link ContextDAO} linked to from the Context. 
 * Currently that file is the {@link HibernateContextDAO}
 * 
 * So far we have thoroughly analyzed:
 *     public User authenticate(String, String) on 21/Aug/2008
 */
public class ContextDAOTest extends BaseContextSensitiveTest {

	private ContextDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * The "@Before" method in {@link BaseContextSensitiveTest} is run
	 * right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runExtraSetup() throws Exception {
		executeDataSet("org/openmrs/api/db/include/contextDAOTest.xml");
		
		if (dao == null) {
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (ContextDAO) applicationContext.getBean("contextDAO");
		}
	}
	
	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 *	 test = should authenticate given username and password
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenUsernameAndPassword() throws Exception {
		User u = dao.authenticate("admin", "test");
		Assert.assertEquals("Should be the admin user", "admin", u.getUsername());
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should authenticate given systemId and password
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenSystemIdAndPassword() throws Exception {
		User u = dao.authenticate("1-8", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should authenticate given systemId without hyphen and password
	 * 
	 * Fixed bug #982
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenSystemIdWithoutHyphenAndPassword() throws Exception {
		User u = dao.authenticate("18", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given username and incorrect password
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenUsernameAndIncorrectPassword()
	        throws Exception {
		dao.authenticate("admin", "wrong");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given systemId and incorrect password
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenSystemIdAndIncorrectPassword() throws Exception {
		dao.authenticate("1-8", "wrong");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given incorrect username
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenIncorrectUsername() throws Exception {
		dao.authenticate("administrator", "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given incorrect systemId
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenIncorrectSystemId() throws Exception {
		dao.authenticate("1-9", "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 *	test = should not authenticate given null login
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenNullLogin() throws Exception {
		dao.authenticate(null, "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given empty login
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenEmptyLogin() throws Exception {
		dao.authenticate("", "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate when password in database is null
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("admin", null);
	}
	
	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given non null password when password in database is null
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("nullpassword", "password");
	}
	
	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given null password when password in database is null
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("nullpassword", null);
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate when password in database is empty
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsEmpty() throws Exception {
		dao.authenticate("emptypassword", "");
	}
	
	/**
	 * This does not use the "expected=Exception" paradigm because it 
	 * is comparing the actual error messages thrown
	 * 
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = give identical error messages between username and password mismatch
	 */
	@Test()
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
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 *  test = lockout user after five failed attempts
	 * 
	 * @throws Exception
	 */
	@Test(expected=ContextAuthenticationException.class)
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
}
