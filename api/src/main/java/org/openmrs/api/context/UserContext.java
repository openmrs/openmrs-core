/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.PrivilegeListener;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.UserSessionListener.Event;
import org.openmrs.UserSessionListener.Status;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.RoleConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an OpenMRS <code>User Context</code> which stores the current user information. Only
 * one <code>User</code> may be authenticated within a UserContext at any given time. The
 * UserContext should not be accessed directly, but rather used through the <code>Context</code>.
 * This class should be kept light-weight. There is one instance of this class per user that is
 * logged into the system.
 *
 * @see org.openmrs.api.context.Context
 */
public class UserContext implements Serializable {
	
	private static final long serialVersionUID = -806631231941890648L;
	
	/**
	 * Logger - shared by entire class
	 */
	private static final Logger log = LoggerFactory.getLogger(UserContext.class);
	
	/**
	 * User object containing details about the authenticated user
	 */
	private User user = null;
	
	/**
	 * User's permission proxies
	 */
	private List<String> proxies = new ArrayList<>();
	
	/**
	 * User's locale
	 */
	private Locale locale = null;
	
	/**
	 * Cached Role given to all authenticated users
	 */
	private Role authenticatedRole = null;
	
	/**
	 * Cache Role given to all users
	 */
	private Role anonymousRole = null;
	
	/**
	 * User's defined location
	 */
	private Integer locationId;
	
	/**
	 * The authentication scheme for this user
	 */
	private final AuthenticationScheme authenticationScheme;
	
	/**
	 * Creates a user context based on the provided auth. scheme.
	 *
	 * @param authenticationScheme The auth. scheme that applies for this user context.
	 * @since 2.3.0
	 */
	public UserContext(AuthenticationScheme authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}
	
	/**
	 * Authenticate user with the provided credentials. The authentication scheme must be Spring wired, see {@link Context#getAuthenticationScheme()}.
	 *
	 * @param credentials The credentials to use to authenticate
	 * @return The authenticated client information
	 * @throws ContextAuthenticationException if authentication fails
	 * @since 2.3.0
	 */
	public Authenticated authenticate(Credentials credentials)
		throws ContextAuthenticationException {
		
		log.debug("Authenticating client '{}' with scheme '{}'", credentials.getClientName(),
			credentials.getAuthenticationScheme());
		
		Authenticated authenticated = null;
		try {
			authenticated = authenticationScheme.authenticate(credentials);
			this.user = authenticated.getUser();
			notifyUserSessionListener(this.user, Event.LOGIN, Status.SUCCESS);
		}
		catch (ContextAuthenticationException e) {
			User loggingInUser = new User();
			loggingInUser.setUsername(credentials.getClientName());
			notifyUserSessionListener(loggingInUser, Event.LOGIN, Status.FAIL);
			throw e;
		}
		
		setUserLocation(true);
		setUserLocale(true);
		
		log.debug("Authenticated as: {}", this.user);
		
		return authenticated;
	}
	
	/**
	 * Refresh the authenticated user object in this UserContext. This should be used when updating
	 * information in the database about the current user and it needs to be reflecting in the
	 * (cached) {@link #getAuthenticatedUser()} User object.
	 *
	 * @since 1.5
	 */
	public void refreshAuthenticatedUser() {
		log.debug("Refreshing authenticated user");
		
		if (user != null) {
			user = Context.getUserService().getUser(user.getUserId());
			//update the stored location in the user's session
			setUserLocation(false);
			setUserLocale(false);
		}
	}
	
	/**
	 * Change current authentication to become another user. (You can only do this if you're already
	 * authenticated as a superuser.)
	 *
	 * @param systemId
	 * @return The new user that this context has been set to. (null means no change was made)
	 * @throws ContextAuthenticationException
	 */
	public User becomeUser(String systemId) throws ContextAuthenticationException {
		if (!Context.getAuthenticatedUser().isSuperUser()) {
			throw new APIAuthenticationException("You must be a superuser to assume another user's identity");
		}
		
		log.debug("Turning the authenticated user into user with systemId: {}", systemId);
		
		User userToBecome = Context.getUserService().getUserByUsername(systemId);
		
		if (userToBecome == null) {
			throw new ContextAuthenticationException("User not found with systemId: " + systemId);
		}
		
		// hydrate the user object
		if (userToBecome.getAllRoles() != null) {
			userToBecome.getAllRoles().size();
		}
		
		if (userToBecome.getUserProperties() != null) {
			userToBecome.getUserProperties().size();
		}
		
		if (userToBecome.getPrivileges() != null) {
			userToBecome.getPrivileges().size();
		}
		
		this.user = userToBecome;
		
		//update the user's location and locale
		setUserLocation(false);
		setUserLocale(false);
		
		log.debug("Becoming user: {}", user);
		
		return userToBecome;
	}
	
