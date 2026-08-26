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

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.UserServiceImpl;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.Security;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDAOTest extends BaseContextSensitiveTest {

	public static final String SECRET_QUESTION = "What is the answer?";

	public static final String SECRET_ANSWER = "42";

	public static final String PASSWORD = "Openmr5xy";

	private User userJoe;

	private UserDAO dao = null;

	@BeforeEach
	public void runBeforeEachTest() throws Exception {
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
			dao = (UserDAO) applicationContext.getBean("userDAO");
		}

		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.saveUser(userJoe, PASSWORD);
		}
		Context.flushSession();
	}

	@Test
	public void openmrsPasswordEncoder_shouldBeRegisteredInSpringContext() {
		assertNotNull(Context.getRegisteredComponent("openmrsPasswordEncoder", PasswordEncoder.class));
	}

	@Test
	public void getUsers_shouldEscapeSqlWildcardsInSearchPhrase() throws Exception {

		User u = new User();
		u.setPerson(new Person());
		u.getPerson().setGender("M");

		String[] wildcards = new String[] { "_" };
		for (String wildcard : wildcards) {

			PersonName name = new PersonName(wildcard + "cats", wildcard + "and", wildcard + "dogs");
			name.setDateCreated(new Date());
			u.addName(name);
			u.setUsername(wildcard + "test" + wildcard);
			u.setSystemId("wildcard-" + wildcard);

			try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
				dao.saveUser(u, "Openmr5xy");
			}

			int size = dao.getUsers(wildcard + "ca", null, false, null, null).size();
			assertEquals(1, size);

			String userName = (dao.getUsers(wildcard + "ca", null, false, null, null).get(0).getUsername());
			assertEquals(wildcard + "test" + wildcard, userName,
			    "Test failed since no user containing the character " + wildcard + " was found, ");

		}
	}

	@Test
	public void saveUser_shouldCreateNewUser() {
		dao.saveUser(userJoe, null);
		User u2 = dao.getUser(userJoe.getId());
		assertNotNull(u2, "User should have been returned");
	}

	@Test
	public void updateUserPassword_shouldNotOverwriteUserSecretQuestionOrAnswer() throws Exception {
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, PASSWORD);
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, "Openmr6zz");
		}
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	@Test
	public void saveUser_shouldNotOverwriteUserSecretQuestionOrAnswer() throws Exception {
		dao.saveUser(userJoe, null);
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		userJoe.setUserProperty("foo", "bar");
		dao.saveUser(userJoe, null);
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
	public void changePassword_shouldNotOverwriteUserSecretQuestionOrAnswer() throws Exception {
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, PASSWORD);
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		Context.authenticate(userJoe.getUsername(), PASSWORD);
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, PASSWORD + "foo");
		}
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	@Test
	public void changeHashedPassword_shouldNotOverwriteUserSecretQuestionOrAnswer() throws Exception {
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, PASSWORD);
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		userJoe.setUserProperty("foo", "bar");
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changeHashedPassword(userJoe, "VakesJkw1", Security.getRandomToken());
		}
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}

	@Test
	public void changePassword_shouldNotInvalidateSecretAnswer() throws Exception {
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, PASSWORD);
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		assertTrue(dao.isSecretAnswer(userJoe, SECRET_ANSWER));
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, "NewPass456");
		}
		assertTrue(dao.isSecretAnswer(userJoe, SECRET_ANSWER));
	}

	@Test
	public void isSecretAnswer_shouldReturnTrueWhenTheAnswerMatches() throws Exception {
		dao.saveUser(userJoe, null);
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		assertTrue(dao.isSecretAnswer(userJoe, SECRET_ANSWER));
	}

	@Test
	public void isSecretAnswer_shouldReturnFalseWhenTheAnswerDoesNotMatch() throws Exception {
		dao.saveUser(userJoe, null);
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		}
		assertFalse(dao.isSecretAnswer(userJoe, "foo"));

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
	public void changeQuestionAnswer_shouldNotAllowChangingQuestionAnswerFromUnknownCaller() {
		Exception caughtException = assertThrows(DAOException.class,
		    () -> dao.changeQuestionAnswer(userJoe, "question", "answer"));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
		assertNull(dao.getLoginCredential(userJoe).getSecretQuestion(), "secret question should not have been set");
	}

	@Test
	public void changeQuestionAnswerString_shouldNotAllowChangingQuestionAnswerFromUnknownCaller() throws Exception {
		Context.authenticate(userJoe.getUsername(), PASSWORD);
		Exception caughtException = assertThrows(DAOException.class,
		    () -> dao.changeQuestionAnswer(PASSWORD, "question", "answer"));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
		assertNull(dao.getLoginCredential(userJoe).getSecretQuestion(), "secret question should not have been set");
	}

	@Test
	public void setUserActivationKey_shouldNotAllowSettingActivationKeyFromUnknownCaller() {
		LoginCredential lc = dao.getLoginCredential(userJoe);

		Exception caughtException = assertThrows(DAOException.class, () -> dao.setUserActivationKey(lc));

		assertThat(caughtException.getMessage(), is("Illegal attempt to change user password from unknown caller"));
	}

	@Test
	public void userPasswordGuard_shouldAllowReEntrantCalls() throws Exception {
		User newUser = new User();
		newUser.setPerson(new Person());
		newUser.getPerson().setGender("M");
		PersonName name = new PersonName("Reentrant", "R", "User");
		name.setDateCreated(new Date());
		newUser.addName(name);
		newUser.setUsername("reentrant");
		newUser.setSystemId("reentrant");
		newUser.setDateCreated(new Date());

		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.saveUser(newUser, PASSWORD);
		}

		assertNotNull(dao.getUser(newUser.getUserId()));
	}

	@Test
	public void changePasswordString_shouldAllowChangingPasswordWhenPermitIsHeld() throws Exception {
		Context.authenticate(userJoe.getUsername(), PASSWORD);
		try (AutoCloseable permit = UserServiceImpl.acquirePasswordGuardPermit()) {
			dao.changePassword(userJoe, PASSWORD + "foo");
		}

		assertFalse(UserServiceImpl.isPasswordGuardPermitted());
		assertTrue(dao.getLoginCredential(userJoe).checkPassword(PASSWORD + "foo"));
	}

}
