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
package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;

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
	public User saveUser(User user, String password) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUser(Integer)
	 */
	public User getUser(Integer userId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUserByUsername(java.lang.String)
	 */
	public User getUserByUsername(String username) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#hasDuplicateUsername(org.openmrs.User)
	 */
	public boolean hasDuplicateUsername(String username, String systemId, Integer userId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#purgeUser(org.openmrs.User)
	 */
	public void deleteUser(User user) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers()
	 */
	public List<User> getAllUsers() throws DAOException;
	
	// Role stuff
	
	/**
	 * @see org.openmrs.api.UserService#saveRole(org.openmrs.Role)
	 */
	public Role saveRole(Role role) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#purgeRole(org.openmrs.Role)
	 */
	public void deleteRole(Role role) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getRole(java.lang.String)
	 */
	public Role getRole(String r) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getAllRoles()
	 */
	public List<Role> getAllRoles() throws DAOException;
	
	// Privilege stuff
	
	/**
	 * @see org.openmrs.api.UserService#savePrivilege(org.openmrs.Privilege)
	 */
	public Privilege savePrivilege(Privilege privilege) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilege(java.lang.String)
	 */
	public Privilege getPrivilege(String p) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getAllPrivileges()
	 */
	public List<Privilege> getAllPrivileges() throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#purgePrivilege(org.openmrs.Privilege)
	 */
	public void deletePrivilege(Privilege privilege) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUsers(java.lang.String, java.util.List, boolean)
	 */
	public List<User> getUsers(String nameSearch, List<Role> roles, boolean includeVoided) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByName(java.lang.String, java.lang.String, boolean)
	 */
	public List<User> getUsersByName(String givenName, String familyName, boolean includeVoided);
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(org.openmrs.User, java.lang.String)
	 */
	public void changePassword(User u, String pw) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	public void changePassword(String pw, String pw2) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void changeQuestionAnswer(String pw, String q, String a) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#isSecretAnswer(org.openmrs.User, java.lang.String)
	 */
	public boolean isSecretAnswer(User u, String answer) throws DAOException;
	
	/**
	 * @see org.openmrs.api.UserService#generateSystemId()
	 */
	public Integer generateSystemId() throws DAOException;
	
}
