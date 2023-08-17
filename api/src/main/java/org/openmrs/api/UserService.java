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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.util.PersonByNameComparator;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.notification.MessageException;

/**
 * Contains methods pertaining to Users in the system Use:<br>
 * 
 * <pre>
 * 
 * 
 * List&lt;User&gt; users = Context.getUserService().getAllUsers();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface UserService extends OpenmrsService {

	public static final String ADMIN_PASSWORD_LOCKED_PROPERTY = "admin_password_locked";
	
	/**
	 * Create user with given password.
	 *
	 * @param user the user to create
	 * @param password the password for created user
	 * @return created user
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.ADD_USERS })
	@Logging(ignoredArgumentIndexes = { 1 })
	public User createUser(User user, String password) throws APIException;
	
	/**
	 * Change user password.
	 *
	 * @param user the user to update password
	 * @param oldPassword the user password  to update
	 * @param newPassword the new user password
	 * @throws APIException for not existing user and if old password is weak
	 * @since 1.12
	 * <strong>Should</strong> throw APIException if old password is not correct
	 * <strong>Should</strong> throw APIException if new password is the same as old passoword
	 * <strong>Should</strong> throw APIException if given user does not exist
	 * <strong>Should</strong> change password for given user if oldPassword is correctly passed
	 * <strong>Should</strong> change password for given user if oldPassword is null and changing user have privileges
	 * <strong>Should</strong> throw exception if oldPassword is null and changing user have not privileges
	 * <strong>Should</strong> throw exception if new password is too short
	 */
	@Authorized( { PrivilegeConstants.EDIT_USER_PASSWORDS })
	@Logging(ignoredArgumentIndexes = { 1, 2 })
	public void changePassword(User user, String oldPassword, String newPassword) throws APIException;
	
	/**
	 * Get user by internal user identifier.
	 * 
	 * @param userId internal identifier
	 * @return requested user
	 * @throws APIException
	 * <strong>Should</strong> fetch user with given userId
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public User getUser(Integer userId) throws APIException;
	
	/**
	 * Get user by the given uuid.
	 * 
	 * @param uuid
	 * @return user or null
	 * @throws APIException
	 * <strong>Should</strong> fetch user with given uuid
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public User getUserByUuid(String uuid) throws APIException;
	
	/**
	 * Get user by username (user's login identifier)
	 * 
	 * @param username User's identifier used for authentication
	 * @return requested user
	 * <strong>Should</strong> get user by username
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public User getUserByUsername(String username);
	
	/**
	 * Gets a user by username or email
	 * 
	 * @param usernameOrEmail User's email address or username
	 * @return requested user or null if not found
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public User getUserByUsernameOrEmail(String usernameOrEmail);
	
	/**
	 * Gets a user with the specified activation key
	 * @param activationKey User's activation key for password reset 
	 * @return requested User with associated  activation key
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public User getUserByActivationKey(String activationKey);
	
	/**
	 * true/false if username or systemId is already in db in username or system_id columns
	 * 
	 * @param user User to compare
	 * @return boolean
	 * @throws APIException
	 * <strong>Should</strong> verify that username and system id is unique
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public boolean hasDuplicateUsername(User user) throws APIException;
	
	/**
	 * Get users by role granted
	 * 
	 * @param role Role that the Users must have to be returned
	 * @return users with requested role
	 * @throws APIException
	 * <strong>Should</strong> fetch users assigned given role
	 * <strong>Should</strong> not fetch user that does not belong to given role
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public List<User> getUsersByRole(Role role) throws APIException;
	
	/**
	 * Updates a given <code>user</code> in the database.
	 * 
	 * @param user
	 * @return the saved user
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.EDIT_USERS })
	public User saveUser(User user) throws APIException;
	
	/**
	 * Deactivate a user account so that it can no longer log in.
	 * 
	 * @param user
	 * @param reason
	 * @throws APIException
	 * <strong>Should</strong> retire user and set attributes
	 */
	@Authorized( { PrivilegeConstants.EDIT_USERS })
	public User retireUser(User user, String reason) throws APIException;
	
	/**
	 * Clears retired flag for a user.
	 * 
	 * @param user
	 * @throws APIException
	 * <strong>Should</strong> unretire and unmark all attributes
	 */
	@Authorized( { PrivilegeConstants.EDIT_USERS })
	public User unretireUser(User user) throws APIException;
	
	/**
	 * Completely remove a location from the database (not reversible). This method delegates to
	 * #purgeLocation(location, boolean) method.
	 * 
	 * @param user the User to remove from the database.
	 * <strong>Should</strong> delete given user
	 */
	@Authorized( { PrivilegeConstants.PURGE_USERS })
	public void purgeUser(User user) throws APIException;
	
	/**
	 * Completely remove a user from the database (not reversible). This is a delete from the
	 * database. This is included for troubleshooting and low-level system administration. Ideally,
	 * this method should <b>never</b> be called &mdash; <code>Users</code> should be
	 * <em>voided</em> and not <em>deleted</em> altogether (since many foreign key constraints
	 * depend on users, deleting a user would require deleting all traces, and any historical trail
	 * would be lost). This method only clears user roles and attempts to delete the user record. If
	 * the user has been included in any other parts of the database (through a foreign key), the
	 * attempt to delete the user will violate foreign key constraints and fail.
	 * 
	 * @param cascade <code>true</code> to delete associated content
	 * <strong>Should</strong> throw APIException if cascade is true
	 * <strong>Should</strong> delete given user when cascade equals false
	 * <strong>Should</strong> not delete user roles for given user when cascade equals false
	 */
	@Authorized( { PrivilegeConstants.PURGE_USERS })
	public void purgeUser(User user, boolean cascade) throws APIException;
	
	/**
	 * Returns all privileges currently possible for any User
	 * 
	 * @return Global list of privileges
	 * @throws APIException
	 * <strong>Should</strong> return all privileges in the system
	 */
	public List<Privilege> getAllPrivileges() throws APIException;
	
	/**
	 * Returns all roles currently possible for any User
	 * 
	 * @return Global list of roles
	 * @throws APIException
	 * <strong>Should</strong> return all roles in the system
	 */
	public List<Role> getAllRoles() throws APIException;
	
	/**
	 * Save the given role in the database
	 * 
	 * @param role Role to update
	 * @return the saved role
	 * @throws APIException
	 * <strong>Should</strong> throw error if role inherits from itself
	 * <strong>Should</strong> save given role to the database
	 */
	@Authorized( { PrivilegeConstants.MANAGE_ROLES })
	public Role saveRole(Role role) throws APIException;
	
	/**
	 * Complete remove a role from the database
	 * 
	 * @param role Role to delete from the database
	 * @throws APIException
	 * <strong>Should</strong> throw error when role is a core role
	 * <strong>Should</strong> return if role is null
	 * <strong>Should</strong> delete given role from database
	 */
	@Authorized( { PrivilegeConstants.PURGE_ROLES })
	public void purgeRole(Role role) throws APIException;
	
	/**
	 * Save the given privilege in the database
	 * 
	 * @param privilege Privilege to update
	 * @return the saved privilege
	 * @throws APIException
	 * <strong>Should</strong> save given privilege to the database
	 */
	@Authorized( { PrivilegeConstants.MANAGE_PRIVILEGES })
	public Privilege savePrivilege(Privilege privilege) throws APIException;
	
	/**
	 * Completely remove a privilege from the database
	 * 
	 * @param privilege Privilege to delete
	 * @throws APIException
	 * <strong>Should</strong> delete given privilege from the database
	 * <strong>Should</strong> throw error when privilege is core privilege
	 */
	@Authorized( { PrivilegeConstants.PURGE_PRIVILEGES })
	public void purgePrivilege(Privilege privilege) throws APIException;
	
	/**
	 * Returns role object with given string role
	 * 
	 * @return Role object for specified string
	 * @throws APIException
	 * <strong>Should</strong> fetch role for given role name
	 */
	public Role getRole(String r) throws APIException;
	
	/**
	 * Get Role by its UUID
	 * 
	 * @param uuid
	 * @return role or null
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 */
	public Role getRoleByUuid(String uuid) throws APIException;
	
	/**
	 * Returns Privilege in the system with given String privilege
	 * 
	 * @return Privilege
	 * @throws APIException
	 * <strong>Should</strong> fetch privilege for given name
	 */
	public Privilege getPrivilege(String p) throws APIException;
	
	/**
	 * Get Privilege by its UUID
	 * 
	 * @param uuid
	 * @return privilege or null
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 * <strong>Should</strong> fetch privilege for given uuid
	 */
	public Privilege getPrivilegeByUuid(String uuid) throws APIException;
	
	/**
	 * Returns all users in the system
	 * 
	 * @return Global list of users
	 * @throws APIException
	 * <strong>Should</strong> fetch all users in the system
	 * <strong>Should</strong> not contains any duplicate users
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public List<User> getAllUsers() throws APIException;
	
	/**
	 * Changes the current user's password.
	 * 
	 * @param pw current password
	 * @param pw2 new password
	 * @throws APIException
	 * <strong>Should</strong> match on correctly hashed sha1 stored password
	 * <strong>Should</strong> match on incorrectly hashed sha1 stored password
	 * <strong>Should</strong> match on sha512 hashed password
	 * <strong>Should</strong> be able to update password multiple times
	 * <strong>Should</strong> respect locking via runtime properties
	 */
	@Logging(ignoredArgumentIndexes = { 0, 1 })
	public void changePassword(String pw, String pw2) throws APIException;

	/**
	 * Changes password of {@link User} passed in
	 * @param user user whose password is to be changed
	 * @param newPassword new password to set
	 * @throws APIException
	 * <strong>Should</strong> update password of given user when logged in user has edit users password privilege
	 * <strong>Should</strong> not update password of given user when logged in user does not have edit users password privilege
	 */
	@Authorized({PrivilegeConstants.EDIT_USER_PASSWORDS})
	public void changePassword(User user, String newPassword) throws APIException;
	
	/**
	 * Changes the current user's password directly. This is most useful if migrating users from
	 * other systems and you want to retain the existing passwords. This method will simply save the
	 * passed hashed password and salt directly to the database.
	 * 
	 * @param user the user whose password you want to change
	 * @param hashedPassword - the <em>already hashed</em> password to store
	 * @param salt - the salt which should be used with this hashed password
	 * @throws APIException
	 * @since 1.5
	 * <strong>Should</strong> change the hashed password for the given user
	 */
	@Authorized( { PrivilegeConstants.EDIT_USER_PASSWORDS })
	public void changeHashedPassword(User user, String hashedPassword, String salt) throws APIException;
	
	/**
	 * Changes the passed user's secret question and answer.
	 * 
	 * @param u User to change
	 * @param question
	 * @param answer
	 * @throws APIException
	 * @since 1.5
	 * <strong>Should</strong> change the secret question and answer for given user
	 */
	@Authorized( { PrivilegeConstants.EDIT_USER_PASSWORDS })
	@Logging(ignoredArgumentIndexes = { 1, 2 })
	public void changeQuestionAnswer(User u, String question, String answer) throws APIException;
	
	/**
	 * Changes the current user's secret question and answer.
	 * 
	 * @param pw user's password
	 * @param q question
	 * @param a answer
	 * @throws APIException
	 * <strong>Should</strong> match on correctly hashed stored password
	 * <strong>Should</strong> match on incorrectly hashed stored password
	 */
	@Logging(ignoreAllArgumentValues = true)
	public void changeQuestionAnswer(String pw, String q, String a) throws APIException;
	
	/**
	 * Returns secret question for the given user.
	 * 
	 * @param user
	 * @return
	 * @throws APIException
	 * @since 2.0
	 */
	public String getSecretQuestion(User user) throws APIException;
	
	/**
	 * Compares <code>answer</code> against the <code>user</code>'s secret answer.
	 * 
	 * @param u user
	 * @param answer
	 * @throws APIException
	 * <strong>Should</strong> return true when given answer matches stored secret answer
	 * <strong>Should</strong> return false when given answer does not match the stored secret answer
	 */
	@Logging(ignoredArgumentIndexes = { 1 })
	public boolean isSecretAnswer(User u, String answer) throws APIException;
	
	/**
	 * Return a list of users sorted by personName (see {@link PersonByNameComparator}) if any part
	 * of the search matches first/last/system id and the user has one at least one of the given
	 * <code>roles</code> assigned to them
	 * 
	 * @param nameSearch string to compare to the beginning of user's given/middle/family/family2
	 *            names
	 * @param roles all the Roles the user must contain
	 * @param includeVoided true/false whether to include voided users
	 * @return list of users matching the given attributes
	 * <strong>Should</strong> match search to familyName2
	 * <strong>Should</strong> fetch voided users if includedVoided is true
	 * <strong>Should</strong> not fetch voided users if includedVoided is false
	 * <strong>Should</strong> fetch users with name that contains given nameSearch
	 * <strong>Should</strong> fetch users with systemId that contains given nameSearch
	 * <strong>Should</strong> fetch users with at least one of the given role objects
	 * <strong>Should</strong> not fetch duplicate users
	 * <strong>Should</strong> fetch all users if nameSearch is empty or null
	 * <strong>Should</strong> not fail if roles are searched but name is empty
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public List<User> getUsers(String nameSearch, List<Role> roles, boolean includeVoided) throws APIException;
	
	/**
	 * Search for a list of users by exact first name and last name.
	 * 
	 * @param givenName
	 * @param familyName
	 * @param includeRetired
	 * @return List&lt;User&gt; object of users matching criteria
	 * <strong>Should</strong> fetch users exactly matching the given givenName and familyName
	 * <strong>Should</strong> fetch voided users whenincludeVoided is true
	 * <strong>Should</strong> not fetch any voided users when includeVoided is false
	 * <strong>Should</strong> not fetch any duplicate users
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public List<User> getUsersByName(String givenName, String familyName, boolean includeRetired) throws APIException;
	
	/**
	 * Get all user accounts that belong to a given person.
	 * 
	 * @param person
	 * @param includeRetired
	 * @return all user accounts that belong to person, including retired ones if specified
	 * @throws APIException
	 * <strong>Should</strong> fetch all accounts for a person when include retired is true
	 * <strong>Should</strong> not fetch retired accounts when include retired is false
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public List<User> getUsersByPerson(Person person, boolean includeRetired) throws APIException;
	
	/**
	 * Adds the <code>key</code>/<code>value</code> pair to the given <code>user</code>.
	 * <p>
	 * <b>Implementations of this method should handle privileges</b>
	 * 
	 * @param user
	 * @param key
	 * @param value
	 * @return the user that was passed in and added to
	 * <strong>Should</strong> return null if user is null
	 * <strong>Should</strong> throw error when user is not authorized to edit users
	 * <strong>Should</strong> add property with given key and value when key does not already exist
	 * <strong>Should</strong> modify property with given key and value when key already exists
	 */
	public User setUserProperty(User user, String key, String value) throws APIException;
	
	/**
	 * Removes the property denoted by <code>key</code> from the <code>user</code>'s properties.
	 * <b>Implementations of this method should handle privileges</b>
	 * 
	 * @param user
	 * @param key
	 * @return the user that was passed in and removed from
	 * <strong>Should</strong> return null if user is null
	 * <strong>Should</strong> throw error when user is not authorized to edit users
	 * <strong>Should</strong> remove user property for given user and key
	 */
	public User removeUserProperty(User user, String key) throws APIException;
	
	/**
	 * Get/generate/find the next system id to be doled out. Assume check digit /not/ applied in
	 * this method
	 * 
	 * @return new system id
	 */
	public String generateSystemId();
	
	/**
	 * Return a batch of users of a specific size sorted by personName (see
	 * {@link PersonByNameComparator}) if any part of the search matches first/last/system id and
	 * the user has one at least one of the given <code>roles</code> assigned to them. If start and
	 * length are not specified, then all matches are returned, If name is empty or null, then all
	 * all users will be returned taking into consideration the values of start and length
	 * arguments.
	 * 
	 * @param name string to compare to the beginning of user's given/middle/family/family2 names
	 * @param roles all the Roles the user must contain
	 * @param includeRetired true/false whether to include voided users
	 * @param start beginning index for the batch
	 * @param length number of users to return in the batch
	 * @return list of matching users of a size based on the specified arguments
	 * @since 1.8
	 * <strong>Should</strong> return users whose roles inherit requested roles
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public List<User> getUsers(String name, List<Role> roles, boolean includeRetired, Integer start, Integer length)
	        throws APIException;
	
	/**
	 * Return the number of users with a matching name or system id and have at least one of the
	 * given roles assigned to them
	 * 
	 * @param name patient name
	 * @param roles all the Roles the user must contain
	 * @param includeRetired Specifies whether voided users should be included
	 * @return the number of users matching the given attributes
	 * @since 1.8
	 */
	@Authorized( { PrivilegeConstants.GET_USERS })
	public Integer getCountOfUsers(String name, List<Role> roles, boolean includeRetired);
	
	/**
	 * Saves the current key/value as a user property for the current user.
	 * 
	 * @param key the authenticated user's property
	 * @param value value of the property
	 * @since 1.10
	 */
	@Authorized
	public User saveUserProperty(String key, String value);
	
	/**
	 * Replaces all user properties with the given map of properties for the current user
	 * 
	 * @param properties the authenticated user's properties
	 * @since 1.10
	 */
	@Authorized
	public User saveUserProperties(Map<String, String> properties);
	
	/**
	 * Change user password given the answer to the secret question
	 * @param secretAnswer the answer to secret question
	 * @param pw the new password
	 * <strong>Should</strong> update password if secret is correct
	 * <strong>Should</strong> not update password if secret is not correct
	 */
	@Authorized
	public void changePasswordUsingSecretAnswer(String secretAnswer, String pw) throws APIException;
	
	/**
	 * Sets a user's activation key
	 * @param user The user for which the activation key will be set
	 */
	public User setUserActivationKey(User user) throws MessageException;
	
	/**
	 * Change user password given the activation key
	 * 
	 * @param activationKey the activation for password reset
	 * @param newPassword the new password
	 */
	public void changePasswordUsingActivationKey(String activationKey, String newPassword);

	/**
	 * @param user the User whose Locale to retrieve
	 * @return the default Locale of the given user, or the system locale if unspecified
	 * @since 2.3.6, 2.4.6, 2.5.4, 2.6.0
	 */
	Locale getDefaultLocaleForUser(User user);
}
