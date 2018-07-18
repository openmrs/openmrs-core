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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.openmrs.util.Security;

/**
 * TODO add more tests to cover the methods in <code>UserService</code>
 */
public class UserServiceTest extends BaseContextSensitiveTest {

	protected static final String XML_FILENAME = "org/openmrs/api/include/UserServiceTest.xml";
	
	protected static final String XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION = "org/openmrs/api/include/UserServiceTest-changePasswordAction.xml";

	protected static final String SOME_VALID_PASSWORD = "s0mePassword";

	public static final String SOME_USERNAME = "butch";

	private final String ADMIN_USERNAME = "admin";

	private UserService userService;

	private MessageSourceService messages;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		userService = Context.getUserService();
		messages = Context.getMessageSourceService();
	}

	/**
	 * Methods in this class might authenticate with a different user, so log that user out after
	 * this whole junit class is done.
	 */
	@AfterClass
	public static void logOutAfterThisTest() {
		Context.logout();
	}
	
	@Test
	public void createUser_shouldCreateNewUserWithBasicElements() {
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());

		User u = new User();
		u.setPerson(new Person());
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");
		
		User createdUser = userService.createUser(u, "Openmr5xy");
		
		// if we're returning the object from create methods, check validity
		assertTrue("The user returned by the create user method should equal the passed in user", createdUser.equals(u));
		
		createdUser = userService.getUserByUsername("bwolfe");
		assertTrue("The created user should equal the passed in user", createdUser.equals(u));
	}
	
	@Test
	@SkipBaseSetup
	public void createUser_shouldShouldCreateUserWhoIsPatientAlready() throws SQLException {
		// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		// authenticate to the temp database
		authenticate();
		
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
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
		Assert.assertNotNull("User was not created", userService.getUser(user.getUserId()));
		
		Integer shouldCreateUserWhoIsPatientAlreadyTestUserIdCreated = user.getUserId();
		
		Context.flushSession();
		
		// get the same user we just created and make sure the user portion exists
		User fetchedUser = userService.getUser(shouldCreateUserWhoIsPatientAlreadyTestUserIdCreated);
		User fetchedUser3 = userService.getUser(3);
		if (fetchedUser3 != null) {
			throw new RuntimeException("There is a user with id #3");
		}
		
		assertNotNull("Uh oh, the user object was not created", fetchedUser);
		assertNotNull("Uh oh, the username was not saved", fetchedUser.getUsername());
		assertTrue("Uh oh, the username was not saved", fetchedUser.getUsername().equals("bwolfe"));
		assertTrue("Uh oh, the role was not assigned", fetchedUser.hasRole("Some Role"));
		
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

		expectedException.expect(APIException.class);
		expectedException.expectMessage("This method can be used for only creating new users");
		userService.createUser(someUser, SOME_VALID_PASSWORD);
	}

	@Test
	public void createUser_shouldNotAllowBlankPassword() {
		User unsavedUser = new User();

		expectedException.expect(ValidationException.class);
		userService.createUser(unsavedUser, "");
	}

	@Test
	public void createUser_shouldNotAllowNullPassword() {
		User unsavedUser = new User();

		expectedException.expect(ValidationException.class);
		userService.createUser(unsavedUser, null);
	}

	@Test
	public void createUser_shouldNotAllowForDuplicatedUsername() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getUsername());

		expectedException.expect(DAOException.class);
		expectedException.expectMessage(
				String.format("Username %s or system id %s is already in use.",
						newUser.getUsername(),
						Context.getUserService().generateSystemId()));
		userService.createUser(newUser, SOME_VALID_PASSWORD);
	}

	@Test
	public void createUser_shouldNotAllowDuplicatedSystemId() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setSystemId(someUser.getSystemId());

		expectedException.expect(DAOException.class);
		expectedException.expectMessage(
				String.format("Username %s or system id %s is already in use.",
						newUser.getUsername(),
						newUser.getSystemId()));
		userService.createUser(newUser, SOME_VALID_PASSWORD);
	}

	@Test
	public void createUser_shouldNotAllowUsernameEqualsExistingSystemId() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getSystemId());

		expectedException.expect(DAOException.class);
		expectedException.expectMessage(
				String.format("Username %s or system id %s is already in use.",
						newUser.getUsername(),
						Context.getUserService().generateSystemId()));
		userService.createUser(newUser, SOME_VALID_PASSWORD);
	}

	@Test
	public void createUser_shouldNotAllowSystemIdEqualsExistingUsername() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setSystemId(someUser.getUsername());

		expectedException.expect(DAOException.class);
		expectedException.expectMessage(
				String.format("Username %s or system id %s is already in use.",
						newUser.getUsername(),
						newUser.getSystemId()));
		userService.createUser(newUser, SOME_VALID_PASSWORD);
	}

	@Test
	public void createUser_shouldNotAllowSystemIdEqualsUsernameWithLuhnCheckDigit() {
		User someUser = userService.getUserByUsername(SOME_USERNAME);

		User newUser = userWithValidPerson();
		newUser.setUsername(someUser.getUsername());
		newUser.setSystemId(decorateWithLuhnIdentifier(someUser.getUsername()));

		expectedException.expect(DAOException.class);
		expectedException.expectMessage(
				String.format("Username %s or system id %s is already in use.",
						newUser.getUsername(),
						newUser.getSystemId()));
		userService.createUser(newUser, SOME_VALID_PASSWORD);
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
		assertNotNull("There needs to be a user with username 'admin' in the database", u);
		
		u.setUsername("admin2");
		userService.saveUser(u);
		
		User u2 = userService.getUserByUsername("admin2");
		
		assertEquals("The fetched user should equal the user we tried to update", u, u2);
	}
	
	/**
	 * Test changing a user's password multiple times in the same transaction
	 * 
	 * @see UserService#changePassword(String,String)
	 */
	@Test
	public void changePassword_shouldBeAbleToUpdatePasswordMultipleTimes() {
		User u = userService.getUserByUsername(ADMIN_USERNAME);
		assertNotNull("There needs to be a user with username 'admin' in the database", u);
		
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

		assertNotNull("username not found " + ADMIN_USERNAME, user);
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
		Assert.assertEquals(3, users.size());
		Assert.assertTrue(containsId(users, 2));
		Assert.assertTrue(containsId(users, 4));
		Assert.assertTrue(containsId(users, 5));
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
		Assert.assertEquals("Some Privilege", privilege.getPrivilege());
	}

	/**
	 * @see UserService#getPrivilegeByUuid(String)
	 */
	@Test
	public void getPrivilegeByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(userService.getPrivilegeByUuid("some invalid uuid"));
	}

	/**
	 * @see UserService#getRoleByUuid(String)
	 */
	@Test
	public void getRoleByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "3480cb6d-c291-46c8-8d3a-96dc33d199fb";
		Role role = userService.getRoleByUuid(uuid);
		Assert.assertEquals("Provider", role.getRole());
	}

	/**
	 * @see UserService#getRoleByUuid(String)
	 */
	@Test
	public void getRoleByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(userService.getRoleByUuid("some invalid uuid"));
	}

	/**
	 * @see UserService#getUserByUuid(String)
	 */
	@Test
	public void getUserByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "c1d8f5c2-e131-11de-babe-001e378eb67e";
		User user = userService.getUserByUuid(uuid);
		Assert.assertEquals(501, (int) user.getUserId());
	}

	/**
	 * @see UserService#getUserByUuid(String)
	 */
	@Test
	public void getUserByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(userService.getUserByUuid("some invalid uuid"));
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
	@Ignore
	// TODO fix: the question not sticking - null expected:<[the question]> but was:<[]>
	public void changeQuestionAnswer_shouldChangeTheSecretQuestionAndAnswerForGivenUser() {
		User u = userService.getUser(501);
		userService.changeQuestionAnswer(u, "the question", "the answer");
		
		// need to retrieve the user since the service method does not modify the given user object
		User o = userService.getUser(501);
		Assert.assertTrue(userService.isSecretAnswer(o, "the answer"));
	}
	
	/**
	 * @see UserService#getAllPrivileges()
	 */
	@Test
	public void getAllPrivileges_shouldReturnAllPrivilegesInTheSystem() {
		executeDataSet(XML_FILENAME);
		List<Privilege> privileges = userService.getAllPrivileges();
		Assert.assertEquals(1, privileges.size());
	}
	
	/**
	 * @see UserService#getAllRoles()
	 */
	@Test
	public void getAllRoles_shouldReturnAllRolesInTheSystem() {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = userService.getAllRoles();
		Assert.assertEquals(7, roles.size());
	}
	
	/**
	 * @see UserService#getAllUsers()
	 */
	@Test
	public void getAllUsers_shouldFetchAllUsersInTheSystem() {
		List<User> users = userService.getAllUsers();
		Assert.assertEquals(3, users.size());
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
		assertEquals("Did not fetch user with given uuid", user, userService.getUser(5505));
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
		Assert.assertEquals("Some Privilege", privilege.getPrivilege());
	}
	
	/**
	 * @see UserService#getRole(String)
	 */
	@Test
	public void getRole_shouldFetchRoleForGivenRoleName() {
		executeDataSet(XML_FILENAME);
		Role role = userService.getRole("Some Role");
		Assert.assertEquals("Some Role", role.getRole());
	}
	
	/**
	 * @see UserService#getUser(Integer)
	 */
	@Test
	public void getUser_shouldFetchUserWithGivenUserId() {
		User user = userService.getUser(501);
		Assert.assertEquals(501, user.getUserId().intValue());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchUsersWithAtLeastOneOfTheGivenRoleObjects() {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = Collections.singletonList(new Role("Some Role"));
		Assert.assertEquals(1, userService.getUsers("Susy Kingman", roles, false).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchUsersWithNameThatContainsGivenNameSearch() {
		Assert.assertEquals(1, userService.getUsers("Hippocrates", null, false).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchUsersWithSystemIdThatContainsGivenNameSearch() {
		Assert.assertEquals(1, userService.getUsers("2-6", null, true).size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldFetchVoidedUsersIfIncludedVoidedIsTrue() {
		Assert.assertEquals(1, userService.getUsers("Bruno", null, true).size());
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
		Assert.assertEquals(1, users.size());
	}
	
	/**
	 * @see UserService#getUsers(String,List,boolean)
	 */
	@Test
	public void getUsers_shouldNotFetchVoidedUsersIfIncludedVoidedIsFalse() {
		Assert.assertEquals(0, userService.getUsers("Bruno", null, false).size());
	}
	
	/**
	 * @see UserService#getUsersByRole(Role)
	 */
	@Test
	public void getUsersByRole_shouldFetchUsersAssignedGivenRole() {
		executeDataSet(XML_FILENAME);
		
		Assert.assertEquals(2, userService.getUsersByRole(new Role("Some Role")).size());
	}
	
	/**
	 * @see UserService#getUsersByRole(Role)
	 */
	@Test
	public void getUsersByRole_shouldNotFetchUserThatDoesNotBelongToGivenRole() {
		executeDataSet(XML_FILENAME);
		
		Assert.assertEquals(0, userService.getUsersByRole(new Role("Nonexistent role")).size());
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
		Assert.assertTrue(userService.hasDuplicateUsername(user));
		
		user = new User();
		user.setSystemId("a unique system id");
		user.setUsername("userWithSha512Hash");
		Assert.assertTrue(userService.hasDuplicateUsername(user));
	}
	
	/**
	 * @see UserService#isSecretAnswer(User,String)
	 */
	@Test
	public void isSecretAnswer_shouldReturnFalseWhenGivenAnswerDoesNotMatchTheStoredSecretAnswer() {
		User user = userService.getUser(502);
		Assert.assertFalse(userService.isSecretAnswer(user, "not the answer"));
	}
	
	/**
	 * @see UserService#isSecretAnswer(User,String)
	 */
	@Test
	public void isSecretAnswer_shouldReturnTrueWhenGivenAnswerMatchesStoredSecretAnswer() {
		executeDataSet(XML_FILENAME);
		User user = userService.getUser(5507);
		userService.changeQuestionAnswer(user, "question", "answer");
		Assert.assertTrue(userService.isSecretAnswer(user, "answer"));
	}
	
	/**
	 * @see UserService#purgePrivilege(Privilege)
	 */
	@Test
	public void purgePrivilege_shouldDeleteGivenPrivilegeFromTheDatabase() {
		userService.purgePrivilege(new Privilege("Some Privilege"));
		Assert.assertNull(userService.getPrivilege("Some Privilege"));
	}
	
	/**
	 * @see UserService#purgePrivilege(Privilege)
	 */
	@Test
	public void purgePrivilege_shouldThrowErrorWhenPrivilegeIsCorePrivilege() {
		expectedException.expect(APIException.class);
		userService.purgePrivilege(new Privilege(PrivilegeConstants.ADD_COHORTS));
	}
	
	/**
	 * @see UserService#purgeRole(Role)
	 */
	@Test
	public void purgeRole_shouldDeleteGivenRoleFromDatabase() {
		executeDataSet(XML_FILENAME);
		Role role = userService.getRole("Some Role To Delete");
		userService.purgeRole(role);
		Assert.assertNull(userService.getRole("Some Role To Delete"));
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

		expectedException.expect(APIException.class);
		userService.purgeRole(role);
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

		expectedException.expect(CannotDeleteRoleWithChildrenException.class);
		userService.purgeRole(role2);
	}
	
	/**
	 * @see UserService#purgeUser(User)
	 */
	@Test
	public void purgeUser_shouldDeleteGivenUser() {
		User user = userService.getUser(502);
		userService.purgeUser(user);
		Assert.assertNull(userService.getUser(2));
	}
	
	/**
	 * @see UserService#purgeUser(User,boolean)
	 */
	@Test
	public void purgeUser_shouldDeleteGivenUserWhenCascadeEqualsFalse() {
		User user = userService.getUser(502);
		userService.purgeUser(user, false);
		Assert.assertNull(userService.getUser(502));
	}
	
	/**
	 * @see UserService#purgeUser(User,boolean)
	 */
	@Test
	public void purgeUser_shouldThrowAPIExceptionIfCascadeIsTrue() {
		User user = userService.getUser(502);

		expectedException.expect(APIException.class);
		userService.purgeUser(user, true);
	}
	
	/**
	 * @see UserService#removeUserProperty(User,String)
	 */
	@Test
	public void removeUserProperty_shouldRemoveUserPropertyForGivenUserAndKey() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		Assert.assertNotSame("", user.getUserProperty("some key"));
		
		userService.removeUserProperty(user, "some key");
		
		user = userService.getUser(5505);
		Assert.assertEquals("", user.getUserProperty("some key"));
	}
	
	/**
	 * @see UserService#removeUserProperty(User,String)
	 */
	@Test
	public void removeUserProperty_shouldReturnNullIfUserIsNull() {
		Assert.assertNull(userService.setUserProperty(null, "some key", "some new value"));
	}
	
	/**
	 * @see UserService#removeUserProperty(User,String)
	 */
	@Test
	public void removeUserProperty_shouldThrowErrorWhenUserIsNotAuthorizedToEditUsers() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		
		Context.logout();

		expectedException.expect(APIException.class);
		userService.removeUserProperty(user, "some key");
	}
	
	/**
	 * @see UserService#savePrivilege(Privilege)
	 */
	@Test
	public void savePrivilege_shouldSaveGivenPrivilegeToTheDatabase() {
		Privilege p = new Privilege("new privilege name", "new privilege desc");
		userService.savePrivilege(p);
		
		Privilege savedPrivilege = userService.getPrivilege("new privilege name");
		Assert.assertNotNull(savedPrivilege);
		
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldAddPropertyWithGivenKeyAndValueWhenKeyDoesNotAlreadyExist() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		
		// Check that it doesn't already exist
		Assert.assertEquals(user.getUserProperty("some new key"), "");
		
		userService.setUserProperty(user, "some new key", "some new value");
		
		user = userService.getUser(5505);
		Assert.assertEquals("some new value", user.getUserProperty("some new key"));
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldModifyPropertyWithGivenKeyAndValueWhenKeyAlreadyExists() {
		executeDataSet(XML_FILENAME);

		User user = userService.getUser(5505);
		
		// Check that it already exists
		Assert.assertEquals(user.getUserProperty("some key"), "some value");
		
		userService.setUserProperty(user, "some key", "some new value");
		
		user = userService.getUser(5505);
		Assert.assertEquals("some new value", user.getUserProperty("some key"));
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldReturnNullIfUserIsNull() {
		Assert.assertNull(userService.setUserProperty(null, "some key", "some value"));
	}
	
	/**
	 * @see UserService#setUserProperty(User,String,String)
	 */
	@Test
	public void setUserProperty_shouldThrowErrorWhenUserIsNotAuthorizedToEditUsers() {
		User user = userService.getUser(502);
		
		Context.logout();
		expectedException.expect(APIException.class);
		userService.setUserProperty(user, "some key", "some value");
	}
	
	/**
	 * @see UserService#saveRole(Role)
	 */
	@Test
	public void saveRole_shouldSaveGivenRoleToTheDatabase() {
		Role role = new Role("new role", "new desc");
		userService.saveRole(role);
		
		Assert.assertNotNull(userService.getRole("new role"));
		
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
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(messages.getMessage("Role.cannot.inherit.descendant"));
		userService.saveRole(parentRole);
	}
	
	/**
	 * @see UserService#getUsersByPerson(Person,null)
	 */
	@Test
	public void getUsersByPerson_shouldFetchAllAccountsForAPersonWhenIncludeRetiredIsTrue() {
		executeDataSet(XML_FILENAME);
		Person person = new Person(5508);
		List<User> users = userService.getUsersByPerson(person, true);
		Assert.assertEquals(3, users.size());
	}
	
	/**
	 * @see UserService#getUsersByPerson(Person,null)
	 */
	@Test
	public void getUsersByPerson_shouldNotFetchRetiredAccountsWhenIncludeRetiredIsFalse() {
		executeDataSet(XML_FILENAME);
		Person person = new Person(5508);
		List<User> users = userService.getUsersByPerson(person, false);
		Assert.assertEquals(2, users.size());
	}
	
	/**
	 * @see UserService#retireUser(User,String)
	 */
	@Test
	public void retireUser_shouldRetireUserAndSetAttributes() {
		User user = userService.getUser(502);
		userService.retireUser(user, "because");
		Assert.assertTrue(user.getRetired());
		Assert.assertNotNull(user.getDateRetired());
		Assert.assertNotNull(user.getRetiredBy());
		Assert.assertEquals("because", user.getRetireReason());
	}
	
	/**
	 * @see UserService#unretireUser(User)
	 */
	@Test
	public void unretireUser_shouldUnretireAndUnmarkAllAttributes() {
		User user = userService.getUser(501);
		userService.unretireUser(user);
		Assert.assertFalse(user.getRetired());
		Assert.assertNull(user.getDateRetired());
		Assert.assertNull(user.getRetiredBy());
		Assert.assertNull(user.getRetireReason());
	}
	
	@Test
	public void saveUser_shouldFailToCreateTheUserWithAWeakPassword() {
		assertTrue("The context needs to be correctly authenticated to by a user", Context.isAuthenticated());
		
		UserService us = userService;
		
		User u = new User();
		u.setPerson(new Person());
		
		u.addName(new PersonName("Benjamin", "A", "Wolfe"));
		u.setUsername("bwolfe");
		u.getPerson().setGender("M");

		expectedException.expect(PasswordException.class);
		us.createUser(u, "short");
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
		
		Assert.assertEquals(2, userService.getUsers("", roles, true).size());
	}
	
	/**
	 * @see UserService#getUsers(String, List, boolean, Integer, Integer)
	 */
	@Test
	public void getUsers_shouldReturnUsersWhoseRolesInheritRequestedRoles() {
		executeDataSet(XML_FILENAME);
		
		List<Role> roles = new ArrayList<>();
		roles.add(userService.getRole("Parent"));
		Assert.assertEquals(3, userService.getUsers(null, roles, true, null, null).size());
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
		
		expectedException.expect(APIAuthenticationException.class);
		expectedException.expectMessage(messages
				.getMessage("error.privilegesRequired", new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null));
		userService.changePassword(user6001, wrongPassword, newPassword);
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
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(messages
				.getMessage("error.privilegesRequired", new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null));
		userService.changePassword(user6001, oldPassword, newPassword);
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
		
		expectedException.expectMessage(
				messages.getMessage("error.password.length", new Object[] {"8"}, null));
		userService.changePassword(user6001, oldPassword, weakPassword);
	}
	
	/**
	 * @see UserService#changePassword(User,String,String)
	 */
	@Test
	public void changePassword_shouldThrowAPIExceptionIfGivenUserDoesNotExist() {
		//user.getUserId is null - so it is not existing user
		User notExistingUser = new User();
		String anyString = "anyString";
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(messages.getMessage("user.must.exist"));
		userService.changePassword(notExistingUser, anyString, anyString);
	}

    @Test
	public void changePassword_shouldThrowShortPasswordExceptionWithShortPassword() {
        expectedException.expect(ShortPasswordException.class);
        expectedException.expectMessage(
		        messages.getMessage("error.password.length", new Object[] {"8"}, null));

        userService.changePassword("test", "");
    }
    
	@Test
	public void changePassword_shouldUpdatePasswordOfGivenUserWhenLoggedInUserHasEditUsersPasswordPrivilege() {
		User user = userService.getUserByUsername(ADMIN_USERNAME);
		assertNotNull("There needs to be a user with username 'admin' in the database", user);
		
		userService.changePassword(user, "testTest123");
		
		Context.authenticate(user.getUsername(), "testTest123");
	}
	
	@Test
	public void changePassword_shouldNotUpdatePasswordOfGivenUserWhenLoggedInUserDoesNotHaveEditUsersPasswordPrivilege() {
		executeDataSet(XML_FILENAME_WITH_DATA_FOR_CHANGE_PASSWORD_ACTION);
		User user = userService.getUser(6001);
		assertFalse(user.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS));
		Context.authenticate(user.getUsername(), "userServiceTest");
		
		expectedException.expect(APIAuthenticationException.class);
		expectedException.expectMessage(
				messages.getMessage("error.privilegesRequired",
						new Object[] {PrivilegeConstants.EDIT_USER_PASSWORDS}, null));
		
		userService.changePassword(user, "testTest123");
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
		
		expectedException.expect(APIException.class);
		expectedException.expectMessage(messages.getMessage("secret.answer.not.correct"));
		
		userService.changePasswordUsingSecretAnswer("wrong answer", "userServiceTest2");
	}
}