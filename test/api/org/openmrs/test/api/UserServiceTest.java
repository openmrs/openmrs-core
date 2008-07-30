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
package org.openmrs.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.annotation.Rollback;

/**
 * TODO add more tests to cover the methods in <code>UserService</code>
 */
public class UserServiceTest extends BaseContextSensitiveTest {
	
	protected static final String XML_FILENAME = "org/openmrs/test/api/include/UserServiceTest.xml";
	
	/**
	 * Set up the database with the initial dataset before every test method
	 * in this class.
	 * 
	 * Require authorization before every test method in this class
	 * 
	 * 
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
	}

	/**
	 * Test that we can create a user
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldCreateUser() throws Exception {
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		UserService us = Context.getUserService();
		
		User u = new User();
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.setGender("M");
		
		User createdUser = us.saveUser(u, "some arbitrary password to use");
		
		// if we're returning the object from create methods, check validity
		assertTrue("The user returned by the create user method should equal the passed in user", createdUser.equals(u));
		
		createdUser = us.getUserByUsername("bwolfe");
		assertTrue("The created user should equal the passed in user", createdUser.equals(u));
	}
	
	/**
	 * variable used to help prevent the {@link #shouldCheckThatPatientUserWasCreatedSuccessfully()}
	 * method from method run alone accidentally
	 */
	private static boolean shouldCreateUserWhoIsPatientAlreadyTestWasRun = false;
	
	/**
	 * Creates a user object that was a patient/person object already.
	 * This test is set to _NOT_ roll back when finished.  This commits the
	 * transaction which is then checked in the method directly following:
	 * {@link #shouldCheckThatPatientUserWasCreatedSuccessfully()}
	 * 
	 * @throws Exception
	 */
	@Test
	@Rollback(false)
	public void shouldCreateUserWhoIsPatientAlready() throws Exception {
		
		//deleteAllData();
		//initializeInMemoryDatabase();
		
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		// add in some basic data
		executeDataSet(XML_FILENAME);
		
		UserService userService = Context.getUserService();
		
		// the user should not exist yet
		User preliminaryFetchedUser = userService.getUser(2);
		assertNull(preliminaryFetchedUser);
		
		// get the person object we'll make into a user
		Person personToMakeUser = Context.getPersonService().getPerson(2);
		Context.clearSession();
		// this is the user object we'll be saving
		User user = new User(personToMakeUser);
		
		user.setUserId(2);
		user.setUsername("bwolfe");
		user.setSystemId("asdf");
		user.addRole(new Role("Some Role", "This is a test role")); //included in xml file
		
		// make sure everything was added to the user correctly
		assertTrue(user.getUsername().equals("bwolfe"));
		assertTrue(user.hasRole("Some Role"));
		
		// do the actual creating of the user object
		userService.saveUser(user, null);
		
		shouldCreateUserWhoIsPatientAlreadyTestWasRun = true;
		System.out.println("Just set the boolean var");
	}
	
	/**
	 * This method works in tandem with the {@link #shouldCreateUserWhoIsPatientAlready()}
	 * test.  The @shouldCreateUserWhoIsPatientAlready test is set to commit its 
	 * transaction.  This test then checks that the username, etc was created correctly.
	 * 
	 * This test deletes all data in the db at the end of it, so this
	 * transaction needs to be marked as non-rollback.  If it was not marked
	 * as such, then the db retains the multiple users that we've added in 
	 * these two tests and bad things could happen.  (Namely, in tests that
	 * expect there to be only one user in the database)
	 * 
	 * @throws Exception
	 */
	@Test
	@Rollback(false)
	public void shouldCheckThatPatientUserWasCreatedSuccessfully() throws Exception {
		
		System.out.println("Checking the boolean var");
		
		assertTrue("This test should not be run without first running 'shouldCreateUserWhoIsPatient' test method", 
		           shouldCreateUserWhoIsPatientAlreadyTestWasRun);
		
		UserService userService = Context.getUserService();
		
		// get the same user we just created and make sure the user portion exists
		User fetchedUser = userService.getUser(2);
		User fetchedUser3 = userService.getUser(3);
		if (fetchedUser3 != null)
			throw new Exception("There is a user with id #3");
		
		assertNotNull("Uh oh, the user object was not created", fetchedUser);
		assertNotNull("Uh oh, the username was not saved", fetchedUser.getUsername());
		assertTrue("Uh oh, the username was not saved", fetchedUser.getUsername().equals("bwolfe"));
		assertTrue("Uh oh, the role was not assigned", fetchedUser.hasRole("Some Role"));
		
		Context.clearSession();
		
		// there should only be 2 users in the system. (the super user that is
		// authenticated to this test and the user we just created)
		List<User> allUsers = userService.getAllUsers();
		assertEquals(2, allUsers.size());
		
		// there should still only be the one patient we created in the xml file
		Cohort allPatientsSet = Context.getPatientSetService().getAllPatients();
		assertEquals(1, allPatientsSet.getSize());
		
		deleteAllData();
	}
	
	/**
	 * Test that we can update a user
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateUsername() throws Exception {
		UserService us = Context.getUserService();
		
		User u = us.getUserByUsername("admin");
		assertNotNull("There needs to be a user with username 'admin' in the database", u);
		
		u.setUsername("admin2");
		us.saveUser(u, null);
		
		User u2 = us.getUserByUsername("admin2");
		
		assertEquals("The fetched user should equal the user we tried to update", u, u2);
	}
	
	/**
	 * Test changing a user's password multiple times in the same transaction
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdatePasswordMultipleTimes() throws Exception {
		UserService us = Context.getUserService();
		
		User u = us.getUserByUsername("admin");
		assertNotNull("There needs to be a user with username 'admin' in the database", u);
		
		us.changePassword("test", "test2");
		us.changePassword("test2", "test");
	}
	
	/**
	 * Make sure we can grant roles to users
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGrantRoles() throws Exception {
		UserService us = Context.getUserService();
		
		// add in some basic properties
		executeDataSet(XML_FILENAME);
		
		User u = us.getUserByUsername("admin");
		//int len = u.getRoles().size();
		//System.out.println("length: " + len);
		
		Role role1 = new Role();
		role1.setDescription("testing1");
		role1.setRole("test1");
		Privilege p1 = us.getAllPrivileges().get(0);
		Set<Privilege> privileges1 = new HashSet<Privilege>();
		privileges1.add(p1);
		role1.setPrivileges(privileges1);
		
		Role role2 = new Role();
		role2.setDescription("testing2");
		role2.setRole("test2");
		Privilege p2 = us.getAllPrivileges().get(0);
		Set<Privilege> privileges2 = new HashSet<Privilege>();
		privileges2.add(p2);
		role2.setPrivileges(privileges2);
		
		us.saveUser(u.addRole(role1), null);

		us.saveUser(u.addRole(role2), null);
		
	}

}