	/**
	 * @return "active" user who has been authenticated, otherwise <code>null</code>
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
		notifyUserSessionListener(user, Event.LOGOUT, Status.SUCCESS);
		user = null;
		locationId = null;
		locale = null;
		proxies.clear();
	}
	
	/**
	 * Gives the given privilege to all calls to hasPrivilege. This method was visualized as being
	 * used as follows (try/finally is important):
	 *
	 * <pre>
	 * try {
	 *   Context.addProxyPrivilege(&quot;AAA&quot;);
	 *   Context.get*Service().methodRequiringAAAPrivilege();
	 * }
	 * finally {
	 *   Context.removeProxyPrivilege(&quot;AAA&quot;);
	 * }
	 * </pre>
	 *
	 * @param privilege to give to users
	 */
	public void addProxyPrivilege(String privilege) {
		log.debug("Adding proxy privilege: {}", privilege);
		
		proxies.add(privilege);
	}
	
	/**
	 * Will remove one instance of privilege from the privileges that are currently proxied
	 *
	 * @param privilege Privilege to remove in string form
	 */
	public void removeProxyPrivilege(String privilege) {
		log.debug("Removing proxy privilege: {}", privilege);
		proxies.remove(privilege);
	}
	
	/**
	 * @param locale new locale for this context
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * @return current locale for this context
	 */
	public Locale getLocale() {
		if (locale == null) {
			// don't cache default locale - allows recognition of changed default at login page
			return LocaleUtility.getDefaultLocale();
		}
		
		return locale;
	}
	
	/**
	 * Gets all the roles for the (un)authenticated user. Anonymous and Authenticated roles are
	 * appended if necessary
	 *
	 * @return all expanded roles for a user
	 * @throws Exception
	 */
	public Set<Role> getAllRoles() throws Exception {
		return getAllRoles(getAuthenticatedUser());
	}
	
	/**
	 * Gets all the roles for a user. Anonymous and Authenticated roles are appended if necessary
	 *
	 * @param user
	 * @return all expanded roles for a user
	 * <strong>Should</strong> not fail with null user
	 * <strong>Should</strong> add anonymous role to all users
	 * <strong>Should</strong> add authenticated role to all authenticated users
	 * <strong>Should</strong> return same roles as user getAllRoles method
	 */
	public Set<Role> getAllRoles(User user) throws Exception {
		Set<Role> roles = new HashSet<>();
		
		// add the Anonymous Role
		roles.add(getAnonymousRole());
		
		// add the Authenticated role
		if (getAuthenticatedUser() != null && getAuthenticatedUser().equals(user)) {
			roles.addAll(user.getAllRoles());
			roles.add(getAuthenticatedRole());
		}
		
		return roles;
	}
	
	/**
	 * Tests whether currently authenticated user has a particular privilege
	 *
	 * @param privilege
	 * @return true if authenticated user has given privilege
	 * <strong>Should</strong> authorize if authenticated user has specified privilege
	 * <strong>Should</strong> authorize if authenticated role has specified privilege
	 * <strong>Should</strong> authorize if proxied user has specified privilege
	 * <strong>Should</strong> authorize if anonymous user has specified privilege
	 * <strong>Should</strong> not authorize if authenticated user does not have specified privilege
	 * <strong>Should</strong> not authorize if authenticated role does not have specified privilege
	 * <strong>Should</strong> not authorize if proxied user does not have specified privilege
	 * <strong>Should</strong> not authorize if anonymous user does not have specified privilege
	 */
	public boolean hasPrivilege(String privilege) {
		
		// if a user has logged in, check their privileges
		if (isAuthenticated()
			&& (getAuthenticatedUser().hasPrivilege(privilege) || getAuthenticatedRole().hasPrivilege(privilege))) {
			
			// check user's privileges
			notifyPrivilegeListeners(getAuthenticatedUser(), privilege, true);
			return true;
			
		}
		
		log.debug("Checking '{}' against proxies: {}", privilege, proxies);
		
		// check proxied privileges
		for (String s : proxies) {
			if (s.equals(privilege)) {
				notifyPrivilegeListeners(getAuthenticatedUser(), privilege, true);
				return true;
			}
		}
		
		if (getAnonymousRole().hasPrivilege(privilege)) {
			notifyPrivilegeListeners(getAuthenticatedUser(), privilege, true);
			return true;
		}
		
		// default return value
		notifyPrivilegeListeners(getAuthenticatedUser(), privilege, false);
		return false;
	}
	
