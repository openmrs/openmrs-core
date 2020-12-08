/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.hibernate.HibernateContextDAO;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.stereotype.Component;

/**
 * This class tests the {@link ContextDAO} linked to from the Context. Currently that file is the
 * {@link HibernateContextDAO}.<br>
 * <br>
 * So far we have thoroughly analyzed:
 * <ul>
 * <li>public User authenticate(String, String) on 21/Aug/2008</li>
 * </ul>
 */
public class ContextDAOTest extends BaseContextSensitiveTest {
	
	private ContextDAO dao = null;
	
	@Resource(name = "testUserSessionListener")
	TestUserSessionListener testUserSessionListener;
  
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void runExtraSetup() {
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
	@AfterAll
	public static void logOutAfterThisTest() {
		Context.logout();
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenUsernameAndPassword() {
		User u = dao.authenticate("admin", "test");
		assertEquals("admin", u.getUsername(), "Should be the admin user");
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenSystemIdAndPassword() {
		User u = dao.authenticate("1-8", "test");
		assertEquals("1-8", u.getSystemId(), "Should be the 1-8 user");
	}
	
	/**
	 * Fixed bug #982
	 * 
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenSystemIdWithoutHyphenAndPassword() {
		User u = dao.authenticate("18", "test");
		assertEquals("1-8", u.getSystemId(), "Should be the 1-8 user");
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenUsernameAndIncorrectPassword() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("admin", "wrong"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenSystemIdAndIncorrectPassword() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("1-8", "wrong"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenIncorrectUsername() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("administrator", "test"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenIncorrectSystemId() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("1-9", "test"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenNullLogin() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate(null, "test"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenEmptyLogin() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("", "test"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsNull() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("admin", null));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("nullpassword", "password"));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("nullpassword", null));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsEmpty() {
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("emptypassword", ""));
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test()
	public void authenticate_shouldGiveIdenticalErrorMessagesBetweenUsernameAndPasswordMismatch() {
		User user = dao.authenticate("admin", "test");
		assertNotNull(user, "This test depends on there being an admin:test user");
		
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
		
		assertEquals(invalidUsernameErrorMessage, invalidPasswordErrorMessage);
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldLockoutUserAfterEightFailedAttempts() {
		// logout after the base setup
		Context.logout();
		
		// we rely on being able to log in as admin/test in this unittest
		// we must do the "improper" try/catch block here because the whole 
		// test is expected to throw and exception at the end
		try {
			dao.authenticate("admin", "test");
		}
		catch (ContextAuthenticationException authException) {
			fail("There must be an admin:test user for this test to run properly");
		}
		Context.logout();
		
		for (int x = 1; x <= 7; x++) {
			// try to authenticate with a proper 
			try {
				dao.authenticate("admin", "not the right password");
				fail("Not sure why this username/password combo worked");
			}
			catch (ContextAuthenticationException authException) {
				// pass
			}
		}
		
		// those were the first seven, now the eighth request
		// (with the same user and right pw) should fail
		dao.authenticate("admin", "test");
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldAuthenticateWithCorrectHashedPassword() {
		dao.authenticate("correct", "test");
	}
	
	/**
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldAuthenticateWithIncorrectHashedPassword() {
		dao.authenticate("incorrect", "test");
	}
	
	/**
	 * #1580: If you type your password wrong, then log in correctly, the API will not lock you out
	 * after multiple login attempts in the future
	 * 
	 * @see ContextDAO#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldPassRegressionTestFor1580() {
		// logout after the base setup
		Context.logout();
		
		// first we fail a login attempt
		try {
			dao.authenticate("admin", "not the right password");
			fail("Not sure why this username/password combo worked");
		}
		catch (ContextAuthenticationException authException) {
			// pass
		}
		
		// next we log in correctly
		try {
			dao.authenticate("admin", "test");
		}
		catch (ContextAuthenticationException authException) {
			fail("There must be an admin:test user for this test to run properly");
		}
		Context.logout();
		
		for (int x = 1; x <= 8; x++) {
			// now we fail several login attempts 
			try {
				dao.authenticate("admin", "not the right password");
				fail("Not sure why this username/password combo worked");
			}
			catch (ContextAuthenticationException authException) {
				// pass
			}
		}
		
		// those were the first eight, now the ninth request 
		// (with the same user and right pw) should fail
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("admin", "test"));
	}
	
	@Test
	public void authenticate_shouldThrowAContextAuthenticationExceptionIfUsernameIsAnEmptyString() {
		//update a user with a username that is an empty string for this test
		UserService us = Context.getUserService();
		
		User u = us.getUser(1);
		u.setUsername("");
		u.getPerson().setGender("M");
		
		us.saveUser(u);
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("", "password"));
		
	}
	
	@Test
	public void authenticate_shouldThrowAPIExceptionIfUsernameIsWhiteSpace() {
		// it would be illegal to save this user (with a whitespace username) but we can get it in the db via xml
		User u = Context.getUserService().getUser(507);
		assertThrows(ContextAuthenticationException.class, () -> dao.authenticate("  ", "password"));
	}
	
	/**
	 *
	 * @see org.openmrs.api.db.hibernate.HibernateContextDAO#mergeDefaultRuntimeProperties(java.util.Properties)
	 */
	@Test
	public void should_mergeDefaultRuntimeProperties() {
		Properties properties = new Properties();
		properties.setProperty("key", "value");
		dao.mergeDefaultRuntimeProperties(properties);
		assertNotNull(properties.getProperty("hibernate.key"));
	}
	
	@Component("testUserSessionListener")
	public static class TestUserSessionListener implements UserSessionListener {
		public Set<String> logins = new LinkedHashSet<>();
		public Set<String> logouts = new LinkedHashSet<>();

		@Override
		public void loggedInOrOut(User user, Event event, Status status) {
			if (event != null && user != null) {
				if (Event.LOGIN.equals(event)) {
					logins.add(user.getUsername() + ":" + event + ":" + status);
				} else if (Event.LOGOUT.equals(event)) {
					logouts.add(
							user.getUsername() + ":" + event + ":" + status);
				}
			}
		}

		public void clear() {
			logins.clear();
			logouts.clear();
		}
	}

	@Test
	public void authenticate_shouldRightlyTriggerUserSessionListener_withSuccessfulLogin() {
		testUserSessionListener.clear();
		Context.authenticate("admin", "test");
		assertThat(testUserSessionListener.logins,
				contains("admin:LOGIN:SUCCESS"));
		assertThat(testUserSessionListener.logouts, empty());
	}

	@Test
	public void authenticate_shouldRightlyTriggerUserSessionListener_withFailedLogin() {
		testUserSessionListener.clear();
		try {
			Context.authenticate("wrongUser", "test");
		} catch (ContextAuthenticationException e) {
		}
		try {
			Context.authenticate("admin", "wrongPassword");
		} catch (ContextAuthenticationException e) {
		}
		Context.authenticate("admin", "test");
		assertThat(testUserSessionListener.logins,
				contains("wrongUser:LOGIN:FAIL", "admin:LOGIN:FAIL",
						"admin:LOGIN:SUCCESS"));
		assertThat(testUserSessionListener.logouts, empty());
	}

	@Test
	public void logout_shouldRightlyTriggerUserSessionListener() {
		testUserSessionListener.clear();
		Context.logout();
		assertThat(testUserSessionListener.logouts,
				contains("admin:LOGOUT:SUCCESS"));
		assertThat(testUserSessionListener.logins, empty());
	}
}
