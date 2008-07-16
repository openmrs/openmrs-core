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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

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
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpBeforeTransaction()
	 */
	@Override
	protected void onSetUpInTransaction() throws Exception {
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
	public void testCreateUser() throws Exception {
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		UserService us = Context.getUserService();
		
		User u = new User();
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.setGender("M");
		
		User createdUser = us.createUser(u, "some arbitrary password to use");
		
		// if we're returning the object from create methods, check validity
		assertTrue("The user returned by the create user method should equal the passed in user", createdUser.equals(u));
		
		createdUser = us.getUserByUsername("bwolfe");
		assertTrue("The created user should equal the passed in user", createdUser.equals(u));
	}
	
	/**
	 * Creates a user object that was a patient/person object already
	 * 
	 * @throws Exception
	 */
	public void testCreateUserWhoIsPatientAlready() throws Exception {
		
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
		userService.updateUser(user);
		
		// commit the user and data so that we can simulate a new page being loaded
		commitTransaction(true);
		
		// clear out the session so that we don't get a hibernate error 
		// about "object already in session"
		Context.clearSession();
		
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
		List<User> allUsers = userService.getUsers();
		assertEquals(2, allUsers.size());
		
		// there should still only be the one patient we created in the xml file
		Cohort allPatientsSet = Context.getPatientSetService().getAllPatients();
		assertEquals(1, allPatientsSet.getSize());
	}
	
	/**
	 * Test that we can update a user
	 * 
	 * @throws Exception
	 */
	public void testUpdateUsername() throws Exception {
		UserService us = Context.getUserService();
		
		User u = us.getUserByUsername("admin");
		assertNotNull("There needs to be a user with username 'admin' in the database", u);
		
		u.setUsername("admin2");
		us.updateUser(u);
		
		User u2 = us.getUserByUsername("admin2");
		
		assertEquals("The fetched user should equal the user we tried to update", u, u2);
	}
	
	/**
	 * Test changing a user's password multiple times in the same transaction
	 * 
	 * @throws Exception
	 */
	public void testUpdatePasswordMultipleTimes() throws Exception {
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
	public void testGrantRoles() throws Exception {
		UserService us = Context.getUserService();
		
		// add in some basic properties
		executeDataSet(XML_FILENAME);
		
		User u = us.getUserByUsername("admin");
		//int len = u.getRoles().size();
		//System.out.println("length: " + len);
		
		Role role1 = new Role();
		role1.setDescription("testing1");
		role1.setRole("test1");
		Privilege p1 = us.getPrivileges().get(0);
		Set<Privilege> privileges1 = new HashSet<Privilege>();
		privileges1.add(p1);
		role1.setPrivileges(privileges1);
		
		Role role2 = new Role();
		role2.setDescription("testing2");
		role2.setRole("test2");
		Privilege p2 = us.getPrivileges().get(0);
		Set<Privilege> privileges2 = new HashSet<Privilege>();
		privileges2.add(p2);
		role2.setPrivileges(privileges2);
		
		us.grantUserRole(u, role1);
		
		us.grantUserRole(u, role2);
		
	}

}