	/**
	 * Convenience method to get the Role in the system designed to be given to all users
	 *
	 * @return Role
	 * <strong>Should</strong> fail if database doesn't contain anonymous role
	 */
	private Role getAnonymousRole() {
		if (anonymousRole != null) {
			return anonymousRole;
		}
		
		anonymousRole = Context.getUserService().getRole(RoleConstants.ANONYMOUS);
		if (anonymousRole == null) {
			throw new RuntimeException(
				"Database out of sync with code: " + RoleConstants.ANONYMOUS + " role does not exist");
		}
		
		return anonymousRole;
	}
	
	/**
	 * Convenience method to get the Role in the system designed to be given to all users that have
	 * authenticated in some manner
	 *
	 * @return Role
	 * <strong>Should</strong> fail if database doesn't contain authenticated role
	 */
	private Role getAuthenticatedRole() {
		if (authenticatedRole != null) {
			return authenticatedRole;
		}
		
		authenticatedRole = Context.getUserService().getRole(RoleConstants.AUTHENTICATED);
		if (authenticatedRole == null) {
			throw new RuntimeException("Database out of sync with code: " + RoleConstants.AUTHENTICATED
				+ " role does not exist");
		}
		
		return authenticatedRole;
	}
	
	/**
	 * @return locationId for this user context if any is set
	 * @since 1.10
	 */
	public Integer getLocationId() {
		return locationId;
	}
	
	/**
	 * @param locationId locationId to set
	 * @since 1.10
	 */
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	
	/**
	 * @return current location for this user context if any is set
	 * @since 1.9
	 */
	public Location getLocation() {
		if (locationId == null) {
			return null;
		}
		return Context.getLocationService().getLocation(locationId);
	}
	
	/**
	 * @param location the location to set to
	 * @since 1.9
	 */
	public void setLocation(Location location) {
		if (location != null) {
			this.locationId = location.getLocationId();
		}
	}
	
	/**
	 * Convenience method that sets the default location of the currently authenticated user using
	 * the value of the user's default location property
	 */
	private void setUserLocation(boolean useDefault) {
		// location should be null if no user is logged in
		if (this.user == null) {
			this.locationId = null;
			return;
		}
		
		// intended to be when the user initially authenticates
		if (this.locationId == null && useDefault) {
			this.locationId = getDefaultLocationId(this.user);
		}
	}

	/**
	 * Convenience method that sets the default localeused by the currently authenticated user, using
	 * the value of the user's default local property
	 */
	private void setUserLocale(boolean useDefault) {
		// local should be null if no user is logged in
		if (this.user == null) {
			this.locale = null;
			return;
		}

		// intended to be when the user initially authenticates
		if (user.getUserProperties().containsKey("defaultLocale")) {
			String localeString = user.getUserProperty("defaultLocale");
			locale = LocaleUtility.fromSpecification(localeString);
		}

		if (locale == null && useDefault) {
			locale = LocaleUtility.getDefaultLocale();
		}

	}
	
	private Integer getDefaultLocationId(User user) {
		String locationId = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
		if (StringUtils.isNotBlank(locationId)) {
			//only go ahead if it has actually changed OR if wasn't set before
			try {
				int defaultId = Integer.parseInt(locationId);
				if (this.locationId == null || this.locationId != defaultId) {
					// validate that the id is a valid id
					if (Context.getLocationService().getLocation(defaultId) != null) {
						return defaultId;
					} else {
						log.warn("The default location for user '{}' is set to '{}', which is not a valid location",
							user.getUserId(), locationId);
					}
				}
			}
			catch (NumberFormatException e) {
				log.warn("The value of the default Location property of the user with id: {} should be an integer",
					user.getUserId(), e);
			}
		}
		
		return null;
	}
	
	/**
	 * Notifies privilege listener beans about any privilege check.
	 * <p>
	 * It is called by {@link UserContext#hasPrivilege(java.lang.String)}.
	 *
	 * @param user         the authenticated user or <code>null</code> if not authenticated
	 * @param privilege    the checked privilege
	 * @param hasPrivilege <code>true</code> if the authenticated user has the required privilege or
	 *                     if it is a proxy privilege
	 * @see PrivilegeListener
	 * @since 1.8.4, 1.9.1, 1.10
	 */
	private void notifyPrivilegeListeners(User user, String privilege, boolean hasPrivilege) {
		for (PrivilegeListener privilegeListener : Context.getRegisteredComponents(PrivilegeListener.class)) {
			try {
				privilegeListener.privilegeChecked(user, privilege, hasPrivilege);
			}
			catch (Exception e) {
				log.error("Privilege listener has failed", e);
			}
		}
	}
	
	private void notifyUserSessionListener(User user, Event event, Status status) {
		for (UserSessionListener userSessionListener : Context.getRegisteredComponents(UserSessionListener.class)) {
			userSessionListener.loggedInOrOut(user, event, status);
		}
	}
}
