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
package org.openmrs.web.filter.initialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import liquibase.ChangeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.openmrs.ImplementationId;
import org.openmrs.api.PasswordException;
import org.openmrs.api.context.Context;
import org.openmrs.module.MandatoryModuleException;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.MemoryAppender;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.DatabaseUpdater.ChangeSetExecutorCallback;
import org.openmrs.web.Listener;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.StartupFilter;
import org.springframework.web.context.ContextLoader;

/**
 * This is the first filter that is processed. It is only active when starting OpenMRS for the very
 * first time. It will redirect all requests to the {@link WebConstants#SETUP_PAGE_URL} if the
 * {@link Listener} wasn't able to find any runtime properties
 */
public class InitializationFilter extends StartupFilter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String LIQUIBASE_SCHEMA_DATA = "liquibase-schema-only.xml";
	
	private static final String LIQUIBASE_CORE_DATA = "liquibase-core-data.xml";
	
	private static final String LIQUIBASE_DEMO_DATA = "liquibase-demo-data.xml";
	
	/**
	 * The first page of the wizard that asks for a current or past database
	 */
	private final String DATABASE_SETUP = "databasesetup.vm";
	
	/**
	 * The velocity macro page to redirect to if an error occurs or on initial startup
	 */
	private final String DEFAULT_PAGE = DATABASE_SETUP;
	
	/**
	 * This page asks whether database tables/demo data should be inserted and what the
	 * username/password that will be put into the runtime properties is
	 */
	private final String DATABASE_TABLES_AND_USER = "databasetablesanduser.vm";
	
	/**
	 * This page lets the user define the admin user
	 */
	private static final String ADMIN_USER_SETUP = "adminusersetup.vm";
	
	/**
	 * This page lets the user pick an implementation id
	 */
	private static final String IMPLEMENTATION_ID_SETUP = "implementationidsetup.vm";
	
	/**
	 * This page asks for settings that will be put into the runtime properties files
	 */
	private final String OTHER_RUNTIME_PROPS = "otherruntimeproperties.vm";
	
	/**
	 * A page that tells the user that everything is collected and will now be processed
	 */
	private static final String WIZARD_COMPLETE = "wizardcomplete.vm";
	
	/**
	 * A page that lists off what is happening while it is going on. This page has ajax that callst
	 * he {@value #PROGRESS_VM_AJAXREQUEST} page
	 */
	private static final String PROGRESS_VM = "progress.vm";
	
	/**
	 * This url is called by javascript to get the status of the install
	 */
	private static final String PROGRESS_VM_AJAXREQUEST = "progress.vm.ajaxRequest";
	
	/**
	 * The model object that holds all the properties that the rendered templates use. All
	 * attributes on this object are made available to all templates via reflection in the
	 * {@link #renderTemplate(String, Map, httpResponse)} method.
	 */
	private InitializationWizardModel wizardModel = null;
	
	private InitializationCompletion initJob;
	
	/**
	 * Variable set at the end of the wizard when spring is being restarted
	 */
	private static boolean initializationComplete = false;
	
	synchronized protected void setInitializationComplete(boolean initializationComplete) {
		InitializationFilter.initializationComplete = initializationComplete;
	}
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 */
	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                      ServletException {
		
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		
		File runtimeProperties = getRuntimePropertiesFile();
		
		if (!runtimeProperties.exists()) {
			try {
				runtimeProperties.createNewFile();
				// reset the error objects in case of refresh
				wizardModel.canCreate = true;
				wizardModel.cannotCreateErrorMessage = "";
			}
			catch (IOException io) {
				wizardModel.canCreate = false;
				wizardModel.cannotCreateErrorMessage = io.getMessage();
			}
			
			// check this before deleting the file again
			wizardModel.canWrite = runtimeProperties.canWrite();
			
			// delete the file again after testing the create/write
			// so that if the user stops the webapp before finishing
			// this wizard, they can still get back into it
			runtimeProperties.delete();
			
		} else {
			wizardModel.canWrite = runtimeProperties.canWrite();
		}
		
		wizardModel.runtimePropertiesPath = runtimeProperties.getAbsolutePath();
		
		// do step one of the wizard
		httpResponse.setContentType("text/html");
		renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse);
	}
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on POST requests
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 */
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                       ServletException {
		
		String page = httpRequest.getParameter("page");
		Map<String, Object> referenceMap = new HashMap<String, Object>();
		
		// TODO make these page names variables.
		// step one
		if (DATABASE_SETUP.equals(page)) {
			
			wizardModel.databaseConnection = httpRequest.getParameter("database_connection");
			checkForEmptyValue(wizardModel.databaseConnection, errors, "Database connection string");
			
			//TODO make each bit of page logic a (unit testable) method
			
			// asked the user for their desired database name
			
			if ("yes".equals(httpRequest.getParameter("current_openmrs_database"))) {
				wizardModel.databaseName = httpRequest.getParameter("openmrs_current_database_name");
				checkForEmptyValue(wizardModel.databaseName, errors, "Current database name");
				wizardModel.hasCurrentOpenmrsDatabase = true;
				// TODO check to see if this is an active database
				
			} else {
				// mark this wizard as a "to create database" (done at the end)
				wizardModel.hasCurrentOpenmrsDatabase = false;
				
				wizardModel.createTables = true;
				
				wizardModel.databaseName = httpRequest.getParameter("openmrs_new_database_name");
				checkForEmptyValue(wizardModel.databaseName, errors, "New database name");
				// TODO create database now to check if its possible?
				
				wizardModel.createDatabaseUsername = httpRequest.getParameter("create_database_username");
				checkForEmptyValue(wizardModel.createDatabaseUsername, errors,
				    "A user that has 'CREATE DATABASE' privileges");
				wizardModel.createDatabasePassword = httpRequest.getParameter("create_database_password");
				checkForEmptyValue(wizardModel.createDatabasePassword, errors,
				    "Password for user with 'CREATE DATABASE' privileges");
			}
			
			if (errors.isEmpty()) {
				page = DATABASE_TABLES_AND_USER;
			}
			
			renderTemplate(page, referenceMap, httpResponse);
			
		} // step two
		else if (DATABASE_TABLES_AND_USER.equals(page)) {
			
			if ("Back".equals(httpRequest.getParameter("back"))) {
				renderTemplate(DATABASE_SETUP, referenceMap, httpResponse);
				return;
			}
			
			if (wizardModel.hasCurrentOpenmrsDatabase) {
				wizardModel.createTables = "yes".equals(httpRequest.getParameter("create_tables"));
			}
			
			wizardModel.addDemoData = "yes".equals(httpRequest.getParameter("add_demo_data"));
			
			if ("yes".equals(httpRequest.getParameter("current_database_user"))) {
				wizardModel.currentDatabaseUsername = httpRequest.getParameter("current_database_username");
				checkForEmptyValue(wizardModel.currentDatabaseUsername, errors, "Curent user account");
				wizardModel.currentDatabasePassword = httpRequest.getParameter("current_database_password");
				checkForEmptyValue(wizardModel.currentDatabasePassword, errors, "Current user account password");
				wizardModel.hasCurrentDatabaseUser = true;
				wizardModel.createDatabaseUser = false;
			} else {
				wizardModel.hasCurrentDatabaseUser = false;
				wizardModel.createDatabaseUser = true;
				// asked for the root mysql username/password 
				wizardModel.createUserUsername = httpRequest.getParameter("create_user_username");
				checkForEmptyValue(wizardModel.createUserUsername, errors, "A user that has 'CREATE USER' privileges");
				wizardModel.createUserPassword = httpRequest.getParameter("create_user_password");
				checkForEmptyValue(wizardModel.createUserPassword, errors,
				    "Password for user that has 'CREATE USER' privileges");
			}
			
			if (errors.isEmpty()) { // go to next page
				page = OTHER_RUNTIME_PROPS;
			}
			
			renderTemplate(page, referenceMap, httpResponse);
		} // step three
		else if (OTHER_RUNTIME_PROPS.equals(page)) {
			
			if ("Back".equals(httpRequest.getParameter("back"))) {
				renderTemplate(DATABASE_TABLES_AND_USER, referenceMap, httpResponse);
				return;
			}
			
			wizardModel.moduleWebAdmin = "yes".equals(httpRequest.getParameter("module_web_admin"));
			wizardModel.autoUpdateDatabase = "yes".equals(httpRequest.getParameter("auto_update_database"));
			
			if (wizardModel.createTables) { // go to next page if they are creating tables
				page = ADMIN_USER_SETUP;
			} else { // skip a page
				page = IMPLEMENTATION_ID_SETUP;
			}
			
			renderTemplate(page, referenceMap, httpResponse);
			
		} // optional step four
		else if (ADMIN_USER_SETUP.equals(page)) {
			
			if ("Back".equals(httpRequest.getParameter("back"))) {
				renderTemplate(OTHER_RUNTIME_PROPS, referenceMap, httpResponse);
				return;
			}
			
			wizardModel.adminUserPassword = httpRequest.getParameter("new_admin_password");
			String adminUserConfirm = httpRequest.getParameter("new_admin_password_confirm");
			
			// throw back to admin user if passwords don't match
			if (!wizardModel.adminUserPassword.equals(adminUserConfirm)) {
				errors.add("Admin passwords don't match");
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse);
				return;
			}
			
			// throw back if the user didn't put in a password
			if (wizardModel.adminUserPassword.equals("")) {
				errors.add("An admin password is required");
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse);
				return;
			}
			
			try {
				OpenmrsUtil.validatePassword("admin", wizardModel.adminUserPassword, "admin");
			}
			catch (PasswordException p) {
				errors
				        .add("The password is not long enough, does not contain both uppercase characters and a number, or matches the username.");
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse);
				return;
			}
			
			if (errors.isEmpty()) { // go to next page
				page = IMPLEMENTATION_ID_SETUP;
			}
			
			renderTemplate(page, referenceMap, httpResponse);
			
		} // optional step five 
		else if (IMPLEMENTATION_ID_SETUP.equals(page)) {
			
			if ("Back".equals(httpRequest.getParameter("back"))) {
				if (wizardModel.createTables)
					renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse);
				else
					renderTemplate(OTHER_RUNTIME_PROPS, referenceMap, httpResponse);
				return;
			}
			
			wizardModel.implementationIdName = httpRequest.getParameter("implementation_name");
			wizardModel.implementationId = httpRequest.getParameter("implementation_id");
			wizardModel.implementationIdPassPhrase = httpRequest.getParameter("pass_phrase");
			wizardModel.implementationIdDescription = httpRequest.getParameter("description");
			
			// throw back if the user-specified ID is invalid (contains ^ or |).
			if (wizardModel.implementationId.indexOf('^') != -1 || wizardModel.implementationId.indexOf('|') != -1) {
				errors.add("Implementation ID cannot contain '^' or '|'");
				renderTemplate(IMPLEMENTATION_ID_SETUP, referenceMap, httpResponse);
				return;
			}
			
			if (errors.isEmpty()) { // go to next page
				page = WIZARD_COMPLETE;
			}
			
			renderTemplate(page, referenceMap, httpResponse);
		} else if (WIZARD_COMPLETE.equals(page)) {
			
			if ("Back".equals(httpRequest.getParameter("back"))) {
				renderTemplate(IMPLEMENTATION_ID_SETUP, referenceMap, httpResponse);
				return;
			}
			
			// TODO send user to confirmation page with results of wizard, location of runtime props, before starting install?
			
			initJob = new InitializationCompletion();
			initJob.start();
			renderTemplate(PROGRESS_VM, referenceMap, httpResponse);
		} else if (PROGRESS_VM_AJAXREQUEST.equals(page)) {
			httpResponse.setContentType("text/json");
			httpResponse.setHeader("Cache-Control", "no-cache");
			Map<String, Object> result = new HashMap<String, Object>();
			if (initJob != null) {
				result.put("hasErrors", initJob.hasErrors());
				if (initJob.hasErrors()) {
					result.put("errorPage", initJob.getErrorPage());
					errors.addAll(initJob.getErrors());
				}
				
				result.put("initializationComplete", isInitializationComplete());
				result.put("message", initJob.getMessage());
				result.put("actionCounter", initJob.getStepsComplete());
				Appender appender = Logger.getRootLogger().getAppender("MEMORY_APPENDER");
				if (appender instanceof MemoryAppender) {
					MemoryAppender memoryAppender = (MemoryAppender) appender;
					result.put("logLines", memoryAppender.getLogLines());
				} else {
					result.put("logLines", new ArrayList<String>());
				}
			}
			
			httpResponse.getWriter().write(toJSONString(result));
		}
	}
	
	/**
	 * Verify the database connection works.
	 * 
	 * @param connectionUsername
	 * @param connectionPassword
	 * @param databaseConnectionFinalUrl
	 * @return true/false whether it was verified or not
	 */
	private boolean verifyConnection(String connectionUsername, String connectionPassword, String databaseConnectionFinalUrl) {
		try {
			// verify connection
			// TODO how to get the driver for the other dbs...
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			DriverManager.getConnection(databaseConnectionFinalUrl, connectionUsername, connectionPassword);
			return true;
			
		}
		catch (Exception e) {
			errors.add("User account " + connectionUsername + " does not work. " + e.getMessage()
			        + " See the error log for more details"); // TODO internationalize this
			log.warn("Error while checking the connection user account", e);
			return false;
		}
	}
	
	/**
	 * Convenience method to load the runtime properties in the application data directory
	 * 
	 * @return
	 */
	private File getRuntimePropertiesFile() {
		String filename = WebConstants.WEBAPP_NAME + "-runtime.properties";
		
		File file = new File(OpenmrsUtil.getApplicationDataDirectory(), filename);
		
		log.debug("Using file: " + file.getAbsolutePath());
		
		return file;
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getTemplatePrefix()
	 */
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/initialization/";
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#getModel()
	 */
	protected Object getModel() {
		return wizardModel;
	}
	
	/**
	 * @see org.openmrs.web.filter.StartupFilter#skipFilter()
	 */
	public boolean skipFilter(HttpServletRequest httpRequest) {
		// If progress.vm makes an ajax request even immediately after initialization has completed
		// let the request pass in order to let progress.vm load the start page of OpenMRS
		// (otherwise progress.vm is displayed "forever")
		return !PROGRESS_VM_AJAXREQUEST.equals(httpRequest.getParameter("page")) && !initializationRequired();
	}
	
	/**
	 * Public method that returns true if database+runtime properties initialization is required
	 * 
	 * @return true if this initialization wizard needs to run
	 */
	public static boolean initializationRequired() {
		return isInitializationComplete() == false && Listener.runtimePropertiesFound() == false;
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		wizardModel = new InitializationWizardModel();
	}
	
	/**
	 * @param silent if this statement fails do not display stack trace or record an error in the
	 *            wizard object.
	 * @param user username to connect with
	 * @param pw password to connect with
	 * @param sql String containing sql and question marks
	 * @param args the strings to fill into the question marks in the given sql
	 * @return result of executeUpdate or -1 for error
	 */
	private int executeStatement(boolean silent, String user, String pw, String sql, String... args) {
		
		Connection connection = null;
		try {
			String replacedSql = sql;
			
			// TODO how to get the driver for the other dbs...
			if (wizardModel.databaseConnection.contains("mysql")) {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} else {
				replacedSql = replacedSql.replaceAll("`", "\"");
			}
			
			String tempDatabaseConnection = "";
			if (sql.contains("create database")) {
				tempDatabaseConnection = wizardModel.databaseConnection.replace("@DBNAME@", ""); // make this dbname agnostic so we can create the db
			} else {
				tempDatabaseConnection = wizardModel.databaseConnection.replace("@DBNAME@", wizardModel.databaseName);
			}
			
			connection = DriverManager.getConnection(tempDatabaseConnection, user, pw);
			
			for (String arg : args) {
				arg = arg.replace(";", "&#094"); // to prevent any sql injection
				replacedSql = replacedSql.replaceFirst("\\?", arg);
			}
			
			// run the sql statement
			Statement statement = connection.createStatement();
			return statement.executeUpdate(replacedSql);
			
		}
		catch (SQLException sqlex) {
			if (!silent) {
				// log and add error
				log.warn("error executing sql: " + sql, sqlex);
				errors.add("Error executing sql: " + sql + " - " + sqlex.getMessage());
			}
		}
		catch (InstantiationException e) {
			log.error("Error generated", e);
		}
		catch (IllegalAccessException e) {
			log.error("Error generated", e);
		}
		catch (ClassNotFoundException e) {
			log.error("Error generated", e);
		}
		finally {
			try {
				if (connection != null) {
					connection.close();
				}
			}
			catch (Throwable t) {
				log.warn("Error while closing connection", t);
			}
		}
		
		return -1;
	}
	
	/**
	 * Convenience variable to know if this wizard has completed successfully and that this wizard
	 * does not need to be executed again
	 * 
	 * @return true if this has been run already
	 */
	synchronized private static boolean isInitializationComplete() {
		return initializationComplete;
	}
	
	/**
	 * Check if the given value is null or a zero-length String
	 * 
	 * @param value the string to check
	 * @param errors the list of errors to append the errorMessage to if value is empty
	 * @param errorMessage the string error message to append if value is empty
	 * @return true if the value is non-empty
	 */
	private boolean checkForEmptyValue(String value, List<String> errors, String errorMessage) {
		if (value != null && !value.equals("")) {
			return true;
		}
		errors.add(errorMessage + " required.");
		return false;
	}
	
	/**
	 * Separate thread that will run through all tasks to complete the initialization. The database
	 * is created, user's created, etc here
	 */
	private class InitializationCompletion {
		
		private Thread thread;
		
		private int steps = 0;
		
		private String message = "";
		
		private List<String> errors = new ArrayList<String>();
		
		private String errorPage = null;
		
		private boolean erroneous = false;
		
		synchronized public void reportError(String error, String errorPage) {
			errors.add(error);
			this.errorPage = errorPage;
			erroneous = true;
		}
		
		synchronized public boolean hasErrors() {
			return erroneous;
		}
		
		synchronized public String getErrorPage() {
			return errorPage;
		}
		
		synchronized public List<String> getErrors() {
			return errors;
		}
		
		/**
		 * Start the completion stage. This fires up the thread to do all the work.
		 */
		public void start() {
			setStepsComplete(0);
			setInitializationComplete(false);
			thread.start();
		}
		
		public void waitForCompletion() {
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("Error generated", e);
			}
		}
		
		synchronized protected void setStepsComplete(int steps) {
			this.steps = steps;
		}
		
		synchronized protected int getStepsComplete() {
			return steps;
		}
		
		synchronized public String getMessage() {
			return message;
		}
		
		synchronized public void setMessage(String message) {
			this.message = message;
			setStepsComplete(getStepsComplete() + 1);
		}
		
		/**
		 * This class does all the work of creating the desired database, user, updates, etc
		 */
		public InitializationCompletion() {
			Runnable r = new Runnable() {
				
				/**
				 * TODO split this up into multiple testable methods
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					try {
						String connectionUsername;
						String connectionPassword;
						
						if (!wizardModel.hasCurrentOpenmrsDatabase) {
							setMessage("Create database");
							// connect via jdbc and create a database
							String sql = "create database `?` default character set utf8";
							int result = executeStatement(false, wizardModel.createDatabaseUsername,
							    wizardModel.createDatabasePassword, sql, wizardModel.databaseName);
							// throw the user back to the main screen if this error occurs
							if (result < 0) {
								reportError("Unable to create the database", DEFAULT_PAGE);
								return;
							} else {
								wizardModel.workLog.add("Created database " + wizardModel.databaseName);
							}
						}
						
						if (wizardModel.createDatabaseUser) {
							setMessage("Create database user");
							connectionUsername = wizardModel.databaseName + "_user";
							if (connectionUsername.length() > 16)
								connectionUsername = wizardModel.databaseName.substring(0, 11) + "_user"; // trim off enough to leave space for _user at the end
								
							connectionPassword = "";
							// generate random password from this subset of alphabet
							// intentionally left out these characters: ufsb$() to prevent certain words forming randomly
							String chars = "acdeghijklmnopqrtvwxyzACDEGHIJKLMNOPQRTVWXYZ0123456789.|~@#^&";
							Random r = new Random();
							for (int x = 0; x < 12; x++) {
								connectionPassword += chars.charAt(r.nextInt(chars.length()));
							}
							
							// connect via jdbc with root user and create an openmrs user
							String sql = "drop user '?'@'localhost'";
							executeStatement(true, wizardModel.createUserUsername, wizardModel.createUserPassword, sql,
							    connectionUsername);
							sql = "create user '?'@'localhost' identified by '?'";
							if (-1 != executeStatement(false, wizardModel.createUserUsername,
							    wizardModel.createUserPassword, sql, connectionUsername, connectionPassword)) {
								wizardModel.workLog.add("Created user " + connectionUsername);
							} else {
								// if error occurs stop
								reportError("Unable to create a database user", DEFAULT_PAGE);
								return;
							}
							
							// grant the roles
							sql = "GRANT ALL ON `?`.* TO '?'@'localhost'";
							int result = executeStatement(false, wizardModel.createUserUsername,
							    wizardModel.createUserPassword, sql, wizardModel.databaseName, connectionUsername);
							// throw the user back to the main screen if this error occurs
							if (result < 0) {
								reportError("Unable to grant privileges on openmrs database to user", DEFAULT_PAGE);
								return;
							} else {
								wizardModel.workLog.add("Granted user " + connectionUsername
								        + " all privileges to database " + wizardModel.databaseName);
							}
							
						} else {
							connectionUsername = wizardModel.currentDatabaseUsername;
							connectionPassword = wizardModel.currentDatabasePassword;
						}
						
						String finalDatabaseConnectionString = wizardModel.databaseConnection.replace("@DBNAME@",
						    wizardModel.databaseName);
						
						// verify that the database connection works
						if (!verifyConnection(connectionUsername, connectionPassword, finalDatabaseConnectionString)) {
							setMessage("Verify that the database connection works");
							// redirect to setup page if we got an error
							reportError("Unable to connect to database", DEFAULT_PAGE);
							return;
						}
						
						// save the properties for startup purposes
						Properties runtimeProperties = new Properties();
						
						runtimeProperties.put("connection.url", finalDatabaseConnectionString);
						runtimeProperties.put("connection.username", connectionUsername);
						runtimeProperties.put("connection.password", connectionPassword);
						runtimeProperties.put("module.allow_web_admin", wizardModel.moduleWebAdmin.toString());
						runtimeProperties.put("auto_update_database", wizardModel.autoUpdateDatabase.toString());
						runtimeProperties.put(SchedulerConstants.SCHEDULER_USERNAME_PROPERTY, "admin");
						runtimeProperties.put(SchedulerConstants.SCHEDULER_PASSWORD_PROPERTY, wizardModel.adminUserPassword);
						
						Context.setRuntimeProperties(runtimeProperties);
						
						/**
						 * A callback class that prints out info about liquibase changesets
						 */
						class PrintingChangeSetExecutorCallback implements ChangeSetExecutorCallback {
							
							private int i = 1;
							
							private String message;
							
							public PrintingChangeSetExecutorCallback(String message) {
								this.message = message;
							}
							
							/**
							 * @see org.openmrs.util.DatabaseUpdater.ChangeSetExecutorCallback#executing(liquibase.ChangeSet,
							 *      int)
							 */
							public void executing(ChangeSet changeSet, int numChangeSetsToRun) {
								setMessage(message + " (" + i++ + "/" + numChangeSetsToRun + "): Author: "
								        + changeSet.getAuthor() + " Comments: " + changeSet.getComments() + " Description: "
								        + changeSet.getDescription());
							}
							
						}
						
						if (wizardModel.createTables) {
							// use liquibase to create core data + tables
							try {
								setMessage("Executing " + LIQUIBASE_SCHEMA_DATA);
								DatabaseUpdater.executeChangelog(LIQUIBASE_SCHEMA_DATA, null,
								    new PrintingChangeSetExecutorCallback("OpenMRS schema file"));
								DatabaseUpdater.executeChangelog(LIQUIBASE_CORE_DATA, null,
								    new PrintingChangeSetExecutorCallback("OpenMRS core data file"));
								wizardModel.workLog.add("Created database tables and added core data");
							}
							catch (Exception e) {
								reportError(e.getMessage() + " See the error log for more details", null);
								log.warn("Error while trying to create tables and demo data", e);
							}
						}
						
						// add demo data only if creating tables fresh and user selected the option add demo data
						if (wizardModel.createTables && wizardModel.addDemoData) {
							try {
								setMessage("Adding demo data");
								DatabaseUpdater.executeChangelog(LIQUIBASE_DEMO_DATA, null,
								    new PrintingChangeSetExecutorCallback("OpenMRS demo patients, users, and forms"));
								wizardModel.workLog.add("Added demo data");
							}
							catch (Exception e) {
								reportError(e.getMessage() + " See the error log for more details", null);
								log.warn("Error while trying to add demo data", e);
							}
						}
						
						// update the database to the latest version
						try {
							setMessage("Updating the database to the latest version");
							DatabaseUpdater.executeChangelog(null, null, new PrintingChangeSetExecutorCallback(
							        "Updating database tables to latest version "));
						}
						catch (Exception e) {
							reportError(e.getMessage() + " Error while trying to update to the latest database version",
							    DEFAULT_PAGE);
							log.warn("Error while trying to update to the latest database version", e);
							return;
						}
						
						setMessage("Starting OpenMRS");
						
						// start spring
						// after this point, all errors need to also call: contextLoader.closeWebApplicationContext(event.getServletContext())
						// logic copied from org.springframework.web.context.ContextLoaderListener
						ContextLoader contextLoader = new ContextLoader();
						contextLoader.initWebApplicationContext(filterConfig.getServletContext());
						
						// start openmrs
						try {
							Context.openSession();
							Context.startup(runtimeProperties);
						}
						catch (DatabaseUpdateException updateEx) {
							log.warn("Error while running the database update file", updateEx);
							reportError(
							    updateEx.getMessage() + " There was an error while running the database update file: "
							            + updateEx.getMessage(), DEFAULT_PAGE);
							return;
						}
						catch (InputRequiredException inputRequiredEx) {
							// TODO display a page looping over the required input and ask the user for each.  
							// 		When done and the user and put in their say, call DatabaseUpdater.update(Map); 
							//		with the user's question/answer pairs
							log
							        .warn("Unable to continue because user input is required for the db updates and we cannot do anything about that right now");
							reportError(
							    "Unable to continue because user input is required for the db updates and we cannot do anything about that right now",
							    DEFAULT_PAGE);
							return;
						}
						catch (MandatoryModuleException mandatoryModEx) {
							log.warn(
							    "A mandatory module failed to start. Fix the error or unmark it as mandatory to continue.",
							    mandatoryModEx);
							reportError(mandatoryModEx.getMessage(), DEFAULT_PAGE);
							return;
						}
						
						// TODO catch openmrs errors here and drop the user back out to the setup screen
						
						if (!wizardModel.implementationId.equals("")) {
							try {
								Context.addProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_GLOBAL_PROPERTIES);
								Context.addProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_SOURCES);
								Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_CONCEPT_SOURCES);
								Context.addProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_IMPLEMENTATION_ID);
								
								ImplementationId implId = new ImplementationId();
								implId.setName(wizardModel.implementationIdName);
								implId.setImplementationId(wizardModel.implementationId);
								implId.setPassphrase(wizardModel.implementationIdPassPhrase);
								implId.setDescription(wizardModel.implementationIdDescription);
								
								Context.getAdministrationService().setImplementationId(implId);
							}
							catch (Throwable t) {
								reportError(t.getMessage() + " Implementation ID could not be set.", DEFAULT_PAGE);
								log.warn("Implementation ID could not be set.", t);
								Context.shutdown();
								WebModuleUtil.shutdownModules(filterConfig.getServletContext());
								contextLoader.closeWebApplicationContext(filterConfig.getServletContext());
								return;
							}
							finally {
								Context.removeProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_GLOBAL_PROPERTIES);
								Context.removeProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_CONCEPT_SOURCES);
								Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_CONCEPT_SOURCES);
								Context.removeProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_IMPLEMENTATION_ID);
							}
						}
						
						try {
							// change the admin user password from "test" to what they input above
							if (wizardModel.createTables) {
								Context.authenticate("admin", "test");
								Context.getUserService().changePassword("test", wizardModel.adminUserPassword);
								Context.logout();
							}
							
							// load modules
							Listener.loadAndStartCoreModules(filterConfig.getServletContext());
							
							// web load modules
							Listener.performWebStartOfModules(filterConfig.getServletContext());
							
							// start the scheduled tasks
							SchedulerUtil.startup(runtimeProperties);
						}
						catch (Throwable t) {
							Context.shutdown();
							WebModuleUtil.shutdownModules(filterConfig.getServletContext());
							contextLoader.closeWebApplicationContext(filterConfig.getServletContext());
							reportError(t.getMessage() + " Unable to complete the startup.", DEFAULT_PAGE);
							log.warn("Unable to complete the startup.", t);
							return;
						}
						
						// output properties to the openmrs runtime properties file so that this wizard is not run again
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(getRuntimePropertiesFile());
							OpenmrsUtil.storeProperties(runtimeProperties, fos,
							    "Auto generated by OpenMRS initialization wizard");
							wizardModel.workLog.add("Saved runtime properties file " + getRuntimePropertiesFile());
							
							// don't need to catch errors here because we tested it at the beginning of the wizard
						}
						finally {
							if (fos != null) {
								fos.close();
							}
						}
						
						// set this so that the wizard isn't run again on next page load
						Context.closeSession();
					}
					catch (IOException e) {
						reportError(e.getMessage() + " Unable to complete the startup.", DEFAULT_PAGE);
					}
					finally {
						if (!hasErrors()) {
							setInitializationComplete(true);
						}
					}
				}
			};
			
			thread = new Thread(r);
		}
	}
}
