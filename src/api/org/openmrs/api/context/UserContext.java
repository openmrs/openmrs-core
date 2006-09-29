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
		log.error("setting user to null");
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
		Role role = Context.getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
		if (role == null) {
			throw new RuntimeException("Database out of sync with code: "
					+ OpenmrsConstants.ANONYMOUS_ROLE + " role does not exist");
		}
		roles.add(role);
		
		// add the Authenticated role
		if (getAuthenticatedUser() != null && getAuthenticatedUser().equals(user)) {
			roles.addAll(user.getAllRoles());
			Role authRole = Context.getUserService().getRole(
					OpenmrsConstants.AUTHENTICATED_ROLE);
			if (authRole == null) {
				throw new RuntimeException("Database out of sync with code: "
						+ OpenmrsConstants.AUTHENTICATED_ROLE + " role does not exist");
			}
			roles.add(authRole);
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

			Role authRole = Context.getUserService().getRole(
					OpenmrsConstants.AUTHENTICATED_ROLE);
			if (authRole == null) {
				throw new RuntimeException("Database out of sync with code: "
						+ OpenmrsConstants.AUTHENTICATED_ROLE + " role does not exist");
			}
			if (authRole.hasPrivilege(privilege))
				return true;
		}

		log.debug("Checking '" + privilege + "' against proxies: " + proxies);
		// check proxied privileges
		for (String s : proxies)
			if (s.equals(privilege))
				return true;
		
		// check anonymous privileges
		Role role = Context.getUserService().getRole(OpenmrsConstants.ANONYMOUS_ROLE);
		if (role == null) {
			throw new RuntimeException("Database out of sync with code: "
					+ OpenmrsConstants.ANONYMOUS_ROLE + " role does not exist");
		}
		if (role.hasPrivilege(privilege))
			return true;

		// default return value
		return false;
	}

}
