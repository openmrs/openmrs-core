package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;

/**
 * User-related database functions
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public interface UserDAO {
	
	/**
	 * Create a new user
	 * @param user
	 * @param password
	 * @throws DAOException
	 */
	public void createUser(User user, String password) throws DAOException;

	/**
	 * Get user by internal user identifier
	 * @param userId internal identifier
	 * @return requested user
	 * @throws DAOException
	 */
	public User getUser(Integer userId) throws DAOException;
	
	/**
	 * Get user by username (user's login identifier)
	 * @param username user's identifier used for authentication
	 * @return requested user
	 * @throws DAOException
	 */
	public User getUserByUsername(String username) throws DAOException;

	/**
	 * true/false if username or systemId is already in db in username or system_id columns
	 * @param User to compare
	 * @return boolean
	 * @throws DAOException
	 */
	public boolean hasDuplicateUsername(User user) throws DAOException;
	
	/**
	 * Get users by role granted
	 * @param Role role that the Users must have to be returned 
	 * @return users with requested role
	 * @throws DAOException
	 */
	public List<User> getUsersByRole(Role role) throws DAOException;
	
	/**
	 * Save changes to user
	 * @param user
	 * @throws DAOException
	 */
	public void updateUser(User user) throws DAOException;
	
	/**
	 * Grant roles for user
	 * @param user
	 * @param role
	 * @throws DAOException
	 */
	public void grantUserRole(User user, Role role) throws DAOException;
	
	/**
	 * Revoke roles from user
	 * @param user
	 * @param role
	 * @throws DAOException
	 */
	public void revokeUserRole(User user, Role role) throws DAOException;

	/** 
	 * Mark user as voided (effectively deleting user without removing
	 * their data &mdash; since anything the user touched in the database
	 * will still have their internal identifier and point to the voided
	 * user for historical tracking purposes.
	 * 
	 * @param user
	 * @param reason
	 * @throws DAOException
	 */
	public void voidUser(User user, String reason) throws DAOException;
	
	/**
	 * Clear voided flag for user (equivalent to an "undelete" or
	 * Lazarus Effect for user)
	 * 
	 * @param user
	 * @throws DAOException
	 */
	public void unvoidUser(User user) throws DAOException;
	
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
	 * @throws DAOException
	 * @see #voidUser(User, String)
	 */
	public void deleteUser(User user) throws DAOException;
	
	/**
	 * Returns all privileges currently possible for any User
	 * @return Global list of privileges
	 * @throws DAOException
	 */
	public List<Privilege> getPrivileges() throws DAOException;
	
	/**
	 * Returns all roles currently possible for any User
	 * @return Global list of roles
	 * @throws DAOException
	 */
	public List<Role> getRoles() throws DAOException;

	/**
	 * Returns roles that inherit from this role
	 * @return inheriting roles
	 * @throws APIException
	 */
	public List<Role> getInheritingRoles(Role role) throws APIException;
	
	/**
	 * Returns all users in the system
	 * @return Global list of users
	 * @throws DAOException
	 */
	public List<User> getUsers() throws DAOException;

	/**
	 * Returns role object with given string role
	 * @return Role
	 * @throws DAOException
	 */
	public Role getRole(String r) throws DAOException;

	/**
	 * Returns Privilege in the system with given String privilege
	 * @return Privilege
	 * @throws DAOException
	 */
	public Privilege getPrivilege(String p) throws DAOException;
	
	/**
	 * Resets the password for the given user
	 * @param User to change
	 * @param New Password
	 * @throws DAOException
	 */
	public void changePassword(User u, String pw) throws DAOException;
	
	/**
	 * Resets the current users password
	 * @param pw
	 * @param pw2
	 * @throws DAOException
	 */
	public void changePassword(String pw, String pw2) throws DAOException;
	
	public void changeQuestionAnswer(String pw, String q, String a) throws DAOException;
	
	public boolean isSecretAnswer(User u, String answer) throws DAOException;
	
	public List<User> findUsers(String name, List<String> roles, boolean includeRetired) throws DAOException;
	
	public List<User> getAllUsers(List<String> roles, boolean includeRetired) throws DAOException;
	
	/**
	 * Get/generate/find the next system id to be doled out.  Assume check digit /not/ applied
	 * in this method
	 * @return new system id
	 */
	public String generateSystemId() throws DAOException;
}
