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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.test.TestUtil.containsId;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.notification.MessageException;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.openmrs.util.Security;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO add more tests to cover the methods in <code>UserService</code>
 */
public class UserServiceTest extends BaseContextSensitiveTest {

	protected static final String XML_FILENAME = "org/openmrs/api/include/UserServiceTest.xml";
	
	protected static final String XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION = "org/openmrs/api/include/UserServiceTest-changePasswordAction.xml";

	protected static final String SOME_VALID_PASSWORD = "s0mePassword";

	public static final String SOME_USERNAME = "butch";
	
	private static final boolean CurrentUser = false;

	private final String ADMIN_USERNAME = "admin";

	private UserService userService;

	private MessageSourceService messages;
	
	@Autowired
	private UserDAO dao;
	
	@BeforeEach
	public void setup() {
		userService = Context.getUserService();
		messages = Context.getMessageSourceService();
	}

	/**
	 * Methods in this class might authenticate with a different user, so log that user out after
	 * this whole junit class is done.
	 */
	@AfterAll
	public static void logOutAfterThisTest() {
		Context.logout();
	}
	
	@Test
	public void createUser_shouldCreateNewUserWithBasicElements() {
		assertTrue(Context.isAuthenticated(), "The context needs to be correctly authenticated to by a user");

		User u = new User();
		u.setPerson(new Person());
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		
		User createdUser = userService.createUser(u, "Openmr5xy");
		
		// if we're returning the object from create methods, check validity
		assertTrue(createdUser.equals(u), "The user returned by the create user method should equal the passed in user");
		
		createdUser = userService.getUserByUsername("bwolfe");
		assertTrue(createdUser.equals(u), "The created user should equal the passed in user");
	}
	
	@Test
	@SkipBaseSetup
	public void createUser_shouldShouldCreateUserWhoIsPatientAlready() throws SQLException {
		// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		assertTrue(Context.isAuthenticated(), "The context needs to be correctly authenticated to by a user");
		
		// add in some basic data
		executeDataSet(XML_FILENAME);

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
		userService.createUser(user, "Openmr5xy");
		assertNotNull(userService.getUser(user.getUserId()), "User was not created");
		
		Integer shouldCreateUserWhoIsPatientAlreadyTestUserIdCreated = user.getUserId();
		
		Context.flushSession();
		
		// get the same user we just created and make sure the user portion exists
		User fetchedUser = userService.getUser(shouldCreateUserWhoIsPatientAlreadyTestUserIdCreated);
		User fetchedUser3 = userService.getUser(3);
		if (fetchedUser3 != null) {
			throw new RuntimeException("There is a user with id #3");
		}
		
		assertNotNull(fetchedUser, "Uh oh, the user object was not created");
		assertNotNull(fetchedUser.getUsername(), "Uh oh, the username was not saved");
		assertTrue(fetchedUser.getUsername().equals("bwolfe"), "Uh oh, the username was not saved");
		assertTrue(fetchedUser.hasRole("Some Role"), "Uh oh, the role was not assigned");
		
		Context.clearSession();
		
		List<User> allUsers = userService.getAllUsers();
		assertEquals(10, allUsers.size());
		
		// there should still only be the one patient we created in the xml file
		List<Patient> allPatientsSet = Context.getPatientService().getAllPatients();
		assertEquals(1, allPatientsSet.size());
	}

	@Test
	public void createUser_shouldNotAllowExistingUser() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		APIException exception = assertThrows(APIException.class, () -> userService.createUser(someUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is("This method can be used for only creating new users"));
	}

	@Test
	public void createUser_shouldNotAllowBlankPassword() {
		User unsavedUser = new User();

		assertThrows(ValidationException.class, () -> userService.createUser(unsavedUser, ""));
	}

	@Test
	public void createUser_shouldNotAllowNullPassword() {
		User unsavedUser = new User();

		assertThrows(ValidationException.class, () -> userService.createUser(unsavedUser, null));
	}

