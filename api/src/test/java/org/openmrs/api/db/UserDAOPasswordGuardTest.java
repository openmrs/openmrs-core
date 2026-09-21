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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.UserServiceImpl;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the thread-scoped permit that lets the password and credential methods of {@link UserDAO}
 * tell a call that arrived through {@link org.openmrs.api.UserService} apart from one made straight
 * against the DAO.
 *
 * @see UserServiceImpl.UserPasswordGuard
 */
public class UserDAOPasswordGuardTest extends BaseContextSensitiveTest {

	private static final String PASSWORD = "Openmr5xy";

	private static final String NEW_PASSWORD = "Openmr6zz";

	private UserDAO dao;

	private User userJoe;

	@BeforeEach
	public void runBeforeEachTest() {
		dao = (UserDAO) applicationContext.getBean("userDAO");

		userJoe = newUser("juser", "100-30");
		Context.getUserService().createUser(userJoe, PASSWORD);
		Context.flushSession(); //needed by postgres
	}

	private User newUser(String username, String systemId) {
		PersonName name = new PersonName("Joe", "J", "Doe");
		name.setDateCreated(new Date());

		Person person = new Person();
		person.setDateCreated(new Date());
		person.setPersonDateCreated(person.getDateCreated());
		person.setGender("M");

		User user = new User();
		user.setSystemId(systemId);
		user.setPerson(person);
		user.addName(name);
		user.setUsername(username);
		user.setDateCreated(new Date());
		return user;
	}

	@Test
	public void saveUser_shouldRejectACallThatDidNotComeThroughTheService() {
		assertThrows(DAOException.class, () -> dao.saveUser(userJoe, null));
	}

	@Test
	public void changePassword_shouldRejectACallThatDidNotComeThroughTheService() {
		assertThrows(DAOException.class, () -> dao.changePassword(userJoe, NEW_PASSWORD));
	}

	@Test
	public void changePasswordForCurrentUser_shouldRejectACallThatDidNotComeThroughTheService() {
		// the authenticated user's password is not PASSWORD, so without the guard this call still fails,
		// on the old password not matching; only the message distinguishes the two
		DAOException thrown = assertThrows(DAOException.class, () -> dao.changePassword(PASSWORD, NEW_PASSWORD));
		assertTrue(thrown.getMessage().startsWith("Illegal attempt to change user password"), thrown.getMessage());
	}

	@Test
	public void changeHashedPassword_shouldRejectACallThatDidNotComeThroughTheService() {
		String salt = Security.getRandomToken();
		assertThrows(DAOException.class,
		    () -> dao.changeHashedPassword(userJoe, Security.encodePassword(NEW_PASSWORD + salt), salt));
	}

	@Test
	public void updateLoginCredential_shouldRejectACallThatDidNotComeThroughTheService() {
		LoginCredential credentials = dao.getLoginCredential(userJoe);
		assertThrows(DAOException.class, () -> dao.updateLoginCredential(credentials));
	}

	@Test
	public void saveUser_shouldBeAllowedThroughTheService() {
		userJoe.setUserProperty("foo", "bar");

		User saved = Context.getUserService().saveUser(userJoe);

		assertEquals("bar", dao.getUser(saved.getUserId()).getUserProperty("foo"));
	}

	@Test
	public void changePassword_shouldBeAllowedThroughTheService() {
		Context.getUserService().changePassword(userJoe, NEW_PASSWORD);

		Context.authenticate(userJoe.getUsername(), NEW_PASSWORD);
		assertEquals(userJoe.getUserId(), Context.getAuthenticatedUser().getUserId());
	}

	@Test
	public void changeHashedPassword_shouldBeAllowedThroughTheService() {
		String salt = Security.getRandomToken();
		String hashedPassword = Security.encodePassword(NEW_PASSWORD + salt);

		Context.getUserService().changeHashedPassword(userJoe, hashedPassword, salt);

		LoginCredential credentials = dao.getLoginCredential(userJoe);
		assertEquals(hashedPassword, credentials.getHashedPassword());
		assertEquals(salt, credentials.getSalt());
	}

