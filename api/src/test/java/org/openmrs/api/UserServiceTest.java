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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.PrivilegeListener;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.context.UserContextTestUtil;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.notification.MessageSender;
import org.openmrs.notification.MessageService;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.openmrs.util.Security;
import org.openmrs.validator.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.test.TestUtil.containsId;

/**
 * TODO add more tests to cover the methods in <code>UserService</code>
 */
public class UserServiceTest extends BaseContextSensitiveTest {

	protected static final String XML_FILENAME = "org/openmrs/api/include/UserServiceTest.xml";

	/**
	 * Roles and privileges no test caller holds, for the role-assignment checks; loaded after
	 * {@link #XML_FILENAME}
	 */
	private static final String ROLE_ASSIGNMENT_XML = "org/openmrs/api/include/UserServiceTest-roleAssignment.xml";

	/**
	 * Gives the test user a role that manages users and roles, but not System Developer; loaded after
	 * {@link #XML_FILENAME}
	 */
	private static final String USER_MANAGER_XML = "org/openmrs/api/include/UserServiceTest-userManager.xml";

	/**
	 * Gives the test user roles that add and purge users, without Edit Users; loaded after
	 * {@link #XML_FILENAME}
	 */
	private static final String LIMITED_ADMINS_XML = "org/openmrs/api/include/UserServiceTest-limitedAdmins.xml";

	protected static final String XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION = "org/openmrs/api/include/UserServiceTest-changePasswordAction.xml";

	protected static final String SOME_VALID_PASSWORD = "s0mePassword";

	public static final String SOME_USERNAME = "butch";

	private static final boolean CurrentUser = false;

	private final String ADMIN_USERNAME = "admin";

	private UserService userService;

	private MessageSourceService messages;

	@Autowired
	private UserDAO dao;

	@Autowired
	private SessionFactory sessionFactory;

	@BeforeEach
	public void setup() {
		userService = Context.getUserService();
		messages = Context.getMessageSourceService();
	}

	/**
	 * Methods in this class might authenticate with a different user, so log that user out after this
	 * whole junit class is done.
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
	public void createUser_shouldShouldCreateUserWhoIsPatientAlready() throws SQLException {
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
		assertThat(allUsers, hasSize(greaterThanOrEqualTo(12)));

		// there should still only be the one patient we created in the xml file
		List<Patient> allPatientsSet = Context.getPatientService().getAllPatients();
		assertThat(allUsers, hasSize(greaterThanOrEqualTo(4)));
	}

	@Test
	public void createUser_shouldNotAllowExistingUser() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		APIException exception = assertThrows(APIException.class,
		    () -> userService.createUser(someUser, SOME_VALID_PASSWORD));
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

		DAOException exception = assertThrows(DAOException.class,
		    () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.",
		    newUser.getUsername(), Context.getUserService().generateSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowDuplicatedSystemId() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setSystemId(someUser.getSystemId());

		DAOException exception = assertThrows(DAOException.class,
		    () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(
		    String.format("Username %s or system id %s is already in use.", newUser.getUsername(), newUser.getSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowUsernameEqualsExistingSystemId() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getSystemId());

		DAOException exception = assertThrows(DAOException.class,
		    () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(String.format("Username %s or system id %s is already in use.",
		    newUser.getUsername(), Context.getUserService().generateSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowSystemIdEqualsExistingUsername() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setSystemId(someUser.getUsername());

		DAOException exception = assertThrows(DAOException.class,
		    () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(
		    String.format("Username %s or system id %s is already in use.", newUser.getUsername(), newUser.getSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowSystemIdEqualsUsernameWithLuhnCheckDigit() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getUsername());
		newUser.setSystemId(decorateWithLuhnIdentifier(someUser.getUsername()));

		DAOException exception = assertThrows(DAOException.class,
		    () -> userService.createUser(newUser, SOME_VALID_PASSWORD));
		assertThat(exception.getMessage(), is(
		    String.format("Username %s or system id %s is already in use.", newUser.getUsername(), newUser.getSystemId())));
	}

	@Test
	public void createUser_shouldNotAllowCreatingUserWithPrivilegeCurrentUserDoesNotHave() throws IllegalAccessException {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		//setup the currently logged in user
		User currentUser = new User();
		Role userRole = new Role("User Adder");
		userRole.setRole(RoleConstants.AUTHENTICATED);
		userRole.addPrivilege(new Privilege("Add Users"));
		userRole.addPrivilege(new Privilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		currentUser.addRole(userRole);
		// setup our expected exception
		// we expect this to fail because the currently logged-in user lacks a privilege to be
		// assigned to the new user
		// set current user to the user defined above
		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// a stub of a stored role: the stored role's privileges are checked, not the stub's empty set
			Role role = new Role("Restricted Role");

			// create our new user object with the required fields
			User u = new User();
			u.setPerson(new Person());
			// assign the specified role to the user
			u.addName(new PersonName("Benjamin", "A", "Wolfe"));
			u.setUsername("bwolfe");
			u.getPerson().setGender("M");
			u.addRole(role);
			// Add Users is held only as a proxy so the createUser gate passes; role privileges resolve from
			// the database by name, so stubbing them on an in-memory role no longer grants them.
			Context.addProxyPrivilege(PrivilegeConstants.ADD_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			try {
				// here we expect the exception to be thrown
				userService.createUser(u, "Openmr5xy");
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				Context.removeProxyPrivilege(PrivilegeConstants.ADD_USERS);
			}
		}));
		assertThat(exception.getMessage(), is("You must have privilege {0} in order to assign it."));
	}

	@Test
	public void createUser_shouldNotAllowCreatingUserWithPrivilegesCurrentUserDoesNotHave() throws IllegalAccessException {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		//setup the currently logged in user
		User currentUser = new User();
		Role userRole = new Role("User Adder");
		userRole.setRole(RoleConstants.AUTHENTICATED);
		userRole.addPrivilege(new Privilege("Add Users"));
		userRole.addPrivilege(new Privilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		currentUser.addRole(userRole);
		// setup our expected exception
		// we expect this to fail because the currently logged-in user lacks a privilege to be
		// assigned to the new user
		// set current user to the user defined above
		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// a stub of a stored role: the stored role's privileges are checked, not the stub's empty set
			Role role = new Role("Doubly Restricted Role");

			// create our new user object with the required fields
			User u = new User();
			u.setPerson(new Person());
			// assign the specified role to the user
			u.addName(new PersonName("Benjamin", "A", "Wolfe"));
			u.setUsername("bwolfe");
			u.getPerson().setGender("M");
			u.addRole(role);
			// Add Users is held only as a proxy so the createUser gate passes; role privileges resolve from
			// the database by name, so stubbing them on an in-memory role no longer grants them.
			Context.addProxyPrivilege(PrivilegeConstants.ADD_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			try {
				// here we expect the exception to be thrown
				userService.createUser(u, "Openmr5xy");
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				Context.removeProxyPrivilege(PrivilegeConstants.ADD_USERS);
			}
		}));
		assertThat(exception.getMessage(), is(
		    "You must have the following privileges in order to assign them: Another Restricted Privilege, Restricted Privilege"));
	}

	@Test
	public void createUser_shouldNotAllowAssigningSuperUserRoleIfCurrentUserDoesNotHaveAssignSystemDeveloperPrivileges()
	        throws IllegalAccessException {
		//setup the currently logged in user
		User currentUser = new User();
		Role userRole = new Role("User Adder");
		userRole.setRole(RoleConstants.AUTHENTICATED);
		userRole.addPrivilege(new Privilege("Add Users"));
		userRole.addPrivilege(new Privilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		currentUser.addRole(userRole);
		// setup our expected exception
		// we expect this to fail because the currently logged-in user lacks a privilege to be
		// assigned to the new user
		// set current user to the user defined above
		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// create a role to assign to the new user
			// the current user cannot assign a user to the superuser role because he lacks AssignSystemDeveloper privileges
			Role role = new Role("add user");
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
			// Add Users is held only as a proxy so the createUser gate passes; role privileges resolve from
			// the database by name, so stubbing them on an in-memory role no longer grants them.
			Context.addProxyPrivilege(PrivilegeConstants.ADD_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			try {
				// here we expect the exception to be thrown
				userService.createUser(u, "Openmr5xy");
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				Context.removeProxyPrivilege(PrivilegeConstants.ADD_USERS);
			}
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
	public void changePassword_shouldRespectLockingViaRuntimeProperty() {
		assertThat("admin", is(Context.getAuthenticatedUser().getUsername()));
		assertTrue(Context.getAuthenticatedUser().isSuperUser());

		User u = userService.getUserByUsername(ADMIN_USERNAME);

		assertThat(u.getUsername(), is("admin"));

		Properties props = Context.getRuntimeProperties();
		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		Context.setRuntimeProperties(props);

		APIException apiException = assertThrows(APIException.class,
		    () -> userService.changePassword(u, "test", "SuperAdmin123"));

		assertThat(apiException.getMessage(), is("admin.password.is.locked"));

		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "True");
		Context.setRuntimeProperties(props);

		apiException = assertThrows(APIException.class, () -> userService.changePassword(u, "test", "SuperAdmin123"));
		assertThat(apiException.getMessage(), is("admin.password.is.locked"));

		props.remove(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY);
		Context.setRuntimeProperties(props);

		userService.changePassword(u, "test", "SuperAdmin123");
	}

	@Test
	public void changePassword_shouldRespectLockingViaRuntimePropertyForSystemIdAdminAndNoUsername() {
		assertThat("admin", is(Context.getAuthenticatedUser().getUsername()));
		assertTrue(Context.getAuthenticatedUser().isSuperUser());

		User u = userService.getUserByUsername(ADMIN_USERNAME);

		u.setSystemId("admin");
		u.setUsername(null);

		Properties props = Context.getRuntimeProperties();
		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		Context.setRuntimeProperties(props);

		APIException apiException = assertThrows(APIException.class,
		    () -> userService.changePassword(u, "test", "SuperAdmin123"));

		assertThat(apiException.getMessage(), is("admin.password.is.locked"));
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

		userService.saveRole(role1);
		userService.saveRole(role2);

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
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
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
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
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
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		userService.changePassword("test", "Tester12");
		Context.logout(); // so that the next test reauthenticates
	}

	/**
	 * This test verifies that {@link PersonName}s are fetched correctly from the hibernate cache. (Or
	 * really, not fetched from the cache but instead are mapped with lazy=false. For some reason
	 * Hibernate isn't able to find objects in the cache if a parent object was the one that loaded
	 * them)
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
		assertEquals(2, privileges.size());
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
		assertThat(users, hasSize(greaterThanOrEqualTo(3)));
	}

	/**
	 * @see UserService#getAllUsers()
	 */
	@Test
	@Disabled("Failing on CI in docker build for some reason - TRUNK-6083")
	public void getAllUsers_shouldNotContainsAnyDuplicateUsers() {
		executeDataSet(XML_FILENAME);
		List<User> users = userService.getAllUsers();
		assertEquals(11, users.size());
		// TODO Need to test with duplicate data in the dataset (not sure if that's possible)

	}

