/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openmrs.test.TestUtil.containsId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.openmrs.util.Security;

/**
 * TODO add more tests to cover the methods in <code>UserService</code>
 */
public class UserServiceTest extends BaseContextSensitiveTest {
	
	protected static final String XML_FILENAME = "org/openmrs/api/include/UserServiceTest.xml";
	
	protected static final String XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION = "org/openmrs/api/include/UserServiceTest-changePasswordAction.xml";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * Methods in this class might authenticate with a different user, so log that user out after
	 * this whole junit class is done.
	 */
	@AfterClass
	public static void logOutAfterThisTest() {
		Context.logout();
	}
	
	/**
	 * Test that we can create a user
	 * 
	 * @see {@link UserService#saveUser(User,String)}
	 */
	@Test
	@Verifies(value = "should create new user with basic elements", method = "saveUser(User,String)")
	public void saveUser_shouldCreateNewUserWithBasicElements() throws Exception {
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		UserService us = Context.getUserService();
		
		User u = new User();
		u.setPerson(new Person());
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		
		User createdUser = us.saveUser(u, "Openmr5xy");
		
		// if we're returning the object from create methods, check validity
		assertTrue("The user returned by the create user method should equal the passed in user", createdUser.equals(u));
		
		createdUser = us.getUserByUsername("bwolfe");
		assertTrue("The created user should equal the passed in user", createdUser.equals(u));
	}
	