	/**
	 * {@code createUser()} reaches {@code HibernateUserDAO.saveUser()}, which stores the password by
	 * way of a second, nested call to {@code saveUser()}. The permit the service raised covers the
	 * whole call, so the nested call is allowed.
	 */
	@Test
	public void createUser_shouldAllowTheDaoToReenterItsOwnGuardedMethods() {
		User userJane = newUser("jquser", "101-6");

		User created = Context.getUserService().createUser(userJane, PASSWORD);

		assertNotNull(created.getUserId());
		assertNotNull(dao.getLoginCredential(created).getHashedPassword());
		Context.authenticate(userJane.getUsername(), PASSWORD);
		assertEquals(created.getUserId(), Context.getAuthenticatedUser().getUserId());
	}

	/**
	 * {@code changeQuestionAnswer()} is not itself guarded, but it writes a login credential, so it
	 * must not depend on a permit the service has no reason to raise.
	 */
	@Test
	public void changeQuestionAnswer_shouldNotRequireThePermit() {
		dao.changeQuestionAnswer(userJoe, "What is the answer?", "42");

		assertTrue(dao.isSecretAnswer(userJoe, "42"));
	}

	@Test
	public void permit_shouldBeLoweredOnceTheServiceCallReturns() {
		Context.getUserService().changePassword(userJoe, NEW_PASSWORD);

		assertFalse(UserServiceImpl.UserPasswordGuard.isPermitted());
		assertThrows(DAOException.class, () -> dao.changePassword(userJoe, "Openmr7yy"));
	}

	/**
	 * A permit that survived a failed call would stay raised for the rest of the thread's life, which
	 * on a pooled request thread means the guard is off for every later request that thread serves.
	 */
	@Test
	public void permit_shouldBeLoweredWhenTheDaoCallThrows() {
		User unsaved = newUser("nouser", "102-1");
		unsaved.setUserId(Integer.MAX_VALUE);
		String salt = Security.getRandomToken();
		String hashedPassword = Security.encodePassword(NEW_PASSWORD + salt);

		// the failure has to come from inside the bracketed DAO call, not from a check ahead of it
		DAOException thrown = assertThrows(DAOException.class,
		    () -> Context.getUserService().changeHashedPassword(unsaved, hashedPassword, salt));
		assertTrue(thrown.getMessage().startsWith("Couldn't find user to set password for"), thrown.getMessage());

		assertFalse(UserServiceImpl.UserPasswordGuard.isPermitted());
		assertThrows(DAOException.class, () -> dao.changePassword(userJoe, "Openmr7yy"));
	}

	/**
	 * {@code isPermitted()} has to be the only member of the guard that code outside
	 * {@link UserServiceImpl} can name. Synthetic members are excluded because the Java 8 backport,
	 * which has no nestmates, reaches the private methods through synthetic accessors that javac will
	 * not let source code refer to.
	 */
	@Test
	public void userPasswordGuard_shouldOnlyLetTheServiceRaiseThePermit() throws NoSuchMethodException {
		Class<?> guard = UserServiceImpl.UserPasswordGuard.class;

		assertTrue(Modifier.isPrivate(guard.getDeclaredMethod("enter").getModifiers()), "enter() should be private");
		assertTrue(Modifier.isPrivate(guard.getDeclaredMethod("exit").getModifiers()), "exit() should be private");

		List<String> callable = Arrays.stream(guard.getDeclaredMethods()).filter(method -> !method.isSynthetic())
		        .filter(method -> !Modifier.isPrivate(method.getModifiers())).map(Method::getName).sorted()
		        .collect(Collectors.toList());
		assertEquals(Collections.singletonList("isPermitted"), callable);

		for (Constructor<?> constructor : guard.getDeclaredConstructors()) {
			assertTrue(Modifier.isPrivate(constructor.getModifiers()), "the guard should not be instantiable");
		}

		// the depth counter itself is a permit: anything that can reach it can raise one with set()
		for (Field field : guard.getDeclaredFields()) {
			if (!field.isSynthetic()) {
				assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName() + " should be private");
			}
		}
	}
}
