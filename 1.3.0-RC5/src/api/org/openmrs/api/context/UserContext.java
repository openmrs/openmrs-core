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
package org.openmrs.api.context;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Represents an OpenMRS <code>User Context</code> which stores the current
 * user information.
 * 
 * Only one <code>User</code> may be authenticated within a UserContext at any
 * given time.
 * 
 * The UserContext should not be accessed directly, but rather used through
 * the <code>Context</code>.
 * 
 * This class should be kept light-weight.  There is one instance of this class
 * per user that is logged into the system.
 * 
 * @see org.openmrs.api.context.Context
 */
public class UserContext {
	/**
	 * Logger - shared by entire class
	 */
	private static final Log log = LogFactory.getLog(UserContext.class);

	/**
	 * User object containing details about the authenticated user 
	 */
	private User user = null;

	/**
	 * User's permission proxies
	 */
	private List<String> proxies = new Vector<String>();

	/**
	 * User's locale 
	 */
	private Locale locale = Locale.US;
	
	/**
	 * Cached Role given to all authenticated users
	 */
	private Role authenticatedRole = null;
	
	/**
	 * Cache Role given to all users
	 */
	private Role anonymousRole = null;
	
	
	/**
	 * Default public constructor
	 */
	public UserContext() { }

	/**
	 * Authenticate the user to this UserContext.
	 * @see org.openmrs.api.context.Context#authenticate(String,String)
	 * 
	 * @param username String login name
	 * @param password String login password
	 * @param ContextDAO contextDAO implementation to use for authentication
	 * @return User that has been authenticated
	 * @throws ContextAuthenticationException
	 */
	public User authenticate(String username, String password, ContextDAO contextDAO) throws ContextAuthenticationException {
		if (log.isDebugEnabled())
			log.debug("Authenticating with username: " + username);
		
		this.user = contextDAO.authenticate(username, password);
		
		if (log.isDebugEnabled())
			log.debug("Authenticated as: " + this.user);
		
		return this.user;
	}
	
	/**
	 * Change current authentication to become another user.
	 * (You can only do this if you're already authenticated as a superuser.)
	 * @param systemId
	 * @return The new user that this context has been set to. (null means no change was made)
	 * @throws ContextAuthenticationException
	 */
	public User becomeUser(String systemId) throws ContextAuthenticationException {
		if (!Context.getAuthenticatedUser().isSuperUser())
			throw new APIAuthenticationException("You must be a superuser to assume another user's identity");
		
		if (log.isDebugEnabled())
			log.debug("Turning the authenticated user into user with systemId: " + systemId);
		
		User userToBecome = Context.getUserService().getUserByUsername(systemId);
		
		if (userToBecome == null)
			throw new ContextAuthenticationException("User not found with systemId: " + systemId);

		// hydrate the user object
		if (userToBecome.getAllRoles() != null)
			userToBecome.getAllRoles().size();
		if (userToBecome.getUserProperties() != null)
			userToBecome.getUserProperties().size();
		if (userToBecome.getPrivileges() != null)
			userToBecome.getPrivileges().size();
		
		this.user = userToBecome;
		
		if (log.isDebugEnabled())
			log.debug("Becoming user: " + user);
		
		return userToBecome;
	}

	/**
	 * @return "active" user who has been authenticated, otherwise
	 *         <code>null</code>
	 */
	public User getAuthenticatedUser() {
		return user;
	}

	/**
	 * @return true if user has been authenticated in this UserContext
	 */
	public boolean isAuthenticated() {
		return user != null;
	}

	/**
	 * logs out the "active" (authenticated) user within this UserContext
	 * 
	 * @see #authenticate
	 */
	public void logout() {
		log.debug("setting user to null on logout");
		user = null;
	}


	
	/**
	 * Gives the given privilege to all calls to hasPrivilege. This method was
	 * visualized as being used as follows:
	 * 
	 * <code>
	 * Context.addProxyPrivilege("AAA");
	 * Context.get*Service().methodRequiringAAAPrivilege();
	 * Context.removeProxyPrivilege("AAA");
	 * </code>
	 * 
	 * @param privilege
	 *            to give to users
	 */
	public void addProxyPrivilege(String privilege) {
		if (log.isDebugEnabled())
			log.debug("Adding proxy privilege: " + privilege);
		
		proxies.add(privilege);
	}

	/**
	 * Will remove one instance of privilege from the privileges that are
	 * currently proxied
	 * 
	 * @param String privilege
	 */
	public void removeProxyPrivilege(String privilege) {
		if (log.isDebugEnabled())
			log.debug("Removing proxy privilege: " + privilege);
		
		if (proxies.contains(privilege))
			proxies.remove(privilege);
	}

	/**
	 * @param locale
	 *            new locale for this context
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return current locale for this context
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * Gets all the roles for the (un)authenticated user.
	 * Anonymous and Authenticated roles are appended if necessary
	 * 
	 * @return all expanded roles for a user
	 * @throws Exception
	 */
	public Set<Role> getAllRoles() throws Exception {
		return getAllRoles(getAuthenticatedUser());
	}
	
	/**
	 * Gets all the roles for a user.  Anonymous and Authenticated roles are 
	 * appended if necessary
	 * 
	 * @param user
	 * @return all expanded roles for a user
	 */
	public Set<Role> getAllRoles(User user) throws Exception {
		Set<Role> roles = new HashSet<Role>();
		
		// add the Anonymous Role
		roles.add(getAnonymousRole());
		
		// add the Authenticated role
		if (user != null && getAuthenticatedUser() != null && getAuthenticatedUser().equals(user)) {
			roles.addAll(user.getAllRoles());
			roles.add(getAuthenticatedRole());
		}
		
		return roles;
	}

	/**
	 * Tests whether or not currently authenticated user has a particular
	 * privilege
	 * 
	 * @param privilege
	 * @return true if authenticated user has given privilege
	 */
	public boolean hasPrivilege(String privilege) {

		// if a user has logged in, check their privileges
		if (isAuthenticated()) {

			// check user's privileges
			if (getAuthenticatedUser().hasPrivilege(privilege))
				return true;

			if (getAuthenticatedRole().hasPrivilege(privilege))
				return true;
		}
		
		if (log.isDebugEnabled())
			log.debug("Checking '" + privilege + "' against proxies: " + proxies);
		
		// check proxied privileges
		for (String s : proxies)
			if (s.equals(privilege))
				return true;
		
		if (getAnonymousRole().hasPrivilege(privilege))
			return true;

		// default return value
		return false;
	}


	/**
	 * Convenience method to get the Role in the system designed to
	 * be given to all users
	 * 
	 * @return Role
	 */
	private Role getAnonymousRole() {
		if (anonymousRole != null)
			return anonymousRole;
		
		anonymousRole = Context.getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
		if (anonymousRole == null) {
			throw new RuntimeException("Database out of sync with code: "
					+ OpenmrsConstants.ANONYMOUS_ROLE + " role does not exist");
		}
		
		return anonymousRole;
	}

	/**
	 * Convenience method to get the Role in the system designed to
	 * be given to all users that have authenticated in some manner
	 * 
	 * @return Role
	 */
	private Role getAuthenticatedRole() {
		if (authenticatedRole != null)
			return authenticatedRole;
		
		authenticatedRole = Context.getUserService().getRole(OpenmrsConstants.AUTHENTICATED_ROLE);
		if (authenticatedRole == null) {
			throw new RuntimeException("Database out of sync with code: "
					+ OpenmrsConstants.AUTHENTICATED_ROLE + " role does not exist");
		}
		
		return authenticatedRole;
	}

}
