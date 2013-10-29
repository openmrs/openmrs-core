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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.openmrs.Location;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.RoleConstants;
import org.openmrs.util.Security;
import org.springframework.transaction.annotation.Transactional;

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
	 * Default public constructor
	 */
	public UserContext() {
	}
	
	/**
	 * Authenticate the user to this UserContext.
	 * 
	 * @see org.openmrs.api.context.Context#authenticate(String,String)
	 * @param username String login name
	 * @param password String login password
	 * @param contextDAO ContextDAO implementation to use for authentication
	 * @return User that has been authenticated
	 * @throws ContextAuthenticationException
	 * @throws ContextAuthenticationLockoutException
	 * @should authenticate given username and password
	 * @should authenticate given systemId and password
	 * @should authenticate given systemId without hyphen and password
	 * @should not authenticate given username and incorrect password
	 * @should not authenticate given systemId and incorrect password
	 * @should not authenticate given incorrect username
	 * @should not authenticate given incorrect systemId
	 * @should not authenticate given null login
	 * @should not authenticate given empty login
	 * @should not authenticate given null password when password in database is null
	 * @should not authenticate given non null password when password in database is null
	 * @should not authenticate when password in database is empty
	 * @should give identical error messages between username and password mismatch
	 * @should lockout user after specified number of failed attempts
	 * @should authenticateWithCorrectHashedPassword
	 * @should authenticateWithIncorrectHashedPassword
	 * @should set uuid on user property when authentication fails with valid user
	 * @should pass regression test for 1580
	 * @should throw a ContextAuthenticationException if username is an empty string
	 * @should throw a ContextAuthenticationException if username is white space 
	 */
	@Transactional(noRollbackFor = ContextAuthenticationException.class)
	public User authenticate(String username, String password, ContextDAO contextDAO) throws ContextAuthenticationException,
	        ContextAuthenticationLockoutException {
		if (log.isDebugEnabled())
			log.debug("Authenticating with username: " + username);
		
		String errorMsg = "Invalid username and/or password: " + username;
		
		User candidateUser = null;
		
		if (username != null) {
			//if username is blank or white space character(s)
			if (StringUtils.isEmpty(username) || StringUtils.isWhitespace(username))
				throw new ContextAuthenticationException(errorMsg);
			
			// loginWithoutDash is used to compare to the system id
			String loginWithDash = username;
			if (username.matches("\\d{2,}"))
				loginWithDash = username.substring(0, username.length() - 1) + "-" + username.charAt(username.length() - 1);
			
			try {
				Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
				candidateUser = Context.getUserService().getUserByUsername(username);
				if (candidateUser == null) {
					candidateUser = Context.getUserService().getUserByUsername(loginWithDash);
				}
				
				if (candidateUser.getRetired()) {
					candidateUser = null;
				}
			}
			catch (HibernateException he) {
				log.error("Got hibernate exception while logging in: '" + username + "'", he);
			}
			catch (Exception e) {
				log.error("Got regular exception while logging in: '" + username + "'", e);
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
			}
		}
		
		// only continue if this is a valid username and a nonempty password
		if (candidateUser != null && password != null) {
			if (log.isDebugEnabled())
				log.debug("Candidate user id: " + candidateUser.getUserId());
			
			String passwordOnRecord = contextDAO.getHashedPassword(candidateUser);
			String saltOnRecord = contextDAO.getSalt(candidateUser);
			
			String lockoutTimeString = candidateUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null);
			Long lockoutTime = null;
			if (lockoutTimeString != null && !lockoutTimeString.equals("0")) {
				try {
					// putting this in a try/catch in case the admin decided to put junk into the property
					lockoutTime = Long.valueOf(lockoutTimeString);
				}
				catch (NumberFormatException e) {
					log.debug("bad value stored in " + OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP + " user property: "
					        + lockoutTimeString);
				}
			}
			
			// if they've been locked out, don't continue with the authentication
			if (lockoutTime != null) {
				Integer loginLockoutDuration = 300000; //default 5 mins
				
				try {
					loginLockoutDuration = Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
					    OpenmrsConstants.GP_LOGIN_LOCKOUT_DURATION).trim());
				}
				catch (Exception ex) {
					log.error("Unable to convert the global property " + OpenmrsConstants.GP_LOGIN_LOCKOUT_DURATION
					        + " to a valid integer. Using default value of 300000.");
				}
				
				// unlock them after loginLockoutDuration ms, otherwise reset the timestamp
				// to now and make them wait another loginLockoutDuration ms
				if (System.currentTimeMillis() - lockoutTime > loginLockoutDuration) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
					saveUser(candidateUser);
				} else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(System
					        .currentTimeMillis()));
					throw new ContextAuthenticationLockoutException(
					        "Invalid number of connection attempts. Please try again later.");
				}
			}
			
			// if the username and password match, hydrate the user and return it
			if (passwordOnRecord != null && Security.hashMatches(passwordOnRecord, password + saltOnRecord)) {
				// hydrate the user object
				candidateUser.getAllRoles().size();
				candidateUser.getUserProperties().size();
				candidateUser.getPrivileges().size();
				
				// only clean up if the were some login failures, otherwise all should be clean
				Integer attempts = getUsersLoginAttempts(candidateUser);
				if (attempts > 0) {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
					candidateUser.removeUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
					saveUser(candidateUser);
				}
				
				setUserLocation();
				
				this.user = candidateUser;
				if (log.isDebugEnabled())
					log.debug("Authenticated as: " + this.user);
				
				// skip out of the method early (instead of throwing the exception)
				// to indicate that this is the valid user					
				return candidateUser;
			} else {
				// the user failed the username/password, increment their
				// attempts here and set the "lockout" timestamp if necessary
				Integer attempts = getUsersLoginAttempts(candidateUser);
				
				attempts++;
				
				Integer allowedFailedLoginCount = 7;
				
				try {
					allowedFailedLoginCount = Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
					    OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT).trim());
				}
				catch (Exception ex) {
					log.error("Unable to convert the global property "
					        + OpenmrsConstants.GP_ALLOWED_FAILED_LOGINS_BEFORE_LOCKOUT
					        + " to a valid integer. Using default value of 7.");
				}
				
				if (attempts > allowedFailedLoginCount) {
					// set the user as locked out at this exact time
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(System
					        .currentTimeMillis()));
				} else {
					candidateUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, String.valueOf(attempts));
				}
				
				saveUser(candidateUser);
			}
		}
		
		// throw this exception only once in the same place with the same
		// message regardless of username/pw combo entered
		log.info("Failed login attempt (login=" + username + ") - " + errorMsg);
		throw new ContextAuthenticationException(errorMsg);
	}
	
	/**
	 * Refresh the authenticated user object in this UserContext. This should be used when updating
	 * information in the database about the current user and it needs to be reflecting in the
	 * (cached) {@link #getAuthenticatedUser()} User object.
	 * 
	 * @since 1.5
	 */
	public void refreshAuthenticatedUser() {
		if (log.isDebugEnabled())
			log.debug("Refreshing authenticated user");
		
		if (user != null) {
			user = Context.getUserService().getUser(user.getUserId());
			//update the stored location in the user's session
			setUserLocation();
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
		//update the user's location
		setUserLocation();
		
		if (log.isDebugEnabled())
			log.debug("Becoming user: " + user);
		
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
		user = null;
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
		if (log.isDebugEnabled())
			log.debug("Adding proxy privilege: " + privilege);
		
		proxies.add(privilege);
	}
	
	/**
	 * Will remove one instance of privilege from the privileges that are currently proxied
	 * 
	 * @param privilege Privilege to remove in string form
	 */
	public void removeProxyPrivilege(String privilege) {
		if (log.isDebugEnabled())
			log.debug("Removing proxy privilege: " + privilege);
		
		if (proxies.contains(privilege))
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
		if (locale == null)
			// don't cache default locale - allows recognition of changed default at login page
			return LocaleUtility.getDefaultLocale();
		
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
	 * @should not fail with null user
	 * @should add anonymous role to all users
	 * @should add authenticated role to all authenticated users
	 * @should return same roles as user getAllRoles method
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
	 * Tests whether or not currently authenticated user has a particular privilege
	 * 
	 * @param privilege
	 * @return true if authenticated user has given privilege
	 * @should authorize if authenticated user has specified privilege
	 * @should authorize if authenticated role has specified privilege
	 * @should authorize if proxied user has specified privilege
	 * @should authorize if anonymous user has specified privilege
	 * @should not authorize if authenticated user does not have specified privilege
	 * @should not authorize if authenticated role does not have specified privilege
	 * @should not authorize if proxied user does not have specified privilege
	 * @should not authorize if anonymous user does not have specified privilege
	 */
	public boolean hasPrivilege(String privilege) {
		
		// if a user has logged in, check their privileges
		if (isAuthenticated()) {
			
			// check user's privileges
			if (getAuthenticatedUser().hasPrivilege(privilege) || getAuthenticatedRole().hasPrivilege(privilege)) {
				Context.getUserService().notifyPrivilegeListeners(getAuthenticatedUser(), privilege, true);
				return true;
			}
			
		}
		
		if (log.isDebugEnabled())
			log.debug("Checking '" + privilege + "' against proxies: " + proxies);
		
		// check proxied privileges
		for (String s : proxies) {
			if (s.equals(privilege)) {
				Context.getUserService().notifyPrivilegeListeners(getAuthenticatedUser(), privilege, true);
				return true;
			}
		}
		
		if (getAnonymousRole().hasPrivilege(privilege)) {
			Context.getUserService().notifyPrivilegeListeners(getAuthenticatedUser(), privilege, true);
			return true;
		}
		
		// default return value
		Context.getUserService().notifyPrivilegeListeners(getAuthenticatedUser(), privilege, false);
		return false;
	}
	
	/**
	 * Convenience method to get the Role in the system designed to be given to all users
	 * 
	 * @return Role
	 * @should fail if database doesn't contain anonymous role
	 */
	private Role getAnonymousRole() {
		if (anonymousRole != null)
			return anonymousRole;
		
		anonymousRole = Context.getUserService().getRole(RoleConstants.ANONYMOUS);
		if (anonymousRole == null) {
			throw new RuntimeException("Database out of sync with code: " + RoleConstants.ANONYMOUS + " role does not exist");
		}
		
		return anonymousRole;
	}
	
	/**
	 * Convenience method to get the Role in the system designed to be given to all users that have
	 * authenticated in some manner
	 * 
	 * @return Role
	 * @should fail if database doesn't contain authenticated role
	 */
	private Role getAuthenticatedRole() {
		if (authenticatedRole != null)
			return authenticatedRole;
		
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
		if (locationId == null)
			return null;
		return Context.getLocationService().getLocation(locationId);
	}
	
	/**
	 * @param location the location to set to
	 * @since 1.9
	 */
	public void setLocation(Location location) {
		if (location != null)
			this.locationId = location.getLocationId();
	}
	
	/**
	 * Convenience method that sets the default location of the currently authenticated user using
	 * the value of the user's default location property
	 */
	private void setUserLocation() {
		if (this.user != null) {
			String locationId = this.user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
			if (StringUtils.isNotBlank(locationId)) {
				//only go ahead if it has actually changed OR if wasn't set before
				if (this.locationId == null || this.locationId != Integer.parseInt(locationId)) {
					try {
						this.locationId = Context.getLocationService().getLocation(Integer.valueOf(locationId))
						        .getLocationId();
					}
					catch (NumberFormatException e) {
						//Drop the stored value since we have no match for the set id
						if (this.locationId != null)
							this.locationId = null;
						log.warn("The value of the default Location property of the user with id:" + this.user.getUserId()
						        + " should be an integer", e);
					}
				}
			} else {
				if (this.locationId != null)
					this.locationId = null;
			}
		}
	}
	
	/**
	 * Get the integer stored for the given user that is their number of login attempts
	 * 
	 * @param user the user to check
	 * @return the # of login attempts for this user defaulting to zero if none defined
	 */
	private Integer getUsersLoginAttempts(User user) {
		String attemptsString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS, "0");
		Integer attempts = 0;
		try {
			attempts = Integer.valueOf(attemptsString);
		}
		catch (NumberFormatException e) {
			// skip over errors and leave the attempts at zero
		}
		return attempts;
	}
	
	/**
	 * Calls UserService saveUser with all required proxy privileges.
	 * @param user User object to be saved.
	 * @return Saved user object.
	 */
	private User saveUser(User user) {
		if (user == null) {
			return user;
		}
		
		Collection<Role> roles = user.getAllRoles();
		List<String> requiredPrivs = new Vector<String>();
		
		for (Role r : roles) {
			if (r.getPrivileges() != null) {
				for (Privilege p : r.getPrivileges())
					requiredPrivs.add(p.getPrivilege());
			}
		}
		
		try {
			Context.addProxyPrivilege(PrivilegeConstants.EDIT_USERS);
			Context.addProxyPrivilege(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE);
			for (String s : requiredPrivs) {
				Context.addProxyPrivilege(s);
			}
			user = Context.getUserService().saveUser(user, null);
		}
		catch (HibernateException he) {
			log.error("Got hibernate exception while logging in: '" + user.getUsername() + "'", he);
		}
		catch (Exception e) {
			log.error("Got regular exception while logging in: '" + user.getUsername() + "'", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USERS);
			Context.removeProxyPrivilege(PrivilegeConstants.ASSIGN_SYSTEM_DEVELOPER_ROLE);
			for (String s : requiredPrivs) {
				Context.removeProxyPrivilege(s);
			}
		}
		
		return user;
	}
}
