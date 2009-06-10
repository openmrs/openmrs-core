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
package org.openmrs.web.filter.update;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.Security;
import org.openmrs.web.Listener;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.StartupFilter;
import org.springframework.web.context.ContextLoader;

/**
 * This is the second filter that is processed. It is only active when OpenMRS has some liquibase
 * updates that need to be run. If updates are needed, this filter/wizard asks for a super user to
 * authenticate and review the updates before continuing.
 */
public class UpdateFilter extends StartupFilter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The velocity macro page to redirect to if an error occurs or on initial startup
	 */
	private final String DEFAULT_PAGE = "maintenance.vm";
	
	/**
	 * The page that lists off all the currently unexecuted changes
	 */
	private final String REVIEW_CHANGES = "reviewchanges.vm";
	
	/**
	 * The model object behind this set of screens
	 */
	private UpdateFilterModel model = null;
	
	/**
	 * Variable set as soon as the update is done or verified to not be needed so that future calls
	 * through this filter are a simple boolean check
	 */
	private static boolean updatesRequired = true;
	
	/**
	 * Used on all pages after the first to make sure the user isn't trying to cheat and do some url
	 * magic to hack in.
	 */
	private boolean authenticatedSuccessfully = false;
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 */
	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                      ServletException {
		
		Writer writer = httpResponse.getWriter();
		
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		
		// do step one of the wizard
		renderTemplate(DEFAULT_PAGE, referenceMap, writer);
	}
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on POST requests
	 * 
	 * @see org.openmrs.web.filter.StartupFilter#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                       ServletException {
		
		String page = httpRequest.getParameter("page");
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		Writer writer = httpResponse.getWriter();
		
		// step one
		if (DEFAULT_PAGE.equals(page)) {
			
			String username = httpRequest.getParameter("username");
			String password = httpRequest.getParameter("password");
			
			log.debug("Attempting to authenticate user: " + username);
			if (authenticateAsSuperUser(username, password)) {
				log.debug("Authentication successful.  Redirecting to 'reviewupdates' page.");
				// set a variable so we know that the user started here
				authenticatedSuccessfully = true;
				renderTemplate(REVIEW_CHANGES, referenceMap, writer);
				return;
			} else {
				// if not authenticated, show main page again
				try {
					log.debug("Sleeping for 3 seconds because of a bad username/password");
					Thread.sleep(3000);
				}
				catch (InterruptedException e) {
					log.error("Unable to sleep", e);
					throw new ServletException("Got interrupted while trying to sleep thread", e);
				}
				errors.add("Unable to authenticate as a " + OpenmrsConstants.SUPERUSER_ROLE
				        + ". Invalid username or password");
				renderTemplate(DEFAULT_PAGE, referenceMap, writer);
				return;
			}
			
		} // step two
		else if (REVIEW_CHANGES.equals(page)) {
			
			if (!authenticatedSuccessfully) {
				// throw the user back to the main page because they are cheating
				renderTemplate(DEFAULT_PAGE, referenceMap, writer);
			}
			
			try {
				DatabaseUpdater.executeChangelog();
			}
			catch (InputRequiredException inputRequired) {
				// the user would be stepped through the questions returned here.
				log.error("Not implemented", inputRequired);
				errors.add("Input during database updates is not yet implemented. " + inputRequired.getMessage());
				renderTemplate(REVIEW_CHANGES, referenceMap, writer);
				return;
			}
			catch (DatabaseUpdateException e) {
				log.error("Unable to update the database", e);
				errors.add("Unable to update the database.  See server error logs for the full stacktrace. "
				        + e.getMessage());
				renderTemplate(REVIEW_CHANGES, referenceMap, writer);
				return;
			}
			
		}
		
		try {
			startOpenmrs(filterConfig.getServletContext());
		}
		catch (Throwable t) {
			log.error("Unable to complete the startup.", t);
			errors.add("Unable to complete the startup.  See the server error log for the complete stacktrace."
			        + t.getMessage());
			renderTemplate(DEFAULT_PAGE, null, writer);
			return;
		}
		
		// set this so that the wizard isn't run again on next page load
		updatesRequired = false;
		httpResponse.sendRedirect("/" + WebConstants.WEBAPP_NAME);
	}
	
	/**
	 * Look in the users table for a user with this username and password and see if they have a
	 * role of {@link OpenmrsConstants#SUPERUSER_ROLE}.
	 * 
	 * @param usernameOrSystemId user entered username
	 * @param password user entered password
	 * @return true if this user has the super user role
	 * @see #isSuperUser(Connection, Integer)
	 * @should return false if given invalid credentials
	 * @should return false if given user is not superuser
	 * @should return true if given user is superuser
	 * @should not authorize voided superusers
	 * @should authenticate with systemId
	 */
	protected boolean authenticateAsSuperUser(String usernameOrSystemId, String password) throws ServletException {
		Connection connection = null;
		try {
			connection = DatabaseUpdater.getConnection();
			
			String select = "select user_id, password, salt from users where (username = ? or system_id = ?) and voided = 0";
			PreparedStatement statement = connection.prepareStatement(select);
			statement.setString(1, usernameOrSystemId);
			statement.setString(2, usernameOrSystemId);
			
			if (statement.execute()) {
				ResultSet results = statement.getResultSet();
				if (results.next()) {
					Integer userId = results.getInt(1);
					String storedPassword = results.getString(2);
					String salt = results.getString(3);
					String passwordToHash = password + salt;
					return Security.hashMatches(storedPassword, passwordToHash) && isSuperUser(connection, userId);
				}
			}
		}
		catch (Throwable t) {
			log.error("Error while trying to authenticate as super user", t);
		}
		finally {
			if (connection != null)
				try {
					connection.close();
				}
				catch (SQLException e) {
					log.debug("Error while closing the database", e);
				}
		}
		
		return false;
	}
	
	/**
	 * Checks the given user to see if they have been given the
	 * {@link OpenmrsConstants#SUPERUSER_ROLE} role. This method does not look at child roles.
	 * 
	 * @param connection the java sql connection to use
	 * @param userId the user id to look at
	 * @return true if the given user is a super user
	 * @should return true if given user has superuser role
	 * @should return false if given user does not have the super user role
	 */
	protected boolean isSuperUser(Connection connection, Integer userId) throws Exception {
		String select = "select 1 from user_role where user_id = ? and role = ?";
		PreparedStatement statement = connection.prepareStatement(select);
		statement.setInt(1, userId);
		statement.setString(2, OpenmrsConstants.SUPERUSER_ROLE);
		if (statement.execute()) {
			ResultSet results = statement.getResultSet();
			if (results.next()) {
				return results.getInt(1) == 1;
			}
		}
		
		return false;
	}
	
	/**
	 * Do everything to get openmrs going.
	 * 
	 * @param servletContext the servletContext from the filterconfig
	 * @see Listener#startOpenmrs(ServletContext)
	 */
	private void startOpenmrs(ServletContext servletContext) throws IOException, ServletException {
		// start spring
		// after this point, all errors need to also call: contextLoader.closeWebApplicationContext(event.getServletContext())
		// logic copied from org.springframework.web.context.ContextLoaderListener
		ContextLoader contextLoader = new ContextLoader();
		contextLoader.initWebApplicationContext(servletContext);
		
		try {
			Listener.startOpenmrs(servletContext);
		}
		catch (ServletException servletException) {
			contextLoader.closeWebApplicationContext(servletContext);
			throw servletException;
		}
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		
		log.debug("Initializing the UpdateFilter");
		
		model = new UpdateFilterModel();
		
		Properties properties = Listener.getRuntimeProperties();
		
		if (properties != null) {
			Context.setRuntimeProperties(properties);
			try {
				log.debug("Setting updates required to " + (model.changes.size() > 0)
				        + " because of the size of unrun changes");
				updatesRequired = model.changes.size() > 0;
			}
			catch (Exception e) {
				throw new ServletException("Unable to determine if updates are required", e);
			}
		} else {
			// the wizard runs the updates, so they will not need any updates.
			log.debug("Setting updates required to false because the user doesn't have any runtime properties yet");
			updatesRequired = false;
		}
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getModel()
	 */
	protected Object getModel() {
		// this object was initialized in the #init(FilterConfig) method
		return model;
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#skipFilter()
	 */
	public boolean skipFilter() {
		return !updatesRequired;
	}
	
	/**
	 * Used by the Listener to know if this filter wants to do its magic
	 * 
	 * @return true if updates have been determined to be required
	 * @see #init(FilterConfig)
	 * @see Listener#setupNeeded
	 */
	public static boolean updatesRequired() {
		return updatesRequired;
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getTemplatePrefix()
	 */
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/update/";
	}
	
}
