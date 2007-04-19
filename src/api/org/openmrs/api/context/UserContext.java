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
 * Represents an OpenMRS <code>Context</code>, which may be used to
 * authenticate to the database and obtain services in order to interact with
 * the system.
 * 
 * Only one <code>User</code> may be authenticated within a context at any
 * given time.
 * 
 * @author Justin Miranda
 * @version 1.0
 */
public class UserContext {
	/*
	 * Logger - shared by entire class
	 */
	private static final Log log = LogFactory.getLog(UserContext.class);

	/*
	 * User details 
	 */
	private User user = null;

	/* 
	 * User permission proxies
	 */
	private List<String> proxies = new Vector<String>();

	/*
	 * User locale 
	 */
	private Locale locale = Locale.US;
	
	/*
	 * contextDAO
	 */
	private ContextDAO contextDAO;
	
	private Role authenticatedRole = null;
	private Role anonymousRole = null;
	
	
	/**
	 * Default public constructor
	 * 
	 */
	public UserContext() { }

	
	/**
	 * Get the context dao
	 */
	private ContextDAO getContextDAO() {
		return this.contextDAO;
	}
	
	/**
	 * set the contextDAO 
	 * @param dao
	 */
	public void setContextDAO(ContextDAO dao) {
		this.contextDAO = dao;
	}
	
	/**
	 * Authenticate the user to the userContext().
	 * See Context.authenticate(String,String)
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ContextAuthenticationException
	 */
	public User authenticate(String username, String password) throws ContextAuthenticationException {
		this.user = getContextDAO().authenticate(username, password);
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
		User u = Context.getUserService().getUserByUsername(systemId);
		
		// hydrate the user object
		if (u.getAllRoles() != null)
			u.getAllRoles().size();
		if (u.getUserProperties() != null)
			u.getUserProperties().size();
		if (u.getPrivileges() != null)
			u.getPrivileges().size();
		
		if (u == null)
			throw new ContextAuthenticationException("SystemId not found: " + systemId);

		this.user = u;
		
		return u;
	}

	/**
	 * @return "active" user who has been authenticated, otherwise
	 *         <code>null</code>
	 */
	public User getAuthenticatedUser() {
		return user != null ? user : null;
	}

	/**
	 * @return true if user has been authenticated in this context
	 */
	public boolean isAuthenticated() {
		return user != null;
	}

	/**
	 * logs out the "active" (authenticated) user within context
	 * 
	 * @see #authenticate
	 */
	public void logout() {
		log.debug("setting user to null");
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
		proxies.add(privilege);
	}

	/**
	 * Will remove one instance of privilege from the privileges that are
	 * currently proxied
	 * 
	 * @param privilege
	 */
	public void removeProxyPrivilege(String privilege) {
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
		if (getAuthenticatedUser() != null && getAuthenticatedUser().equals(user)) {
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