	@Test
	public void getUserByUuid_shouldFetchUserWithGivenUuid() throws SQLException {
		executeDataSet(XML_FILENAME);

		User user = userService.getUserByUuid("013c49c6-e132-11de-babe-001e378eb67e");
		assertEquals(user, userService.getUser(5505), "Did not fetch user with given uuid");
	}

	@Test
	public void getUsersByName_shouldFetchUsersExactlyMatchingTheGivenGivenNameAndFamilyName() throws SQLException {
		executeDataSet(XML_FILENAME);

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
	public void getUsersByName_shouldNotFetchAnyDuplicateUsers() throws SQLException {
		executeDataSet(XML_FILENAME);

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
		assertThat(userService.getUsers("", null, true), hasSize(greaterThanOrEqualTo(3)));
		assertThat(userService.getUsers(null, null, true), hasSize(greaterThanOrEqualTo(3)));
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

		assertThrows(AccessDeniedException.class, () -> userService.removeUserProperty(user, "some key"));
	}

	/**
	 * @see UserService#savePrivilege(Privilege)
	 */
	@Test
	public void savePrivilege_shouldSaveGivenPrivilegeToTheDatabase() {
		Privilege p = new Privilege("new privilege name", "new privilege desc");
		p.setName("Test Privilege");
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
		assertThrows(AccessDeniedException.class, () -> userService.setUserProperty(user, "some key", "some value"));
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
		executeDataSet(XML_FILENAME);
		// "Some Role" already inherits "Parent", so "Parent" inheriting "Some Role" is a cycle
		Role parentRole = userService.getRole("Parent");
		parentRole.getInheritedRoles().add(new Role("Some Role"));

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

		userService.savePrivilege(myPrivilege);
		withCurrentUserAs(currentUser, () -> {
			Role newRole = new Role("another role");
			newRole.addPrivilege(myPrivilege);
			userService.saveRole(newRole);
		});
	}

	@Test
	public void saveRole_shouldThrowErrorWhenCurrentUserLacksPrivilegeAssignedToRole() throws IllegalAccessException {
		User currentUser = new User();

		Privilege myPrivilege = new Privilege("custom privilege");

		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// Manage Roles is held only as a proxy so the saveRole gate passes; role privileges resolve from
			// the database by name. The assigned privilege is deliberately not held, so the assignment check
			// fails on it.
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
			try {
				Role newRole = new Role("another role");
				newRole.addPrivilege(myPrivilege);
				userService.saveRole(newRole);
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
			}
		}));
		assertThat(exception.getMessage(),
		    is("You must have the following privileges in order to assign them: custom privilege"));
	}

	@Test
	public void saveRole_shouldThrowErrorWhenCurrentUserLacksAPrivilegeAssignedToRole() throws IllegalAccessException {
		User currentUser = new User();

		Privilege myFirstPrivilege = new Privilege("custom privilege");
		Privilege mySecondPrivilege = new Privilege("another privilege");

		APIException exception = assertThrows(APIException.class, () -> withCurrentUserAs(currentUser, () -> {
			// Manage Roles (the gate) is a proxy. Proxy privileges never count as held when handing them
			// out, so "custom privilege", held only as a proxy, is reported along with "another privilege".
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
			Context.addProxyPrivilege(myFirstPrivilege.getPrivilege());
			try {
				Role newRole = new Role("another role");
				newRole.addPrivilege(myFirstPrivilege);
				newRole.addPrivilege(mySecondPrivilege);
				userService.saveRole(newRole);
			} finally {
				Context.removeProxyPrivilege(myFirstPrivilege.getPrivilege());
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
			}
		}));
		assertThat(exception.getMessage(),
		    is("You must have the following privileges in order to assign them: another privilege, custom privilege"));
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

		assertThat(userService.getUsers("", roles, true), hasSize(greaterThanOrEqualTo(2)));
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
		assertEquals(3, user.getUserProperties().size());
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
		assertEquals(2, user.getUserProperties().size());
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
	public void changePassword_shouldChangePasswordForGivenUserIfOldPasswordIsNullAndChangingUserHavePrivileges() {
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

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
		    () -> userService.changePassword(user6001, wrongPassword, newPassword));
		assertThat(exception.getMessage(), is(
		    messages.getMessage("error.privilegesRequired", new Object[] { PrivilegeConstants.EDIT_USER_PASSWORDS }, null)));
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

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
		    () -> userService.changePassword(user6001, oldPassword, newPassword));
		assertThat(exception.getMessage(), is(
		    messages.getMessage("error.privilegesRequired", new Object[] { PrivilegeConstants.EDIT_USER_PASSWORDS }, null)));
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

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
		    () -> userService.changePassword(user6001, oldPassword, newPassword));
		assertThat(exception.getMessage(), is(
		    messages.getMessage("error.privilegesRequired", new Object[] { PrivilegeConstants.EDIT_USER_PASSWORDS }, null)));
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

		APIException exception = assertThrows(APIException.class,
		    () -> userService.changePassword(user6001, oldPassword, weakPassword));
		assertThat(exception.getMessage(), is(messages.getMessage("error.password.length", new Object[] { "8" }, null)));
	}

	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfGivenUserDoesNotExist() {
		//user.getUserId is null - so it is not existing user
		User notExistingUser = new User();
		String anyString = "anyString";

		APIException exception = assertThrows(APIException.class,
		    () -> userService.changePassword(notExistingUser, anyString, anyString));
		assertThat(exception.getMessage(), is(messages.getMessage("user.must.exist")));
	}

	@Test
	public void changePassword_shouldThrowShortPasswordExceptionWithShortPassword() {
		ShortPasswordException exception = assertThrows(ShortPasswordException.class,
		    () -> userService.changePassword("test", ""));
		assertThat(exception.getMessage(), is(messages.getMessage("error.password.length", new Object[] { "8" }, null)));
	}

	@Test
	public void changePassword_shouldUpdatePasswordOfGivenUserWhenLoggedInUserHasEditUsersPasswordPrivilege() {
		User user = userService.getUserByUsername(ADMIN_USERNAME);
		assertNotNull(user, "There needs to be a user with username 'admin' in the database");

		userService.changePassword(user, "test", "testTest123");

		Context.authenticate(user.getUsername(), "testTest123");
	}

	@Test
	public void changePassword_shouldNotUpdatePasswordOfGivenUserWhenLoggedInUserDoesNotHaveEditUsersPasswordPrivilege() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
		    () -> userService.changePassword(user, "userServiceTest", "testTest123"));

		assertThat(exception.getMessage(), is(
		    messages.getMessage("error.privilegesRequired", new Object[] { PrivilegeConstants.EDIT_USER_PASSWORDS }, null)));
	}

	@Test
	public void changePasswordUsingSecretAnswer_shouldUpdatePasswordIfSecretIsCorrect() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		userService.changePasswordUsingSecretAnswer("answer", "userServiceTest2");
		Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		Context.authenticate(user.getUsername(), "userServiceTest2");
	}

	@Test
	public void changePasswordUsingSecretAnswer_shouldNotUpdatePasswordIfSecretIsNotCorrect() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");

		APIException exception = assertThrows(APIException.class,
		    () -> userService.changePasswordUsingSecretAnswer("wrong answer", "userServiceTest2"));
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
		User createdUser = createTestUser();
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_PASSWORD_RESET_URL,
		    "http://localhost:8080/openmrs/admin/users/changePassword.form/{activationKey}");
		assertNull(dao.getLoginCredential(createdUser).getActivationKey());
		assertThrows(MessageException.class, () -> userService.setUserActivationKey(createdUser));
		assertNotNull(dao.getLoginCredential(createdUser).getActivationKey());
	}

	@Test
	public void getUserByActivationKey_shouldGetUserByActivationKey() {
		User createdUser = createTestUser();
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() + validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.setUserActivationKey(credentials);
		assertEquals(createdUser, userService.getUserByActivationKey(key));
	}

	@Test
	public void getUserByActivationKey_shouldReturnNullIfTokenTimeExpired() {
		User createdUser = createTestUser();
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() - validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.setUserActivationKey(credentials);
		assertNull(userService.getUserByActivationKey(key));
	}

	@Test
	public void changePasswordUsingActivationKey_shouldUpdatePasswordIfActivationKeyIsCorrect() {
		User createdUser = createTestUser();
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() + validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.setUserActivationKey(credentials);

		final String PASSWORD = "Admin123";
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		userService.changePasswordUsingActivationKey(key, PASSWORD);
		Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		Context.authenticate(createdUser.getUsername(), PASSWORD);

	}

	@Test
	public void changePasswordUsingActivationKey_shouldNotUpdatePasswordIfActivationKeyIsIncorrect() {
		User createdUser = createTestUser();
		String key = "wrongactivationkeyin";
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		InvalidActivationKeyException exception = assertThrows(InvalidActivationKeyException.class,
		    () -> userService.changePasswordUsingActivationKey(key, "Pa55w0rd"));
		assertThat(exception.getMessage(), is(messages.getMessage("activation.key.not.correct")));
	}

	@Test
	public void changePasswordUsingActivationKey_shouldNotUpdatePasswordIfActivationKeyExpired() {
		User createdUser = createTestUser();
		String key = "h4ph0fpNzQCIPSw8plJI";
		int validTime = 10 * 60 * 1000; //equivalent to 10 minutes for token to be valid
		Long tokenTime = System.currentTimeMillis() - validTime;
		LoginCredential credentials = dao.getLoginCredential(createdUser);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.setUserActivationKey(credentials);
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");

		InvalidActivationKeyException exception = assertThrows(InvalidActivationKeyException.class,
		    () -> userService.changePasswordUsingActivationKey(key, "Pa55w0rd"));
		assertThat(exception.getMessage(), is(messages.getMessage("activation.key.not.correct")));
	}

	@Test
	public void changePasswordUsingActivationKey_shouldClearActivationKeyOnceUsed() {
		User createdUser = createTestUser();
		String key = givenPendingActivationKey(createdUser);
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		userService.changePasswordUsingActivationKey(key, "Admin123");
		Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		assertNull(dao.getLoginCredential(createdUser).getActivationKey());
	}

	@Test
	public void changePasswordUsingActivationKey_shouldNotAllowReusingActivationKey() {
		User createdUser = createTestUser();
		String key = givenPendingActivationKey(createdUser);
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		userService.changePasswordUsingActivationKey(key, "Admin123");
		Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);

		InvalidActivationKeyException exception = assertThrows(InvalidActivationKeyException.class,
		    () -> userService.changePasswordUsingActivationKey(key, "Pa55w0rd"));
		assertThat(exception.getMessage(), is(messages.getMessage("activation.key.not.correct")));
	}

	@Test
	public void changePassword_shouldClearPendingActivationKeyWhenUserChangesOwnPassword() {
		User createdUser = createTestUser();
		givenPendingActivationKey(createdUser);
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		userService.changePassword("Openmr5xy", "Admin123");
		assertNull(dao.getLoginCredential(createdUser).getActivationKey());
	}

	@Test
	public void changePassword_shouldClearPendingActivationKeyWhenAdminChangesUsersPassword() {
		User createdUser = createTestUser();
		givenPendingActivationKey(createdUser);
		userService.changePassword(createdUser, "Admin123");
		assertNull(dao.getLoginCredential(createdUser).getActivationKey());
	}

	@Test
	public void changePassword_shouldKeepPendingActivationKeyIfOldPasswordIsNotCorrect() {
		User createdUser = createTestUser();
		givenPendingActivationKey(createdUser);
		Context.authenticate(createdUser.getUsername(), "Openmr5xy");
		assertThrows(APIException.class, () -> userService.changePassword("wrongPassword", "Admin123"));
		assertNotNull(dao.getLoginCredential(createdUser).getActivationKey());
	}

	/**
	 * Stores a pending, unexpired activation key on the user's login credential and returns the raw key
	 * the user would have received by email.
	 *
	 * @param user the user to give a pending password reset
	 * @return the raw (unhashed) activation key
	 */
	private String givenPendingActivationKey(User user) {
		String key = "h4ph0fpNzQCIPSw8plJI";
		long tokenTime = System.currentTimeMillis() + 10 * 60 * 1000;
		LoginCredential credentials = dao.getLoginCredential(user);
		credentials.setActivationKey(
		    "b071c88d6d877922e35af2e6a90dd57d37ac61143a03bb986c5f353566f3972a86ce9b2604c31a22dfa467922dcfd54fa7d18b0a7c7648d94ca3d97a88ea2fd0:"
		            + tokenTime);
		dao.setUserActivationKey(credentials);
		return key;
	}

	@Test
	public void setUserActivationKey_shouldAppendResetLinkAfterMessageBody() throws Exception {
		User createdUser = createTestUser();
		String content = sendPasswordResetEmail(createdUser, "Hi {name}, use the link below.", "text/plain");
		assertThat(content.substring(content.lastIndexOf("\n\n") + 2), startsWith(RESET_URL_PREFIX));
	}

	@Test
	public void setUserActivationKey_shouldNotSubstituteResetLinkIntoMessageBody() throws Exception {
		User createdUser = createTestUser();
		String content = sendPasswordResetEmail(createdUser, "Go to https://evil.example/?u={link}", "text/plain");
		assertThat(content, not(containsString("?u=http")));
	}

	@Test
	public void setUserActivationKey_shouldAppendResetLinkAsAnchorForHtmlMail() throws Exception {
		User createdUser = createTestUser();
		String content = sendPasswordResetEmail(createdUser, "Hi {name},<br/>use the link below.",
		    "text/html; charset=UTF-8");
		assertThat(content.substring(content.lastIndexOf("<br/><br/>") + 10), startsWith("<a href=\"" + RESET_URL_PREFIX));
	}

	private static final String RESET_URL_PREFIX = "http://localhost:8080/openmrs/reset/";

	/**
	 * Sends the password reset email for the user with the given text as the mail.passwordreset.content
	 * template and the given mail content type, and returns the email body.
	 */
	private String sendPasswordResetEmail(User user, String template, String contentType) throws MessageException {
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_PASSWORD_RESET_URL,
		    RESET_URL_PREFIX + "{activationKey}");
		Context.getAdministrationService().setGlobalProperty("mail.default_content_type", contentType);
		MutableMessageSource source = mock(MutableMessageSource.class);
		when(source.getMessage(eq("mail.passwordreset.content"), isNull(), anyString(), any(Locale.class)))
		        .thenReturn(template);
		when(source.getMessage(eq("mail.passwordreset.subject"), isNull(), anyString(), any(Locale.class)))
		        .thenReturn("Password Reset");
		MessageSourceService messageSourceService = Context.getMessageSourceService();
		MutableMessageSource originalSource = messageSourceService.getActiveMessageSource();
		MessageService messageService = Context.getMessageService();
		MessageSender originalSender = messageService.getMessageSender();
		List<Message> sent = new ArrayList<>();
		messageSourceService.setActiveMessageSource(source);
		messageService.setMessageSender(sent::add);
		try {
			userService.setUserActivationKey(user);
		} finally {
			messageService.setMessageSender(originalSender);
			messageSourceService.setActiveMessageSource(originalSource);
		}
		return sent.getFirst().getContent();
	}

	/**
	 * Utility method to set the current executing user to test various permission levels.
	 *
	 * @param user the user to set as the currently running user
	 * @param internals the functionality to test with this current running user
	 */
	private void withCurrentUserAs(User user, Runnable internals) {
		UserContext userContext = Context.getUserContext();
		User authenticatedUser = userContext.getAuthenticatedUser();
		try {
			UserContextTestUtil.setAuthenticatedUser(userContext, user);
			internals.run();
		} finally {
			UserContextTestUtil.setAuthenticatedUser(userContext, authenticatedUser);
		}
	}

	@Test
	public void saveUserProperty_shouldAddANewPropertyWithAVeryLargeStringWithoutRunningIntoError() {
		final String USER_PROPERTY_KEY = liquibase.util.StringUtil.repeat("emrapi.lastViewedPatientIds,", 10);
		final String USER_PROPERTY_VALUE = liquibase.util.StringUtil.repeat("52345", 9899);
		User updatedUser = userService.saveUserProperty(USER_PROPERTY_KEY, USER_PROPERTY_VALUE);

		Set<String> emrApiPropertyKeys = updatedUser.getUserProperties().keySet().stream()
		        .filter(key -> key.contains("emrapi.lastViewedPatientIds")).collect(Collectors.toSet());

		assertEquals(280, emrApiPropertyKeys.stream().findFirst().orElse("").length());
		assertEquals(49495, updatedUser.getUserProperties().get(USER_PROPERTY_KEY).length());
	}

	@Test
	public void getDefaultLocaleForUser_shouldReturnSystemLocaleIfNoUserLocaleConfigured() {
		User createdUser = createTestUser();
		assertEquals("", createdUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE));
		assertEquals(Context.getLocale(), Context.getUserService().getDefaultLocaleForUser(createdUser));
	}

	@Test
	public void getDefaultLocaleForUser_shouldReturnDefaultLocaleForUserIfConfigured() {
		User createdUser = createTestUser();
		createdUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "en");
		Context.getUserService().saveUser(createdUser);
		assertEquals(Locale.ENGLISH, Context.getUserService().getDefaultLocaleForUser(createdUser));
		createdUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "fr");
		Context.getUserService().saveUser(createdUser);
		assertEquals(Locale.FRENCH, Context.getUserService().getDefaultLocaleForUser(createdUser));
	}

	@Test
	public void getDefaultLocaleForUser_shouldReturnDefaultLocaleForUserIfAlreadySet() {
		executeDataSet(XML_FILENAME);
		Context.authenticate("test", "testUser1234");

		Locale locale = Context.getLocale();
		assertEquals(Locale.FRENCH, locale);
	}

	@Test
	public void getLastLoginTimeForUser_shouldReturnEmptyStringOnLastLoginTimeIfPropertyNotSet() {
		User createdUser = createTestUser();
		assertThat(createdUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP), emptyString());
		assertThat(Context.getUserService().getLastLoginTime(createdUser), emptyString());
	}

	@Test
	public void getLastLoginTimeForUser_shouldReturnEmptyStringOnLastLoginTimeIfADifferentUserIsLoggedIn() {
		executeDataSet(XML_FILENAME);
		User createdUser = createTestUser();
		Context.authenticate(getTestUserCredentials());

		assertThat(createdUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP), emptyString());
		assertThat(Context.getUserService().getLastLoginTime(createdUser), emptyString());
	}

	@Test
	public void getLastLoginTimeForUser_shouldNotBeEmptyIfUserIsAuthenticated() {
		executeDataSet(XML_FILENAME);
		User createdUser = createTestUser();
		Context.authenticate(new UsernamePasswordCredentials("bwolfe", "Openmr5xy"));

		assertThat(createdUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP), notNullValue());
		assertThat(createdUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP), not(emptyString()));
		assertThat(Context.getUserService().getLastLoginTime(createdUser), not(emptyString()));
	}

	private Credentials getTestUserCredentials() {
		return new UsernamePasswordCredentials("test", "testUser1234");
	}

	private User createTestUser() {
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		return userService.createUser(u, "Openmr5xy");
	}

	/**
	 * Pins the superuser-assignment guard in {@code UserServiceImpl.checkSuperUserPrivilege}, which
	 * compares the role name case-insensitively. It has to: a role differing from
	 * {@link RoleConstants#SUPERUSER} only in case still confers superuser status, since
	 * {@code RolePrivilegeCache} resolves that name with {@code equalsIgnoreCase}. Comparing exactly
	 * here would let such a role be handed out without {@code ASSIGN_SYSTEM_DEVELOPER_ROLE}.
	 */
	@Test
	public void saveUser_shouldRequireAssignSystemDeveloperRoleForADifferentlyCasedSuperuserRole() throws Exception {
		// an already-valid user, because RequiredDataAdvice validates a save* call before the service
		// method body runs - a hand-built User fails validation long before reaching the guard. Not a
		// superuser, since changing one already requires ASSIGN_SYSTEM_DEVELOPER_ROLE.
		User toSave = userService.getUser(502);
		// drop the user's own roles, so the differently-cased one added below is the only thing that can
		// trip the guard
		toSave.getRoles().clear();
		toSave.addRole(new Role(RoleConstants.SUPERUSER.toUpperCase()));

		// a principal with no roles, so it lacks ASSIGN_SYSTEM_DEVELOPER_ROLE. EDIT_USERS satisfies
		// saveUser's own requirePrivilege and @Authorized; GET_GLOBAL_PROPERTIES is what the validator
		// run by RequiredDataAdvice needs before the guard is reached.
		Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			APIException exception = assertThrows(APIException.class,
			    () -> withCurrentUserAs(new User(), () -> userService.saveUser(toSave)));

			// the role guard, not the privileges one below it - the message arrives as its raw template
			// here, since the test message source does not interpolate arguments
			assertThat(exception.getMessage(), containsString("must have the role"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		}
	}

	/**
	 * @see User#setRoles(Set)
	 */
	@Test
	public void setRoles_shouldNotRewriteRowsWhenGivenTheSameInstances() {
		User user = userService.getUser(501);
		Statistics statistics = ((SessionFactory) applicationContext.getBean("sessionFactory")).getStatistics();
		statistics.clear();

		user.setRoles(new HashSet<>(user.getRoles()));
		Context.flushSession();

		CollectionStatistics roles = statistics.getCollectionStatistics("org.openmrs.User.roles");
		assertEquals(0, roles.getRecreateCount() + roles.getRemoveCount() + roles.getUpdateCount());
	}

	@Test
	public void saveRole_shouldNotAllowInheritingSystemDeveloperWithoutAssignSystemDeveloperRole() throws Exception {
		executeDataSet(XML_FILENAME);
		Role role = detachedRole("Some Role");
		role.getInheritedRoles().add(new Role(RoleConstants.SUPERUSER));

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		try {
			APIException exception = assertThrows(APIException.class,
			    () -> withCurrentUserAs(new User(), () -> userService.saveRole(role)));
			assertThat(exception.getMessage(), containsString("must have the role"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}

		assertThat(storedParentRoleNames("Some Role"), not(hasItem(RoleConstants.SUPERUSER)));
	}

	@Test
	public void saveRole_shouldNotAllowInheritingARoleWhoseInheritedPrivilegesCurrentUserLacks() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Role role = detachedRole("Some Role");
		// "Inherits Restricted Role" grants nothing itself; the restricted privilege comes from its parent
		role.getInheritedRoles().add(new Role("Inherits Restricted Role"));

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		try {
			APIException exception = assertThrows(APIException.class,
			    () -> withCurrentUserAs(new User(), () -> userService.saveRole(role)));
			assertThat(exception.getMessage(), containsString("Restricted Privilege"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}

		assertThat(storedParentRoleNames("Some Role"), not(hasItem("Inherits Restricted Role")));
	}

	@Test
	public void saveRole_shouldNotWriteChildRoles() throws Exception {
		executeDataSet(XML_FILENAME);
		Role someRole = userService.getRole("Some Role");
		someRole.getChildRoles().add(userService.getRole("Some Role To Delete"));

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		try {
			withCurrentUserAs(new User(), () -> userService.saveRole(someRole));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}

		assertThat(storedParentRoleNames("Some Role To Delete"), not(hasItem("Some Role")));
	}

	@Test
	public void saveRole_shouldFailForAnInheritedRoleThatIsNotStored() {
		executeDataSet(XML_FILENAME);
		Role role = detachedRole("Some Role");
		role.getInheritedRoles().add(new Role("No Such Role"));

		APIException exception = assertThrows(APIException.class, () -> userService.saveRole(role));
		assertThat(exception.getMessage(), containsString("No Such Role"));

		assertThat(storedParentRoleNames("Some Role"), not(hasItem("No Such Role")));
		assertNull(userService.getRole("No Such Role"));
	}

	@Test
	public void saveRole_shouldSaveAPrivilegeGivenAsANewInstanceOfAStoredPrivilege() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Role role = userService.getRole("Restricted Role");

		role.addPrivilege(new Privilege("Revocable Privilege"));
		userService.saveRole(role);

		assertThat(storedPrivilegeNames("Restricted Role"), is(Set.of("Restricted Privilege", "Revocable Privilege")));
	}

	@Test
	public void saveRole_shouldSaveAPrivilegeLoadedInAnEarlierSession() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Privilege privilege = userService.getPrivilege("Revocable Privilege");
		Context.clearSession();
		Role role = userService.getRole("Restricted Role");

		role.addPrivilege(privilege);
		userService.saveRole(role);

		assertThat(storedPrivilegeNames("Restricted Role"), is(Set.of("Restricted Privilege", "Revocable Privilege")));
	}

	@Test
	public void saveRole_shouldSaveANewRoleWithANewInstanceOfAStoredPrivilege() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Role role = new Role("New Role", "new");

		role.addPrivilege(new Privilege("Revocable Privilege"));
		userService.saveRole(role);

		assertThat(storedPrivilegeNames("New Role"), is(Set.of("Revocable Privilege")));
	}

	@Test
	public void saveRole_shouldFailForAPrivilegeThatIsNotStored() {
		executeDataSet(XML_FILENAME);
		Role role = userService.getRole("Some Role");
		role.addPrivilege(new Privilege("No Such Privilege"));

		APIException exception = assertThrows(APIException.class, () -> userService.saveRole(role));
		assertThat(exception.getMessage(), containsString("No Such Privilege"));
	}

	@Test
	public void saveUser_shouldNotAllowAssigningARoleWhoseInheritedPrivilegesCurrentUserLacks() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		User toSave = userService.getUser(5505);
		toSave.addRole(new Role("Inherits Restricted Role"));

		Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			APIException exception = assertThrows(APIException.class,
			    () -> withCurrentUserAs(new User(), () -> userService.saveUser(toSave)));
			assertThat(exception.getMessage(), containsString("must have privilege"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		}
	}

	@Test
	public void saveUser_shouldFailForARoleThatIsNotStored() {
		executeDataSet(XML_FILENAME);
		User toSave = userService.getUser(5505);
		toSave.addRole(new Role("No Such Role"));

		APIException exception = assertThrows(APIException.class, () -> userService.saveUser(toSave));
		assertThat(exception.getMessage(), containsString("No Such Role"));
		Context.clearSession();

		assertNull(userService.getRole("No Such Role"));
		assertThat(userService.getUser(5505).getRoles().stream().map(Role::getRole).collect(Collectors.toSet()),
		    not(hasItem("No Such Role")));
	}

	@Test
	public void createUser_shouldAcceptAnImmutableSetOfRoles() {
		User u = new User();
		u.setPerson(new Person());
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		u.setRoles(Set.of(new Role("Provider")));

		User created = userService.createUser(u, "Openmr5xy");

		assertTrue(created.hasRole("Provider"));
	}

	/**
	 * Guards against a REMOVE cascade being added to User.roles: purging a user must never remove their
	 * roles.
	 */
	@Test
	public void purgeUser_shouldNotDeleteTheUsersRoles() {
		User user = userService.getUser(502);
		assertThat(user.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()), hasItem("Provider"));

		userService.purgeUser(user);
		Context.flushSession();
		Context.clearSession();

		assertNotNull(userService.getRole("Provider"));
	}

	@Test
	public void saveUserProperty_shouldNotRestoreRolesOrUnretireAUserChangedSinceLogin() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		UserContext admin = Context.getUserContext();
		UserContext session = new UserContext(Context.getAuthenticationScheme());
		session.authenticate(getTestUserCredentials());
		// a session's user was loaded by an earlier request, so it is detached from this one
		User sessionUser = session.getAuthenticatedUser();
		Context.evictFromSession(sessionUser);

		User stored = userService.getUser(5511);
		stored.removeRole(userService.getRole("Revocable Role"));
		userService.retireUser(stored, "testing");
		Context.flushSession();
		Context.clearSession();

		Context.setUserContext(session);
		try {
			userService.saveUserProperty("test-key", "test-value");
			Context.flushSession();
			Context.clearSession();
		} finally {
			Context.setUserContext(admin);
		}

		assertEquals("test-value", sessionUser.getUserProperty("test-key"));
		stored = userService.getUser(5511);
		assertTrue(stored.getRetired());
		assertThat(stored.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()),
		    not(hasItem("Revocable Role")));
		assertEquals("test-value", stored.getUserProperty("test-key"));
	}

	@Test
	public void saveUser_shouldNotLetUsersGrantThemselvesRolesTheyCouldNotAssign() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());
		User me = Context.getAuthenticatedUser();
		me.addRole(new Role("Doubly Restricted Role"));

		Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			APIException exception = assertThrows(APIException.class, () -> userService.saveUser(me));
			assertThat(exception.getMessage(), containsString("Restricted Privilege"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		}
	}

	@Test
	public void saveUser_shouldNotLetUsersGrantThemselvesSystemDeveloper() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());
		User me = Context.getAuthenticatedUser();
		me.addRole(new Role(RoleConstants.SUPERUSER));

		Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			APIException exception = assertThrows(APIException.class, () -> userService.saveUser(me));
			assertThat(exception.getMessage(), containsString("must have the role"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
		}
	}

	@Test
	public void saveUser_shouldNotWriteChangesMadeInMemoryToARole() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		withProxyPrivileges(() -> {
			Role restricted = userService.getRole("Restricted Role");
			restricted.getPrivileges().clear();
			User target = userService.getUser(5505);
			target.addRole(restricted);

			assertThrows(APIException.class, () -> {
				userService.saveUser(target);
				Context.flushSession();
			});
		});
	}

	@Test
	public void saveRole_shouldNotLetAHolderGrantTheirRoleAPrivilegeAddedInMemory() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		// a role written earlier in the transaction makes privilege checks read roles through the session
		userService.saveRole(userService.getRole("Some Role"));
		Context.authenticate(getTestUserCredentials());

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		Context.addProxyPrivilege(PrivilegeConstants.GET_ROLES);
		Context.addProxyPrivilege(PrivilegeConstants.GET_PRIVILEGES);
		try {
			Role mine = userService.getRole("Revocable Role");
			mine.addPrivilege(userService.getPrivilege("Restricted Privilege"));

			assertFalse(Context.hasPrivilege("Restricted Privilege"));
			APIException exception = assertThrows(APIException.class, () -> userService.saveRole(mine));
			assertThat(exception.getMessage(), containsString("Restricted Privilege"));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_PRIVILEGES);
			Context.removeProxyPrivilege(PrivilegeConstants.GET_ROLES);
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}
	}

	@Test
	public void saveRole_shouldNotLetAHolderApproveAPrivilegeAddedInMemoryAfterSavingTheRole() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		Context.addProxyPrivilege(PrivilegeConstants.GET_ROLES);
		Context.addProxyPrivilege(PrivilegeConstants.GET_PRIVILEGES);
		try {
			Role mine = userService.getRole("Revocable Role");
			userService.saveRole(mine);
			mine.addPrivilege(userService.getPrivilege("Restricted Privilege"));

			assertFalse(Context.hasPrivilege("Restricted Privilege"));
			assertThrows(APIException.class, () -> userService.saveRole(mine));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_PRIVILEGES);
			Context.removeProxyPrivilege(PrivilegeConstants.GET_ROLES);
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}
	}

	@Test
	public void saveRole_shouldNotWriteAChangeMadeInMemoryAfterTheSave() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		withProxyPrivileges(() -> {
			Role mine = userService.getRole("Revocable Role");
			userService.saveRole(mine);
			mine.addPrivilege(userService.getPrivilege("Restricted Privilege"));

			assertThrows(APIException.class, Context::flushSession);
		});
	}

	@Test
	public void saveUser_shouldNotWriteARoleChangeMadeInMemoryAfterTheSave() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		withProxyPrivileges(() -> {
			User user = userService.getUser(5505);
			userService.saveUser(user);
			user.addRole(userService.getRole("Restricted Role"));

			assertThrows(APIException.class, Context::flushSession);
		});
	}

	@Test
	public void setUserProperty_shouldNotWriteARoleChangeMadeInMemoryToTheSameUser() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		withProxyPrivileges(() -> {
			User me = userService.getUser(5511);
			me.addRole(userService.getRole("Restricted Role"));

			userService.setUserProperty(me, "some key", "changed");

			assertThrows(APIException.class, Context::flushSession);
		});
	}

	@Test
	public void saveUser_shouldNotLetProxyPrivilegesSatisfyTheAssignmentCheck() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		withProxyPrivileges(() -> {
			Context.addProxyPrivilege("Restricted Privilege");
			try {
				User target = userService.getUser(5505);
				target.addRole(new Role("Restricted Role"));

				assertThrows(APIException.class, () -> userService.saveUser(target));
			} finally {
				Context.removeProxyPrivilege("Restricted Privilege");
			}
		});
	}

	@Test
	public void saveUser_shouldLetAUserWithTheRequiredPrivilegesWriteARoleChangeMadeBeforeTheSave() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		User user = userService.getUser(5505);
		user.addRole(userService.getRole("Restricted Role"));
		// a query that touches users can flush the change before saveUser is reached
		Context.flushSession();

		userService.saveUser(user);
		Context.flushSession();
		Context.clearSession();

		assertTrue(userService.getUser(5505).hasRole("Restricted Role"));
	}

	@Test
	public void refreshAuthenticatedUser_shouldIgnoreARoleAddedInMemory() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		Context.getAuthenticatedUser().addRole(userService.getRole("Restricted Role"));
		Context.refreshAuthenticatedUser();

		assertFalse(Context.hasPrivilege("Restricted Privilege"));
	}

	@Test
	public void refreshAuthenticatedUser_shouldNotBecomeTheUserWhoseUuidWasSetInMemory() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		String adminUuid = userService.getUserByUsername(ADMIN_USERNAME).getUuid();
		Context.authenticate(getTestUserCredentials());

		Context.getAuthenticatedUser().setUuid(adminUuid);
		Context.refreshAuthenticatedUser();

		assertEquals(5511, Context.getAuthenticatedUser().getUserId());
		assertFalse(Context.hasPrivilege("Restricted Privilege"));
	}

	@Test
	public void saveUserProperty_shouldWriteToTheAuthenticatedUserRatherThanTheIdSetInMemory() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());

		Context.getAuthenticatedUser().setUserId(1);
		userService.saveUserProperty("planted key", "planted value");
		Context.getAuthenticatedUser().setUserId(5511);
		Context.flushSession();
		Context.clearSession();
		authenticate();

		assertEquals("", userService.getUser(1).getUserProperty("planted key"));
		assertEquals("planted value", userService.getUser(5511).getUserProperty("planted key"));
	}

	@Test
	public void setUserProperty_shouldNotLetAUserManagerChangeASuperusersProperties() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.authenticate(getTestUserCredentials());

		APIException exception = assertThrows(APIException.class,
		    () -> userService.setUserProperty(admin, OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, "9999999999999"));
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
		assertThrows(APIException.class,
		    () -> userService.removeUserProperty(admin, OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP));
	}

	@Test
	public void setUserProperty_shouldLetAUserManagerChangeTheirOwnAndOtherUsersProperties() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User other = userService.getUser(5505);
		Context.authenticate(getTestUserCredentials());

		userService.setUserProperty(other, "some key", "changed");
		userService.setUserProperty(Context.getAuthenticatedUser(), "some key", "changed");
		Context.flushSession();
		Context.clearSession();

		assertEquals("changed", userService.getUser(5505).getUserProperty("some key"));
		assertEquals("changed", userService.getUser(5511).getUserProperty("some key"));
	}

	@Test
	public void changePassword_shouldNotLetAUserManagerResetASuperusersPassword() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.authenticate(getTestUserCredentials());

		APIException exception = assertThrows(APIException.class,
		    () -> userService.changePassword(admin, null, "NewPassw0rd"));
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
		assertThrows(APIException.class, () -> userService.changeHashedPassword(admin, "hashed", "salt"));
		assertThrows(APIException.class, () -> userService.changeQuestionAnswer(admin, "question", "answer"));
	}

	@Test
	public void changePassword_shouldLetAUserManagerResetAnotherUsersPassword() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User other = userService.getUser(5505);
		Context.authenticate(getTestUserCredentials());

		userService.changePassword(other, null, "NewPassw0rd");

		assertTrue(dao.getLoginCredential(other).checkPassword("NewPassw0rd"));
	}

	@Test
	public void hasPrivilege_shouldNotBeGrantedByRenamingTheImplicitAuthenticatedRole() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());
		Role authenticated = Context.getAllRoles(Context.getAuthenticatedUser()).stream()
		        .filter(role -> RoleConstants.AUTHENTICATED.equals(role.getRole())).findFirst().orElseThrow();
		Context.evictFromSession(authenticated);

		authenticated.setRole(RoleConstants.SUPERUSER);
		try {
			assertFalse(Context.hasPrivilege("Restricted Privilege"));
		} finally {
			authenticated.setRole(RoleConstants.AUTHENTICATED);
		}
	}

	@Test
	public void becomeUser_shouldNotBeAllowedByASuperuserRoleAddedInMemory() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());
		Context.evictFromSession(Context.getAuthenticatedUser());

		Context.getAuthenticatedUser().addRole(new Role(RoleConstants.SUPERUSER));

		assertThrows(APIAuthenticationException.class, () -> Context.becomeUser(ADMIN_USERNAME));
		assertEquals(5511, Context.getAuthenticatedUser().getUserId());
	}

	@Test
	public void hasRole_shouldReadTheRolesTheUserHeldWhenLoaded() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());
		Context.evictFromSession(Context.getAuthenticatedUser());

		Context.getAuthenticatedUser().addRole(new Role("Some Role"));
		Context.getAuthenticatedUser().addRole(new Role(RoleConstants.SUPERUSER));

		assertTrue(Context.getUserContext().hasRole("Revocable Role"));
		assertTrue(Context.getUserContext().hasRole("revocable role"));
		assertFalse(Context.getUserContext().hasRole("Some Role"));
	}

	@Test
	public void hasRole_shouldIncludeInheritedRoles() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		User user = userService.getUser(5511);
		user.addRole(userService.getRole("Inherits Restricted Role"));
		userService.saveUser(user);
		Context.flushSession();
		Context.authenticate(getTestUserCredentials());

		assertTrue(Context.getUserContext().hasRole("Restricted Role"));
	}

	@Test
	public void saveRole_shouldNotWriteAPrivilegeAddedToARoleSavedWithoutPrivileges() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		Privilege restricted = userService.getPrivilege("Restricted Privilege");

		Role role = new Role("Fresh Role", "saved without privileges");
		userService.saveRole(role);
		role.addPrivilege(restricted);

		assertThrows(APIException.class, Context::flushSession);
	}

	@Test
	public void saveRole_shouldNotWriteAPlainSetPutInPlaceOfALoadedRolesPrivileges() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		Privilege restricted = userService.getPrivilege("Restricted Privilege");
		Role role = userService.getRole("Revocable Role");

		Set<Privilege> replacement = new HashSet<>(role.getPrivileges());
		replacement.add(restricted);
		FieldUtils.writeField(role, "privileges", replacement, true);

		assertThrows(APIException.class, Context::flushSession);
	}

	@Test
	public void saveRole_shouldNotWriteAPrivilegeAddedToTheSetTheRoleWasSavedWith() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		Privilege restricted = userService.getPrivilege("Restricted Privilege");

		Role role = new Role("Fresh Role", "saved with an empty set");
		role.setPrivileges(new HashSet<>());
		Set<Privilege> original = role.getPrivileges();
		userService.saveRole(role);
		Context.flushSession();
		original.add(restricted);

		assertThrows(APIException.class, Context::flushSession);
	}

	@Test
	public void saveUser_shouldNotLetAUserManagerGiveARoleTheyJustCreatedAPrivilegeTheyLack() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());

		Role role = new Role("Fresh Role", "granted a privilege its creator lacks");
		role.addPrivilege(userService.getPrivilege("Restricted Privilege"));

		assertThrows(APIException.class, () -> userService.saveRole(role));
	}

	@Test
	public void saveUser_shouldLetAUserManagerAssignARoleCreatedEarlierInTheTransaction() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());

		Role role = new Role("Fresh Role", "created and assigned in one transaction");
		role.addPrivilege(userService.getPrivilege(PrivilegeConstants.GET_USERS));
		userService.saveRole(role);
		User other = userService.getUser(5505);
		other.addRole(role);
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			userService.saveUser(other);
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
		Context.flushSession();
		Context.clearSession();

		assertTrue(userService.getUser(5505).hasRole("Fresh Role"));
	}

	@Test
	public void saveRole_shouldRejectARoleInheritingItselfDirectly() {
		executeDataSet(XML_FILENAME);
		Role role = detachedRole("Some Role");
		role.getInheritedRoles().add(new Role("Some Role"));

		APIException exception = assertThrows(APIException.class, () -> userService.saveRole(role));
		assertThat(exception.getMessage(), is(messages.getMessage("Role.cannot.inherit.descendant")));
	}

	@Test
	public void saveRole_shouldNotLetALaterFlushWriteARejectedCycle() {
		executeDataSet(XML_FILENAME);
		// "Some Role" already inherits "Parent"
		Role parentRole = userService.getRole("Parent");
		parentRole.getInheritedRoles().add(userService.getRole("Some Role"));

		assertThrows(APIException.class, () -> userService.saveRole(parentRole));
		assertThrows(APIException.class, Context::flushSession);
	}

	@Test
	public void saveRole_shouldNotWriteAnInheritanceCycleMadeInMemory() {
		executeDataSet(XML_FILENAME);
		// "Some Role" already inherits "Parent"
		Role parentRole = userService.getRole("Parent");
		parentRole.getInheritedRoles().add(userService.getRole("Some Role"));

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), is(messages.getMessage("Role.cannot.inherit.descendant")));
	}

	@Test
	public void flush_shouldRefuseAFlushByCodeRunByTheRoleCheck() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		Role role = userService.getRole("Revocable Role");
		role.addPrivilege(userService.getPrivilege(PrivilegeConstants.GET_USERS));

		CheckTimePrivilegeListener.ACTION.set(Context::flushSession);
		try {
			Context.flushSession();
			assertThat(CheckTimePrivilegeListener.FAILURE.get(), notNullValue());
			assertThat(CheckTimePrivilegeListener.FAILURE.get().getMessage(), containsString("can't be flushed"));
		} finally {
			CheckTimePrivilegeListener.ACTION.remove();
			CheckTimePrivilegeListener.FAILURE.remove();
		}
	}

	@Test
	public void flush_shouldRefuseAnAutoFlushByCodeRunByTheRoleCheck() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		Role role = userService.getRole("Revocable Role");
		role.addPrivilege(userService.getPrivilege(PrivilegeConstants.GET_USERS));

		CheckTimePrivilegeListener.ACTION.set(() -> {
			Session session = sessionFactory.getCurrentSession();
			session.setHibernateFlushMode(FlushMode.AUTO);
			session.createQuery("from Role", Role.class).list();
		});
		try {
			Context.flushSession();
			assertThat(CheckTimePrivilegeListener.FAILURE.get(), notNullValue());
			assertThat(CheckTimePrivilegeListener.FAILURE.get().getMessage(), containsString("can't be flushed"));
		} finally {
			CheckTimePrivilegeListener.ACTION.remove();
			CheckTimePrivilegeListener.FAILURE.remove();
		}
	}

	@Test
	public void flush_shouldRejectRoleChangesMadeByCodeRunByTheRoleCheck() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		User other = userService.getUser(5505);
		Role superuser = userService.getRole(RoleConstants.SUPERUSER);
		// a change the user manager may make, so that the check runs
		other.addRole(userService.getRole("Revocable Role"));

		CheckTimePrivilegeListener.ACTION.set(() -> other.addRole(superuser));
		try {
			APIException exception = assertThrows(APIException.class, Context::flushSession);
			assertThat(exception.getMessage(), containsString("changed while role changes were being checked"));
		} finally {
			CheckTimePrivilegeListener.ACTION.remove();
			CheckTimePrivilegeListener.FAILURE.remove();
		}
	}

	@Test
	public void flush_shouldCheckRolesMovedFromAnotherUser() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		User other = (User) Hibernate.unproxy(userService.getUser(5505));
		User admin = (User) Hibernate.unproxy(userService.getUserByUsername(ADMIN_USERNAME));

		// the admin keeps the same roles in a plain set, while their loaded set is given to the other user
		Object adminRoles = FieldUtils.readField(admin, "roles", true);
		FieldUtils.writeField(admin, "roles", new HashSet<>(admin.getRoles()), true);
		FieldUtils.writeField(other, "roles", adminRoles, true);

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString("must have the role"));
	}

	@Test
	public void flush_shouldRejectARoleRenamedAfterBeingEvicted() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		User other = userService.getUser(5505);
		Role held = other.getRoles().iterator().next();
		Role superuser = userService.getRole(RoleConstants.SUPERUSER);

		// the snapshot of the user's roles holds this object, so renaming it would hide the addition below
		Context.evictFromSession(held);
		held.setRole(RoleConstants.SUPERUSER);
		other.addRole(superuser);

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString("loaded in the current session"));
	}

	@Test
	public void saveUser_shouldNotLetAUserManagerRemoveSystemDeveloperFromASuperuser() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.authenticate(getTestUserCredentials());

		admin.removeRole(userService.getRole(RoleConstants.SUPERUSER));
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			APIException exception = assertThrows(APIException.class, () -> userService.saveUser(admin));
			assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}

	@Test
	public void flush_shouldNotLetAUserManagerRemoveSystemDeveloperFromASuperuser() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.authenticate(getTestUserCredentials());

		admin.removeRole(userService.getRole(RoleConstants.SUPERUSER));

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
	}

	@Test
	public void saveRole_shouldNotLetAUserManagerChangeARoleThatInheritsSystemDeveloper() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		Role someRole = userService.getRole("Some Role");
		someRole.getInheritedRoles().add(userService.getRole(RoleConstants.SUPERUSER));
		userService.saveRole(someRole);
		Context.flushSession();
		Role detached = detachedRole("Some Role");
		Context.authenticate(getTestUserCredentials());

		detached.getInheritedRoles().removeIf(role -> RoleConstants.SUPERUSER.equals(role.getRole()));
		APIException exception = assertThrows(APIException.class, () -> userService.saveRole(detached));
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
		assertThat(storedParentRoleNames("Some Role"), hasItem(RoleConstants.SUPERUSER));
	}

	@Test
	public void flush_shouldNotLetAUserManagerChangeARoleThatInheritsSystemDeveloper() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		Role someRole = userService.getRole("Some Role");
		someRole.getInheritedRoles().add(userService.getRole(RoleConstants.SUPERUSER));
		userService.saveRole(someRole);
		Context.flushSession();
		Context.authenticate(getTestUserCredentials());

		someRole.getInheritedRoles().removeIf(role -> RoleConstants.SUPERUSER.equals(role.getRole()));

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
	}

	@Test
	public void saveRole_shouldNotSaveUnderANameTheDatabaseMatchesToAnotherRole() {
		executeDataSet(XML_FILENAME);
		Role role = new Role("some role", "differs from Some Role only in case");

		// the validator also rejects this name; the service must too, since validation can be turned off
		ValidateUtil.disableValidationForThread();
		try {
			assertThrows(APIException.class, () -> userService.saveRole(role));
		} finally {
			ValidateUtil.resumeValidationForThread();
		}
	}

	@Test
	public void saveUser_shouldRefreshTheAuthenticatedUsersRolesOnceTheirOwnChangeIsCommitted() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());

		User me = userService.getUser(5511);
		me.removeRole(userService.getRole("Revocable Role"));
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			userService.saveUser(me);
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
		Context.flushSession();
		assertTrue(Context.hasPrivilege("Revocable Privilege"));

		TransactionSynchronizationUtils.triggerAfterCommit();

		assertFalse(Context.hasPrivilege("Revocable Privilege"));
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void flush_shouldRejectAnEntityOtherThanARoleAmongAUsersRoles() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		// Hibernate writes any managed entity's id as the role, so this would make the user a superuser
		GlobalProperty namedLikeARole = Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(RoleConstants.SUPERUSER, "x"));
		Context.authenticate(getTestUserCredentials());

		((Set) userService.getUser(5511).getRoles()).add(namedLikeARole);

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString("may only hold roles"));
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void flush_shouldRejectAnEntityOtherThanARoleAmongARolesInheritedRoles() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		GlobalProperty namedLikeARole = Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(RoleConstants.SUPERUSER, "x"));
		Context.authenticate(getTestUserCredentials());

		((Set) userService.getRole("Some Role").getInheritedRoles()).add(namedLikeARole);

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString("may only hold roles"));
	}

	@Test
	public void flush_shouldRequireEditUsersToReplaceTheRolesOfAnExistingUserWithNone() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(LIMITED_ADMINS_XML);
		Context.authenticate(getTestUserCredentials());
		User existing = userService.getUser(5509);
		assertTrue(existing.getRoles().isEmpty());

		existing.setRoles(new HashSet<>(Collections.singleton(userService.getRole("Revocable Role"))));

		ContextAuthenticationException exception = assertThrows(ContextAuthenticationException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.EDIT_USERS));
	}

	@Test
	public void flush_shouldNotRestoreARevokedRoleThroughAReplacedRolesSet() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		sessionFactory.getCurrentSession()
		        .createNativeMutationQuery("insert into user_role (user_id, role) values (5505, :role)")
		        .setParameter("role", RoleConstants.SUPERUSER).executeUpdate();
		Context.authenticate(getTestUserCredentials());
		User target = userService.getUser(5505);
		assertTrue(target.hasRole(RoleConstants.SUPERUSER));

		// revoked by another transaction after the user was loaded
		sessionFactory.getCurrentSession()
		        .createNativeMutationQuery("delete from user_role where user_id = 5505 and role = :role")
		        .setParameter("role", RoleConstants.SUPERUSER).executeUpdate();
		FieldUtils.writeField(target, "roles", new HashSet<>(target.getRoles()), true);

		assertThrows(APIException.class, Context::flushSession);
	}

	@Test
	public void flush_shouldNotRestoreARevokedPrivilegeThroughAReplacedPrivilegesSet() throws Exception {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		Context.authenticate(getTestUserCredentials());
		Role role = userService.getRole("Restricted Role");
		assertFalse(role.getPrivileges().isEmpty());

		// revoked by another transaction after the role was loaded
		sessionFactory.getCurrentSession()
		        .createNativeMutationQuery("delete from role_privilege where role = 'Restricted Role'").executeUpdate();
		FieldUtils.writeField(role, "privileges", new HashSet<>(role.getPrivileges()), true);

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString("Restricted Privilege"));
	}

	@Test
	public void flush_shouldRequireEditUsersToChangeAnExistingUsersRolesAfterAQueryThatDoesNotFlush() {
		executeDataSet(XML_FILENAME);
		executeDataSet(ROLE_ASSIGNMENT_XML);
		executeDataSet(LIMITED_ADMINS_XML);
		Context.authenticate(getTestUserCredentials());
		User existing = userService.getUser(5509);
		assertTrue(existing.getRoles().isEmpty());

		existing.setRoles(new HashSet<>(existing.getRoles()));
		// Hibernate prepares a flush for this query, wrapping the new set, but then skips it
		userService.getRoleByUuid(userService.getRole("Revocable Role").getUuid());
		existing.getRoles().add(userService.getRole("Revocable Role"));

		ContextAuthenticationException exception = assertThrows(ContextAuthenticationException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.EDIT_USERS));
	}

	@Test
	public void createUser_shouldLetAUserWithAddUsersButNotEditUsersAssignRolesTheyCanHandOut() {
		executeDataSet(XML_FILENAME);
		executeDataSet(LIMITED_ADMINS_XML);
		Context.authenticate(getTestUserCredentials());
		assertFalse(Context.hasPrivilege(PrivilegeConstants.EDIT_USERS));
		User user = userWithValidPerson();
		user.setUsername("newbie");
		user.addRole(userService.getRole("User Adder"));

		// only so UserValidator can read its global properties
		Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		try {
			userService.createUser(user, "Passw0rdOk1");
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		assertEquals(Collections.singleton("User Adder"), storedRoleNames(user.getUserId()));
	}

	@Test
	public void saveUserProperties_shouldKeepStoredPropertiesWhenGivenTheLoadedUsersOwnMap() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		Context.authenticate(getTestUserCredentials());
		// as on a later request, where the authenticated user isn't the session's copy
		Context.evictFromSession(Context.getAuthenticatedUser());
		Map<String, String> properties = userService.getUser(5511).getUserProperties();

		properties.put("new key", "new value");
		userService.saveUserProperties(properties);
		Context.flushSession();
		Context.clearSession();

		Map<String, String> stored = dao.getUser(5511).getUserProperties();
		assertEquals("new value", stored.get("new key"));
		assertEquals("some value", stored.get("some key"));
	}

	@Test
	public void purgeUser_shouldNotLetAUserWithoutAssignSystemDeveloperRolePurgeASuperuser() {
		executeDataSet(XML_FILENAME);
		executeDataSet(LIMITED_ADMINS_XML);
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.authenticate(getTestUserCredentials());

		APIException exception = assertThrows(APIException.class, () -> userService.purgeUser(admin));
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
		assertThrows(APIException.class, () -> userService.purgeUser(admin, false));
	}

	@Test
	public void purgeUser_shouldCheckTheUserStoredUnderTheIdTheUserWasLoadedWith() {
		executeDataSet(XML_FILENAME);
		executeDataSet(LIMITED_ADMINS_XML);
		Context.authenticate(getTestUserCredentials());
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Integer adminId = admin.getUserId();

		admin.setUserId(Context.getAuthenticatedUserId());
		assertThrows(APIException.class, () -> userService.purgeUser(admin));
		assertThrows(APIException.class, () -> userService.purgeUser(admin, false));
		admin.setUserId(null);
		assertThrows(APIException.class, () -> userService.purgeUser(admin));

		admin.setUserId(adminId);
		assertNotNull(userService.getUser(adminId));
	}

	@Test
	public void flush_shouldNotLetAUserWithoutAssignSystemDeveloperRoleDeleteASuperuser() {
		executeDataSet(XML_FILENAME);
		executeDataSet(LIMITED_ADMINS_XML);
		// a superuser nothing else refers to, so only the guard stands in the way of deleting them
		User superuser = userService.getUser(5505);
		superuser.addRole(userService.getRole(RoleConstants.SUPERUSER));
		userService.saveUser(superuser);
		Context.flushSession();
		Context.clearSession();
		Context.authenticate(getTestUserCredentials());

		sessionFactory.getCurrentSession().remove(userService.getUser(5505));

		APIException exception = assertThrows(APIException.class, Context::flushSession);
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
	}

	@Test
	public void setUserActivationKey_shouldNotLetAUserManagerResetASuperusersPassword() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.authenticate(getTestUserCredentials());

		APIException exception = assertThrows(APIException.class, () -> userService.setUserActivationKey(admin));
		assertThat(exception.getMessage(), containsString(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE));
		assertNull(dao.getLoginCredential(admin).getActivationKey());
	}

	@Test
	public void setUserActivationKey_shouldLetAnAnonymousCallerResetASuperusersPassword() {
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_PASSWORD_RESET_URL,
		    "http://localhost:8080/openmrs/admin/users/changePassword.form/{activationKey}");
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.logout();

		// as the forgot-password flow does
		String[] privileges = { PrivilegeConstants.EDIT_USER_PASSWORDS, PrivilegeConstants.GET_GLOBAL_PROPERTIES,
		        PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES };
		for (String privilege : privileges) {
			Context.addProxyPrivilege(privilege);
		}
		try {
			// the mail can't be sent in tests, but the key is set first
			assertThrows(MessageException.class, () -> userService.setUserActivationKey(admin));
		} finally {
			for (String privilege : privileges) {
				Context.removeProxyPrivilege(privilege);
			}
		}
		assertNotNull(dao.getLoginCredential(admin).getActivationKey());
	}

	@Test
	public void changePassword_shouldLetAnAnonymousCallerResetASuperusersPassword() {
		User admin = userService.getUserByUsername(ADMIN_USERNAME);
		Context.logout();

		// as the forgot-password flow does once the secret answer is checked
		Context.addProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
		try {
			userService.changePassword(admin, "NewPassw0rd");
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
		}

		assertTrue(dao.getLoginCredential(admin).checkPassword("NewPassw0rd"));
	}

	@Test
	public void changePassword_shouldRecordTheAuthenticatedUserAsLoadedAsChangedBy() {
		executeDataSet(XML_FILENAME);
		executeDataSet(USER_MANAGER_XML);
		User other = userService.getUser(5505);
		Context.authenticate(getTestUserCredentials());
		Integer callerId = Context.getAuthenticatedUserId();
		Context.evictFromSession(Context.getAuthenticatedUser());
		Context.getAuthenticatedUser().setUserId(1);

		userService.changePassword(other, null, "NewPassw0rd");
		assertEquals(callerId, dao.getLoginCredential(other).getChangedBy().getUserId());

		userService.changeHashedPassword(other, "hashed", "salt");
		assertEquals(callerId, dao.getLoginCredential(other).getChangedBy().getUserId());
	}

	/**
	 * Runs an action the first time a privilege is checked on a thread that sets one, standing in for
	 * module code that runs while roles are being checked, and records why the action failed, since
	 * listener failures are only logged.
	 */
	@Component("userServiceTestCheckTimePrivilegeListener")
	public static class CheckTimePrivilegeListener implements PrivilegeListener {

		private static final ThreadLocal<Runnable> ACTION = new ThreadLocal<>();

		private static final ThreadLocal<RuntimeException> FAILURE = new ThreadLocal<>();

		@Override
		public void privilegeChecked(User user, String privilege, boolean hasPrivilege) {
			Runnable action = ACTION.get();
			if (action != null) {
				ACTION.remove();
				try {
					action.run();
				} catch (RuntimeException e) {
					FAILURE.set(e);
				}
			}
		}
	}

	/** Runs as a caller holding the service-level privileges only as proxies */
	private void withProxyPrivileges(Runnable runnable) {
		String[] privileges = { PrivilegeConstants.EDIT_USERS, PrivilegeConstants.GET_USERS, PrivilegeConstants.MANAGE_ROLES,
		        PrivilegeConstants.GET_ROLES, PrivilegeConstants.GET_PRIVILEGES, PrivilegeConstants.GET_GLOBAL_PROPERTIES };
		for (String privilege : privileges) {
			Context.addProxyPrivilege(privilege);
		}
		try {
			runnable.run();
		} finally {
			for (String privilege : privileges) {
				Context.removeProxyPrivilege(privilege);
			}
		}
	}

	private Role detachedRole(String name) {
		Role role = userService.getRole(name);
		Context.evictFromSession(role);
		return role;
	}

	private Set<String> storedRoleNames(Integer userId) {
		Context.flushSession();
		Context.clearSession();
		List<String> roles = sessionFactory.getCurrentSession()
		        .createNativeQuery("select role from user_role where user_id = :userId", String.class)
		        .setParameter("userId", userId).getResultList();
		return new HashSet<>(roles);
	}

	private Set<String> storedPrivilegeNames(String role) {
		Context.flushSession();
		Context.clearSession();
		List<String> privileges = sessionFactory.getCurrentSession()
		        .createNativeQuery("select privilege from role_privilege where role = :role", String.class)
		        .setParameter("role", role).getResultList();
		return new HashSet<>(privileges);
	}

	/** Reads role_role directly, since writes through one side of it don't refresh the other's cache */
	private Set<String> storedParentRoleNames(String name) {
		Context.flushSession();
		Context.clearSession();
		List<String> parents = sessionFactory.getCurrentSession()
		        .createNativeQuery("select parent_role from role_role where child_role = :child", String.class)
		        .setParameter("child", name).getResultList();
		return new HashSet<>(parents);
	}

}
