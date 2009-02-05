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
package org.openmrs.api;

import java.util.List;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contains methods pertaining to Users in the system Use:<br/>
 * 
 * <pre>
 *   List<User> users = Context.getUserService().getAllUsers();
 * </pre>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface UserService extends OpenmrsService {
	
	/**
	 * Saves a user to the database.
	 * 
	 * @param user
	 * @param password
	 * @returns a User object
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_USERS, OpenmrsConstants.PRIV_EDIT_USERS })
	@Logging(ignoredArgumentIndexes = { 1 })
	public User saveUser(User user, String password) throws APIException;
	
	/**
	 * @see {@link #saveUser(User, String)}
	 * @deprecated replaced by {@link #saveUser(User, String)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_USERS })
	@Logging(ignoredArgumentIndexes = { 1 })
	public User createUser(User user, String password) throws APIException;
	
	/**
	 * Get user by internal user identifier.
	 * 
	 * @param userId internal identifier
	 * @return requested user
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public User getUser(Integer userId) throws APIException;
	
	/**
	 * Get user by username (user's login identifier)
	 * 
	 * @param username user's identifier used for authentication
	 * @return requested user
	 * @throws APIException
	 * @should get user by username
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public User getUserByUsername(String username) throws APIException;
	
	/**
	 * true/false if username or systemId is already in db in username or system_id columns
	 * 
	 * @param User to compare
	 * @return boolean
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public boolean hasDuplicateUsername(User user) throws APIException;
	
	/**
	 * Get users by role granted
	 * 
	 * @param Role role that the Users must have to be returned
	 * @return users with requested role
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> getUsersByRole(Role role) throws APIException;
	
	/**
	 * Save changes to given <code>user</code> to the database.
	 * 
	 * @param user
	 * @throws APIException
	 * @see {@link #saveUser(User, String)}
	 * @deprecated replaced by {@link #saveUser(User, String)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USERS })
	public void updateUser(User user) throws APIException;
	
	/**
	 * Use should be UserService.saveUser(user.addRole(role))
	 * 
	 * @deprecated use {@link org.openmrs.User#addRole(Role)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USERS })
	public void grantUserRole(User user, Role role) throws APIException;
	
	/**
	 * Use UserService.saveUser(user.removeRole(role))
	 * 
	 * @deprecated use {@link org.openmrs.User#removeRole(Role)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USERS })
	public void revokeUserRole(User user, Role role) throws APIException;
	
	/**
	 * Mark user as voided (effectively deleting user without removing their data &mdash; since
	 * anything the user touched in the database will still have their internal identifier and point
	 * to the voided user for historical tracking purposes.
	 * 
	 * @param user
	 * @param reason
	 * @return the given user voided out
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USERS })
	public User voidUser(User user, String reason) throws APIException;
	
	/**
	 * Clear voided flag for user (equivalent to an "undelete" or Lazarus Effect for user)
	 * 
	 * @param user
	 * @return the given user unvoided
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USERS })
	public User unvoidUser(User user) throws APIException;
	
	/**
	 * @see #voidUser(User, String)
	 * @see #purgeUser(User)
	 * @deprecated use {@link #purgeUser(User)}
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_USERS })
	public void deleteUser(User user) throws APIException;
	
	/**
	 * Completely remove a location from the database (not reversible). This method delegates to
	 * #purgeLocation(location, boolean) method.
	 * 
	 * @param user the User to remove from the database.
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_USERS })
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
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_USERS })
	public void purgeUser(User user, boolean cascade) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllPrivileges()}
	 */
	@Transactional(readOnly = true)
	public List<Privilege> getPrivileges() throws APIException;
	
	/**
	 * Returns all privileges currently possible for any User
	 * 
	 * @return Global list of privileges
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Privilege> getAllPrivileges() throws APIException;
	
	/**
	 * @deprecated use {@link #getAllRoles()}
	 */
	@Transactional(readOnly = true)
	public List<Role> getRoles() throws APIException;
	
	/**
	 * Returns all roles currently possible for any User
	 * 
	 * @return Global list of roles
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Role> getAllRoles() throws APIException;
	
	/**
	 * @deprecated use {@link org.openmrs.Role#getInheritedRoles()}
	 */
	@Transactional(readOnly = true)
	public List<Role> getInheritingRoles(Role role) throws APIException;
	
	/**
	 * Save the given role in the database
	 * 
	 * @param Role to update
	 * @return the saved role
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_ROLES })
	public Role saveRole(Role role) throws APIException;
	
	/**
	 * Complete remove a role from the database
	 * 
	 * @param Role to delete from the database
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_ROLES })
	public void purgeRole(Role role) throws APIException;
	
	/**
	 * Save the given privilege in the database
	 * 
	 * @param Privilege to update
	 * @return the saved privilege
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_MANAGE_PRIVILEGES })
	public Privilege savePrivilege(Privilege privilege) throws APIException;
	
	/**
	 * Complete remove a privilege from the database
	 * 
	 * @param Privilege to delete
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_PURGE_PRIVILEGES })
	public void purgePrivilege(Privilege privilege) throws APIException;
	
	/**
	 * Returns role object with given string role
	 * 
	 * @return Role
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public Role getRole(String r) throws APIException;
	
	/**
	 * Returns Privilege in the system with given String privilege
	 * 
	 * @return Privilege
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public Privilege getPrivilege(String p) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllUsers()}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> getUsers() throws APIException;
	
	/**
	 * Returns all users in the system
	 * 
	 * @return Global list of users
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> getAllUsers() throws APIException;
	
	/**
	 * Changes the <code>user<code>'s password
	 * ** Restricted to Super User access**
	 * 
	 * @param u user
	 * @param pw2 new password
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USER_PASSWORDS })
	@Logging(ignoredArgumentIndexes = { 1 })
	public void changePassword(User u, String pw) throws APIException;
	
	/**
	 * Changes the current user's password.
	 * 
	 * @param pw current password
	 * @param pw2 new password
	 * @throws APIException
	 * @should matchOnCorrectlyHashedStoredPassword
	 * @should matchOnIncorrectlyHashedStoredPassword
	 */
	@Logging(ignoredArgumentIndexes = { 0, 1 })
	public void changePassword(String pw, String pw2) throws APIException;
	
	/**
	 * Changes the current user's password directly. This is most useful if migrating users from
	 * other systems and you want to retain the existing passwords. This method will simply save the
	 * passed hashed password and salt directly to the database.
	 * 
	 * @param user the user whose password you want to change
	 * @param hashedPassword - the <em>already hashed</em> password to store
	 * @param salt - the salt which should be used with this hashed password
	 * @throws APIException
	 * @should successfullySaveToTheDatabase
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_USER_PASSWORDS })
	public void changeHashedPassword(User user, String hashedPassword, String salt) throws APIException;
	
	/**
	 * Changes the current user's secret question and answer.
	 * 
	 * @param pw user's password
	 * @param question
	 * @param answer
	 * @throws APIException
	 * @should matchOnCorrectlyHashedStoredPassword
	 * @should matchOnIncorrectlyHashedStoredPassword
	 */
	@Logging(ignoreAllArgumentValues = true)
	public void changeQuestionAnswer(String pw, String q, String a) throws APIException;
	
	/**
	 * Compares <code>answer</code> against the <code>user</code>'s secret answer.
	 * 
	 * @param user
	 * @param answer
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Logging(ignoredArgumentIndexes = { 1 })
	public boolean isSecretAnswer(User u, String answer) throws APIException;
	
	/**
	 * Return a user if any part of the search matches first/last/system id and the user has one at
	 * least one of the given <code>roles</code> assigned to them
	 * 
	 * @param nameSearch
	 * @param roles
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> getUsers(String nameSearch, List<Role> roles, boolean includeVoided) throws APIException;
	
	/**
	 * @deprecated use {@link #getUsers(String, List, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided) throws APIException;
	
	/**
	 * @deprecated use {@link #getUsersByName(String, String, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> findUsers(String givenName, String familyName, boolean includeVoided) throws APIException;
	
	/**
	 * Search for a list of users by exact first name and last name.
	 * 
	 * @param givenName
	 * @param familyName
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> getUsersByName(String givenName, String familyName, boolean includeVoided) throws APIException;
	
	/**
	 * @deprecated use {@link #getUsers(String, List, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_USERS })
	public List<User> getAllUsers(List<Role> roles, boolean includeVoided) throws APIException;
	
	/**
	 * Adds the <code>key</code>/<code>value</code> pair to the given <code>user</code>.
	 * <b>Implementations of this method should handle privileges</b>
	 * 
	 * @param user
	 * @param key
	 * @param value
	 * @return the user that was passed in and added to
	 */
	public User setUserProperty(User user, String key, String value) throws APIException;
	
	/**
	 * Removes the property denoted by <code>key</code> from the <code>user</code>'s properties.
	 * <b>Implementations of this method should handle privileges</b>
	 * 
	 * @param user
	 * @param key
	 * @return the user that was passed in and removed from
	 */
	public User removeUserProperty(User user, String key) throws APIException;
	
	/**
	 * Get/generate/find the next system id to be doled out. Assume check digit /not/ applied in
	 * this method
	 * 
	 * @return new system id
	 */
	public String generateSystemId() throws APIException;
	
}
