package org.openmrs.api;

import java.util.List;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;

/**
 * User-related services
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public interface UserService {
	
	/**
	 * Create a new user
	 * @param user
	 * @throws APIException
	 */
	public void createUser(User user) throws APIException;

	/**
	 * Get user by internal user identifier
	 * @param userId internal identifier
	 * @return requested user
	 * @throws APIException
	 */
	public User getUser(Integer userId) throws APIException;
	
	/**
	 * Get user by username (user's login identifier)
	 * @param username user's identifier used for authentication
	 * @return requested user
	 * @throws APIException
	 */
	public User getUserByUsername(String username) throws APIException;

	/**
	 * Get users by role granted
	 * @param Role role that the Users must have to be returned 
	 * @return users with requested role
	 * @throws APIException
	 */
	public List<User> getUsersByRole(Role role) throws APIException;
	
	/**
	 * Save changes to user
	 * @param user
	 * @throws APIException
	 */
	public void updateUser(User user) throws APIException;
	
	/**
	 * Grant roles for user
	 * @param user
	 * @param role
	 * @throws APIException
	 */
	public void grantUserRole(User user, Role role) throws APIException;
	
	/**
	 * Revoke roles from user
	 * @param user
	 * @param role
	 * @throws APIException
	 */
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
	public void voidUser(User user, String reason) throws APIException;
	
	/**
	 * Clear voided flag for user (equivalent to an "undelete" or
	 * Lazarus Effect for user)
	 * 
	 * @param user
	 * @throws APIException
	 */
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
	public void deleteUser(User user) throws APIException;
	
	/**
	 * Returns all privileges currently possible for any User
	 * @return Global list of privileges
	 * @throws APIException
	 */
	public List<Privilege> getPrivileges() throws APIException;
	
	/**
	 * Returns all roles currently possible for any User
	 * @return Global list of roles
	 * @throws APIException
	 */
	public List<Role> getRoles() throws APIException;

	/**
	 * Returns all users in the system
	 * @return Global list of users
	 * @throws APIException
	 */
	public List<User> getUsers() throws APIException;

}
