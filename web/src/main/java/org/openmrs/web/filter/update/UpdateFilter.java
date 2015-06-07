/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.update;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import liquibase.changelog.ChangeSet;
import liquibase.exception.LockException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.DatabaseUpdater.ChangeSetExecutorCallback;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.MemoryAppender;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.RoleConstants;
import org.openmrs.util.Security;
import org.openmrs.web.Listener;
import org.openmrs.web.WebDaemon;
import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.util.CustomResourceLoader;
import org.openmrs.web.filter.util.ErrorMessageConstants;
import org.openmrs.web.filter.util.FilterUtil;
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
	private static final String DEFAULT_PAGE = "maintenance.vm";
	
	/**
	 * The page that lists off all the currently unexecuted changes
	 */
	private static final String REVIEW_CHANGES = "reviewchanges.vm";
	
	private static final String PROGRESS_VM_AJAXREQUEST = "updateProgress.vm.ajaxRequest";
	
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
	
	private UpdateFilterCompletion updateJob;
	
	/**
	 * Variable set to true as soon as the update begins and set to false when the process ends.
	 * This thread should only be accesses through the synchronized method.
	 */
	private static boolean isDatabaseUpdateInProgress = false;
	
	/**
	 * Variable set to true when the db lock is released. It's needed to prevent repeatedly
	 * releasing this lock by other threads. This var should only be accessed through
	 * the synchronized method.
	 */
	private static Boolean lockReleased = false;
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 *
	 * @param httpRequest
	 * @param httpResponse
	 */
	@Override
	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	        ServletException {
		
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		checkLocaleAttributesForFirstTime(httpRequest);
		// we need to save current user language in references map since it will be used when template
		// will be rendered
		if (httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE) != null) {
			referenceMap
			        .put(FilterUtil.LOCALE_ATTRIBUTE, httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));
		}
		// do step one of the wizard
		renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse);
	}
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on POST requests
	 *
	 * @see org.openmrs.web.filter.StartupFilter#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected synchronized void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	        ServletException {
		
		String page = httpRequest.getParameter("page");
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		if (httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE) != null) {
			referenceMap
			        .put(FilterUtil.LOCALE_ATTRIBUTE, httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));
		}
		
		// step one
		if (DEFAULT_PAGE.equals(page)) {
			
			String username = httpRequest.getParameter("username");
			String password = httpRequest.getParameter("password");
			
			log.debug("Attempting to authenticate user: " + username);
			if (authenticateAsSuperUser(username, password)) {
				log.debug("Authentication successful.  Redirecting to 'reviewupdates' page.");
				// set a variable so we know that the user started here
				authenticatedSuccessfully = true;
				
				//Set variable to tell us whether updates are already in progress
				referenceMap.put("isDatabaseUpdateInProgress", isDatabaseUpdateInProgress);
				
				// if another super user has already launched database update
				// allow current super user to review update progress
				if (isDatabaseUpdateInProgress) {
					referenceMap.put("updateJobStarted", true);
					httpResponse.setContentType("text/html");
					renderTemplate(REVIEW_CHANGES, referenceMap, httpResponse);
					return;
				}
				
				// we will only get here if the db update is NOT running. 
				// so if we find a db lock, we should release it because
				// it was leftover from a previous db update crash
				
				if (!isLockReleased() && DatabaseUpdater.isLocked()) {
					// first we trying to release db lock if it exists
					try {
						DatabaseUpdater.releaseDatabaseLock();
						setLockReleased(true);
					}
					catch (LockException e) {
						// do nothing
					}
					// if lock was released successfully we need to get unrun changes
					model.updateChanges();
				}
				
				// need to configure velocity tool box for using user's preferred locale
				// so we should store it for further using when configuring velocity tool context
				String localeParameter = FilterUtil.restoreLocale(username);
				httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, localeParameter);
				referenceMap.put(FilterUtil.LOCALE_ATTRIBUTE, localeParameter);
				
				renderTemplate(REVIEW_CHANGES, referenceMap, httpResponse);
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
				errors.put(ErrorMessageConstants.UPDATE_ERROR_UNABLE_AUTHENTICATE, null);
				renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse);
			}
		}
		// step two of wizard in case if there were some warnings
		else if (REVIEW_CHANGES.equals(page)) {
			
			if (!authenticatedSuccessfully) {
				// throw the user back to the main page because they are cheating
				renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse);
				return;
			}
			
			//if no one has run any required updates
			if (!isDatabaseUpdateInProgress) {
				isDatabaseUpdateInProgress = true;
				updateJob = new UpdateFilterCompletion();
				updateJob.start();
				
				// allows current user see progress of running update
				// and also will hide the "Run Updates" button
				
				referenceMap.put("updateJobStarted", true);
			} else {
				referenceMap.put("isDatabaseUpdateInProgress", true);
				// as well we need to allow current user to
				// see progress of already started updates
				// and also will hide the "Run Updates" button
				referenceMap.put("updateJobStarted", true);
			}
			
			renderTemplate(REVIEW_CHANGES, referenceMap, httpResponse);
			
		} else if (PROGRESS_VM_AJAXREQUEST.equals(page)) {
			
			httpResponse.setContentType("text/json");
			httpResponse.setHeader("Cache-Control", "no-cache");
			Map<String, Object> result = new HashMap<String, Object>();
			if (updateJob != null) {
				result.put("hasErrors", updateJob.hasErrors());
				if (updateJob.hasErrors()) {
					errors.putAll(updateJob.getErrors());
				}
				
				if (updateJob.hasWarnings() && updateJob.getExecutingChangesetId() == null) {
					result.put("hasWarnings", updateJob.hasWarnings());
					StringBuilder sb = new StringBuilder("<ul>");
					
					for (String warning : updateJob.getUpdateWarnings()) {
						sb.append("<li>" + warning + "</li>");
					}
					
					sb.append("</ul>");
					result.put("updateWarnings", sb.toString());
					result.put("updateLogFile", StringUtils.replace(OpenmrsUtil.getApplicationDataDirectory()
					        + DatabaseUpdater.DATABASE_UPDATES_LOG_FILE, "\\", "\\\\"));
					updateJob.hasUpdateWarnings = false;
					updateJob.getUpdateWarnings().clear();
				}
				
				result.put("updatesRequired", updatesRequired());
				result.put("message", updateJob.getMessage());
				result.put("changesetIds", updateJob.getChangesetIds());
				result.put("executingChangesetId", updateJob.getExecutingChangesetId());
				Appender appender = Logger.getRootLogger().getAppender("MEMORY_APPENDER");
				if (appender instanceof MemoryAppender) {
					MemoryAppender memoryAppender = (MemoryAppender) appender;
					List<String> logLines = memoryAppender.getLogLines();
					// truncate the list to the last five so we don't overwhelm jquery
					if (logLines.size() > 5) {
						logLines = logLines.subList(logLines.size() - 5, logLines.size());
					}
					result.put("logLines", logLines);
				} else {
					result.put("logLines", new ArrayList<String>());
				}
			}
			
			String jsonText = toJSONString(result, true);
			httpResponse.getWriter().write(jsonText);
		}
	}
	
	/**
	 * It sets locale attribute for current session when user is making first GET http request
	 * to application. It retrieves user locale from request object and checks if this locale is
	 * supported by application. If not, it tries to load system default locale. If it's not specified it 
	 * uses {@link Locale#ENGLISH} by default
	 *
	 * @param httpRequest the http request object
	 */
	public void checkLocaleAttributesForFirstTime(HttpServletRequest httpRequest) {
		Locale locale = httpRequest.getLocale();
		String systemDefaultLocale = FilterUtil.readSystemDefaultLocale(null);
		if (CustomResourceLoader.getInstance(httpRequest).getAvailablelocales().contains(locale)) {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, locale.toString());
			log.info("Used client's locale " + locale.toString());
		} else if (StringUtils.isNotBlank(systemDefaultLocale)) {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, systemDefaultLocale);
			log.info("Used system default locale " + systemDefaultLocale);
		} else {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, Locale.ENGLISH.toString());
			log.info("Used default locale " + Locale.ENGLISH.toString());
		}
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
	 * @should not authorize retired superusers
	 * @should authenticate with systemId
	 */
	protected boolean authenticateAsSuperUser(String usernameOrSystemId, String password) throws ServletException {
		Connection connection = null;
		try {
			connection = DatabaseUpdater.getConnection();
			
			String select = "select user_id, password, salt from users where (username = ? or system_id = ?) and retired = '0'";
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(select);
				statement.setString(1, usernameOrSystemId);
				statement.setString(2, usernameOrSystemId);
				
				if (statement.execute()) {
					ResultSet results = null;
					try {
						results = statement.getResultSet();
						if (results.next()) {
							Integer userId = results.getInt(1);
							DatabaseUpdater.setAuthenticatedUserId(userId);
							String storedPassword = results.getString(2);
							String salt = results.getString(3);
							String passwordToHash = password + salt;
							boolean result = Security.hashMatches(storedPassword, passwordToHash)
							        && isSuperUser(connection, userId);
							return result;
						}
					}
					finally {
						if (results != null) {
							try {
								results.close();
							}
							catch (Exception resultsCloseEx) {
								log.error("Failed to quietly close ResultSet", resultsCloseEx);
							}
						}
					}
				}
			}
			finally {
				if (statement != null) {
					try {
						statement.close();
					}
					catch (Exception statementCloseEx) {
						log.error("Failed to quietly close Statement", statementCloseEx);
					}
				}
			}
		}
		catch (Exception connectionEx) {
			log
			        .error(
			            "Error while trying to authenticate as super user. Ignore this if you are upgrading from OpenMRS 1.5 to 1.6",
			            connectionEx);
			
			// we may not have upgraded User to have retired instead of voided yet, so if the query above fails, we try
			// again the old way
			if (connection != null) {
				String select = "select user_id, password, salt from users where (username = ? or system_id = ?) and voided = '0'";
				PreparedStatement statement = null;
				try {
					statement = connection.prepareStatement(select);
					statement.setString(1, usernameOrSystemId);
					statement.setString(2, usernameOrSystemId);
					if (statement.execute()) {
						ResultSet results = null;
						try {
							results = statement.getResultSet();
							if (results.next()) {
								Integer userId = results.getInt(1);
								DatabaseUpdater.setAuthenticatedUserId(userId);
								String storedPassword = results.getString(2);
								String salt = results.getString(3);
								String passwordToHash = password + salt;
								boolean result = Security.hashMatches(storedPassword, passwordToHash)
								        && isSuperUser(connection, userId);
								return result;
							}
						}
						finally {
							if (results != null) {
								try {
									results.close();
								}
								catch (Exception resultsCloseEx) {
									log.error("Failed to quietly close ResultSet", resultsCloseEx);
								}
							}
						}
					}
				}
				catch (Exception unhandeledEx) {
					log.error("Error while trying to authenticate as super user (voided version)", unhandeledEx);
				}
				finally {
					if (statement != null) {
						try {
							statement.close();
						}
						catch (Exception statementCloseEx) {
							log.error("Failed to quietly close Statement", statementCloseEx);
						}
					}
				}
			}
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (SQLException e) {
					log.debug("Error while closing the database", e);
				}
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
		// the 'Administrator' part of this string is necessary because if the database was upgraded
		// by OpenMRS 1.6 alpha then System Developer was renamed to that. This has to be here so we
		// can roll back that change in 1.6 beta+
		String select = "select 1 from user_role where user_id = ? and (role = ? or role = 'Administrator')";
		PreparedStatement statement = connection.prepareStatement(select);
		statement.setInt(1, userId);
		statement.setString(2, RoleConstants.SUPERUSER);
		if (statement.execute()) {
			ResultSet results = statement.getResultSet();
			if (results.next()) {
				return results.getInt(1) == 1;
			}
		}
		
		return false;
	}
	
	/**``
	 * Do everything to get openmrs going.
	 *
	 * @param servletContext the servletContext from the filterconfig
	 * @see Listener#startOpenmrs(ServletContext)
	 */
	private void startOpenmrs(ServletContext servletContext) throws Exception {
		// start spring
		// after this point, all errors need to also call: contextLoader.closeWebApplicationContext(event.getServletContext())
		// logic copied from org.springframework.web.context.ContextLoaderListener
		ContextLoader contextLoader = new ContextLoader();
		contextLoader.initWebApplicationContext(servletContext);
		
		try {
			WebDaemon.startOpenmrs(servletContext);
		}
		catch (Exception exception) {
			contextLoader.closeWebApplicationContext(servletContext);
			throw exception;
		}
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		
		log.debug("Initializing the UpdateFilter");
		
		if (!InitializationFilter.initializationRequired()) {
			model = new UpdateFilterModel();
			/*
			 * In this case, Listener#runtimePropertiesFound == true and InitializationFilter Wizard is skipped,
			 * so no need to reset Context's RuntimeProperties again, because of Listener.contextInitialized has set it.
			 */
			try {
				// this pings the DatabaseUpdater.updatesRequired which also
				// considers a db lock to be a 'required update'
				if (model.updateRequired) {
					setUpdatesRequired(true);
				} else if (model.changes == null) {
					setUpdatesRequired(false);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Setting updates required to " + (model.changes.size() > 0)
						        + " because of the size of unrun changes");
					}
					setUpdatesRequired(model.changes.size() > 0);
				}
			}
			catch (Exception e) {
				throw new ServletException("Unable to determine if updates are required", e);
			}
		} else {
			/*
			 * The initialization wizard will update the database to the latest version, so the user will not need any updates here.
			 * See end of InitializationFilter#InitializationCompletion
			 */
			log
			        .debug("Setting updates required to false because the user doesn't have any runtime properties yet or database is empty");
			setUpdatesRequired(false);
		}
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getModel()
	 */
	@Override
	protected Object getModel() {
		// this object was initialized in the #init(FilterConfig) method
		return model;
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#skipFilter()
	 */
	@Override
	public boolean skipFilter(HttpServletRequest httpRequest) {
		return !PROGRESS_VM_AJAXREQUEST.equals(httpRequest.getParameter("page")) && !updatesRequired();
	}
	
	/**
	 * Used by the Listener to know if this filter wants to do its magic
	 *
	 * @return true if updates have been determined to be required
	 * @see #init(FilterConfig)
	 * @see Listener#setupNeeded
	 */
	public static synchronized boolean updatesRequired() {
		return updatesRequired;
	}
	
	/**
	 * @param updatesRequired the updatesRequired to set
	 */
	protected static synchronized void setUpdatesRequired(boolean updatesRequired) {
		UpdateFilter.updatesRequired = updatesRequired;
	}
	
	/**
	 * Indicates if database lock was released. It will also used to prevent releasing
	 * existing lock of liquibasechangeloglock table by another user, when he also tries
	 * to run database update when another user is currently running it
	 */
	public static Boolean isLockReleased() {
		return lockReleased;
	}
	
	public static synchronized void setLockReleased(Boolean lockReleased) {
		UpdateFilter.lockReleased = lockReleased;
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getTemplatePrefix()
	 */
	@Override
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/update/";
	}
	
	/**
	 * This class controls the final steps and is used by the ajax calls to know what updates have
	 * been executed. TODO: Break this out into a separate (non-inner) class
	 */
	private class UpdateFilterCompletion {
		
		private Thread thread;
		
		private String executingChangesetId = null;
		
		private List<String> changesetIds = new ArrayList<String>();
		
		private Map<String, Object[]> errors = new HashMap<String, Object[]>();
		
		private String message = null;
		
		private boolean erroneous = false;
		
		private boolean hasUpdateWarnings = false;
		
		private List<String> updateWarnings = new LinkedList<String>();
		
		synchronized public void reportError(String error, Object... params) {
			Map<String, Object[]> errors = new HashMap<String, Object[]>();
			errors.put(error, params);
			reportErrors(errors);
		}
		
		synchronized public void reportErrors(Map<String, Object[]> errs) {
			errors.putAll(errs);
			erroneous = true;
		}
		
		synchronized public boolean hasErrors() {
			return erroneous;
		}
		
		synchronized public Map<String, Object[]> getErrors() {
			return errors;
		}
		
		/**
		 * Start the completion stage. This fires up the thread to do all the work.
		 */
		public void start() {
			setUpdatesRequired(true);
			thread.start();
		}
		
		@SuppressWarnings("unused")
		public void waitForCompletion() {
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				log.error("Error generated", e);
			}
		}
		
		synchronized public void setMessage(String message) {
			this.message = message;
		}
		
		synchronized public String getMessage() {
			return message;
		}
		
		synchronized public void addChangesetId(String changesetid) {
			this.changesetIds.add(changesetid);
			this.executingChangesetId = changesetid;
		}
		
		synchronized public List<String> getChangesetIds() {
			return changesetIds;
		}
		
		synchronized public String getExecutingChangesetId() {
			return executingChangesetId;
		}
		
		/**
		 * @return the database updater Warnings
		 */
		public synchronized List<String> getUpdateWarnings() {
			return updateWarnings;
		}
		
		synchronized public boolean hasWarnings() {
			return hasUpdateWarnings;
		}
		
		synchronized public void reportWarnings(List<String> warnings) {
			updateWarnings.addAll(warnings);
			hasUpdateWarnings = true;
		}
		
		/**
		 * This class does all the work of creating the desired database, user, updates, etc
		 */
		public UpdateFilterCompletion() {
			Runnable r = new Runnable() {
				
				/**
				 * TODO split this up into multiple testable methods
				 *
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					try {
						/**
						 * A callback class that prints out info about liquibase changesets
						 */
						class PrintingChangeSetExecutorCallback implements ChangeSetExecutorCallback {
							
							private String message;
							
							public PrintingChangeSetExecutorCallback(String message) {
								this.message = message;
							}
							
							/**
							 * @see org.openmrs.util.DatabaseUpdater.ChangeSetExecutorCallback#executing(liquibase.ChangeSet,
							 *      int)
							 */
							@Override
							public void executing(ChangeSet changeSet, int numChangeSetsToRun) {
								addChangesetId(changeSet.getId());
								setMessage(message);
							}
							
						}
						
						try {
							setMessage("Updating the database to the latest version");
							List<String> warnings = DatabaseUpdater.executeChangelog(null, null,
							    new PrintingChangeSetExecutorCallback("Updating database tables to latest version "));
							executingChangesetId = null; // clear out the last changeset
							
							if (CollectionUtils.isNotEmpty(warnings)) {
								reportWarnings(warnings);
								warnings = null;
							}
						}
						catch (InputRequiredException inputRequired) {
							// the user would be stepped through the questions returned here.
							log.error("Not implemented", inputRequired);
							model.updateChanges();
							reportError(ErrorMessageConstants.UPDATE_ERROR_INPUT_NOT_IMPLEMENTED, inputRequired.getMessage());
							return;
						}
						catch (DatabaseUpdateException e) {
							log.error("Unable to update the database", e);
							Map<String, Object[]> errors = new HashMap<String, Object[]>();
							errors.put(ErrorMessageConstants.UPDATE_ERROR_UNABLE, null);
							for (String message : Arrays.asList(e.getMessage().split("\n"))) {
								errors.put(message, null);
							}
							model.updateChanges();
							reportErrors(errors);
							return;
						}
						
						setMessage("Starting OpenMRS");
						try {
							startOpenmrs(filterConfig.getServletContext());
						}
						catch (Exception e) {
							log.error("Unable to complete the startup.", e);
							reportError(ErrorMessageConstants.UPDATE_ERROR_COMPLETE_STARTUP, e.getMessage());
							return;
						}
						
						// set this so that the wizard isn't run again on next page load
						setUpdatesRequired(false);
					}
					finally {
						if (!hasErrors()) {
							setUpdatesRequired(false);
						}
						//reset to let other user's make requests after updates are run
						isDatabaseUpdateInProgress = false;
					}
				}
			};
			
			thread = new Thread(r);
		}
	}
}