	@Test
	public void createUser_shouldNotAllowForDuplicatedUsername() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getUsername());

		DAOException exception = assertThrows(DAOException.class, () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.",
			newUser.getUsername(),
			Context.getUserService().generateSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowDuplicatedSystemId() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setSystemId(someUser.getSystemId());

		DAOException exception = assertThrows(DAOException.class, () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.", newUser.getUsername(), newUser.getSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowUsernameEqualsExistingSystemId() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getSystemId());

		DAOException exception = assertThrows(DAOException.class, () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.", newUser.getUsername(), Context.getUserService().generateSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowSystemIdEqualsExistingUsername() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setSystemId(someUser.getUsername());

		DAOException exception = assertThrows(DAOException.class, () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.", newUser.getUsername(), newUser.getSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowSystemIdEqualsUsernameWithLuhnCheckDigit() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getUsername());
		newUser.setSystemId(decorateWithLuhnIdentifier(someUser.getUsername()));

		DAOException exception = assertThrows(DAOException.class, () ->  userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.", newUser.getUsername(), newUser.getSystemId())));
	}
	
	@Test
	public void createUser_shouldNotAllowCreatingUserWithPrivilegeCurrentUserDoesNotHave() throws IllegalAccessException {
		//setup the currently logged in user
		User currentUser = new User();
		Role userRole = new Role("User Adder");
		userRole.setRole(RoleConstants.AUTHENTICATED);
		userRole.addPrivilege(new Privilege("Add Users"));
		currentUser.addRole(userRole);
		// setup our expected exception
		// we expect this to fail because the currently logged-in user lacks a privilege to be
		// assigned to the new user
		// set current user to the user defined above
		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// create a role to assign to the new user
			Role role = new Role();
			role.setRole(RoleConstants.AUTHENTICATED);
			// add a privilege to the role
			role.addPrivilege(new Privilege("Custom Privilege"));

			// create our new user object with the required fields
			User u = new User();
			u.setPerson(new Person());
			// assign the specified role to the user
			u.addName(new PersonName("Benjamin", "A", "Wolfe"));
			u.setUsername("bwolfe");
			u.getPerson().setGender("M");
			u.addRole(role);
			// here we expect the exception to be thrown
			userService.createUser(u, "Openmr5xy");
		}));
		assertThat(exception.getMessage(), is("You must have privilege {0} in order to assign it."));
	}
	
	@Test
	public void createUser_shouldNotAllowCreatingUserWithPrivilegesCurrentUserDoesNotHave() throws IllegalAccessException {
		//setup the currently logged in user
		User currentUser = new User();
		Role userRole = new Role("User Adder");
		userRole.setRole(RoleConstants.AUTHENTICATED);
		userRole.addPrivilege(new Privilege("Add Users"));
		currentUser.addRole(userRole);
		// setup our expected exception
		// we expect this to fail because the currently logged-in user lacks a privilege to be
		// assigned to the new user
		// set current user to the user defined above
		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// create a role to assign to the new user
			Role role = new Role();
			role.setRole(RoleConstants.AUTHENTICATED);
			// add privileges to the role
			role.addPrivilege(new Privilege("Custom Privilege"));
			role.addPrivilege(new Privilege("Another Privilege"));

			// create our new user object with the required fields
			User u = new User();
			u.setPerson(new Person());
			// assign the specified role to the user
			u.addName(new PersonName("Benjamin", "A", "Wolfe"));
			u.setUsername("bwolfe");
			u.getPerson().setGender("M");
			u.addRole(role);
			// here we expect the exception to be thrown
			userService.createUser(u, "Openmr5xy");
		}));
		assertThat(exception.getMessage(), is("You must have the following privileges in order to assign them: Another Privilege, Custom Privilege"));
	}
	
	@Test
	public void createUser_shouldNotAllowAssigningSuperUserRoleIfCurrentUserDoesNotHaveAssignSystemDeveloperPrivileges() throws IllegalAccessException {
		//setup the currently logged in user
		User currentUser = new User();
		Role userRole = new Role("User Adder");
		userRole.setRole(RoleConstants.AUTHENTICATED);
		userRole.addPrivilege(new Privilege("Add Users"));
		currentUser.addRole(userRole);

		// setup our expected exception
		// we expect this to fail because the currently logged-in user lacks a privilege to be
		// assigned to the new user
		// set current user to the user defined above
		APIException exception = assertThrows(APIException.class, () ->  withCurrentUserAs(currentUser, () -> {
			// create a role to assign to the new user
			// the current user cannot assign a user to the superuser role because he lacks AssignSystemDeveloper privileges
			Role role= new Role("add user");
			role.setRole(RoleConstants.SUPERUSER);
			// add a privilege to the role
			role.hasPrivilege(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE);			

			// create our new user object with the required fields
			User u = new User();
			u.setPerson(new Person());
			// assign the specified role to the user
			u.addName(new PersonName("Benjamin", "A", "Wolfe"));
			u.setUsername("bwolfe");
			u.getPerson().setGender("M");
			u.isSuperUser();
			u.addRole(role);
			// here we expect the exception to be thrown
			userService.createUser(u, "Openmr5xy");
		}));
		assertThat(exception.getMessage(), is("You must have the role {0} in order to assign it."));
	}

	private User userWithValidPerson() {
		Person person = new Person();
		person.addName(new PersonName("jane", "sue", "doe"));
		person.setGender("F");
		return new User(person);
	}

	private String decorateWithLuhnIdentifier(String value) {
		return new LuhnIdentifierValidator().getValidIdentifier(value);
	}

	@Test
	public void saveUser_shouldUpdateUsersUsername() {
		User u = userService.getUserByUsername(ADMIN_USERNAME);
		assertNotNull(u, "There needs to be a user with username 'admin' in the database");
		
		u.setUsername("admin2");
		userService.saveUser(u);
		
		User u2 = userService.getUserByUsername("admin2");
		
		assertEquals(u, u2, "The fetched user should equal the user we tried to update");
	}
	
	/**
	 * Test changing a user's password multiple times in the same transaction
	 * 
	 * @see UserService#changePassword(String,String)
	 */
	@Test
	public void changePassword_shouldBeAbleToUpdatePasswordMultipleTimes() {
		User u = userService.getUserByUsername(ADMIN_USERNAME);
		assertNotNull(u, "There needs to be a user with username 'admin' in the database");
		
		userService.changePassword("test", "Tester12");
		userService.changePassword("Tester12", "Tester13");
	}

	@Test
	public void saveUser_shouldGrantNewRolesInRolesListToUser() {
		// add in some basic properties
		executeDataSet(XML_FILENAME);

		User u = userService.getUserByUsername(ADMIN_USERNAME);

		Role role1 = new Role();
		role1.setDescription("testing1");
		role1.setRole("test1");
		Privilege p1 = userService.getAllPrivileges().get(0);
		Set<Privilege> privileges1 = new HashSet<>();
		privileges1.add(p1);
		role1.setPrivileges(privileges1);

		Role role2 = new Role();
		role2.setDescription("testing2");
		role2.setRole("test2");
		Privilege p2 = userService.getAllPrivileges().get(0);
		Set<Privilege> privileges2 = new HashSet<>();
		privileges2.add(p2);
		role2.setPrivileges(privileges2);

		userService.saveUser(u.addRole(role1));

		userService.saveUser(u.addRole(role2));

		// so the contents are fetched from the db
		Context.evictFromSession(u);

		userService.getUser(u.getUserId()).hasRole("test1");
		userService.getUser(u.getUserId()).hasRole("test2");
	}

	/**
	 * @see UserService#getUserByUsername(String)
	 */
	@Test
	public void getUserByUsername_shouldGetUserByUsername() {
		User user = userService.getUserByUsername(ADMIN_USERNAME);

		assertNotNull(user, "username not found " + ADMIN_USERNAME);
	}

	/**
	 * @see UserService#changePassword(String,String)
	 */
	@Test
	public void changePassword_shouldMatchOnIncorrectlyHashedSha1StoredPassword() {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("incorrectlyhashedSha1", "test");

		userService.changePassword("test", "Tester12");

		Context.logout(); // so that the next test reauthenticates
	}

	/**
	 * @see UserService#changeQuestionAnswer(String,String,String)
	 */
	@Test
	public void changeQuestionAnswer_shouldMatchOnCorrectlyHashedStoredPassword() {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("correctlyhashedSha1", "test");

		userService.changeQuestionAnswer("test", "some question", "some answer");

		Context.logout(); // so that the next test reauthenticates
	}

	/**
	 * @see UserService#changeQuestionAnswer(String,String,String)
	 */
	@Test
	public void changeQuestionAnswer_shouldMatchOnIncorrectlyHashedStoredPassword() {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("incorrectlyhashedSha1", "test");

		userService.changeQuestionAnswer("test", "some question", "some answer");

		Context.logout(); // so that the next test reauthenticates
	}

	/**
	 * @see UserService#changePassword(String,String)
	 */
	@Test
	public void changePassword_shouldMatchOnCorrectlyHashedSha1StoredPassword() {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("correctlyhashedSha1", "test");

		userService.changePassword("test", "Tester12");

		Context.logout(); // so that the next test reauthenticates
	}

	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldMatchSearchToFamilyName2() {
		executeDataSet("org/openmrs/api/include/PersonServiceTest-extranames.xml");

		List<User> users = userService.getUsers("Johnson", null, false);
		assertEquals(3, users.size());
		assertTrue(containsId(users, 2));
		assertTrue(containsId(users, 4));
		assertTrue(containsId(users, 5));
	}

	/**
	 * @see UserService#changePassword(String,String)
	 */
	@Test
	public void changePassword_shouldMatchOnSha512HashedPassword() {
		executeDataSet(XML_FILENAME);
		Context.logout();
		Context.authenticate("userWithSha512Hash", "test");

		userService.changePassword("test", "Tester12");

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
	public void shouldFetchNamesForPersonsThatWereFirstFetchedAsUsers() {
		Person person = Context.getPersonService().getPerson(1);
		User user = userService.getUser(1);

		user.getNames().size();
		person.getNames().size();
	}

	/**
	 * @see UserService#getPrivilegeByUuid(String)
	 */
	@Test
	public void getPrivilegeByUuid_shouldFindObjectGivenValidUuid() {
		executeDataSet(XML_FILENAME);
		String uuid = "d979d066-15e6-467c-9d4b-cb575ef97f0f";
		Privilege privilege = userService.getPrivilegeByUuid(uuid);
		assertEquals("Some Privilege", privilege.getPrivilege());
	}

	/**
	 * @see UserService#getPrivilegeByUuid(String)
	 */
	@Test
	public void getPrivilegeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(userService.getPrivilegeByUuid("some invalid uuid"));
	}

	/**
	 * @see UserService#getRoleByUuid(String)
	 */
	@Test
	public void getRoleByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "3480cb6d-c291-46c8-8d3a-96dc33d199fb";
		Role role = userService.getRoleByUuid(uuid);
		assertEquals("Provider", role.getRole());
	}

	/**
	 * @see UserService#getRoleByUuid(String)
	 */
	@Test
	public void getRoleByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(userService.getRoleByUuid("some invalid uuid"));
	}

	/**
	 * @see UserService#getUserByUuid(String)
	 */
	@Test
	public void getUserByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "c1d8f5c2-e131-11de-babe-001e378eb67e";
		User user = userService.getUserByUuid(uuid);
		assertEquals(501, (int) user.getUserId());
	}

	/**
	 * @see UserService#getUserByUuid(String)
	 */
	@Test
	public void getUserByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(userService.getUserByUuid("some invalid uuid"));
	}

	/**
	 * @see UserService#changeHashedPassword(User,String,String)
	 */
	@Test
	public void changeHashedPassword_shouldChangeTheHashedPasswordForTheGivenUser() {
		User user = userService.getUser(1);
		String salt = Security.getRandomToken();
		String hash = Security.encodeString("new password" + salt);
		userService.changeHashedPassword(user, hash, salt);

		// TODO Review this a little further
		// This is the assert - checks to see if current user can use the new password
		userService.changePassword("new password", "Another new password1"); // try to change the password with the new one

	}

	/**
	 * @see UserService#changePassword(User,String)
	 */
	@Test
	public void changePassword_shouldChangePasswordForTheGivenUserAndPassword() {
		userService.changePassword("test", "Another new password1");
		userService.changePassword("Another new password1", "Yet another new password1"); // try to change the password with the new one
	}
	
	/**
	 * @see UserService#changeQuestionAnswer(User,String,String)
	 */
	@Test
	@Disabled
	// TODO fix: the question not sticking - null expected:<[the question]> but was:<[]>
	public void changeQuestionAnswer_shouldChangeTheSecretQuestionAndAnswerForGivenUser() {
		User u = userService.getUser(501);
		userService.changeQuestionAnswer(u, "the question", "the answer");
		
		// need to retrieve the user since the service method does not modify the given user object
		User o = userService.getUser(501);
		assertTrue(userService.isSecretAnswer(o, "the answer"));
	}
	
	/**
	 * @see UserService#getAllPrivileges()
	 */
	@Test
	public void getAllPrivileges_shouldReturnAllPrivilegesInTheSystem() {
		executeDataSet(XML_FILENAME);
		List<Privilege> privileges = userService.getAllPrivileges();
		assertEquals(1, privileges.size());
	}
	
	/**
	 * @see UserService#getAllRoles()
	 */
	@Test
	public void getAllRoles_shouldReturnAllRolesInTheSystem() {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = userService.getAllRoles();
		assertEquals(7, roles.size());
	}
	
	/**
	 * @see UserService#getAllUsers()
	 */
	@Test
	public void getAllUsers_shouldFetchAllUsersInTheSystem() {
		List<User> users = userService.getAllUsers();
		assertEquals(3, users.size());
	}
	
	/**
	 * @see UserService#getAllUsers()
	 */
	@Test
	public void getAllUsers_shouldNotContainsAnyDuplicateUsers() {
		executeDataSet(XML_FILENAME);
		List<User> users = userService.getAllUsers();
		assertEquals(11, users.size());
		// TODO Need to test with duplicate data in the dataset (not sure if that's possible)
		
	}
	
	@Test
	@SkipBaseSetup
	public void getUserByUuid_shouldFetchUserWithGivenUuid() throws SQLException {
		initializeInMemoryDatabase();
		executeDataSet(XML_FILENAME);
		authenticate();

		User user = userService.getUserByUuid("013c49c6-e132-11de-babe-001e378eb67e");
		assertEquals(user, userService.getUser(5505), "Did not fetch user with given uuid");
	}
	
	@Test
	@SkipBaseSetup
	public void getUsersByName_shouldFetchUsersExactlyMatchingTheGivenGivenNameAndFamilyName() throws SQLException {
		initializeInMemoryDatabase();
		executeDataSet(XML_FILENAME);
		authenticate();

		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName] 
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName 
		// = :familyName and u.voided = false]
		List<User> users = userService.getUsersByName("Susy", "Kingman", false);
		assertEquals(1, users.size());
	}
	
	@Test
	public void getUsersByName_shouldFetchVoidedUsersWhenincludeVoidedIsTrue() {
		User voidedUser = userService.getUser(501);
		// assertTrue(voidedUser.getVoided());
		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName]
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName
		// = :familyName]
		List<User> users = userService.getUsersByName("Bruno", "Otterbourg", true);
		assertTrue(users.contains(voidedUser));
	}
	
	@Test
	public void getUsersByName_shouldNotFetchAnyVoidedUsersWhenIncludeVoidedIsFalse() {
		User voidedUser = userService.getUser(501);
		// assertTrue(voidedUser.getVoided());
		// this generates an error:
		// org.hibernate.QueryException: illegal attempt to dereference 
		// collection [user0_.user_id.names] with element property reference [givenName]
		// [from org.openmrs.User u where u.names.givenName = :givenName and u.names.familyName
		// = :familyName and u.voided = false]
		List<User> users = userService.getUsersByName("Bruno", "Otterbourg", false);
		assertFalse(users.contains(voidedUser));
	}
	
	@Test
	@SkipBaseSetup
	public void getUsersByName_shouldNotFetchAnyDuplicateUsers() throws SQLException {
		initializeInMemoryDatabase();
		executeDataSet(XML_FILENAME);
		authenticate();

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
	 * @see UserService#getPrivilege(String)
	 */
	@Test
	public void getPrivilege_shouldFetchPrivilegeForGivenName() {
		executeDataSet(XML_FILENAME);
		Privilege privilege = userService.getPrivilege("Some Privilege");
		assertEquals("Some Privilege", privilege.getPrivilege());
	}
	
	/**
	 * @see UserService#getRole(String)
	 */
	@Test
	public void getRole_shouldFetchRoleForGivenRoleName() {
		executeDataSet(XML_FILENAME);
		Role role = userService.getRole("Some Role");
		assertEquals("Some Role", role.getRole());
	}
	
	/**
	 * @see UserService#getUser(Integer)
	 */
	@Test
	public void getUser_shouldFetchUserWithGivenUserId() {
		User user = userService.getUser(501);
		assertEquals(501, user.getUserId().intValue());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchUsersWithAtLeastOneOfTheGivenRoleObjects() {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = Collections.singletonList(new Role("Some Role"));
		assertEquals(1, userService.getUsers("Susy Kingman", roles, false).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchUsersWithNameThatContainsGivenNameSearch() {
		assertEquals(1, userService.getUsers("Hippocrates", null, false).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchUsersWithSystemIdThatContainsGivenNameSearch() {
		assertEquals(1, userService.getUsers("2-6", null, true).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchVoidedUsersIfIncludedVoidedIsTrue() {
		assertEquals(1, userService.getUsers("Bruno", null, true).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchAllUsersIfNameSearchIsEmptyOrNull() {
		assertEquals(3, userService.getUsers("", null, true).size());
		assertEquals(3, userService.getUsers(null, null, true).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldNotFetchDuplicateUsers() {
		executeDataSet(XML_FILENAME);
		
		List<User> users = userService.getUsers("John Doe", null, false);
		assertEquals(1, users.size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldNotFetchVoidedUsersIfIncludedVoidedIsFalse() {
		assertEquals(0, userService.getUsers("Bruno", null, false).size());
	}
	
	/**
	 * @see UserService#getUsersByRole(Role)
	 */
	@Test
	public void getUsersByRole_shouldFetchUsersAssignedGivenRole() {
		executeDataSet(XML_FILENAME);
		
		assertEquals(2, userService.getUsersByRole(new Role("Some Role")).size());
	}
	
	/**
	 * @see UserService#getUsersByRole(Role)
	 */
	@Test
	public void getUsersByRole_shouldNotFetchUserThatDoesNotBelongToGivenRole() {
		executeDataSet(XML_FILENAME);
		
		assertEquals(0, userService.getUsersByRole(new Role("Nonexistent role")).size());
	}
	
	/**
	 * @see UserService#hasDuplicateUsername(User)
	 */
	@Test
	public void hasDuplicateUsername_shouldVerifyThatUsernameAndSystemIdIsUnique() {
		executeDataSet(XML_FILENAME);
		
		User user = new User();
		user.setSystemId("8-3");
		user.setUsername("a unique username");
		assertTrue(userService.hasDuplicateUsername(user));
		
		user = new User();
		user.setSystemId("a unique system id");
		user.setUsername("userWithSha512Hash");
		assertTrue(userService.hasDuplicateUsername(user));
	}
	
	/**
	 * @see UserService#isSecretAnswer(User,String)
	 */
	@Test
	public void isSecretAnswer_shouldReturnFalseWhenGivenAnswerDoesNotMatchTheStoredSecretAnswer() {
		User user = userService.getUser(502);
		assertFalse(userService.isSecretAnswer(user, "not the answer"));
	}
	
	/**
	 * @see UserService#isSecretAnswer(User,String)
	 */
	@Test
	public void isSecretAnswer_shouldReturnTrueWhenGivenAnswerMatchesStoredSecretAnswer() {
		executeDataSet(XML_FILENAME);
		User user = userService.getUser(5507);
		userService.changeQuestionAnswer(user, "question", "answer");
		assertTrue(userService.isSecretAnswer(user, "answer"));
	}
	
	/**
	 * @see UserService#purgePrivilege(Privilege)
	 */
	@Test
	public void purgePrivilege_shouldDeleteGivenPrivilegeFromTheDatabase() {
		userService.purgePrivilege(new Privilege("Some Privilege"));
		assertNull(userService.getPrivilege("Some Privilege"));
	}
	
	/**
	 * @see UserService#purgePrivilege(Privilege)
	 */
	@Test
	public void purgePrivilege_shouldThrowErrorWhenPrivilegeIsCorePrivilege() {
		assertThrows(APIException.class, () -> userService.purgePrivilege(new Privilege(PrivilegeConstants.ADD_COHORTS)));
	}
	
	/**
	 * @see UserService#purgeRole(Role)
	 */
	@Test
	public void purgeRole_shouldDeleteGivenRoleFromDatabase() {
		executeDataSet(XML_FILENAME);
		Role role = userService.getRole("Some Role To Delete");
		userService.purgeRole(role);
		assertNull(userService.getRole("Some Role To Delete"));
	}
	
	/**
	 * @see UserService#purgeRole(Role)
	 */
	@Test
	public void purgeRole_shouldReturnIfRoleIsNull() {
		userService.purgeRole(null);
	}
	
	/**
	 * @see UserService#purgeRole(Role)
	 */
	@Test
	public void purgeRole_shouldThrowErrorWhenRoleIsACoreRole() {
		Role role = new Role(RoleConstants.ANONYMOUS);

		assertThrows(APIException.class, () -> userService.purgeRole(role));
	}
	
	/**
	 * @see UserService#purgeUser(User)
	 */
	@Test
	public void purgeRole_shouldThrowErrorWhenRoleHasChildRoles() {
		Set<Role> childRole = new HashSet<>();
		Role role1 = new Role("role_parent");
		Role role2 = new Role("role_child");
		childRole.add(role1);
		role2.setChildRoles(childRole);

		assertThrows(CannotDeleteRoleWithChildrenException.class, () -> userService.purgeRole(role2));
	}
	
	/**
	 * @see UserService#purgeUser(User)
	 */
	@Test
	public void purgeUser_shouldDeleteGivenUser() {
		User user = userService.getUser(502);
		userService.purgeUser(user);
		assertNull(userService.getUser(2));
	}
	
	/**
	 * @see UserService#purgeUser(User,boolean)
	 */
	@Test
	public void purgeUser_shouldDeleteGivenUserWhenCascadeEqualsFalse() {
		User user = userService.getUser(502);
		userService.purgeUser(user, false);
		assertNull(userService.getUser(502));
	}
	
	/**
	 * @see UserService#purgeUser(User,boolean)
	 */
	@Test
	public void purgeUser_shouldThrowAPIExceptionIfCascadeIsTrue() {
		User user = userService.getUser(502);

		assertThrows(APIException.class, () -> userService.purgeUser(user, true));
	}
	
	/**
	 * @see UserService#removeUserProperty(User,String)
	 */
	@Test
	public void removeUserProperty_shouldRemoveUserPropertyForGivenUserAndKey() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		assertNotSame("", user.getUserProperty("some key"));
		
		userService.removeUserProperty(user, "some key");
		
		user = userService.getUser(5505);
		assertEquals("", user.getUserProperty("some key"));
	}
	
	/**
	 * @see UserService#removeUserProperty(User,String)
	 */
	@Test
	public void removeUserProperty_shouldReturnNullIfUserIsNull() {
		assertNull(userService.setUserProperty(null, "some key", "some new value"));
	}
	
	/**
	 * @see UserService#removeUserProperty(User,String)
	 */
	@Test
	public void removeUserProperty_shouldThrowErrorWhenUserIsNotAuthorizedToEditUsers() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		
		Context.logout();

		assertThrows(APIException.class, () -> userService.removeUserProperty(user, "some key"));
	}
	
	/**
	 * @see UserService#savePrivilege(Privilege)
	 */
	@Test
	public void savePrivilege_shouldSaveGivenPrivilegeToTheDatabase() {
		Privilege p = new Privilege("new privilege name", "new privilege desc");
		userService.savePrivilege(p);
		
		Privilege savedPrivilege = userService.getPrivilege("new privilege name");
		assertNotNull(savedPrivilege);
		
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldAddPropertyWithGivenKeyAndValueWhenKeyDoesNotAlreadyExist() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		
		// Check that it doesn't already exist
		assertEquals(user.getUserProperty("some new key"), "");
		
		userService.setUserProperty(user, "some new key", "some new value");
		
		user = userService.getUser(5505);
		assertEquals("some new value", user.getUserProperty("some new key"));
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldModifyPropertyWithGivenKeyAndValueWhenKeyAlreadyExists() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		
		// Check that it already exists
		assertEquals(user.getUserProperty("some key"), "some value");
		
		userService.setUserProperty(user, "some key", "some new value");
		
		user = userService.getUser(5505);
		assertEquals("some new value", user.getUserProperty("some key"));
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldReturnNullIfUserIsNull() {
		assertNull(userService.setUserProperty(null, "some key", "some value"));
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldThrowErrorWhenUserIsNotAuthorizedToEditUsers() {
		User user = userService.getUser(502);
		
		Context.logout();
		assertThrows(APIException.class, () -> userService.setUserProperty(user, "some key", "some value"));
	}
	
	/**
	 * @see UserService#saveRole(Role)
	 */
	@Test
	public void saveRole_shouldSaveGivenRoleToTheDatabase() {
		Role role = new Role("new role", "new desc");
		userService.saveRole(role);
		
		assertNotNull(userService.getRole("new role"));
		
	}
	
	/**
	 * @see UserService#saveRole(Role)
	 */
	@Test
	public void saveRole_shouldThrowErrorIfRoleInheritsFromItself() {
		Role parentRole = new Role("parent role");
		
		// Have child inherit parent role
		Role childRole = new Role("child role");
		Set<Role> inheritsFromParent = new HashSet<>();
		inheritsFromParent.add(parentRole);
		childRole.setInheritedRoles(inheritsFromParent);
		
		// Now have parent try to inherit the child role.
		Set<Role> inheritsFromChild = new HashSet<>();
		inheritsFromChild.add(childRole);
		parentRole.setInheritedRoles(inheritsFromChild);
		
		APIException exception = assertThrows(APIException.class, () -> userService.saveRole(parentRole));
		assertThat(exception.getMessage(), is(messages.getMessage("Role.cannot.inherit.descendant")));
	}
	
	@Test
	public void saveRole_shouldAllowARoleToBeSavedWithCorrectPermissions() throws IllegalAccessException {
		Role role = new Role("my role");
		Privilege myPrivilege = new Privilege("custom privilege");
		role.addPrivilege(myPrivilege);
		
		User currentUser = new User();
		currentUser.addRole(new Role(RoleConstants.SUPERUSER));
		
		withCurrentUserAs(currentUser, () -> {
			Role newRole = new Role("another role");
			newRole.addPrivilege(myPrivilege);
			userService.saveRole(newRole);
		});
	}

	@Test
	public void saveRole_shouldThrowErrorWhenCurrentUserLacksPrivilegeAssignedToRole() throws IllegalAccessException {
		Role adminRole = new Role("my role");
		adminRole.addPrivilege(new Privilege(PrivilegeConstants.MANAGE_ROLES));

		User currentUser = new User();
		currentUser.addRole(adminRole);

		Privilege myPrivilege = new Privilege("custom privilege");
		
		APIException exception = assertThrows(APIException.class, () ->  withCurrentUserAs(currentUser, () -> {
			Role newRole = new Role("another role");
			newRole.addPrivilege(myPrivilege);
			userService.saveRole(newRole);
		}));
		assertThat(exception.getMessage(), is("You must have the following privileges in order to assign them: custom privilege"));
	}

	@Test
	public void saveRole_shouldThrowErrorWhenCurrentUserLacksAPrivilegeAssignedToRole() throws IllegalAccessException {
		Privilege myFirstPrivilege = new Privilege("custom privilege");
		Privilege mySecondPrivilege = new Privilege("another privilege");
		
		Role adminRole = new Role("my role");
		adminRole.addPrivilege(new Privilege(PrivilegeConstants.MANAGE_ROLES));
		adminRole.addPrivilege(myFirstPrivilege);

		User currentUser = new User();
		currentUser.addRole(adminRole);

		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			Role newRole = new Role("another role");
			newRole.addPrivilege(myFirstPrivilege);
			newRole.addPrivilege(mySecondPrivilege);
			userService.saveRole(newRole);
		}));
		assertThat(exception.getMessage(), is("You must have the following privileges in order to assign them: another privilege"));
	}
	
	/**
	 * @see UserService#getUsersByPerson(Person,null)
	 */
	@Test
	public void getUsersByPerson_shouldFetchAllAccountsForAPersonWhenIncludeRetiredIsTrue() {
		executeDataSet(XML_FILENAME);
		Person person = new Person(5508);
		List<User> users = userService.getUsersByPerson(person, true);
		assertEquals(3, users.size());
	}
	
	/**
	 * @see UserService#getUsersByPerson(Person,null)
	 */
	@Test
	public void getUsersByPerson_shouldNotFetchRetiredAccountsWhenIncludeRetiredIsFalse() {
		executeDataSet(XML_FILENAME);
		Person person = new Person(5508);
		List<User> users = userService.getUsersByPerson(person, false);
		assertEquals(2, users.size());
	}
	
	/**
	 * @see UserService#retireUser(User,String)
	 */
	@Test
	public void retireUser_shouldRetireUserAndSetAttributes() {
		User user = userService.getUser(502);
		userService.retireUser(user, "because");
		assertTrue(user.getRetired());
		assertNotNull(user.getDateRetired());
		assertNotNull(user.getRetiredBy());
		assertEquals("because", user.getRetireReason());
	}
	
	/**
	 * @see UserService#unretireUser(User)
	 */
	@Test
	public void unretireUser_shouldUnretireAndUnmarkAllAttributes() {
		User user = userService.getUser(501);
		userService.unretireUser(user);
		assertFalse(user.getRetired());
		assertNull(user.getDateRetired());
		assertNull(user.getRetiredBy());
		assertNull(user.getRetireReason());
	}
	
	@Test
	public void saveUser_shouldFailToCreateTheUserWithAWeakPassword() {
		assertTrue(Context.isAuthenticated(), "The context needs to be correctly authenticated to by a user");
		
		UserService us = userService;
		
		User u = new User();
		u.setPerson(new Person());
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");

		assertThrows(PasswordException.class, () -> us.createUser(u, "short"));
	}
	
	/**
	 * This is a regression test for TRUNK-2108 <br>
	 * 
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldNotFailIfRolesAreSearchedButNameIsEmpty() {
		Role role = new Role("Provider");
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		
		assertEquals(2, userService.getUsers("", roles, true).size());
	}
	
	/**
	 * @see UserService#getUsers(String, List, boolean, Integer, Integer)
	 */
	@Test
	public void getUsers_shouldReturnUsersWhoseRolesInheritRequestedRoles() {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = new ArrayList<>();
		roles.add(userService.getRole("Parent"));
		assertEquals(3, userService.getUsers(null, roles, true, null, null).size());
	}
	
	@Test
	public void saveUserProperty_shouldAddNewPropertyToExistingUserProperties() {
		executeDataSet(XML_FILENAME);

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
	public void saveUserProperties_shouldRemoveAllExistingPropertiesAndAssignNewProperties() {
		executeDataSet(XML_FILENAME);

		//  retrieve a user who has UserProperties
		User user = userService.getUser(5511);
		assertEquals(1, user.getUserProperties().size());
		// Authenticate the test  user so that Context.getAuthenticatedUser() method returns above user
		Context.authenticate(user.getUsername(), "testUser1234");
		final String USER_PROPERTY_KEY_1 = "test-key1";
		final String USER_PROPERTY_VALUE_1 = "test-value1";
		final String USER_PROPERTY_KEY_2 = "test-key2";
		final String USER_PROPERTY_VALUE_2 = "test-value2";
		Map<String, String> propertiesMap = new HashMap<>();
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
	 */
	@Test
	public void changePassword_shouldChangePasswordForGivenUserIfOldPasswordIsCorrectlyPassed() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
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
	 */
	@Test
	public void changePassword_shouldChangePasswordForGivenUserIfOldPasswordIsNullAndChangingUserHavePrivileges()	{
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String oldPassword = null;
		String newPassword = "newPasswordString123";
		userService.changePassword(user6001, oldPassword, newPassword);
		Context.authenticate(user6001.getUsername(), newPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfOldPasswordIsNotCorrect() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String wrongPassword = "wrong password!";
		String newPassword = "newPasswordString";
		//log in user without change user passwords privileges
		//user6001 has not got required priviliges
		Context.authenticate(user6001.getUsername(), "userServiceTest");
		
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class, () -> userService.changePassword(user6001, wrongPassword, newPassword));
		assertThat(exception.getMessage(), is(messages.getMessage("error.privilegesRequired", new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null)));
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfNewPasswordIsTheSameAsOld() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String oldPassword = "userServiceTest";
		String newPassword = "userServiceTest";
		//log in user without change user passwords privileges
		//user6001 has not got required priviliges
		Context.authenticate(user6001.getUsername(), "userServiceTest");
		
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class, () -> userService.changePassword(user6001, oldPassword, newPassword));
		assertThat(exception.getMessage(), is(messages.getMessage("error.privilegesRequired", new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null)));
	}

	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowExceptionIfOldPasswordIsNullAndChangingUserHaveNotPrivileges() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		assertFalse(user6001.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		String oldPassword = null;
		String newPassword = "newPasswordString";
		//log in user without change user passwords privileges
		//user6001 has not got required priviliges
		Context.authenticate(user6001.getUsername(), "userServiceTest");
		
		APIException exception = assertThrows(APIException.class, () -> userService.changePassword(user6001, oldPassword, newPassword));
		assertThat(exception.getMessage(), is(messages.getMessage("error.privilegesRequired", new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null)));	
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowExceptionIfNewPasswortIsTooShort() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		//user 6001 has password userServiceTest
		User user6001 = userService.getUser(6001);
		String oldPassword = "userServiceTest";
		String weakPassword = "weak";
		
		APIException exception = assertThrows(APIException.class, () -> userService.changePassword(user6001, oldPassword, weakPassword));
		assertThat( exception.getMessage(), is(messages.getMessage("error.password.length", new Object[] {"8"}, null)));
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfGivenUserDoesNotExist() {
		//user.getUserId is null - so it is not existing user
		User notExistingUser = new User();
		String anyString = "anyString";
		
		APIException exception = assertThrows(APIException.class, () ->  userService.changePassword(notExistingUser, anyString, anyString));
		assertThat(exception.getMessage(), is(messages.getMessage("user.must.exist")));
	}

    @Test
	public void changePassword_shouldThrowShortPasswordExceptionWithShortPassword() { 
		ShortPasswordException exception = assertThrows(ShortPasswordException.class, () -> userService.changePassword("test", ""));
		assertThat(exception.getMessage(), is(messages.getMessage("error.password.length", new Object[] {"8"}, null)));
    }
    
	@Test
	public void changePassword_shouldUpdatePasswordOfGivenUserWhenLoggedInUserHasEditUsersPasswordPrivilege() {
		User user = userService.getUserByUsername(ADMIN_USERNAME);
		assertNotNull(user, "There needs to be a user with username 'admin' in the database");
		
		userService.changePassword(user, "testTest123");
		
		Context.authenticate(user.getUsername(), "testTest123");
	}
	
	@Test
	public void changePassword_shouldNotUpdatePasswordOfGivenUserWhenLoggedInUserDoesNotHaveEditUsersPasswordPrivilege() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class, () ->  userService.changePassword(user, "testTest123"));
		assertThat(exception.getMessage(), is(messages.getMessage("error.privilegesRequired", new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null)));
	}
	
	@Test
	public void changePasswordUsingSecretAnswer_shouldUpdatePasswordIfSecretIsCorrect() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");
		
		userService.changePasswordUsingSecretAnswer("answer", "userServiceTest2");
		
		Context.authenticate(user.getUsername(), "userServiceTest2");
	}

	@Test
	public void changePasswordUsingSecretAnswer_shouldNotUpdatePasswordIfSecretIsNotCorrect() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");
		
		APIException exception = assertThrows(APIException.class, () -> userService.changePasswordUsingSecretAnswer("wrong answer", "userServiceTest2"));
		assertThat(exception.getMessage(), is(messages.getMessage("secret.answer.not.correct")));
	}
	
	/**
	 * @see UserService#getUserByUsernameOrEmail(String)
	 */
	@Test
	public void getUserByUsernameOrEmail_shouldGetUserByUsingEmail() {
		executeDataSet(XML_FILENAME);
		User user = userService.getUserByUsernameOrEmail("hank.williams@gmail.com");
		assertNotNull(user, "User with email hank.williams@gmail not found in database");
	}
	
	@Test
	public void getUserByUsernameOrEmail_shouldNotGetUserIfEmailIsEmpty() {
		APIException exception = assertThrows(APIException.class, () -> userService.getUserByUsernameOrEmail(""));
		assertThat(exception.getMessage(), is(messages.getMessage("error.usernameOrEmail.notNullOrBlank")));
	}
	
	@Test
	public void getUserByUsernameOrEmail_shouldFailIfEmailIsWhiteSpace() {
		APIException exception = assertThrows(APIException.class, () -> userService.getUserByUsernameOrEmail("  "));
		assertThat(exception.getMessage(), is(messages.getMessage("error.usernameOrEmail.notNullOrBlank")));
	}
	
	@Test
	public void getUserByUsernameOrEmail_shouldFailIfEmailIsNull() {
		APIException exception = assertThrows(APIException.class, () -> userService.getUserByUsernameOrEmail(null));
		assertThat(exception.getMessage(), is(messages.getMessage("error.usernameOrEmail.notNullOrBlank")));
	}
	
	
	@Test
	public void setUserActivationKey_shouldCreateUserActivationKey() throws Exception {
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_HOST_URL,
		    "http://localhost:8080/openmrs/admin/users/changePassword.form/{activationKey}");
		User createdUser = userService.createUser(u, "Openmr5xy");
		assertNull(dao.getLoginCredential(createdUser).getActivationKey());
		assertThrows(MessageException.class, () -> userService.setUserActivationKey(createdUser));
		assertNotNull(dao.getLoginCredential(createdUser).getActivationKey());
	}
	
	@Test 
	public void getUserByActivationKey_shouldGetUserByActivationKey(){
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		User createdUser = userService.createUser(u, "Openmr5xy");
		String key="h4ph0fpNzQCIPSw8plJI";
		int validTime = 10*60*1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() + validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey("b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"+tokenTime);			
		dao.updateLoginCredential(credentials);
		assertEquals(createdUser, userService.getUserByActivationKey(key)); 	
	}
	
	@Test
	public void getUserByActivationKey_shouldReturnNullIfTokenTimeExpired(){
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		User createdUser = userService.createUser(u, "Openmr5xy");
		String key="h4ph0fpNzQCIPSw8plJI";
		int validTime = 10*60*1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() - validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey("b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"+tokenTime);			
		dao.updateLoginCredential(credentials); 
		assertNull(userService.getUserByActivationKey(key)); 
	}
	
	@Test
	public void changePasswordUsingActivationKey_shouldUpdatePasswordIfActivationKeyIsCorrect() {
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		User createdUser = userService.createUser(u, "Openmr5xy");
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() + validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.updateLoginCredential(credentials);
		
		final String PASSWORD = "Admin123";
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		userService.changePasswordUsingActivationKey(key, PASSWORD);
		Context.authenticate(createdUser.getUsername(), PASSWORD);
		
	}
	
	@Test
	public void changePasswordUsingActivationKey_shouldNotUpdatePasswordIfActivationKeyIsIncorrect() {
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		User createdUser = userService.createUser(u, "Openmr5xy");
		String key = "wrongactivationkeyin";
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		InvalidActivationKeyException exception = assertThrows(InvalidActivationKeyException.class, () -> userService.changePasswordUsingActivationKey(key, "Pa55w0rd"));
		assertThat(exception.getMessage(), is(messages.getMessage("activation.key.not.correct")));
	}
	
	@Test
	public void changePasswordUsingActivationKey_shouldNotUpdatePasswordIfActivationKeyExpired() {
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		User createdUser = userService.createUser(u, "Openmr5xy");
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() - validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.updateLoginCredential(credentials);
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		
		
		InvalidActivationKeyException exception = assertThrows(InvalidActivationKeyException.class, () -> userService.changePasswordUsingActivationKey(key, "Pa55w0rd"));
		assertThat(exception.getMessage(), is(messages.getMessage("activation.key.not.correct")));
	}

	/**
	 * Utility method to set the current executing user to test various permission levels.
	 * 
	 * @param user the user to set as the currently running user
	 * @param internals the functionality to test with this current running user
	 * @throws IllegalAccessException because we use reflection to set the currently running user, this may fail and throw
	 *  and {@link IllegalArgumentException}
	 */
	private void withCurrentUserAs(User user, Runnable internals) throws IllegalAccessException {
		UserContext userContext = Context.getUserContext();
		User authenticatedUser = userContext.getAuthenticatedUser();
		try {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, user);
			internals.run();
		} finally {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, authenticatedUser);
		}
	}

	@Test
	public void saveUserProperty_shouldAddANewPropertyWithAVeryLargeStringWithoutRunningIntoError() {
		final String USER_PROPERTY_KEY = liquibase.util.StringUtil.repeat("emrapi.lastViewedPatientIds,",10);
		final String USER_PROPERTY_VALUE = liquibase.util.StringUtil.repeat("52345",9899);
		User updatedUser = userService.saveUserProperty(USER_PROPERTY_KEY, USER_PROPERTY_VALUE);
		assertEquals(280, updatedUser.getUserProperties().keySet().iterator().next().length());
		assertEquals(49495, updatedUser.getUserProperties().get(USER_PROPERTY_KEY).length());
	}
}
