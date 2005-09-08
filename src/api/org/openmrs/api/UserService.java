package org.openmrs.api;

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
	 * @return reference to newly created user
	 * @throws APIException
	 */
	public User createUser(User user) throws APIException;

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
	 */
	public void deleteUser(User user) throws APIException;
	
}
