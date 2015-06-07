/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.PrivilegeListener;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.CannotDeleteRoleWithChildrenException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserDAO;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the user service. This class should not be used on its own. The current
 * OpenMRS implementation should be fetched from the Context
 * 
 * @see org.openmrs.api.UserService
 * @see org.openmrs.api.context.Context
 */
@Transactional
public class UserServiceImpl extends BaseOpenmrsService implements UserService {
	
	protected final Log log = LogFactory.getLog(UserServiceImpl.class);
	
	protected UserDAO dao;
	
	@Autowired(required = false)
	List<PrivilegeListener> privilegeListeners;
	
	public UserServiceImpl() {
	}
	
	public void setUserDAO(UserDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.api.UserService#createUser(org.openmrs.User, java.lang.String)
	 * @deprecated
	 */
	@Deprecated
	public User createUser(User user, String password) throws APIException {
		return Context.getUserService().saveUser(user, password);
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveUser(org.openmrs.User, java.lang.String)
	 * @deprecated replaced by {@link #createUser(User, String)}
	 */
	public User saveUser(User user, String password) throws APIException {
		if (user.getUserId() == null) {
			Context.requirePrivilege(PrivilegeConstants.ADD_USERS);
		} else {
			Context.requirePrivilege(PrivilegeConstants.EDIT_USERS);
		}
		
		checkPrivileges(user);
		
		// if we're creating a user and a password wasn't supplied, throw an
		// error
		if (user.getUserId() == null && (password == null || password.length() < 1)) {
			throw new APIException("User.creating.password.required", (Object[]) null);
		}
		
		if (hasDuplicateUsername(user)) {
			throw new DAOException("Username " + user.getUsername() + " or system id " + user.getSystemId()
			        + " is already in use.");
		}
		
		// TODO Check required fields for user!!
		
		if (user.getUserId() == null && password != null) {
			OpenmrsUtil.validatePassword(user.getUsername(), password, user.getSystemId());
		}
		
		return dao.saveUser(user, password);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUser(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public User getUser(Integer userId) throws APIException {
		return dao.getUser(userId);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUserByUsername(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public User getUserByUsername(String username) throws APIException {
		return dao.getUserByUsername(username);
	}
	
	/**
	 * @see org.openmrs.api.UserService#hasDuplicateUsername(org.openmrs.User)
	 */
	@Transactional(readOnly = true)
	public boolean hasDuplicateUsername(User user) throws APIException {
		return dao.hasDuplicateUsername(user.getUsername(), user.getSystemId(), user.getUserId());
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByRole(org.openmrs.Role)
	 */
	@Transactional(readOnly = true)
	public List<User> getUsersByRole(Role role) throws APIException {
		List<Role> roles = new Vector<Role>();
		roles.add(role);
		
		return Context.getUserService().getUsers(null, roles, false);
	}
	
	/**
	 * @deprecated replaced by {@link #saveUser(User, String)}
	 * @see org.openmrs.api.UserService#updateUser(org.openmrs.User)
	 */
	public void updateUser(User user) throws APIException {
		Context.getUserService().saveUser(user, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#grantUserRole(org.openmrs.User, org.openmrs.Role)
	 * @deprecated
	 */
	public void grantUserRole(User user, Role role) throws APIException {
		Context.getUserService().saveUser(user.addRole(role), null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#revokeUserRole(org.openmrs.User, org.openmrs.Role)
	 * @deprecated
	 */
	public void revokeUserRole(User user, Role role) throws APIException {
		Context.getUserService().saveUser(user.removeRole(role), null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#voidUser(org.openmrs.User, java.lang.String)
	 */
	public User voidUser(User user, String reason) throws APIException {
		return Context.getUserService().retireUser(user, reason);
	}
	
	/**
	 * @see org.openmrs.api.UserService#retireUser(org.openmrs.User, java.lang.String)
	 */
	public User retireUser(User user, String reason) throws APIException {
		user.setRetired(true);
		user.setRetireReason(reason);
		user.setRetiredBy(Context.getAuthenticatedUser());
		user.setDateRetired(new Date());
		
		return saveUser(user, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#unvoidUser(org.openmrs.User)
	 */
	public User unvoidUser(User user) throws APIException {
		return Context.getUserService().unretireUser(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#unretireUser(org.openmrs.User)
	 */
	public User unretireUser(User user) throws APIException {
		user.setRetired(false);
		user.setRetireReason(null);
		user.setRetiredBy(null);
		user.setDateRetired(null);
		
		return saveUser(user, null);
	}
	
	/**
	 * @see org.openmrs.api.UserService#deleteUser(org.openmrs.User)
	 * @deprecated
	 */
	public void deleteUser(User user) throws APIException {
		Context.getUserService().purgeUser(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsers()
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	public List<User> getUsers() throws APIException {
		return Context.getUserService().getAllUsers();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers()
	 */
	@Transactional(readOnly = true)
	public List<User> getAllUsers() throws APIException {
		return dao.getAllUsers();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivileges()
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	public List<Privilege> getPrivileges() throws APIException {
		return Context.getUserService().getAllPrivileges();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllPrivileges()
	 */
	@Transactional(readOnly = true)
	public List<Privilege> getAllPrivileges() throws APIException {
		return dao.getAllPrivileges();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilege(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Privilege getPrivilege(String p) throws APIException {
		return dao.getPrivilege(p);
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgePrivilege(org.openmrs.Privilege)
	 */
	public void purgePrivilege(Privilege privilege) throws APIException {
		if (OpenmrsUtil.getCorePrivileges().keySet().contains(privilege.getPrivilege())) {
			throw new APIException("Privilege.cannot.delete.core", (Object[]) null);
		}
		
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
	@Transactional(readOnly = true)
	public List<Role> getRoles() throws APIException {
		return Context.getUserService().getAllRoles();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllRoles()
	 */
	@Transactional(readOnly = true)
	public List<Role> getAllRoles() throws APIException {
		return dao.getAllRoles();
	}
	
	/**
	 * @see org.openmrs.api.UserService#getInheritingRoles(org.openmrs.Role)
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	public List<Role> getInheritingRoles(Role role) throws APIException {
		
		List<Role> roles = new Vector<Role>();
		roles.addAll(role.getInheritedRoles());
		
		return roles;
	}
	
	/**
	 * @see org.openmrs.api.UserService#getRole(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Role getRole(String r) throws APIException {
		return dao.getRole(r);
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeRole(org.openmrs.Role)
	 */
	public void purgeRole(Role role) throws APIException {
		if (role == null || role.getRole() == null) {
			return;
		}
		
		if (OpenmrsUtil.getCoreRoles().keySet().contains(role.getRole())) {
			throw new APIException("Role.cannot.delete.core", (Object[]) null);
		}
		
		if (role.hasChildRoles()) {
			throw new CannotDeleteRoleWithChildrenException();
		}
		
		dao.deleteRole(role);
	}
	
	/**
	 * @see org.openmrs.api.UserService#saveRole(org.openmrs.Role)
	 */
	public Role saveRole(Role role) throws APIException {
		// make sure one of the parents of this role isn't itself...this would
		// cause an infinite loop
		if (role.getAllParentRoles().contains(role)) {
			throw new APIException("Role.cannot.inherit.descendant", (Object[]) null);
		}
		
		checkPrivileges(role);
		
		return dao.saveRole(role);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changePassword(org.openmrs.User, java.lang.String)
	 * @deprecated replaced by {@link #changePassword(User, String, String)}
	 */
	@Deprecated
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
	 * @see org.openmrs.api.UserService#changeHashedPassword(User, String, String)
	 */
	public void changeHashedPassword(User user, String hashedPassword, String salt) throws APIException {
		dao.changeHashedPassword(user, hashedPassword, salt);
	}
	
	/**
	 * @see org.openmrs.api.UserService#changeQuestionAnswer(User, String, String)
	 */
	public void changeQuestionAnswer(User u, String question, String answer) throws APIException {
		dao.changeQuestionAnswer(u, question, answer);
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
	@Transactional(readOnly = true)
	public boolean isSecretAnswer(User u, String answer) {
		return dao.isSecretAnswer(u, answer);
	}
	
	/**
	 * @see org.openmrs.api.UserService#findUsers(String, List, boolean)
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	public List<User> findUsers(String name, List<String> roles, boolean includeVoided) {
		
		List<Role> rolesToSearch = new Vector<Role>();
		
		for (String role : roles) {
			rolesToSearch.add(new Role(role));
		}
		
		return Context.getUserService().getUsers(name, rolesToSearch, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#findUsers(String, String, boolean)
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	public List<User> findUsers(String givenName, String familyName, boolean includeVoided) {
		return Context.getUserService().getUsersByName(givenName, familyName, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByName(java.lang.String, java.lang.String, boolean)
	 */
	@Transactional(readOnly = true)
	public List<User> getUsersByName(String givenName, String familyName, boolean includeVoided) throws APIException {
		return dao.getUsersByName(givenName, familyName, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsersByPerson(org.openmrs.Person, boolean)
	 */
	@Transactional(readOnly = true)
	public List<User> getUsersByPerson(Person person, boolean includeRetired) throws APIException {
		return dao.getUsersByPerson(person, includeRetired);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getAllUsers(List, boolean)
	 * @deprecated
	 */
	@Transactional(readOnly = true)
	public List<User> getAllUsers(List<Role> roles, boolean includeVoided) {
		return Context.getUserService().getUsers(null, roles, includeVoided);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUsers(java.lang.String, java.util.List, boolean)
	 */
	@Transactional(readOnly = true)
	public List<User> getUsers(String nameSearch, List<Role> roles, boolean includeVoided) throws APIException {
		return Context.getUserService().getUsers(nameSearch, roles, includeVoided, null, null);
	}
	
	/**
	 * Convenience method to check if the authenticated user has all privileges they are giving out
	 * 
	 * @param new user that has privileges
	 */
	private void checkPrivileges(User user) {
		Collection<Role> roles = user.getAllRoles();
		List<String> requiredPrivs = new Vector<String>();
		
		for (Role r : roles) {
			if (r.getRole().equals(RoleConstants.SUPERUSER)
			        && !Context.hasPrivilege(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE)) {
				throw new APIException("User.you.must.have.role", new Object[] { RoleConstants.SUPERUSER });
			}
			if (r.getPrivileges() != null) {
				for (Privilege p : r.getPrivileges()) {
					if (!Context.hasPrivilege(p.getPrivilege())) {
						requiredPrivs.add(p.getPrivilege());
					}
				}
			}
		}
		
		if (requiredPrivs.size() == 1) {
			throw new APIException("User.you.must.have.privilege", new Object[] { requiredPrivs.get(0) });
		} else if (requiredPrivs.size() > 1) {
			StringBuilder txt = new StringBuilder("You must have the following privileges in order to assign them: ");
			for (String s : requiredPrivs) {
				txt.append(s).append(", ");
			}
			throw new APIException(txt.substring(0, txt.length() - 2));
		}
	}
	
	/**
	 * @see org.openmrs.api.UserService#setUserProperty(User, String, String)
	 */
	public User setUserProperty(User user, String key, String value) {
		if (user != null) {
			if (!Context.hasPrivilege(PrivilegeConstants.EDIT_USERS) && !user.equals(Context.getAuthenticatedUser())) {
				throw new APIException("you.are.not.authorized.change.properties", new Object[] { user.getUserId() });
			}
			
			user.setUserProperty(key, value);
			try {
				Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
				Context.getUserService().saveUser(user, null);
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
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
			// the user being edited is not the current user, throw an
			// exception
			if (!Context.hasPrivilege(PrivilegeConstants.EDIT_USERS) && !user.equals(Context.getAuthenticatedUser())) {
				throw new APIException("you.are.not.authorized.change.properties", new Object[] { user.getUserId() });
			}
			
			user.removeUserProperty(key);
			
			try {
				Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
				Context.getUserService().saveUser(user, null);
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
			}
		}
		
		return user;
	}
	
	/**
	 * Generates system ids based on the following algorithm scheme: user_id-check digit
	 * 
	 * @see org.openmrs.api.UserService#generateSystemId()
	 */
	@Transactional(readOnly = true)
	public String generateSystemId() {
		// Hardcoding Luhn algorithm since all existing openmrs user ids have
		// had check digits generated this way.
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
	 * @see org.openmrs.api.UserService#purgeUser(org.openmrs.User)
	 */
	public void purgeUser(User user) throws APIException {
		dao.deleteUser(user);
	}
	
	/**
	 * @see org.openmrs.api.UserService#purgeUser(org.openmrs.User, boolean)
	 */
	public void purgeUser(User user, boolean cascade) throws APIException {
		if (cascade) {
			throw new APIException("cascade.do.not.think", (Object[]) null);
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
		
		if (privileges != null) {
			for (Privilege p : privileges) {
				if (!Context.hasPrivilege(p.getPrivilege())) {
					throw new APIAuthenticationException("Privilege required: " + p);
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.UserService#getPrivilegeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Privilege getPrivilegeByUuid(String uuid) throws APIException {
		return dao.getPrivilegeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getRoleByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Role getRoleByUuid(String uuid) throws APIException {
		return dao.getRoleByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.UserService#getUserByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public User getUserByUuid(String uuid) throws APIException {
		return dao.getUserByUuid(uuid);
	}
	
	/**
	 * @see UserService#getCountOfUsers(String, List, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public Integer getCountOfUsers(String name, List<Role> roles, boolean includeRetired) {
		if (name != null) {
			name = name.replace(", ", " ");
		}
		
		// if the authenticated role is in the list of searched roles, then all
		// persons should be searched
		Role auth_role = getRole(RoleConstants.AUTHENTICATED);
		if (roles.contains(auth_role)) {
			return dao.getCountOfUsers(name, new Vector<Role>(), includeRetired);
		}
		
		return dao.getCountOfUsers(name, roles, includeRetired);
	}
	
	/**
	 * @see UserService#getUsers(String, List, boolean, Integer, Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<User> getUsers(String name, List<Role> roles, boolean includeRetired, Integer start, Integer length)
	        throws APIException {
		if (name != null) {
			name = name.replace(", ", " ");
		}
		
		if (roles == null) {
			roles = new Vector<Role>();
		}
		
		// add the requested roles and all child roles for consideration
		Set<Role> allRoles = new HashSet<Role>();
		for (Role r : roles) {
			allRoles.add(r);
			allRoles.addAll(r.getAllChildRoles());
		}
		
		// if the authenticated role is in the list of searched roles, then all
		// persons should be searched
		Role auth_role = getRole(RoleConstants.AUTHENTICATED);
		if (roles.contains(auth_role)) {
			return dao.getUsers(name, new Vector<Role>(), includeRetired, start, length);
		}
		
		return dao.getUsers(name, new ArrayList<Role>(allRoles), includeRetired, start, length);
	}
	
	/**
	 * @see UserService#notifyPrivilegeListeners(User, String, boolean)
	 */
	@Override
	@Transactional(readOnly = true)
	public void notifyPrivilegeListeners(User user, String privilege, boolean hasPrivilege) {
		if (privilegeListeners != null) {
			for (PrivilegeListener privilegeListener : privilegeListeners) {
				try {
					privilegeListener.privilegeChecked(user, privilege, hasPrivilege);
				}
				catch (Exception e) {
					log.error("Privilege listener has failed", e);
				}
			}
		}
	}
	
	@Override
	public User saveUserProperty(String key, String value) {
		User user = Context.getAuthenticatedUser();
		if (user == null) {
			throw new APIException("no.authenticated.user.found", (Object[]) null);
		}
		user.setUserProperty(key, value);
		return dao.saveUser(user, null);
	}
	
	@Override
	public User saveUserProperties(Map<String, String> properties) {
		User user = Context.getAuthenticatedUser();
		if (user == null) {
			throw new APIException("no.authenticated.user.found", (Object[]) null);
		}
		user.getUserProperties().clear();
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			user.setUserProperty(entry.getKey(), entry.getValue());
		}
		return dao.saveUser(user, null);
	}
	
	/**
	 * @see UserService#changePassword(User, String, String)
	 */
	@Override
	@Authorized(PrivilegeConstants.EDIT_USER_PASSWORDS)
	@Logging(ignoredArgumentIndexes = { 1, 2 })
	public void changePassword(User user, String oldPassword, String newPassword) throws APIException {
		if (user.getUserId() == null) {
			throw new APIException("user.must.exist", (Object[]) null);
		}
		if (oldPassword == null) {
			if (!Context.hasPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS)) {
				throw new APIException("null.old.password.privilege.required", (Object[]) null);
			}
		} else if (!dao.getLoginCredential(user).checkPassword(oldPassword)) {
			throw new APIException("old.password.not.correct", (Object[]) null);
		}
		OpenmrsUtil.validatePassword(user.getUsername(), newPassword, user.getSystemId());
		dao.changePassword(user, newPassword);
	}
	
}
