package org.openmrs.api;

import java.util.List;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserService {

	/**
	 * Create a new user
	 * @param user
	 * @param password
	 * @returns newly created User object
	 * @throws APIException
	 */
	@Authorized({"Add Users"})
	public User createUser(User user, String password) throws APIException;

	/**
	 * Get user by internal user identifier
	 * @param userId internal identifier
	 * @return requested user
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public User getUser(Integer userId) throws APIException;

	/**
	 * Get user by username (user's login identifier)
	 * @param username user's identifier used for authentication
	 * @return requested user
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public User getUserByUsername(String username) throws APIException;

	/**
	 * true/false if username or systemId is already in db in username or system_id columns
	 * @param User to compare
	 * @return boolean
	 * @throws APIException
	 */
	@Authorized({"View Users"})
	public boolean hasDuplicateUsername(User user) throws APIException;

	/**
	 * Get users by role granted
	 * @param Role role that the Users must have to be returned 
	 * @return users with requested role
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public List<User> getUsersByRole(Role role) throws APIException;

	/**
	 * Save changes to user
	 * @param user
	 * @throws APIException
	 */
	@Authorized({"Edit Users"})
	public void updateUser(User user) throws APIException;

	/**
	 * Grant roles for user
	 * @param user
	 * @param role
	 * @throws APIException
	 */
	@Authorized({"Edit Users"})
	public void grantUserRole(User user, Role role) throws APIException;

	/**
	 * Revoke roles from user
	 * @param user
	 * @param role
	 * @throws APIException
	 */
	@Authorized({"Edit Users"})
	public void revokeUserRole(User user, Role role) throws APIException;

	/** 
	 * Mark user as voided (effectively deleting user without removing
	 * their data &mdash; since anything the user touched in the database
	 * will still have their internal identifier and point to the voided
	 * user for historical tracking purposes.
	 * 
	 * @param user
	 * @param reason
	 * @throws APIException
	 */
	@Authorized({"Edit Users"})
	public void voidUser(User user, String reason) throws APIException;

	/**
	 * Clear voided flag for user (equivalent to an "undelete" or
	 * Lazarus Effect for user)
	 * 
	 * @param user
	 * @throws APIException
	 */
	@Authorized({"Edit Users"})
	public void unvoidUser(User user) throws APIException;

	/**
	 * Delete user from database. This is included for troubleshooting and
	 * low-level system administration. Ideally, this method should <b>never</b>
	 * be called &mdash; <code>Users</code> should be <em>voided</em> and
	 * not <em>deleted</em> altogether (since many foreign key constraints
	 * depend on users, deleting a user would require deleting all traces, and
	 * any historical trail would be lost).
	 * 
	 * This method only clears user roles and attempts to delete the user
	 * record. If the user has been included in any other parts of the database
	 * (through a foreign key), the attempt to delete the user will violate
	 * foreign key constraints and fail.
	 * 
	 * @param user
	 * @throws APIException
	 * @see #voidUser(User, String)
	 */
	@Authorized({"Delete Users"})
	public void deleteUser(User user) throws APIException;

	/**
	 * Returns all privileges currently possible for any User
	 * @return Global list of privileges
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Privilege> getPrivileges() throws APIException;

	/**
	 * Returns all roles currently possible for any User
	 * @return Global list of roles
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Role> getRoles() throws APIException;

	/**
	 * Returns roles that inherit from this role
	 * @return inheriting roles
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public List<Role> getInheritingRoles(Role role) throws APIException;

	/**
	 * Returns all users in the system
	 * @return Global list of users
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public List<User> getUsers() throws APIException;

	/**
	 * Returns role object with given string role
	 * @return Role
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Role getRole(String r) throws APIException;

	/**
	 * Returns Privilege in the system with given String privilege
	 * @return Privilege
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public Privilege getPrivilege(String p) throws APIException;

	/**
	 * Changes the <code>user<code>'s password
	 * ** Restricted to Super User access**
	 * @param u user
	 * @param pw2 new password
	 * @throws APIException
	 */
	@Authorized({"Edit Users"})
	public void changePassword(User u, String pw) throws APIException;

	/**
	 * Changes the current user's password
	 * @param pw current password
	 * @param pw2 new password
	 * @throws APIException
	 */
	public void changePassword(String pw, String pw2) throws APIException;

	/**
	 * Changes the current user's secret question and answer
	 * @param pw user's password
	 * @param question
	 * @param answer
	 * @throws APIException
	 */
	public void changeQuestionAnswer(String pw, String q, String a);

	/**
	 * Compares <code>answer</code> against the <code>user</code>'s secret answer
	 * @param user
	 * @param answer
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public boolean isSecretAnswer(User u, String answer);

	/**
	 * Return a user if any part of the search matches first/last/system id and the user
	 * has one of the roles supplied
	 * @param name
	 * @param roles
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided);

	/**
	 * Find a user by exact first name and last name
	 * @param givenName
	 * @param familyName
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public List<User> findUsers(String givenName, String familyName, boolean includeVoided);
	
	/**
	 * Get users that have any role in <code>roles</code> granted
	 * @param Role role that the Users must have to be returned 
	 * @return users with requested role
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({"View Users"})
	public List<User> getAllUsers(List<Role> roles, boolean includeVoided);

	/**
	 * Adds the <code>key</code>/<code>value</code> pair to the 
	 * given <code>user</code> 
	 * 
	 * <b>Implementations of this method should handle privileges</b>
	 * 
	 * @param user
	 * @param key
	 * @param value
	 */
	public void setUserProperty(User user, String key, String value);
	
	/**
	 * Removes the property denoted by <code>key</code> from the 
	 * <code>user</code>'s properties 
	 * 
	 * <b>Implementations of this method should handle privileges</b>
	 * 
	 * @param user
	 * @param key
	 */
	public void removeUserProperty(User user, String key);
	
	
	/**
	 * Get/generate/find the next system id to be doled out.  Assume check digit /not/ applied
	 * in this method
	 * @return new system id
	 */
	String generateSystemId();

}