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
package org.openmrs.api.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.util.OpenmrsConstants;

/**
 * Default implementation of the user service. This class should not be used on its own. The current
 * OpenMRS implementation should be fetched from the Context
 * 
 * @see org.openmrs.api.UserService
 * @see org.openmrs.api.context.Context
 */
public class UserServiceImpl extends BaseOpenmrsService implements UserService {
	
	private static Log log = LogFactory.getLog(UserServiceImpl.class);
	
	protected UserDAO dao;
	
	public UserServiceImpl() {
	}
	
	public void setUserDAO(UserDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.UserService#createUser(org.openmrs.User, java.lang.String)
	 * @deprecated
	 */
	public User createUser(User user, String password) throws APIException {
		return saveUser(user, password);
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveUser(org.openmrs.User, java.lang.String)
	 */
	public User saveUser(User user, String password) throws APIException {
		if (user.getUserId() == null) {
			Context.requirePrivilege(OpenmrsConstants.PRIV_ADD_USERS);
		} else {
			Context.requirePrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
		}
		
		checkPrivileges(user);
		setCollectionProperties(user);
		
		// if we're creating a user and a password wasn't supplied, throw an error
		if (user.getUserId() == null && (password == null || password.length() < 1))
			throw new APIException("A password is required when creating a user");
		
		// if the user doesn't have a system id, generate one
		if (user.getSystemId() == null || user.getSystemId().equals(""))
			user.setSystemId(generateSystemId());
		
		if (hasDuplicateUsername(user))
			throw new DAOException("Username " + user.getUsername() + " or system id " + user.getSystemId()
			        + " is already in use.");
		
		// TODO Check required fields for user!!
		
		Date now = new Date();
		if (user.getDateCreated() == null) {
			user.setDateCreated(now);
		}
		if (user.getCreator() == null) {
			user.setCreator(Context.getAuthenticatedUser());
		}
		if (user.getUserId() != null) {
			user.setChangedBy(Context.getAuthenticatedUser());
			user.setDateChanged(now);
		}
		
		return dao.saveUser(user, password);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUser(java.lang.Integer)
	 */
	public User getUser(Integer userId) throws APIException {
		return dao.getUser(userId);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUserByUsername(java.lang.String)
	 */
	public User getUserByUsername(String username) throws APIException {
		return dao.getUserByUsername(username);
	}
	
	/**
	 * @see org.openmrs.api.UserService#hasDuplicateUsername(org.openmrs.User)
	 */
	public boolean hasDuplicateUsername(User user) throws APIException {
		return dao.hasDuplicateUsername(user.getUsername(), user.getSystemId(), user.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByRole(org.openmrs.Role)
	 */
	public List<User> getUsersByRole(Role role) throws APIException {
		List<Role> roles = new Vector<Role>();
		roles.add(role);
		
		return getUsers(null, roles, false);
	}
	
	/**
	 * @deprecated replaced by {@link #saveUser(User, String)}
	 * @see org.openmrs.api.UserService#updateUser(org.openmrs.User)
	 */
	public void updateUser(User user) throws APIException {
		saveUser(user, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#grantUserRole(org.openmrs.User, org.openmrs.Role)
	 * @deprecated
	 */
	public void grantUserRole(User user, Role role) throws APIException {
		saveUser(user.addRole(role), null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#revokeUserRole(org.openmrs.User, org.openmrs.Role)
	 * @deprecated
	 */
	public void revokeUserRole(User user, Role role) throws APIException {
		saveUser(user.removeRole(role), null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#voidUser(org.openmrs.User, java.lang.String)
	 */
	public User voidUser(User user, String reason) throws APIException {
		user.setVoided(true);
		user.setVoidReason(reason);
		user.setVoidedBy(Context.getAuthenticatedUser());
		user.setDateVoided(new Date());
		
		return saveUser(user, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#unvoidUser(org.openmrs.User)
	 */
	public User unvoidUser(User user) throws APIException {
		user.setVoided(false);
		user.setVoidReason(null);
		user.setVoidedBy(null);
		user.setDateVoided(null);
		
		return saveUser(user, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#deleteUser(org.openmrs.User)
	 * @deprecated
	 */
	public void deleteUser(User user) throws APIException {
		purgeUser(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsers()
	 * @deprecated
	 */
	public List<User> getUsers() throws APIException {
		return getAllUsers();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers()
	 */
	public List<User> getAllUsers() throws APIException {
		return dao.getAllUsers();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivileges()
	 * @deprecated
	 */
	public List<Privilege> getPrivileges() throws APIException {
		return getAllPrivileges();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllPrivileges()
	 */
	public List<Privilege> getAllPrivileges() throws APIException {
		return dao.getAllPrivileges();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilege(java.lang.String)
	 */
	public Privilege getPrivilege(String p) throws APIException {
		return dao.getPrivilege(p);
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgePrivilege(org.openmrs.Privilege)
	 */
	public void purgePrivilege(Privilege privilege) throws APIException {
		if (OpenmrsConstants.CORE_PRIVILEGES().keySet().contains(privilege.getPrivilege()))
			throw new APIException("Cannot delete a core privilege");
		
		dao.deletePrivilege(privilege);
	}
	
	/**
	 * @see org.openmrs.api.UserService#savePrivilege(org.openmrs.Privilege)
	 */
	public Privilege savePrivilege(Privilege privilege) throws APIException {
		return dao.savePrivilege(privilege);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getRoles()
	 * @deprecated
	 */
	public List<Role> getRoles() throws APIException {
		return getAllRoles();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllRoles()
	 */
	public List<Role> getAllRoles() throws APIException {
		return dao.getAllRoles();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getInheritingRoles(org.openmrs.Role)
	 * @deprecated
	 */
	public List<Role> getInheritingRoles(Role role) throws APIException {
		
		List<Role> roles = new Vector<Role>();
		roles.addAll(role.getInheritedRoles());
		
		return roles;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getRole(java.lang.String)
	 */
	public Role getRole(String r) throws APIException {
		return dao.getRole(r);
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeRole(org.openmrs.Role)
	 */
	public void purgeRole(Role role) throws APIException {
		if (role == null || role.getRole() == null)
			return;
		
		if (OpenmrsConstants.CORE_ROLES().keySet().contains(role.getRole()))
			throw new APIException("Cannot delete a core role");
		
		dao.deleteRole(role);
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveRole(org.openmrs.Role)
	 */
	public Role saveRole(Role role) throws APIException {
		
		// make sure one of the parents of this role isn't itself...this would
		// cause an infinite loop
		if (role.getAllParentRoles().contains(role))
			throw new APIAuthenticationException("Invalid Role or parent Role.  A role cannot inherit itself.");
		
		checkPrivileges(role);
		
		return dao.saveRole(role);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(org.openmrs.User, java.lang.String)
	 */
	public void changePassword(User u, String pw) throws APIException {
		dao.changePassword(u, pw);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(java.lang.String, java.lang.String)
	 */
	public void changePassword(String pw, String pw2) throws APIException {
		dao.changePassword(pw, pw2);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void changeQuestionAnswer(String pw, String q, String a) {
		dao.changeQuestionAnswer(pw, q, a);
	}
	
	/**
	 * @see org.openmrs.api.UserService#isSecretAnswer(org.openmrs.User, java.lang.String)
	 */
	public boolean isSecretAnswer(User u, String answer) {
		return dao.isSecretAnswer(u, answer);
	}
	
	/**
	 * @see org.openmrs.api.UserService#findUsers(String, List, boolean)
	 * @deprecated
	 */
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided) {
		
		List<Role> rolesToSearch = new Vector<Role>();
		
		for (String role : roles) {
			rolesToSearch.add(new Role(role));
		}
		
		return getUsers(name, rolesToSearch, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#findUsers(String, String, boolean)
	 * @deprecated
	 */
	public List<User> findUsers(String givenName, String familyName, boolean includeVoided) {
		return getUsersByName(givenName, familyName, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByName(java.lang.String, java.lang.String, boolean)
	 */
	public List<User> getUsersByName(String givenName, String familyName, boolean includeVoided) throws APIException {
		return dao.getUsersByName(givenName, familyName, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers(List, boolean)
	 * @deprecated
	 */
	public List<User> getAllUsers(List<Role> roles, boolean includeVoided) {
		return getUsers(null, roles, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsers(java.lang.String, java.util.List, boolean)
	 */
	public List<User> getUsers(String nameSearch, List<Role> roles, boolean includeVoided) throws APIException {
		
		if (nameSearch != null)
			nameSearch = nameSearch.replace(", ", " ");
		
		if (roles == null)
			roles = new Vector<Role>();
		
		// if the authenticated role is in the list of searched roles, then all
		// persons should be searched
		Role auth_role = getRole(OpenmrsConstants.AUTHENTICATED_ROLE);
		if (roles.contains(auth_role))
			return dao.getUsers(nameSearch, new Vector<Role>(), includeVoided);
		else
			return dao.getUsers(nameSearch, roles, includeVoided);
	}
	
	/**
	 * Convenience method to check if the authenticated user has all privileges they are giving out
	 * 
	 * @param new user that has privileges
	 */
	private void checkPrivileges(User user) {
		Collection<Role> roles = user.getAllRoles();
		User authUser = Context.getAuthenticatedUser();
		
		List<String> requiredPrivs = new Vector<String>();
		
		for (Role r : roles) {
			if (r.getRole().equals(OpenmrsConstants.SUPERUSER_ROLE) && !authUser.hasRole(OpenmrsConstants.SUPERUSER_ROLE))
				throw new APIException("You must have the role '" + OpenmrsConstants.SUPERUSER_ROLE
				        + "' in order to assign it.");
			if (r.getPrivileges() != null) {
				for (Privilege p : r.getPrivileges())
					if (!authUser.hasPrivilege(p.getPrivilege()))
						requiredPrivs.add(p.getPrivilege());
			}
		}
		
		if (requiredPrivs.size() == 1) {
			throw new APIException("You must have privilege '" + requiredPrivs.get(0) + "' in order to assign it.");
		} else if (requiredPrivs.size() > 1) {
			String txt = "You must have the following privileges in order to assign them: ";
			for (String s : requiredPrivs)
				txt += s + ", ";
			txt = txt.substring(0, txt.length() - 2);
			throw new APIException(txt);
		}
	}
	
	/**
	 * @see org.openmrs.api.UserService#addUserProperty(org.openmrs.User, java.lang.String,
	 *      java.lang.String)
	 */
	public User setUserProperty(User user, String key, String value) {
		if (user != null) {
			if (!user.hasPrivilege(OpenmrsConstants.PRIV_EDIT_USERS) && !user.equals(Context.getAuthenticatedUser()))
				throw new APIException("You are not authorized to change " + user.getUserId() + "'s properties");
			
			user.setUserProperty(key, value);
			try {
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
				saveUser(user, null);
			}
			finally {
				Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
			}
		}
		
		return user;
	}
	
	/**
	 * @see org.openmrs.api.UserService#removeUserProperty(org.openmrs.User, java.lang.String)
	 */
	public User removeUserProperty(User user, String key) {
		if (user != null) {
			
			// if the current user isn't allowed to edit users and
			// the user being edited is not the current user, throw an exceiption
			if (!Context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_USERS) && !user.equals(Context.getAuthenticatedUser()))
				throw new APIException("You are not authorized to change " + user.getUserId() + "'s properties");
			
			user.removeUserProperty(key);
			
			try {
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
				saveUser(user, null);
			}
			finally {
				Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
			}
		}
		
		return user;
	}
	
	/**
	 * @see org.openmrs.api.UserService#generateSystemId()
	 */
	public String generateSystemId() {
		//Hardcoding Luhn algorithm since all existing openmrs user ids have had check digits generated this way.
		LuhnIdentifierValidator liv = new LuhnIdentifierValidator();
		
		String systemId;
		Integer offset = 0;
		do {
			// generate and increment the system id if necessary
			Integer generatedId = dao.generateSystemId() + offset++;
			
			systemId = generatedId.toString();
			
			try {
				systemId = liv.getValidIdentifier(systemId);
			}
			catch (Exception e) {
				log.error("error getting check digit", e);
				return systemId;
			}
			
			// loop until we find a system id that no one has 
		} while (dao.hasDuplicateUsername(null, systemId, null));
		
		return systemId;
	}
	
	/**
	 * Iterates over Names/Addresses/Identifiers to set dateCreated and creator properties if needed
	 * 
	 * @param user
	 */
	private void setCollectionProperties(User user) {
		// set the properties on the collections for the generic person object
		Context.getPersonService().setCollectionProperties(user);
		
		// user creator/changer
		if (user.getCreator() == null) {
			user.setCreator(Context.getAuthenticatedUser());
			user.setDateCreated(new Date());
		} else {
			user.setChangedBy(Context.getAuthenticatedUser());
			user.setDateChanged(new Date());
		}
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeUser(org.openmrs.User)
	 */
	public void purgeUser(User user) throws APIException {
		dao.deleteUser(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeUser(org.openmrs.User, boolean)
	 */
	public void purgeUser(User user, boolean cascade) throws APIException {
		if (cascade == true) {
			throw new APIException("I don't think we want to cascade here");
		}
		
		dao.deleteUser(user);
	}
	
	/**
	 * Convenience method to check if the authenticated user has all privileges they are giving out
	 * to the new role
	 * 
	 * @param new user that has privileges
	 */
	private void checkPrivileges(Role role) {
		Collection<Privilege> privileges = role.getPrivileges();
		
		if (privileges != null)
			for (Privilege p : privileges) {
				if (!Context.hasPrivilege(p.getPrivilege()))
					throw new APIAuthenticationException("Privilege required: " + p);
			}
	}
	
}
