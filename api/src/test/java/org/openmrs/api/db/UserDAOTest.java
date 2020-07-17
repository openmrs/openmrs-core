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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.Security;

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
		
		dao.saveUser(userJoe, null);
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
			assertEquals( wildcard + "test" + wildcard, userName, "Test failed since no user containing the character " + wildcard + " was found, ");
			
		}
	}
	
	@Test
	public void saveUser_shouldCreateNewUser() {
		dao.saveUser(userJoe, "Openmr5xy");
		User u2 = dao.getUser(userJoe.getId());
		assertNotNull(u2, "User should have been returned");
	}
	
	@Test
	public void updateUserPassword_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		dao.changePassword(userJoe, PASSWORD);
		dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		dao.changePassword(userJoe, "Openmr6zz");
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}
	
	@Test
	public void saveUser_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		dao.saveUser(userJoe, PASSWORD);
		dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		userJoe.setUserProperty("foo", "bar");
		dao.saveUser(userJoe, PASSWORD);
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}
	
	@Test
	public void changePassword_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		dao.changePassword(userJoe, PASSWORD);
		dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals( hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		Context.authenticate(userJoe.getUsername(), PASSWORD);
		dao.changePassword(PASSWORD, PASSWORD + "foo");
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}
	
	@Test
	public void changeHashedPassword_shouldNotOverwriteUserSecretQuestionOrAnswer() {
		dao.changePassword(userJoe, PASSWORD);
		dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		LoginCredential lc = dao.getLoginCredential(userJoe);
		String hashedSecretAnswer = Security.encodeString(SECRET_ANSWER + lc.getSalt());
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should be set");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should be set");
		userJoe.setUserProperty("foo", "bar");
		dao.changeHashedPassword(userJoe, "VakesJkw1", Security.getRandomToken());
		lc = dao.getLoginCredential(userJoe);
		assertEquals(SECRET_QUESTION, lc.getSecretQuestion(), "question should not have changed");
		assertEquals(hashedSecretAnswer, lc.getSecretAnswer(), "answer should not have changed");
	}
	
	@Test
	public void isSecretAnswer_shouldReturnTrueWhenTheAnswerMatches() {
		dao.saveUser(userJoe, PASSWORD);
		dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		assertTrue(dao.isSecretAnswer(userJoe, SECRET_ANSWER));
	}
	
	@Test
	public void isSecretAnswer_shouldReturnFalseWhenTheAnswerDoesNotMatch() {
		dao.saveUser(userJoe, PASSWORD);
		dao.changeQuestionAnswer(userJoe, SECRET_QUESTION, SECRET_ANSWER);
		assertFalse(dao.isSecretAnswer(userJoe, "foo"));
		
	}
	
}
