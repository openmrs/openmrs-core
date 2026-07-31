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

import java.lang.reflect.Method;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.UserServiceImpl;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.Security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDAOTest extends BaseContextSensitiveTest {

	public static final String SECRET_QUESTION = "What is the answer?";

	public static final String SECRET_ANSWER = "42";

	public static final String PASSWORD = "Openmr5xy";

	private User userJoe;

	private UserDAO dao = null;

	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 *
	 * @throws Exception
	 */
	@BeforeEach
	public void runBeforeEachTest() {
		PersonName name = new PersonName("Joe", "J", "Doe");
		name.setDateCreated(new Date());
		Person person = new Person();
		person.setDateCreated(new Date());
		person.setPersonDateCreated(person.getDateCreated());
		person.setGender("M");
		userJoe = new User();
		userJoe.setSystemId("100-30");
		userJoe.setPerson(person);
		userJoe.addName(name);
		userJoe.setUsername("juser");
		userJoe.setDateCreated(new Date());

		if (dao == null) {
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (UserDAO) applicationContext.getBean("userDAO");
		}

		Context.getUserService().createUser(userJoe, PASSWORD);
		Context.flushSession(); //needed by postgres
	}

	@Test
	public void getUsers_shouldEscapeSqlWildcardsInSearchPhrase() {

		User u = new User();
		u.setPerson(new Person());
		u.getPerson().setGender("M");

		String wildcards[] = new String[] { "_" }; // we used to also test %, but UserValidator actually doesn't allow that in usernames. TODO: remove the loop
		//for each of the wildcards in the array, insert a user with a username or names
		//with the wildcards and carry out a search for that user
		for (String wildcard : wildcards) {

			PersonName name = new PersonName(wildcard + "cats", wildcard + "and", wildcard + "dogs");
			name.setDateCreated(new Date());
			u.addName(name);
			u.setUsername(wildcard + "test" + wildcard);
			Context.getUserService().createUser(u, "Openmr5xy");

			//we expect only one matching name or or systemId  to be returned
			int size = dao.getUsers(wildcard + "ca", null, false, null, null).size();
			assertEquals(1, size);

			//if actually the search returned the matching name or system id
			String userName = (dao.getUsers(wildcard + "ca", null, false, null, null).get(0).getUsername());
			assertEquals(wildcard + "test" + wildcard, userName,
			    "Test failed since no user containing the character " + wildcard + " was found, ");

		}
	}

	@Test
	public void saveUser_shouldCreateNewUser() {
		Context.getUserService().saveUser(userJoe);
		User u2 = dao.getUser(userJoe.getId());
		assertNotNull(u2, "User should have been returned");
	}

	@Test
	public void updateUserPassword_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		Context.getUserService().changePassword(userJoe, PASSWORD);
		Context.getUserService().changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		Context.getUserService().changePassword(userJoe, "Openmr6zz");
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	@Test
	public void saveUser_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		Context.getUserService().saveUser(userJoe);
		Context.getUserService().changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		userJoe.setUserProperty("foo", "bar");
		Context.getUserService().saveUser(userJoe);
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	private static class DisallowedCaller {

		private final UserDAO userDao;

		DisallowedCaller(UserDAO userDao) {
			this.userDao = userDao;
		}

		public void changePassword(User user, String password) {
			userDao.changePassword(user, password);
		}
	}

	@Test
	public void changePassword_shouldNotAllowChangingPasswordFromUnknownClass() {
		DisallowedCaller caller = new DisallowedCaller(dao);

		Exception caughtException = assertThrows(DAOException.class, () -> caller.changePassword(userJoe, PASSWORD));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
	}

	@Test
	public void changePassword_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		Context.getUserService().changePassword(userJoe, PASSWORD);
		Context.getUserService().changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		Context.authenticate(userJoe.getUsername(), PASSWORD);
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		Context.getUserService().changePassword(PASSWORD, PASSWORD + "foo");
		Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	@Test
	public void changeHashedPassword_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		Context.getUserService().changePassword(userJoe, PASSWORD);
		Context.getUserService().changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		userJoe.setUserProperty("foo", "bar");
		Context.getUserService().changeHashedPassword(userJoe, "VakesJkw1", Security.getRandomToken());
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	@Test
	public void isSecretAnswer_shouldReturnTrueWhenTheAnswerMatches() {
		Context.getUserService().saveUser(userJoe);
		Context.getUserService().changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		assertTrue(dao.isSecretAnswer(userJoe, SECRET_ANSWER));
	}

	@Test
	public void isSecretAnswer_shouldReturnFalseWhenTheAnswerDoesNotMatch() {
		Context.getUserService().saveUser(userJoe);
		Context.getUserService().changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		assertFalse(dao.isSecretAnswer(userJoe, "foo"));

	}

	@Test
	public void saveUser_shouldNotAllowSavingUserFromUnknownCaller() {
		Exception caughtException = assertThrows(DAOException.class, () -> dao.saveUser(userJoe, PASSWORD));

		assertThat(caughtException.getMessage(), is("Illegal attempt to save user from unknown caller"));
	}

	@Test
	public void changePasswordString_shouldNotAllowChangingPasswordFromUnknownCaller() {
		Exception caughtException = assertThrows(DAOException.class, () -> dao.changePassword("old", "new"));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
	}

	@Test
	public void changeHashedPassword_shouldNotAllowChangingHashedPasswordFromUnknownCaller() {
		Exception caughtException = assertThrows(DAOException.class,
		    () -> dao.changeHashedPassword(userJoe, "hashed", "salt"));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
	}

	@Test
	public void updateLoginCredential_shouldNotAllowUpdatingLoginCredentialFromUnknownCaller() {
		Context.getUserService().changePassword(userJoe, PASSWORD);
		LoginCredential lc = dao.getLoginCredential(userJoe);

		Exception caughtException = assertThrows(DAOException.class, () -> dao.updateLoginCredential(lc));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
	}

	@Test
	public void userPasswordGuard_shouldAllowReEntrantCalls() {
		User newUser = new User();
		newUser.setPerson(new Person());
		newUser.getPerson().setGender("M");
		PersonName name = new PersonName("Reentrant", "R", "User");
		name.setDateCreated(new Date());
		newUser.addName(name);
		newUser.setUsername("reentrant");
		newUser.setSystemId("reentrant");
		newUser.setDateCreated(new Date());

		Context.getUserService().createUser(newUser, PASSWORD);

		assertNotNull(dao.getUser(newUser.getUserId()));
	}

	@Test
	public void changePasswordString_shouldAllowChangingPasswordWhenPermitIsHeld() throws Exception {
		Method enter = getGuardMethod("enter");
		Method exit = getGuardMethod("exit");

		Context.authenticate(userJoe.getUsername(), PASSWORD);
		enter.invoke(null);
		try {
			dao.changePassword(PASSWORD, PASSWORD + "foo");
		} finally {
			exit.invoke(null);
		}

		assertFalse(UserServiceImpl.UserPasswordGuard.isPermitted());
		assertTrue(dao.getLoginCredential(userJoe).checkPassword(PASSWORD + "foo"));
	}

	@Test
	public void userPasswordGuard_shouldAllowNestedPermitAcquisition() throws Exception {
		Method enter = getGuardMethod("enter");
		Method exit = getGuardMethod("exit");

		enter.invoke(null);
		enter.invoke(null);
		try {
			dao.saveUser(userJoe, PASSWORD);
			assertTrue(UserServiceImpl.UserPasswordGuard.isPermitted(), "permit should be held at depth 2");

			exit.invoke(null);
			assertTrue(UserServiceImpl.UserPasswordGuard.isPermitted(), "permit should still be held at depth 1");

			exit.invoke(null);
			assertFalse(UserServiceImpl.UserPasswordGuard.isPermitted(),
			    "permit should be released after the outermost exit");
		} finally {
			while (UserServiceImpl.UserPasswordGuard.isPermitted()) {
				exit.invoke(null);
			}
		}
	}

	private Method getGuardMethod(String name) throws Exception {
		Method method = UserServiceImpl.UserPasswordGuard.class.getDeclaredMethod(name);
		method.setAccessible(true);
		return method;
	}

}