	/**
	 * Creates a user object that was a patient/person object already.
	 * 
	 * @throws Exception
	 * @see {@link UserService#saveUser(User,String)}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should should create user who is patient already", method = "saveUser(User,String)")
	public void saveUser_shouldShouldCreateUserWhoIsPatientAlready() throws Exception {
		// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		// add in some basic data
		executeDataSet(XML_FILENAME);
		
		UserService userService = Context.getUserService();
		
		// the user should not exist yet
		User preliminaryFetchedUser = userService.getUser(2);
		assertNull(preliminaryFetchedUser);
		
		// get the person object we'll make into a user
		Person personToMakeUser = Context.getPersonService().getPerson(2);
		// this avoids a lazy init exception, since we're going to clear the session
		((Patient) personToMakeUser).getIdentifiers().size();
		Context.clearSession();
		// this is the user object we'll be saving
		User user = new User(personToMakeUser);
		
		user.setUsername("bwolfe");
		user.setSystemId("asdf");
		user.addRole(new Role("Some Role", "This is a test role")); //included in xml file
		
		// make sure everything was added to the user correctly
		assertTrue(user.getUsername().equals("bwolfe"));
		assertTrue(user.hasRole("Some Role"));
		
		// do the actual creating of the user object
		userService.saveUser(user, "Openmr5xy");
		Assert.assertNotNull("User was not created", userService.getUser(user.getUserId()));
		
		Integer shouldCreateUserWhoIsPatientAlreadyTestUserIdCreated = user.getUserId();
		
		Context.flushSession();
		
		// get the same user we just created and make sure the user portion exists
		User fetchedUser = userService.getUser(shouldCreateUserWhoIsPatientAlreadyTestUserIdCreated);
		User fetchedUser3 = userService.getUser(3);
		if (fetchedUser3 != null)
			throw new Exception("There is a user with id #3");
		
		assertNotNull("Uh oh, the user object was not created", fetchedUser);
		assertNotNull("Uh oh, the username was not saved", fetchedUser.getUsername());
		assertTrue("Uh oh, the username was not saved", fetchedUser.getUsername().equals("bwolfe"));
		assertTrue("Uh oh, the role was not assigned", fetchedUser.hasRole("Some Role"));
		
		Context.clearSession();
		
		List<User> allUsers = userService.getAllUsers();
		assertEquals(11, allUsers.size());
		
		// there should still only be the one patient we created in the xml file
		Cohort allPatientsSet = Context.getPatientSetService().getAllPatients();
		assertEquals(1, allPatientsSet.getSize());
	}
	
	/**
	 * Test that we can update a user
	 * 
	 * @see {@link UserService#saveUser(User,String)}
	 */
	@Test
	@Verifies(value = "should update users username", method = "saveUser(User,String)")
	public void saveUser_shouldUpdateUsersUsername() throws Exception {
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
	 * @see {@link UserService#changePassword(String,String)}
	 */
	@Test
	@Verifies(value = "should be able to update password multiple times", method = "changePassword(String,String)")
	public void changePassword_shouldBeAbleToUpdatePasswordMultipleTimes() throws Exception {
		UserService us = Context.getUserService();
		
		User u = us.getUserByUsername("admin");
		assertNotNull("There needs to be a user with username 'admin' in the database", u);
		
		us.changePassword("test", "test2");
		us.changePassword("test2", "test");
	}
	
	/**
	 * Make sure we can grant roles to users
	 * 
	 * @see {@link UserService#saveUser(User,String)}
	 */
	@Test
	@Verifies(value = "should grant new roles in roles list to user", method = "saveUser(User,String)")
	public void saveUser_shouldGrantNewRolesInRolesListToUser() throws Exception {
		UserService us = Context.getUserService();
		
		// add in some basic properties
		executeDataSet(XML_FILENAME);
		
		User u = us.getUserByUsername("admin");
		
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
		
		// so the contents are fetched from the db
		Context.evictFromSession(u);
		
		us.getUser(u.getUserId()).hasRole("test1");
		us.getUser(u.getUserId()).hasRole("test2");
	}
	
	/**
	 * @see {@link UserService#getUserByUsername(String)}
	 */
	@Test
	@Verifies(value = "should get user by username", method = "getUserByUsername(String)")
	public void getUserByUsername_shouldGetUserByUsername() throws Exception {
		UserService us = Context.getUserService();
		String username = "admin";
		User user = us.getUserByUsername(username);
		assertNotNull("username not found " + username, user);
	}
	
	/**
	 * @see {@link UserService#changePassword(String,String)}
	 */
	@Test
	@Verifies(value = "should match on incorrectly hashed sha1 stored password", method = "changePassword(String,String)")
	public void changePassword_shouldMatchOnIncorrectlyHashedSha1StoredPassword() throws Exception {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("incorrectlyhashedSha1", "test");
		
		UserService us = Context.getUserService();
		us.changePassword("test", "test2");
		
		Context.logout(); // so that the next test reauthenticates
	}
	
	/**
	 * @see {@link UserService#changeQuestionAnswer(String,String,String)}
	 */
	@Test
	@Verifies(value = "should match on correctly hashed stored password", method = "changeQuestionAnswer(String,String,String)")
	public void changeQuestionAnswer_shouldMatchOnCorrectlyHashedStoredPassword() throws Exception {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("correctlyhashedSha1", "test");
		
		UserService us = Context.getUserService();
		us.changeQuestionAnswer("test", "some question", "some answer");
		
		Context.logout(); // so that the next test reauthenticates
	}
	
	/**
	 * @see {@link UserService#changeQuestionAnswer(String,String,String)}
	 */
	@Test
	@Verifies(value = "should match on incorrectly hashed stored password", method = "changeQuestionAnswer(String,String,String)")
	public void changeQuestionAnswer_shouldMatchOnIncorrectlyHashedStoredPassword() throws Exception {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("incorrectlyhashedSha1", "test");
		
		UserService us = Context.getUserService();
		us.changeQuestionAnswer("test", "some question", "some answer");
		
		Context.logout(); // so that the next test reauthenticates
	}
	
	/**
	 * @see {@link UserService#changePassword(String,String)}
	 */
	@Test
	@Verifies(value = "should match on correctly hashed sha1 stored password", method = "changePassword(String,String)")
	public void changePassword_shouldMatchOnCorrectlyHashedSha1StoredPassword() throws Exception {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("correctlyhashedSha1", "test");
		
		UserService us = Context.getUserService();
		us.changePassword("test", "test2");
		
		Context.logout(); // so that the next test reauthenticates
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should match search to familyName2", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldMatchSearchToFamilyName2() throws Exception {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-extranames.xml");
		
		List<User> users = Context.getUserService().getUsers("Johnson", null, false);
		Assert.assertEquals(3, users.size());
		Assert.assertTrue(containsId(users, 2));
		Assert.assertTrue(containsId(users, 4));
		Assert.assertTrue(containsId(users, 5));
	}
	
	/**
	 * @see {@link UserService#changePassword(String,String)}
	 */
	@Test
	@Verifies(value = "should match on sha512 hashed password", method = "changePassword(String,String)")
	public void changePassword_shouldMatchOnSha512HashedPassword() throws Exception {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("userWithSha512Hash", "test");
		
		UserService us = Context.getUserService();
		us.changePassword("test", "test2");
		
		Context.logout(); // so that the next test reauthenticates
	}
	
	/**
	 * This test verifies that {@link PersonName}s are fetched correctly from the hibernate cache.
	 * (Or really, not fetched from the cache but instead are mapped with lazy=false. For some
	 * reason Hibernate isn't able to find objects in the cache if a parent object was the one that
	 * loaded them)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFetchNamesForPersonsThatWereFirstFetchedAsUsers() throws Exception {
		Person person = Context.getPersonService().getPerson(1);
		User user = Context.getUserService().getUser(1);
		
		user.getNames().size();
		person.getNames().size();
	}
	
	/**
	 * @see {@link UserService#getPrivilegeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPrivilegeByUuid(String)")
	public void getPrivilegeByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		executeDataSet(XML_FILENAME);
		String uuid = "d979d066-15e6-467c-9d4b-cb575ef97f0f";
		Privilege privilege = Context.getUserService().getPrivilegeByUuid(uuid);
		Assert.assertEquals("Some Privilege", privilege.getPrivilege());
	}
	
	/**
	 * @see {@link UserService#getPrivilegeByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPrivilegeByUuid(String)")
	public void getPrivilegeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getUserService().getPrivilegeByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link UserService#getRoleByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getRoleByUuid(String)")
	public void getRoleByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "3480cb6d-c291-46c8-8d3a-96dc33d199fb";
		Role role = Context.getUserService().getRoleByUuid(uuid);
		Assert.assertEquals("Provider", role.getRole());
	}
	
	/**
	 * @see {@link UserService#getRoleByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getRoleByUuid(String)")
	public void getRoleByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getUserService().getRoleByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link UserService#getUserByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getUserByUuid(String)")
	public void getUserByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "c1d8f5c2-e131-11de-babe-001e378eb67e";
		User user = Context.getUserService().getUserByUuid(uuid);
		Assert.assertEquals(501, (int) user.getUserId());
	}
	
	/**
	 * @see {@link UserService#getUserByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getUserByUuid(String)")
	public void getUserByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getUserService().getUserByUuid("some invalid uuid"));
	}
	
	/**
	 * @see {@link UserService#changeHashedPassword(User,String,String)}
	 */
	@Test
	@Verifies(value = "should change the hashed password for the given user", method = "changeHashedPassword(User,String,String)")
	public void changeHashedPassword_shouldChangeTheHashedPasswordForTheGivenUser() throws Exception {
		UserService userService = Context.getUserService();
		User user = userService.getUser(1);
		String salt = Security.getRandomToken();
		String hash = Security.encodeString("new password" + salt);
		userService.changeHashedPassword(user, hash, salt);
		
		// TODO Review this a little further
		// This is the assert - checks to see if current user can use the new password
		userService.changePassword("new password", "another new password"); // try to change the password with the new one
		
	}
	
	/**
	 * @see {@link UserService#changePassword(User,String)}
	 */
	@Test
	@Verifies(value = "should change password for the given user and password", method = "changePassword(User,String)")
	public void changePassword_shouldChangePasswordForTheGivenUserAndPassword() throws Exception {
		UserService userService = Context.getUserService();
		userService.changePassword("test", "another new password");
		userService.changePassword("another new password", "yet another new password"); // try to change the password with the new one
	}
	
	/**
	 * @see {@link UserService#changeQuestionAnswer(User,String,String)}
	 */
	@Test
	@Ignore
	// TODO fix: the question not sticking - null expected:<[the question]> but was:<[]>
	@Verifies(value = "should change the secret question and answer for given user", method = "changeQuestionAnswer(User,String,String)")
	public void changeQuestionAnswer_shouldChangeTheSecretQuestionAndAnswerForGivenUser() throws Exception {
		UserService userService = Context.getUserService();
		User u = userService.getUser(501);
		userService.changeQuestionAnswer(u, "the question", "the answer");
		
		// need to retrieve the user since the service method does not modify the given user object
		User o = userService.getUser(501);
		Assert.assertEquals("the question", o.getSecretQuestion());
		Assert.assertTrue(userService.isSecretAnswer(o, "the answer"));
	}
	
	/**
	 * @see {@link UserService#getAllPrivileges()}
	 */
	@Test
	@Verifies(value = "should return all privileges in the system", method = "getAllPrivileges()")
	public void getAllPrivileges_shouldReturnAllPrivilegesInTheSystem() throws Exception {
		executeDataSet(XML_FILENAME);
		List<Privilege> privileges = Context.getUserService().getAllPrivileges();
		Assert.assertEquals(1, privileges.size());
	}
	
	/**
	 * @see {@link UserService#getAllRoles()}
	 */
	@Test
	@Verifies(value = "should return all roles in the system", method = "getAllRoles()")
	public void getAllRoles_shouldReturnAllRolesInTheSystem() throws Exception {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = Context.getUserService().getAllRoles();
		Assert.assertEquals(7, roles.size());
	}
	
	/**
	 * @see {@link UserService#getAllUsers()}
	 */
	@Test
	@Verifies(value = "should fetch all users in the system", method = "getAllUsers()")
	public void getAllUsers_shouldFetchAllUsersInTheSystem() throws Exception {
		List<User> users = Context.getUserService().getAllUsers();
		Assert.assertEquals(4, users.size());
	}
	
	/**
	 * @see {@link UserService#getAllUsers()}
	 */
	@Test
	@Verifies(value = "should not contains any duplicate users", method = "getAllUsers()")
	public void getAllUsers_shouldNotContainsAnyDuplicateUsers() throws Exception {
		executeDataSet(XML_FILENAME);
		List<User> users = Context.getUserService().getAllUsers();
		Assert.assertEquals(12, users.size());
		// TODO Need to test with duplicate data in the dataset (not sure if that's possible)
		
	}
	
	/**
	 * @verifies {@link UserService#getUserByUuid(String)} test = should fetch user with given uuid
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should fetch user with given uuid", method = "getUserByUuid(String)")
	public void getUserByUuid_shouldFetchUserWithGivenUuid() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(XML_FILENAME);
		authenticate();
		
		UserService userService = Context.getUserService();
		User user = userService.getUserByUuid("013c49c6-e132-11de-babe-001e378eb67e");
		assertEquals("Did not fetch user with given uuid", user, userService.getUser(5505));
	}
	
	/**
	 * @verifies {@link UserService#getUsersByName(String,String,null)} test = should fetch users
	 *           exactly matching the given givenName and familyName
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should fetch users exactly matching the given givenName and familyName", method = "getUsersByName(String,String,boolean)")
	public void getUsersByName_shouldFetchUsersExactlyMatchingTheGivenGivenNameAndFamilyName() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(XML_FILENAME);
		authenticate();
		
		UserService userService = Context.getUserService();
		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName] 
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName 
		// = :familyName and u.voided = false]
		List<User> users = userService.getUsersByName("Susy", "Kingman", false);
		assertEquals(1, users.size());
	}
	
	/**
	 * @verifies {@link UserService#getUsersByName(String,String,null)} test = should fetch voided
	 *           users whenincludeVoided is true
	 */
	@Test
	public void getUsersByName_shouldFetchVoidedUsersWhenincludeVoidedIsTrue() throws Exception {
		
		UserService userService = Context.getUserService();
		User voidedUser = userService.getUser(501);
		// assertTrue(voidedUser.isVoided());
		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName]
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName
		// = :familyName]
		List<User> users = userService.getUsersByName("Bruno", "Otterbourg", true);
		assertTrue(users.contains(voidedUser));
	}
	
