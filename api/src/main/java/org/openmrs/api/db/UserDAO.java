/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;

/**
 * Database methods for the UserService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.UserService
 */
public interface UserDAO {
	
	/**
	 * @see org.openmrs.api.UserService#saveUser(org.openmrs.User, java.lang.String)
	 */
	User saveUser(User user, String password) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUser(Integer)
	 */
	User getUser(Integer userId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUserByUsername(java.lang.String)
	 */
	User getUserByUsername(String username) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#hasDuplicateUsername(org.openmrs.User)
	 */
	boolean hasDuplicateUsername(String username, String systemId, Integer userId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#purgeUser(org.openmrs.User)
	 */
	void deleteUser(User user) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers()
	 */
	List<User> getAllUsers() throws DAOException;
	
	// Role stuff
	
	/**
	 * @see org.openmrs.api.UserService#saveRole(org.openmrs.Role)
	 */
	Role saveRole(Role role) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#purgeRole(org.openmrs.Role)
	 */
	void deleteRole(Role role) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getRole(java.lang.String)
	 */
	Role getRole(String r) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getAllRoles()
	 */
	List<Role> getAllRoles() throws DAOException;
	
	// Privilege stuff
	
	/**
	 * @see org.openmrs.api.UserService#savePrivilege(org.openmrs.Privilege)
	 */
	Privilege savePrivilege(Privilege privilege) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilege(java.lang.String)
	 */
	Privilege getPrivilege(String p) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getAllPrivileges()
	 */
	List<Privilege> getAllPrivileges() throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#purgePrivilege(org.openmrs.Privilege)
	 */
	void deletePrivilege(Privilege privilege) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByName(java.lang.String, java.lang.String, boolean)
	 */
	List<User> getUsersByName(String givenName, String familyName, boolean includeVoided);
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(org.openmrs.User, java.lang.String)
	 */
	void changePassword(User u, String pw) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	void changePassword(String pw, String pw2) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#changeHashedPassword(User, String, String)
	 */
	void changeHashedPassword(User user, String hashedPassword, String salt) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(User, String, String)
	 */
	void changeQuestionAnswer(User u, String question, String answer) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	void changeQuestionAnswer(String pw, String q, String a) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#isSecretAnswer(org.openmrs.User, java.lang.String)
	 */
	boolean isSecretAnswer(User u, String answer) throws DAOException;
	
	/**
	 * @param uuid
	 * @return privilege or null
	 */
	Privilege getPrivilegeByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return role or null
	 */
	Role getRoleByUuid(String uuid);
	
	/**
	 * @param uuid
	 * @return user or null
	 */
	User getUserByUuid(String uuid);
	
	/**
	 * @param user
	 * @return The login credentials for a specified user.
	 */
	LoginCredential getLoginCredential(User user);
	
	/**
	 * @param uuid
	 * @return login credential or null
	 */
	LoginCredential getLoginCredentialByUuid(String uuid);
	
	/**
	 * Updates a user's login credentials. Note that there is no
	 * createLoginCredential(LoginCredential) method. Login credentials are dependent on a User
	 * already existing.
	 * 
	 * @param credential
	 */
	void updateLoginCredential(LoginCredential credential);
	
	/**
	 * @see org.openmrs.api.UserService#generateSystemId()
	 */
	Integer generateSystemId() throws DAOException;
	
	/**
	 * @see UserService#getUsersByPerson(Person, boolean)
	 */
	List<User> getUsersByPerson(Person person, boolean includeRetired);
	
	/**
	 * @see UserService#getUsers(String, List, boolean, Integer, Integer)
	 */
	List<User> getUsers(String name, List<Role> roles, boolean includeRetired, Integer start, Integer length)
	        throws DAOException;
	
	/**
	 * @see UserService#getCountOfUsers(String, List, boolean)
	 */
	Integer getCountOfUsers(String name, List<Role> roles, boolean includeRetired);
	
}