	/**
	 * @verifies {@link UserService#getUsersByName(String,String,null)} test = should not fetch any
	 *           voided users when includeVoided is false
	 */
	@Test
	public void getUsersByName_shouldNotFetchAnyVoidedUsersWhenIncludeVoidedIsFalse() throws Exception {
		
		UserService userService = Context.getUserService();
		User voidedUser = userService.getUser(501);
		// assertTrue(voidedUser.isVoided());
		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName]
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName
		// = :familyName and u.voided = false]
		List<User> users = userService.getUsersByName("Bruno", "Otterbourg", false);
		assertFalse(users.contains(voidedUser));
	}
	
	/**
	 * @verifies {@link UserService#getUsersByName(String,String,null)} test = should not fetch any
	 *           duplicate users
	 */
	@Test
	@SkipBaseSetup
	public void getUsersByName_shouldNotFetchAnyDuplicateUsers() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet(XML_FILENAME);
		authenticate();
		
		UserService userService = Context.getUserService();
		// user with ID 4 has a preferred name "John Doe" and a not preferred name "John Doe."
		// If this method does not fetch any duplicate users, this user should only 
		// appear once in the list of users that are returned with this method.
		
		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName]
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName
		// = :familyName and u.voided = false]
		List<User> users = userService.getUsersByName("John", "Doe", false);
		assertEquals(1, users.size());
		
	}
	
	/**
	 * @see {@link UserService#getPrivilege(String)}
	 */
	@Test
	@Verifies(value = "should fetch privilege for given name", method = "getPrivilege(String)")
	public void getPrivilege_shouldFetchPrivilegeForGivenName() throws Exception {
		executeDataSet(XML_FILENAME);
		Privilege privilege = Context.getUserService().getPrivilege("Some Privilege");
		Assert.assertEquals("Some Privilege", privilege.getPrivilege());
	}
	
	/**
	 * @see {@link UserService#getRole(String)}
	 */
	@Test
	@Verifies(value = "should fetch role for given role name", method = "getRole(String)")
	public void getRole_shouldFetchRoleForGivenRoleName() throws Exception {
		executeDataSet(XML_FILENAME);
		Role role = Context.getUserService().getRole("Some Role");
		Assert.assertEquals("Some Role", role.getRole());
	}
	
	/**
	 * @see {@link UserService#getUser(Integer)}
	 */
	@Test
	@Verifies(value = "should fetch user with given userId", method = "getUser(Integer)")
	public void getUser_shouldFetchUserWithGivenUserId() throws Exception {
		User user = Context.getUserService().getUser(501);
		Assert.assertEquals(501, user.getUserId().intValue());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should fetch users with at least one of the given role objects", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldFetchUsersWithAtLeastOneOfTheGivenRoleObjects() throws Exception {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = Collections.singletonList(new Role("Some Role"));
		Assert.assertEquals(1, Context.getUserService().getUsers("Susy Kingman", roles, false).size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should fetch users with name that contains given nameSearch", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldFetchUsersWithNameThatContainsGivenNameSearch() throws Exception {
		Assert.assertEquals(1, Context.getUserService().getUsers("Hippocrates", null, false).size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should fetch users with systemId that contains given nameSearch", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldFetchUsersWithSystemIdThatContainsGivenNameSearch() throws Exception {
		Assert.assertEquals(1, Context.getUserService().getUsers("2-6", null, true).size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should fetch voided users if includedVoided is true", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldFetchVoidedUsersIfIncludedVoidedIsTrue() throws Exception {
		Assert.assertEquals(1, Context.getUserService().getUsers("Bruno", null, true).size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should fetch all users if nameSearch is empty or null", method = "getUsers(String,List,null)")
	public void getUsers_shouldFetchAllUsersIfNameSearchIsEmptyOrNull() throws Exception {
		Assert.assertEquals(4, Context.getUserService().getUsers("", null, true).size());
		Assert.assertEquals(4, Context.getUserService().getUsers(null, null, true).size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should not fetch duplicate users", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldNotFetchDuplicateUsers() throws Exception {
		executeDataSet(XML_FILENAME);
		
		List<User> users = Context.getUserService().getUsers("John Doe", null, false);
		Assert.assertEquals(1, users.size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String,List,boolean)}
	 */
	@Test
	@Verifies(value = "should not fetch voided users if includedVoided is false", method = "getUsers(String,List,boolean)")
	public void getUsers_shouldNotFetchVoidedUsersIfIncludedVoidedIsFalse() throws Exception {
		Assert.assertEquals(0, Context.getUserService().getUsers("Bruno", null, false).size());
	}
	
	/**
	 * @see {@link UserService#getUsersByRole(Role)}
	 */
	@Test
	@Verifies(value = "should fetch users assigned given role", method = "getUsersByRole(Role)")
	public void getUsersByRole_shouldFetchUsersAssignedGivenRole() throws Exception {
		executeDataSet(XML_FILENAME);
		
		Assert.assertEquals(2, Context.getUserService().getUsersByRole(new Role("Some Role")).size());
	}
	
	/**
	 * @see {@link UserService#getUsersByRole(Role)}
	 */
	@Test
	@Verifies(value = "should not fetch user that does not belong to given role", method = "getUsersByRole(Role)")
	public void getUsersByRole_shouldNotFetchUserThatDoesNotBelongToGivenRole() throws Exception {
		executeDataSet(XML_FILENAME);
		
		Assert.assertEquals(0, Context.getUserService().getUsersByRole(new Role("Nonexistent role")).size());
	}
	
	/**
	 * @see {@link UserService#hasDuplicateUsername(User)}
	 */
	@Test
	@Verifies(value = "should verify that username and system id is unique", method = "hasDuplicateUsername(User)")
	public void hasDuplicateUsername_shouldVerifyThatUsernameAndSystemIdIsUnique() throws Exception {
		executeDataSet(XML_FILENAME);
		
		User user = new User();
		user.setSystemId("8-3");
		user.setUsername("a unique username");
		Assert.assertTrue(Context.getUserService().hasDuplicateUsername(user));
		
		user = new User();
		user.setSystemId("a unique system id");
		user.setUsername("userWithSha512Hash");
		Assert.assertTrue(Context.getUserService().hasDuplicateUsername(user));
	}
	
	/**
	 * @see {@link UserService#isSecretAnswer(User,String)}
	 */
	@Test
	@Verifies(value = "should return false when given answer does not match the stored secret answer", method = "isSecretAnswer(User,String)")
	public void isSecretAnswer_shouldReturnFalseWhenGivenAnswerDoesNotMatchTheStoredSecretAnswer() throws Exception {
		User user = Context.getUserService().getUser(502);
		Assert.assertFalse(Context.getUserService().isSecretAnswer(user, "not the answer"));
	}
	
	/**
	 * @see {@link UserService#isSecretAnswer(User,String)}
	 */
	@Test
	@Verifies(value = "should return true when given answer matches stored secret answer", method = "isSecretAnswer(User,String)")
	public void isSecretAnswer_shouldReturnTrueWhenGivenAnswerMatchesStoredSecretAnswer() throws Exception {
		executeDataSet(XML_FILENAME);
		User user = Context.getUserService().getUser(5507);
		Context.getUserService().changeQuestionAnswer(user, "question", "answer");
		Assert.assertTrue(Context.getUserService().isSecretAnswer(user, "answer"));
	}
	
	/**
	 * @see {@link UserService#purgePrivilege(Privilege)}
	 */
	@Test
	@Verifies(value = "should delete given privilege from the database", method = "purgePrivilege(Privilege)")
	public void purgePrivilege_shouldDeleteGivenPrivilegeFromTheDatabase() throws Exception {
		Context.getUserService().purgePrivilege(new Privilege("Some Privilege"));
		Assert.assertNull(Context.getUserService().getPrivilege("Some Privilege"));
	}
	
	/**
	 * @see {@link UserService#purgePrivilege(Privilege)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw error when privilege is core privilege", method = "purgePrivilege(Privilege)")
	public void purgePrivilege_shouldThrowErrorWhenPrivilegeIsCorePrivilege() throws Exception {
		Context.getUserService().purgePrivilege(new Privilege(PrivilegeConstants.ADD_COHORTS));
	}
	
	/**
	 * @see {@link UserService#purgeRole(Role)}
	 */
	@Test
	@Verifies(value = "should delete given role from database", method = "purgeRole(Role)")
	public void purgeRole_shouldDeleteGivenRoleFromDatabase() throws Exception {
		executeDataSet(XML_FILENAME);
		Role role = Context.getUserService().getRole("Some Role To Delete");
		Context.getUserService().purgeRole(role);
		Assert.assertNull(Context.getUserService().getRole("Some Role To Delete"));
	}
	
	/**
	 * @see {@link UserService#purgeRole(Role)}
	 */
	@Test
	@Verifies(value = "should return if role is null", method = "purgeRole(Role)")
	public void purgeRole_shouldReturnIfRoleIsNull() throws Exception {
		Context.getUserService().purgeRole(null);
	}
	
	/**
	 * @see {@link UserService#purgeRole(Role)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw error when role is a core role", method = "purgeRole(Role)")
	public void purgeRole_shouldThrowErrorWhenRoleIsACoreRole() throws Exception {
		Role role = new Role(RoleConstants.ANONYMOUS);
		Context.getUserService().purgeRole(role);
	}
	
	/**
	 * @see {@link UserService#purgeUser(User)}
	 */
	@Test(expected = CannotDeleteRoleWithChildrenException.class)
	@Verifies(value = "should throw error when role has child roles", method = "purgeRole(Role)")
	public void purgeRole_shouldThrowErrorWhenRoleHasChildRoles() throws Exception {
		Set<Role> childRole = new HashSet<Role>();
		Role role1 = new Role("role_parent");
		Role role2 = new Role("role_child");
		childRole.add(role1);
		role2.setChildRoles(childRole);
		Context.getUserService().purgeRole(role2);
	}
	
	/**
	 * @see {@link UserService#purgeUser(User)}
	 */
	@Test
	@Verifies(value = "should delete given user", method = "purgeUser(User)")
	public void purgeUser_shouldDeleteGivenUser() throws Exception {
		User user = Context.getUserService().getUser(502);
		Context.getUserService().purgeUser(user);
		Assert.assertNull(Context.getUserService().getUser(2));
	}
	
	/**
	 * @see {@link UserService#purgeUser(User,boolean)}
	 */
	@Test
	@Verifies(value = "should delete given user when cascade equals false", method = "purgeUser(User,boolean)")
	public void purgeUser_shouldDeleteGivenUserWhenCascadeEqualsFalse() throws Exception {
		User user = Context.getUserService().getUser(502);
		Context.getUserService().purgeUser(user, false);
		Assert.assertNull(Context.getUserService().getUser(502));
	}
	
	/**
	 * @see {@link UserService#purgeUser(User,boolean)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if cascade is true", method = "purgeUser(User,null)")
	public void purgeUser_shouldThrowAPIExceptionIfCascadeIsTrue() throws Exception {
		User user = Context.getUserService().getUser(502);
		Context.getUserService().purgeUser(user, true);
	}
	
	/**
	 * @see {@link UserService#removeUserProperty(User,String)}
	 */
	@Test
	@Verifies(value = "should remove user property for given user and key", method = "removeUserProperty(User,String)")
	public void removeUserProperty_shouldRemoveUserPropertyForGivenUserAndKey() throws Exception {
		executeDataSet(XML_FILENAME);
		
		UserService userService = Context.getUserService();
		User user = userService.getUser(5505);
		Assert.assertNotSame("", user.getUserProperty("some key"));
		
		userService.removeUserProperty(user, "some key");
		
		user = userService.getUser(5505);
		Assert.assertEquals("", user.getUserProperty("some key"));
	}
	
	/**
	 * @see {@link UserService#removeUserProperty(User,String)}
	 */
	@Test
	@Verifies(value = "should return null if user is null", method = "removeUserProperty(User,String)")
	public void removeUserProperty_shouldReturnNullIfUserIsNull() throws Exception {
		UserService userService = Context.getUserService();
		Assert.assertNull(userService.setUserProperty(null, "some key", "some new value"));
	}
	
	/**
	 * @see {@link UserService#removeUserProperty(User,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw error when user is not authorized to edit users", method = "removeUserProperty(User,String)")
	public void removeUserProperty_shouldThrowErrorWhenUserIsNotAuthorizedToEditUsers() throws Exception {
		executeDataSet(XML_FILENAME);
		
		UserService userService = Context.getUserService();
		User user = userService.getUser(5505);
		
		Context.logout();
		
		userService.removeUserProperty(user, "some key");
		
		//user = userService.getUser(5505);
		//Assert.assertNull(user.getUserProperty("some key"));
	}
	
	/**
	 * @see {@link UserService#savePrivilege(Privilege)}
	 */
	@Test
	@Verifies(value = "should save given privilege to the database", method = "savePrivilege(Privilege)")
	public void savePrivilege_shouldSaveGivenPrivilegeToTheDatabase() throws Exception {
		Privilege p = new Privilege("new privilege name", "new privilege desc");
		Context.getUserService().savePrivilege(p);
		
		Privilege savedPrivilege = Context.getUserService().getPrivilege("new privilege name");
		Assert.assertNotNull(savedPrivilege);
		
	}
	
	/**
	 * @see {@link UserService#setUserProperty(User,String,String)}
	 */
	@Test
	@Verifies(value = "should add property with given key and value when key does not already exist", method = "setUserProperty(User,String,String)")
	public void setUserProperty_shouldAddPropertyWithGivenKeyAndValueWhenKeyDoesNotAlreadyExist() throws Exception {
		executeDataSet(XML_FILENAME);
		
		UserService userService = Context.getUserService();
		User user = userService.getUser(5505);
		
		// Check that it doesn't already exist
		Assert.assertEquals(user.getUserProperty("some new key"), "");
		
		userService.setUserProperty(user, "some new key", "some new value");
		
		user = userService.getUser(5505);
		Assert.assertEquals("some new value", user.getUserProperty("some new key"));
	}
	
	/**
	 * @see {@link UserService#setUserProperty(User,String,String)}
	 */
	@Test
	@Verifies(value = "should modify property with given key and value when key already exists", method = "setUserProperty(User,String,String)")
	public void setUserProperty_shouldModifyPropertyWithGivenKeyAndValueWhenKeyAlreadyExists() throws Exception {
		executeDataSet(XML_FILENAME);
		
		UserService userService = Context.getUserService();
		User user = userService.getUser(5505);
		
		// Check that it already exists
		Assert.assertEquals(user.getUserProperty("some key"), "some value");
		
		userService.setUserProperty(user, "some key", "some new value");
		
		user = userService.getUser(5505);
		Assert.assertEquals("some new value", user.getUserProperty("some key"));
	}
	
	/**
	 * @see {@link UserService#setUserProperty(User,String,String)}
	 */
	@Test
	@Verifies(value = "should return null if user is null", method = "setUserProperty(User,String,String)")
	public void setUserProperty_shouldReturnNullIfUserIsNull() throws Exception {
		UserService userService = Context.getUserService();
		
		Assert.assertNull(userService.setUserProperty(null, "some key", "some value"));
	}
	
	/**
	 * @see {@link UserService#setUserProperty(User,String,String)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw error when user is not authorized to edit users", method = "setUserProperty(User,String,String)")
	public void setUserProperty_shouldThrowErrorWhenUserIsNotAuthorizedToEditUsers() throws Exception {
		UserService userService = Context.getUserService();
		User user = userService.getUser(502);
		
		Context.logout();
		userService.setUserProperty(user, "some key", "some value");
	}
	
	/**
	 * @see {@link UserService#saveRole(Role)}
	 */
	@Test
	@Verifies(value = "should save given role to the database", method = "saveRole(Role)")
	public void saveRole_shouldSaveGivenRoleToTheDatabase() throws Exception {
		Role role = new Role("new role", "new desc");
		Context.getUserService().saveRole(role);
		
		Assert.assertNotNull(Context.getUserService().getRole("new role"));
		
	}
	
	/**
	 * @see {@link UserService#saveRole(Role)}
	 */
	@Test
	@Verifies(value = "should throw error if role inherits from itself", method = "saveRole(Role)")
	public void saveRole_shouldThrowErrorIfRoleInheritsFromItself() throws Exception {
		Role parentRole = new Role("parent role");
		
		// Have child inherit parent role
		Role childRole = new Role("child role");
		Set<Role> inheritsFromParent = new HashSet<Role>();
		inheritsFromParent.add(parentRole);
		childRole.setInheritedRoles(inheritsFromParent);
		
		// Now have parent try to inherit the child role.
		Set<Role> inheritsFromChild = new HashSet<Role>();
		inheritsFromChild.add(childRole);
		parentRole.setInheritedRoles(inheritsFromChild);
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("Role.cannot.inherit.descendant"));
		Context.getUserService().saveRole(parentRole);
	}
	
	/**
	 * @see {@link UserService#getUsersByPerson(Person,null)}
	 */
	@Test
	@Verifies(value = "should fetch all accounts for a person when include retired is true", method = "getUsersByPerson(Person,null)")
	public void getUsersByPerson_shouldFetchAllAccountsForAPersonWhenIncludeRetiredIsTrue() throws Exception {
		executeDataSet(XML_FILENAME);
		Person person = new Person(5508);
		List<User> users = Context.getUserService().getUsersByPerson(person, true);
		Assert.assertEquals(3, users.size());
	}
	
	/**
	 * @see {@link UserService#getUsersByPerson(Person,null)}
	 */
	@Test
	@Verifies(value = "should not fetch retired accounts when include retired is false", method = "getUsersByPerson(Person,null)")
	public void getUsersByPerson_shouldNotFetchRetiredAccountsWhenIncludeRetiredIsFalse() throws Exception {
		executeDataSet(XML_FILENAME);
		Person person = new Person(5508);
		List<User> users = Context.getUserService().getUsersByPerson(person, false);
		Assert.assertEquals(2, users.size());
	}
	
	/**
	 * @see {@link UserService#retireUser(User,String)}
	 */
	@Test
	@Verifies(value = "should retire user and set attributes", method = "retireUser(User,String)")
	public void retireUser_shouldRetireUserAndSetAttributes() throws Exception {
		UserService userService = Context.getUserService();
		User user = userService.getUser(502);
		userService.retireUser(user, "because");
		Assert.assertTrue(user.isRetired());
		Assert.assertNotNull(user.getDateRetired());
		Assert.assertNotNull(user.getRetiredBy());
		Assert.assertEquals("because", user.getRetireReason());
	}
	
	/**
	 * @see {@link UserService#unretireUser(User)}
	 */
	@Test
	@Verifies(value = "should unretire and unmark all attributes", method = "unretireUser(User)")
	public void unretireUser_shouldUnretireAndUnmarkAllAttributes() throws Exception {
		UserService userService = Context.getUserService();
		User user = userService.getUser(501);
		userService.unretireUser(user);
		Assert.assertFalse(user.isRetired());
		Assert.assertNull(user.getDateRetired());
		Assert.assertNull(user.getRetiredBy());
		Assert.assertNull(user.getRetireReason());
	}
	
	/**
	 * Test that user is not created with a weak password
	 * 
	 * @see {@link UserService#saveUser(User,String)}
	 */
	@Test(expected = PasswordException.class)
	@Verifies(value = "fail to create the user with a weak password", method = "saveUser(User,String)")
	public void saveUser_shouldFailToCreateTheUserWithAWeakPassword() throws Exception {
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		UserService us = Context.getUserService();
		
		User u = new User();
		u.setPerson(new Person());
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		
		us.saveUser(u, "short");
	}
	
	/**
	 * This is a regression test for TRUNK-2108 <br/>
	 * 
	 * @see UserService#getUsers(String,List,boolean)
	 * @verifies not fail if roles are searched but name is empty
	 */
	@Test
	public void getUsers_shouldNotFailIfRolesAreSearchedButNameIsEmpty() throws Exception {
		Role role = new Role("Provider");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		
		Assert.assertEquals(2, Context.getUserService().getUsers("", roles, true).size());
	}
	
	/**
	 * @see {@link UserService#getUsers(String, List, boolean, Integer, Integer)}
	 */
	@Test
	@Verifies(value = "return users whose roles inherit requested roles", method = "getUsers(String,List,boolean,Integer,Integer)")
	public void getUsers_shouldReturnUsersWhoseRolesInheritRequestedRoles() throws Exception {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = new ArrayList<Role>();
		roles.add(Context.getUserService().getRole("Parent"));
		Assert.assertEquals(3, Context.getUserService().getUsers(null, roles, true, null, null).size());
	}
	
	@Test
	public void saveUserProperty_shouldAddNewPropertyToExistingUserProperties() throws Exception {
		executeDataSet(XML_FILENAME);
		
		final UserService userService = Context.getUserService();
		
		//  retrieve a user who has UserProperties
		User user = userService.getUser(5511);
		
		// Authenticate the test  user so that Context.getAuthenticatedUser() method returns above user
		Context.authenticate(user.getUsername(), "testUser1234");
		
		final int numberOfUserProperties = user.getUserProperties().size();
		assertEquals(1, user.getUserProperties().size());
		final String USER_PROPERTY_KEY = "test-key";
		final String USER_PROPERTY_VALUE = "test-value";
		
		User updatedUser = userService.saveUserProperty(USER_PROPERTY_KEY, USER_PROPERTY_VALUE);
		
		assertNotNull(updatedUser.getUserProperty(USER_PROPERTY_KEY));
		assertEquals(USER_PROPERTY_VALUE, updatedUser.getUserProperty(USER_PROPERTY_KEY));
		//make sure that properties count is incremented by one
		assertEquals((numberOfUserProperties + 1), updatedUser.getUserProperties().size());
		
	}
	
	@Test
	public void saveUserProperties_shouldRemoveAllExistingPropertiesAndAssignNewProperties() throws Exception {
		executeDataSet(XML_FILENAME);
		
		final UserService userService = Context.getUserService();
		
		//  retrieve a user who has UserProperties
		User user = userService.getUser(5511);
		assertEquals(1, user.getUserProperties().size());
		// Authenticate the test  user so that Context.getAuthenticatedUser() method returns above user
		Context.authenticate(user.getUsername(), "testUser1234");
		final String USER_PROPERTY_KEY_1 = "test-key1";
		final String USER_PROPERTY_VALUE_1 = "test-value1";
		final String USER_PROPERTY_KEY_2 = "test-key2";
		final String USER_PROPERTY_VALUE_2 = "test-value2";
		Map<String, String> propertiesMap = new HashMap<String, String>();
		propertiesMap.put(USER_PROPERTY_KEY_1, USER_PROPERTY_VALUE_1);
		propertiesMap.put(USER_PROPERTY_KEY_2, USER_PROPERTY_VALUE_2);
		propertiesMap = Collections.unmodifiableMap(propertiesMap);
		User updatedUser = userService.saveUserProperties(propertiesMap);
		
		//we should have only the new properties
		assertEquals(2, updatedUser.getUserProperties().size());
		
		//Verify that the new properties were saved
		assertEquals(USER_PROPERTY_VALUE_1, updatedUser.getUserProperty(USER_PROPERTY_KEY_1));
		assertEquals(USER_PROPERTY_VALUE_2, updatedUser.getUserProperty(USER_PROPERTY_KEY_2));
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 * @verifies change password for given user if oldPassword is correctly passed
	 */
	@Test
	public void changePassword_shouldChangePasswordForGivenUserIfOldPasswordIsCorrectlyPassed() throws Exception {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		final UserService userService = Context.getUserService();
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String oldPassword = "userServiceTest";
		String newPassword = "newPasswordString123";
		userService.changePassword(user6001, oldPassword, newPassword);
		//try to authenticate with new password
		Context.authenticate(user6001.getUsername(), newPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 * @verifies change password for given user if oldPassword is null and changing user have privileges
	 */
	@Test
	public void changePassword_shouldChangePasswordForGivenUserIfOldPasswordIsNullAndChangingUserHavePrivileges()
	        throws Exception {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		final UserService userService = Context.getUserService();
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String oldPassword = null;
		String newPassword = "newPasswordString123";
		userService.changePassword(user6001, oldPassword, newPassword);
		Context.authenticate(user6001.getUsername(), newPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 * @verifies throw APIException if old password is not correct
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfOldPasswordIsNotCorrect() throws Exception {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		final UserService userService = Context.getUserService();
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String wrongPassword = "wrong password!";
		String newPassword = "newPasswordString";
		//log in user without change user passwords privileges
		//user6001 has not got required priviliges
		Context.authenticate(user6001.getUsername(), "userServiceTest");
		
		expectedException.expect(APIAuthenticationException.class);
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("error.privilegesRequired"));
		userService.changePassword(user6001, wrongPassword, newPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 * @verifies throw exception if oldPassword is null and changing user have not privileges
	 */
	@Test
	public void changePassword_shouldThrowExceptionIfOldPasswordIsNullAndChangingUserHaveNotPrivileges() throws Exception {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		final UserService userService = Context.getUserService();
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		assertFalse(user6001.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		String oldPassword = null;
		String newPassword = "newPasswordString";
		//log in user without change user passwords privileges
		//user6001 has not got required priviliges
		Context.authenticate(user6001.getUsername(), "userServiceTest");
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("error.privilegesRequired"));
		userService.changePassword(user6001, oldPassword, newPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 * @verifies throw exception if new password is too short
	 */
	@Test
	public void changePassword_shouldThrowExceptionIfNewPasswortIsTooShort() throws Exception {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		final UserService userService = Context.getUserService();
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String oldPassword = "userServiceTest";
		String weakPassword = "weak";
		
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("error.password.length"));
		userService.changePassword(user6001, oldPassword, weakPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 * @verifies throw APIException if given user does not exist
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfGivenUserDoesNotExist() throws Exception {
		//user.getUserId is null - so it is not existing user
		User notExistingUser = new User();
		final UserService userService = Context.getUserService();
		String anyString = "anyString";
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage("user.must.exist");
		userService.changePassword(notExistingUser, anyString, anyString);
	}
}
