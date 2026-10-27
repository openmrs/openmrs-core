/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.openmrs.ImplementationId;
import org.openmrs.api.PasswordException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.liquibase.ChangeLogDetective;
import org.openmrs.liquibase.ChangeLogVersionFinder;
import org.openmrs.liquibase.ChangeSetExecutorCallback;
import org.openmrs.module.MandatoryModuleException;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.DatabaseUpdaterLiquibaseProvider;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.InputRequiredException;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsThreadPoolHolder;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.Security;
import org.openmrs.web.Listener;
import org.openmrs.web.WebConstants;
import org.openmrs.web.WebDaemon;
import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.openmrs.web.filter.util.CustomResourceLoader;
import org.openmrs.web.filter.util.ErrorMessageConstants;
import org.openmrs.web.filter.util.FilterUtil;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import liquibase.changelog.ChangeSet;

import static org.openmrs.util.PrivilegeConstants.GET_GLOBAL_PROPERTIES;
import static org.openmrs.web.filter.initialization.InitializationWizardModel.DEFAULT_MYSQL_CONNECTION;
import static org.openmrs.web.filter.initialization.InitializationWizardModel.DEFAULT_POSTGRESQL_CONNECTION;

/**
 * This is the first filter that is processed. It is only active when starting OpenMRS for the very
 * first time. It will redirect all requests to the {@link WebConstants#SETUP_PAGE_URL} if the
 * {@link Listener} wasn't able to find any runtime properties
 */
public class InitializationFilter extends StartupFilter {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(InitializationFilter.class);

	private static final String DATABASE_POSTGRESQL = "postgresql";

	private static final String DATABASE_MYSQL = "mysql";

	private static final String DATABASE_SQLSERVER = "sqlserver";

	private static final String DATABASE_H2 = "h2";

	private static final String DATABASE_MARIADB = "mariadb";

	/**
	 * The very first page of wizard, that asks user for select his preferred language
	 */
	private static final String CHOOSE_LANG = "chooselang.vm";

	/**
	 * The second page of the wizard that asks for simple or advanced installation.
	 */
	private static final String INSTALL_METHOD = "installmethod.vm";

	/**
	 * The simple installation setup page.
	 */
	private static final String SIMPLE_SETUP = "simplesetup.vm";

	/**
	 * The first page of the advanced installation of the wizard that asks for a current or past
	 * database
	 */
	private static final String DATABASE_SETUP = "databasesetup.vm";

	/**
	 * The page from where the user specifies the url to a remote system, username and password
	 */
	private static final String TESTING_REMOTE_DETAILS_SETUP = "remotedetails.vm";

	/**
	 * The velocity macro page to redirect to if an error occurs or on initial startup
	 */
	private static final String DEFAULT_PAGE = CHOOSE_LANG;

	/**
	 * This page asks whether database tables/demo data should be inserted and what the
	 * username/password that will be put into the runtime properties is
	 */
	private static final String DATABASE_TABLES_AND_USER = "databasetablesanduser.vm";

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
	private static final String OTHER_RUNTIME_PROPS = "otherruntimeproperties.vm";

	/**
	 * A page that tells the user that everything is collected and will now be processed
	 */
	private static final String WIZARD_COMPLETE = "wizardcomplete.vm";

	/**
	 * A page that lists off what is happening while it is going on. This page has ajax that callst he
	 * {@value #PROGRESS_VM_AJAXREQUEST} page
	 */
	private static final String PROGRESS_VM = "progress.vm";

	/**
	 * This url is called by javascript to get the status of the install
	 */
	private static final String PROGRESS_VM_AJAXREQUEST = "progress.vm.ajaxRequest";

	public static final String RELEASE_TESTING_MODULE_PATH = "/module/releasetestinghelper/";

	/**
	 * The session attribute holding the wizard model of the client the session belongs to
	 */
	static final String WIZARD_MODEL_ATTRIBUTE = InitializationWizardModel.class.getName();

	/**
	 * The names the wizard puts into SQL as database and user names: letters, digits and underscores,
	 * with single hyphens or dots between them as in the OpenMRS SDK's openmrs-&lt;server id&gt;
	 */
	// Sonar (java:S5998) flags the repeated group as able to overflow the stack on a long input. Not here:
	// isValidDatabaseName, which matches names against it, first checks they are within DATABASE_NAME_MAX_LENGTH.
	@SuppressWarnings("java:S5998")
	private static final Pattern DATABASE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+([-.][A-Za-z0-9_]+)*");

	private static final int DATABASE_NAME_MAX_LENGTH = 64;

	/**
	 * The password the core data gives the admin user
	 */
	private static final String SEEDED_ADMIN_PASSWORD = "test";

	/**
	 * Guards the check and set of isInstallationStarted in startInstallation, so only one install runs
	 * at a time
	 */
	private static final Object INSTALLATION_LOCK = new Object();

	volatile InitializationCompletion initJob;

	/**
	 * Variable set to true as soon as the installation begins and set to false when the process ends
	 */
	private static volatile boolean isInstallationStarted = false;

	private static final Set<String> NON_NORMALIZED_KEYS = new HashSet<>(Arrays.asList("INSTALL_METHOD", "DATABASE_NAME",
	    "HAS_CURRENT_OPENMRS_DATABASE", "CREATE_DATABASE_USER", "CREATE_TABLES", "ADD_DEMO_DATA", "MODULE_WEB_ADMIN",
	    "AUTO_UPDATE_DATABASE", "ADMIN_USER_PASSWORD", "CREATE_DATABASE_USERNAME", "CREATE_DATABASE_PASSWORD",
	    "CREATE_USER_USERNAME", "CREATE_USER_PASSWORD", "CONNECTION_DRIVER_CLASS", "IMPORT_TEST_DATA", "REMOTE_URL",
	    "REMOTE_USERNAME", "REMOTE_PASSWORD", "ADMIN_PASSWORD_LOCKED"));

	/**
	 * Variable set at the end of the wizard when spring is being restarted
	 */
	private static volatile boolean initializationComplete = false;

	protected void setInitializationComplete(boolean complete) {
		initializationComplete = complete;
	}

	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 *
	 * @param httpRequest
	 * @param httpResponse
	 */
	@Override
	protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
		log.debug("Entered initialization filter");

		// we need to save current user language in references map since it will be used when template
		// will be rendered
		if (httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE) == null) {
			checkLocaleAttributesForFirstTime(httpRequest);
		}

		Map<String, Object> referenceMap = new HashMap<>();
		String page = httpRequest.getParameter("page");

		referenceMap.put(FilterUtil.LOCALE_ATTRIBUTE, httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));

		httpResponse.setHeader("Cache-Control", "no-cache");

		// the progress poll and the in-progress page are answered before the session's model is created or
		// loaded with the resolved properties, because a running install reads the model of the session
		// that started it
		if (PROGRESS_VM_AJAXREQUEST.equals(page)) {
			httpResponse.setContentType("text/json");
			PrintWriter writer = httpResponse.getWriter();
			writer.write(toJSONString(getInstallationProgress(httpRequest.getSession())));
			writer.close();
			return;
		}

		// if any body has already started installation
		if (isInstallationStarted()) {
			renderInstallationInProgress(httpRequest.getSession(), referenceMap, httpResponse);
			return;
		}

		InitializationWizardModel wizardModel = getWizardModel(httpRequest.getSession());
		initializeWizardFromResolvedPropertiesIfPresent(wizardModel);
		// the confirmation holds only for the values the confirmation page showed
		wizardModel.wizardCompletePageShown = false;

		if (InitializationWizardModel.INSTALL_METHOD_AUTO.equals(wizardModel.installMethod)
		        || httpRequest.getServletPath().equals("/" + AUTO_RUN_OPENMRS)) {
			autoRunOpenMRS(httpRequest, wizardModel);
			referenceMap.put("isInstallationStarted", true);
			httpResponse.setContentType("text/html");
			renderTemplate(PROGRESS_VM, referenceMap, httpResponse, wizardModel);
		} else if (page == null) {
			httpResponse.setContentType("text/html");// if any body has already started installation

			//If someone came straight here without setting the hidden page input,
			// then we need to clear out all the passwords
			clearPasswords(wizardModel);

			renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse, wizardModel);
		} else if (INSTALL_METHOD.equals(page)) {
			// get props and render the second page
			File runtimeProperties = getRuntimePropertiesFile();

			if (!runtimeProperties.exists()) {
				try {
					runtimeProperties.createNewFile();
					// reset the error objects in case of refresh
					wizardModel.canCreate = true;
					wizardModel.cannotCreateErrorMessage = "";
				} catch (IOException io) {
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

				wizardModel.databaseConnection = Context.getRuntimeProperties().getProperty("connection.url",
				    wizardModel.databaseConnection);

				wizardModel.currentDatabaseUsername = Context.getRuntimeProperties().getProperty("connection.username",
				    wizardModel.currentDatabaseUsername);

				wizardModel.currentDatabasePassword = Context.getRuntimeProperties().getProperty("connection.password",
				    wizardModel.currentDatabasePassword);
			}

			wizardModel.runtimePropertiesPath = runtimeProperties.getAbsolutePath();

			// do step one of the wizard
			httpResponse.setContentType("text/html");
			renderTemplate(INSTALL_METHOD, referenceMap, httpResponse, wizardModel);
		}
	}

	/**
	 * Reports the progress of the running or the last install. Errors and server log lines go only to
	 * the session that started the install.
	 *
	 * @param session the session polling for progress
	 * @return the progress to send to the progress page
	 */
	private Map<String, Object> getInstallationProgress(HttpSession session) {
		Map<String, Object> result = new HashMap<>();
		InitializationCompletion job = initJob;
		if (job == null) {
			return result;
		}

		boolean startedBySession = job.wizardModel == getExistingWizardModel(session);
		result.put("hasErrors", job.hasErrors());
		if (job.hasErrors() && startedBySession) {
			result.put("errorPage", job.getErrorPage());
			job.wizardModel.errors.putAll(job.getErrors());
		}

		result.put("initializationComplete", isInitializationComplete());
		result.put("message", job.getMessage());
		result.put("actionCounter", job.getStepsComplete());
		if (!isInitializationComplete()) {
			result.put("executingTask", job.getExecutingTask());
			result.put("executedTasks", job.getExecutedTasks());
			result.put("completedPercentage", job.getCompletedPercentage());
		} else {
			// the wizard is done, so the session no longer needs the values it entered
			session.removeAttribute(WIZARD_MODEL_ATTRIBUTE);
		}

		if (startedBySession) {
			addLogLinesToResponse(result);
		}
		return result;
	}

	/**
	 * Renders the page shown while an install runs. The session that started the install sees it with
	 * its own model, any other session with a model holding only the install's method and tasks.
	 *
	 * @param session the session of the request
	 * @param referenceMap
	 * @param httpResponse
	 */
	private void renderInstallationInProgress(HttpSession session, Map<String, Object> referenceMap,
	        HttpServletResponse httpResponse) throws IOException {
		InitializationCompletion job = initJob;
		InitializationWizardModel wizardModel = getExistingWizardModel(session);
		if (job == null || job.wizardModel != wizardModel) {
			wizardModel = new InitializationWizardModel();
			if (job != null) {
				wizardModel.installMethod = job.wizardModel.installMethod;
				wizardModel.tasksToExecute = job.wizardModel.tasksToExecute;
			}
		}

		referenceMap.put("isInstallationStarted", true);
		httpResponse.setContentType("text/html");
		renderTemplate(PROGRESS_VM, referenceMap, httpResponse, wizardModel);
	}

	/**
	 * Returns the wizard model of the given session, creating it the first time the session reaches the
	 * wizard
	 *
	 * @param session the session of the request
	 * @return the session's wizard model
	 */
	// Sonar (java:S2441) wants session attributes to be Serializable. Deliberate: the javadoc of
	// InitializationWizardModel says why it is not.
	@SuppressWarnings("java:S2441")
	InitializationWizardModel getWizardModel(HttpSession session) {
		InitializationWizardModel wizardModel = getExistingWizardModel(session);
		if (wizardModel == null) {
			wizardModel = new InitializationWizardModel();
			session.setAttribute(WIZARD_MODEL_ATTRIBUTE, wizardModel);
		}
		return wizardModel;
	}

	/**
	 * @param session the session of the request
	 * @return the session's wizard model, or null if the session has none
	 */
	private InitializationWizardModel getExistingWizardModel(HttpSession session) {
		Object wizardModel = session.getAttribute(WIZARD_MODEL_ATTRIBUTE);
		return wizardModel instanceof InitializationWizardModel ? (InitializationWizardModel) wizardModel : null;
	}

	/**
	 * Renders the template with the given session model and its errors and messages. Rendering the page
	 * that asks to confirm the install records that on the model, which an install from the wizard
	 * requires. Pages show the connection URL as displayedDatabaseConnection, which is empty when the
	 * URL may carry credentials.
	 */
	private void renderTemplate(String templateName, Map<String, Object> referenceMap, HttpServletResponse httpResponse,
	        InitializationWizardModel wizardModel) throws IOException {
		if (WIZARD_COMPLETE.equals(templateName)) {
			wizardModel.wizardCompletePageShown = true;
		}
		String databaseConnection = wizardModel.databaseConnection;
		referenceMap.put("displayedDatabaseConnection",
		    databaseConnection == null || mayCarryCredentials(databaseConnection) ? "" : databaseConnection);
		renderTemplate(templateName, referenceMap, httpResponse, wizardModel, wizardModel.errors, wizardModel.msgs);
	}

	/**
	 * Initializes the setup wizard model by resolving configuration properties from multiple sources.
	 * <p>
	 * Properties are loaded and prioritized from system properties, environment variables (normalized),
	 * and the installation script file. Resolved values are then applied to the corresponding fields of
	 * the {@link InitializationWizardModel}.
	 *
	 * @param wizardModel the wizard model to apply the resolved properties to
	 */
	protected void initializeWizardFromResolvedPropertiesIfPresent(InitializationWizardModel wizardModel) {
		Properties script = new Properties();

		Properties installScript = getInstallationScript();
		script.putAll(installScript);

		getEnvironmentVariables().forEach((key, value) -> {
			String normalizedKey = normalizeEnvVariableKey(key);
			script.setProperty(normalizedKey, value);
		});

		System.getProperties().forEach((key, value) -> script.setProperty(key.toString(), value.toString()));

		if (log.isDebugEnabled()) {
			for (String key : script.stringPropertyNames()) {
				String value = script.getProperty(key);
				log.debug("{} = {}", key, key.toLowerCase().contains("password") ? "*******" : value);
			}
		}

		if (!script.isEmpty()) {
			wizardModel.installMethod = script.getProperty("install_method", wizardModel.installMethod);

			wizardModel.databaseConnection = script.getProperty("connection.url", wizardModel.databaseConnection);
			wizardModel.databaseDriver = script.getProperty("connection.driver_class",
			    script.getProperty("connection_driver_class", wizardModel.databaseDriver));
			wizardModel.databaseName = script.getProperty("database_name", wizardModel.databaseName);
			wizardModel.currentDatabaseUsername = script.getProperty("connection.username",
			    wizardModel.currentDatabaseUsername);
			wizardModel.currentDatabasePassword = script.getProperty("connection.password",
			    wizardModel.currentDatabasePassword);

			String hasCurrentOpenmrsDatabase = script.getProperty("has_current_openmrs_database");
			if (hasCurrentOpenmrsDatabase != null) {
				wizardModel.hasCurrentOpenmrsDatabase = Boolean.parseBoolean(hasCurrentOpenmrsDatabase);
			}
			wizardModel.createDatabaseUsername = script.getProperty("create_database_username",
			    wizardModel.createDatabaseUsername);
			wizardModel.createDatabasePassword = script.getProperty("create_database_password",
			    wizardModel.createDatabasePassword);

			String createTables = script.getProperty("create_tables");
			if (createTables != null) {
				wizardModel.createTables = Boolean.parseBoolean(createTables);
			}

			String createDatabaseUser = script.getProperty("create_database_user");
			if (createDatabaseUser != null) {
				wizardModel.createDatabaseUser = Boolean.parseBoolean(createDatabaseUser);
			}
			wizardModel.createUserUsername = script.getProperty("create_user_username", wizardModel.createUserUsername);
			wizardModel.createUserPassword = script.getProperty("create_user_password", wizardModel.createUserPassword);

			String moduleWebAdmin = script.getProperty("module_web_admin");
			if (moduleWebAdmin != null) {
				wizardModel.moduleWebAdmin = Boolean.parseBoolean(moduleWebAdmin);
			}

			String autoUpdateDatabase = script.getProperty("auto_update_database");
			if (autoUpdateDatabase != null) {
				wizardModel.autoUpdateDatabase = Boolean.parseBoolean(autoUpdateDatabase);
			}

			wizardModel.adminUserPassword = script.getProperty("admin_user_password", wizardModel.adminUserPassword);

			String importTestData = script.getProperty("import_test_data", script.getProperty("add_demo_data"));
			if (importTestData != null) {
				wizardModel.importTestData = Boolean.parseBoolean(importTestData);
			}

			wizardModel.remoteUrl = script.getProperty("remote_url", wizardModel.remoteUrl);
			wizardModel.remoteUsername = script.getProperty("remote_username", wizardModel.remoteUsername);
			wizardModel.remotePassword = script.getProperty("remote_password", wizardModel.remotePassword);

			for (Map.Entry<Object, Object> entry : script.entrySet()) {
				if (entry.getKey() instanceof String && ((String) entry.getKey()).startsWith("property.")) {
					wizardModel.additionalPropertiesFromInstallationScript.put(((String) entry.getKey()).substring(9),
					    entry.getValue());
				}
			}

			String adminPasswordLocked = script.getProperty("admin_password_locked",
			    script.getProperty("admin.password.locked"));
			if (adminPasswordLocked != null) {
				wizardModel.additionalPropertiesFromInstallationScript.put("admin.password.locked", adminPasswordLocked);
			}
		}
	}

	private void clearPasswords(InitializationWizardModel wizardModel) {
		wizardModel.databaseRootPassword = "";
		wizardModel.createDatabasePassword = "";
		wizardModel.createUserPassword = "";
		wizardModel.currentDatabasePassword = "";
		wizardModel.remotePassword = "";
	}

	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on POST requests
	 *
	 * @param httpRequest
	 * @param httpResponse
	 */
	@Override
	protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	        throws IOException, ServletException {
		String page = httpRequest.getParameter("page");
		Map<String, Object> referenceMap = new HashMap<>();
		// we need to save current user language in references map since it will be used when template
		// will be rendered
		if (httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE) != null) {
			referenceMap.put(FilterUtil.LOCALE_ATTRIBUTE,
			    httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));
		}

		// if any body has already started installation
		if (isInstallationStarted()) {
			renderInstallationInProgress(httpRequest.getSession(), referenceMap, httpResponse);
			return;
		}

		InitializationWizardModel wizardModel = getWizardModel(httpRequest.getSession());
		// only clear errors before POSTS so that redirects can show errors too.
		wizardModel.errors.clear();
		wizardModel.msgs.clear();
		// the confirmation holds only for the values the confirmation page showed
		boolean wizardCompletePageShown = wizardModel.wizardCompletePageShown;
		wizardModel.wizardCompletePageShown = false;

		if (DEFAULT_PAGE.equals(page)) {
			// get props and render the first page
			File runtimeProperties = getRuntimePropertiesFile();
			if (!runtimeProperties.exists()) {
				try {
					runtimeProperties.createNewFile();
					// reset the error objects in case of refresh
					wizardModel.canCreate = true;
					wizardModel.cannotCreateErrorMessage = "";
				} catch (IOException io) {
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

				wizardModel.databaseConnection = Context.getRuntimeProperties().getProperty("connection.url",
				    wizardModel.databaseConnection);

				wizardModel.currentDatabaseUsername = Context.getRuntimeProperties().getProperty("connection.username",
				    wizardModel.currentDatabaseUsername);

				wizardModel.currentDatabasePassword = Context.getRuntimeProperties().getProperty("connection.password",
				    wizardModel.currentDatabasePassword);
			}

			wizardModel.runtimePropertiesPath = runtimeProperties.getAbsolutePath();

			checkLocaleAttributes(httpRequest, wizardModel);
			referenceMap.put(FilterUtil.LOCALE_ATTRIBUTE,
			    httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));
			log.info("Locale stored in session is " + httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));

			httpResponse.setContentType("text/html");
			// otherwise do step one of the wizard
			renderTemplate(INSTALL_METHOD, referenceMap, httpResponse, wizardModel);
		} else if (INSTALL_METHOD.equals(page)) {
			if (goBack(httpRequest)) {
				referenceMap.put(FilterUtil.REMEMBER_ATTRIBUTE,
				    httpRequest.getSession().getAttribute(FilterUtil.REMEMBER_ATTRIBUTE) != null);
				referenceMap.put(FilterUtil.LOCALE_ATTRIBUTE,
				    httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE));
				renderTemplate(CHOOSE_LANG, referenceMap, httpResponse, wizardModel);
				return;
			}
			wizardModel.installMethod = httpRequest.getParameter("install_method");
			if (InitializationWizardModel.INSTALL_METHOD_SIMPLE.equals(wizardModel.installMethod)) {
				page = SIMPLE_SETUP;
			} else if (InitializationWizardModel.INSTALL_METHOD_TESTING.equals(wizardModel.installMethod)) {
				page = TESTING_REMOTE_DETAILS_SETUP;
				wizardModel.currentStepNumber = 1;
				wizardModel.numberOfSteps = skipDatabaseSetupPage() ? 1 : 3;
			} else {
				page = DATABASE_SETUP;
				wizardModel.currentStepNumber = 1;
				wizardModel.numberOfSteps = 5;
			}
			renderTemplate(page, referenceMap, httpResponse, wizardModel);
		} // simple method
		else if (SIMPLE_SETUP.equals(page)) {
			if (goBack(httpRequest)) {
				renderTemplate(INSTALL_METHOD, referenceMap, httpResponse, wizardModel);
				return;
			}

			String databaseType = httpRequest.getParameter("database_type");
			if (databaseType != null) {
				wizardModel.databaseType = databaseType;
				if (DATABASE_POSTGRESQL.equals(databaseType)) {
					wizardModel.databaseConnection = DEFAULT_POSTGRESQL_CONNECTION;
					String postgresUsername = httpRequest.getParameter("create_database_username");
					wizardModel.createDatabaseUsername = StringUtils.hasText(postgresUsername) ? postgresUsername
					        : Context.getRuntimeProperties().getProperty("connection.username", "postgres");
				} else {
					wizardModel.databaseConnection = DEFAULT_MYSQL_CONNECTION;
					wizardModel.createDatabaseUsername = Context.getRuntimeProperties().getProperty("connection.username",
					    wizardModel.createDatabaseUsername);
				}
			}

			wizardModel.databaseRootPassword = httpRequest.getParameter("database_root_password");
			checkForEmptyValue(wizardModel.databaseRootPassword, wizardModel.errors,
			    ErrorMessageConstants.ERROR_DB_PSDW_REQ);
			wizardModel.createUserUsername = wizardModel.createDatabaseUsername;
			wizardModel.hasCurrentOpenmrsDatabase = false;
			wizardModel.createTables = true;
			// default wizardModel.databaseName is openmrs
			// default wizardModel.createDatabaseUsername is root
			wizardModel.createDatabasePassword = wizardModel.databaseRootPassword;
			wizardModel.hasCurrentDatabaseUser = false;
			wizardModel.createDatabaseUser = true;
			// default wizardModel.createUserUsername is root
			wizardModel.createUserPassword = wizardModel.databaseRootPassword;

			wizardModel.moduleWebAdmin = true;
			wizardModel.autoUpdateDatabase = false;

			wizardModel.adminUserPassword = InitializationWizardModel.ADMIN_DEFAULT_PASSWORD;

			createSimpleSetup(wizardModel, httpRequest.getParameter("database_root_password"));

			try {
				wizardModel.loadedDriverString = DatabaseUtil.loadDatabaseDriver(wizardModel.databaseConnection,
				    wizardModel.databaseDriver);
			} catch (ClassNotFoundException e) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_DRIVER_CLASS_REQ, null);
				renderTemplate(page, referenceMap, httpResponse, wizardModel);
				return;
			}

			if (wizardModel.errors.isEmpty()) {
				page = WIZARD_COMPLETE;
			}
			renderTemplate(page, referenceMap, httpResponse, wizardModel);
		} // step one
		else if (DATABASE_SETUP.equals(page)) {
			if (goBack(httpRequest)) {
				wizardModel.currentStepNumber -= 1;
				if (InitializationWizardModel.INSTALL_METHOD_TESTING.equals(wizardModel.installMethod)) {
					renderTemplate(TESTING_REMOTE_DETAILS_SETUP, referenceMap, httpResponse, wizardModel);
				} else {
					renderTemplate(INSTALL_METHOD, referenceMap, httpResponse, wizardModel);
				}
				return;
			}

			wizardModel.databaseConnection = httpRequest.getParameter("database_connection");
			checkForEmptyValue(wizardModel.databaseConnection, wizardModel.errors, ErrorMessageConstants.ERROR_DB_CONN_REQ);

			wizardModel.databaseDriver = httpRequest.getParameter("database_driver");
			checkForEmptyValue(wizardModel.databaseConnection, wizardModel.errors,
			    ErrorMessageConstants.ERROR_DB_DRIVER_REQ);

			wizardModel.loadedDriverString = loadDriver(wizardModel.databaseConnection, wizardModel.databaseDriver);
			if (!StringUtils.hasText(wizardModel.loadedDriverString)) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_DRIVER_CLASS_REQ, null);
				renderTemplate(page, referenceMap, httpResponse, wizardModel);
				return;
			}

			//TODO make each bit of page logic a (unit testable) method

			// asked the user for their desired database name

			if ("yes".equals(httpRequest.getParameter("current_openmrs_database"))) {
				wizardModel.databaseName = httpRequest.getParameter("openmrs_current_database_name");
				checkForEmptyValue(wizardModel.databaseName, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_CURR_NAME_REQ);
				checkDatabaseName(wizardModel.databaseName, wizardModel.errors);
				wizardModel.hasCurrentOpenmrsDatabase = true;
				// TODO check to see if this is an active database

			} else {
				// mark this wizard as a "to create database" (done at the end)
				wizardModel.hasCurrentOpenmrsDatabase = false;

				wizardModel.createTables = true;

				wizardModel.databaseName = httpRequest.getParameter("openmrs_new_database_name");
				checkForEmptyValue(wizardModel.databaseName, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_NEW_NAME_REQ);
				checkDatabaseName(wizardModel.databaseName, wizardModel.errors);
				// TODO create database now to check if its possible?

				wizardModel.createDatabaseUsername = httpRequest.getParameter("create_database_username");
				checkForEmptyValue(wizardModel.createDatabaseUsername, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_USER_NAME_REQ);
				wizardModel.createDatabasePassword = httpRequest.getParameter("create_database_password");
				checkForEmptyValue(wizardModel.createDatabasePassword, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_USER_PSWD_REQ);
			}

			if (wizardModel.errors.isEmpty()) {
				page = DATABASE_TABLES_AND_USER;

				if (InitializationWizardModel.INSTALL_METHOD_TESTING.equals(wizardModel.installMethod)) {
					wizardModel.currentStepNumber = 3;
				} else {
					wizardModel.currentStepNumber = 2;
				}
			}

			renderTemplate(page, referenceMap, httpResponse, wizardModel);

		} // step two
		else if (DATABASE_TABLES_AND_USER.equals(page)) {

			if (goBack(httpRequest)) {
				wizardModel.currentStepNumber -= 1;
				renderTemplate(DATABASE_SETUP, referenceMap, httpResponse, wizardModel);
				return;
			}

			if (wizardModel.hasCurrentOpenmrsDatabase) {
				wizardModel.createTables = "yes".equals(httpRequest.getParameter("create_tables"));
			}

			if ("yes".equals(httpRequest.getParameter("current_database_user"))) {
				wizardModel.currentDatabaseUsername = httpRequest.getParameter("current_database_username");
				checkForEmptyValue(wizardModel.currentDatabaseUsername, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_CUR_USER_NAME_REQ);
				wizardModel.currentDatabasePassword = httpRequest.getParameter("current_database_password");
				checkForEmptyValue(wizardModel.currentDatabasePassword, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_CUR_USER_PSWD_REQ);
				wizardModel.hasCurrentDatabaseUser = true;
				wizardModel.createDatabaseUser = false;
			} else {
				wizardModel.hasCurrentDatabaseUser = false;
				wizardModel.createDatabaseUser = true;
				// asked for the root mysql username/password
				wizardModel.createUserUsername = httpRequest.getParameter("create_user_username");
				checkForEmptyValue(wizardModel.createUserUsername, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_USER_NAME_REQ);
				wizardModel.createUserPassword = httpRequest.getParameter("create_user_password");
				checkForEmptyValue(wizardModel.createUserPassword, wizardModel.errors,
				    ErrorMessageConstants.ERROR_DB_USER_PSWD_REQ);
			}

			if (wizardModel.errors.isEmpty()) { // go to next page
				page = InitializationWizardModel.INSTALL_METHOD_TESTING.equals(wizardModel.installMethod) ? WIZARD_COMPLETE
				        : OTHER_RUNTIME_PROPS;
			}

			renderTemplate(page, referenceMap, httpResponse, wizardModel);
		} // step three
		else if (OTHER_RUNTIME_PROPS.equals(page)) {

			if (goBack(httpRequest)) {
				renderTemplate(DATABASE_TABLES_AND_USER, referenceMap, httpResponse, wizardModel);
				return;
			}

			wizardModel.moduleWebAdmin = "yes".equals(httpRequest.getParameter("module_web_admin"));
			wizardModel.autoUpdateDatabase = "yes".equals(httpRequest.getParameter("auto_update_database"));

			// an existing database can still have the admin password the core data seeds, so this page is never
			// skipped
			renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse, wizardModel);

		} // step four
		else if (ADMIN_USER_SETUP.equals(page)) {

			if (goBack(httpRequest)) {
				renderTemplate(OTHER_RUNTIME_PROPS, referenceMap, httpResponse, wizardModel);
				return;
			}

			wizardModel.adminUserPassword = httpRequest.getParameter("new_admin_password");
			String adminUserConfirm = httpRequest.getParameter("new_admin_password_confirm");

			// throw back to admin user if passwords don't match
			if (!wizardModel.adminUserPassword.equals(adminUserConfirm)) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_ADM_PSWDS_MATCH, null);
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse, wizardModel);
				return;
			}

			// throw back if the user didn't put in a password
			if ("".equals(wizardModel.adminUserPassword)) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_ADM_PSDW_EMPTY, null);
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse, wizardModel);
				return;
			}

			try {
				OpenmrsUtil.validatePassword("admin", wizardModel.adminUserPassword, "admin");
			} catch (PasswordException p) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_ADM_PSDW_WEAK, null);
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse, wizardModel);
				return;
			}

			if (wizardModel.errors.isEmpty()) { // go to next page
				page = IMPLEMENTATION_ID_SETUP;
			}

			renderTemplate(page, referenceMap, httpResponse, wizardModel);

		} // optional step five
		else if (IMPLEMENTATION_ID_SETUP.equals(page)) {

			if (goBack(httpRequest)) {
				renderTemplate(ADMIN_USER_SETUP, referenceMap, httpResponse, wizardModel);
				return;
			}

			wizardModel.implementationIdName = httpRequest.getParameter("implementation_name");
			wizardModel.implementationId = httpRequest.getParameter("implementation_id");
			wizardModel.implementationIdPassPhrase = httpRequest.getParameter("pass_phrase");
			wizardModel.implementationIdDescription = httpRequest.getParameter("description");

			// throw back if the user-specified ID is invalid (contains ^ or |).
			if (wizardModel.implementationId.indexOf('^') != -1 || wizardModel.implementationId.indexOf('|') != -1) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_IMPL_ID_REQ, null);
				renderTemplate(IMPLEMENTATION_ID_SETUP, referenceMap, httpResponse, wizardModel);
				return;
			}

			if (wizardModel.errors.isEmpty()) { // go to next page
				page = WIZARD_COMPLETE;
			}

			renderTemplate(page, referenceMap, httpResponse, wizardModel);
		} else if (WIZARD_COMPLETE.equals(page)) {

			if (goBack(httpRequest)) {

				if (InitializationWizardModel.INSTALL_METHOD_SIMPLE.equals(wizardModel.installMethod)) {
					page = SIMPLE_SETUP;
				} else if (InitializationWizardModel.INSTALL_METHOD_TESTING.equals(wizardModel.installMethod)) {
					if (skipDatabaseSetupPage()) {
						page = TESTING_REMOTE_DETAILS_SETUP;
					} else {
						page = DATABASE_TABLES_AND_USER;
					}
				} else {
					page = IMPLEMENTATION_ID_SETUP;
				}
				renderTemplate(page, referenceMap, httpResponse, wizardModel);
				return;
			}

			// only a session that was asked to confirm its own values can install with them
			if (!wizardCompletePageShown) {
				renderTemplate(DEFAULT_PAGE, referenceMap, httpResponse, wizardModel);
				return;
			}

			if (!isValidDatabaseName(wizardModel.databaseName)) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_NAME_INVALID, null);
				renderTemplate(WIZARD_COMPLETE, referenceMap, httpResponse, wizardModel);
				return;
			}

			if (isRootWebApplicationContextSet()) {
				wizardModel.errors.put(ErrorMessageConstants.ERROR_RESTART_REQUIRED, null);
				renderTemplate(WIZARD_COMPLETE, referenceMap, httpResponse, wizardModel);
				return;
			}

			wizardModel.tasksToExecute = new ArrayList<>();
			createDatabaseTask(wizardModel);
			if (InitializationWizardModel.INSTALL_METHOD_TESTING.equals(wizardModel.installMethod)) {
				wizardModel.importTestData = true;
				wizardModel.createTables = false;
				// there is no admin password page in this install, so it sets no admin password: neither
				// ADMIN_DEFAULT_PASSWORD nor one from the environment or installation script
				wizardModel.adminUserPassword = "";
				//if we have a runtime properties file
				if (skipDatabaseSetupPage()) {
					wizardModel.hasCurrentOpenmrsDatabase = false;
					wizardModel.hasCurrentDatabaseUser = true;
					wizardModel.createDatabaseUser = false;
					Properties props = OpenmrsUtil.getRuntimeProperties(WebConstants.WEBAPP_NAME);
					wizardModel.currentDatabaseUsername = props.getProperty("connection.username");
					wizardModel.currentDatabasePassword = props.getProperty("connection.password");
					wizardModel.createDatabaseUsername = wizardModel.currentDatabaseUsername;
					wizardModel.createDatabasePassword = wizardModel.currentDatabasePassword;
				}

				wizardModel.tasksToExecute.add(WizardTask.IMPORT_TEST_DATA);
				wizardModel.tasksToExecute.add(WizardTask.ADD_MODULES);
			} else {
				createTablesTask(wizardModel);
			}
			wizardModel.tasksToExecute.add(WizardTask.UPDATE_TO_LATEST);

			startInstallation(wizardModel);
			renderInstallationInProgress(httpRequest.getSession(), referenceMap, httpResponse);
		} else if (TESTING_REMOTE_DETAILS_SETUP.equals(page)) {
			if (goBack(httpRequest)) {
				wizardModel.currentStepNumber -= 1;
				renderTemplate(INSTALL_METHOD, referenceMap, httpResponse, wizardModel);
				return;
			}

			wizardModel.remoteUrl = httpRequest.getParameter("remoteUrl");
			checkForEmptyValue(wizardModel.remoteUrl, wizardModel.errors, "install.testing.remote.url.required");
			if (wizardModel.errors.isEmpty()) {
				//Check if the remote system is running
				if (TestInstallUtil.testConnection(wizardModel.remoteUrl)) {
					//Check if the test module is installed by connecting to its setting page
					if (TestInstallUtil
					        .testConnection(wizardModel.remoteUrl.concat(RELEASE_TESTING_MODULE_PATH + "settings.htm"))) {

						wizardModel.remoteUsername = httpRequest.getParameter("username");
						wizardModel.remotePassword = httpRequest.getParameter("password");
						checkForEmptyValue(wizardModel.remoteUsername, wizardModel.errors,
						    "install.testing.username.required");
						checkForEmptyValue(wizardModel.remotePassword, wizardModel.errors,
						    "install.testing.password.required");

						if (wizardModel.errors.isEmpty()) {
							//check if the username and password are valid
							try {
								TestInstallUtil.getResourceInputStream(
								    wizardModel.remoteUrl + RELEASE_TESTING_MODULE_PATH + "verifycredentials.htm",
								    wizardModel.remoteUsername, wizardModel.remotePassword);
							} catch (AccessDeniedException e) {
								log.debug("Error generated: ", e);
								page = TESTING_REMOTE_DETAILS_SETUP;
								wizardModel.errors.put(ErrorMessageConstants.UPDATE_ERROR_UNABLE_AUTHENTICATE, null);
								renderTemplate(page, referenceMap, httpResponse, wizardModel);
								return;
							}

							//If we have a runtime properties file, get the database setup details from it
							if (skipDatabaseSetupPage()) {
								Properties props = OpenmrsUtil.getRuntimeProperties(WebConstants.WEBAPP_NAME);
								wizardModel.databaseConnection = props.getProperty("connection.url");
								wizardModel.loadedDriverString = loadDriver(wizardModel.databaseConnection,
								    wizardModel.databaseDriver);
								if (!StringUtils.hasText(wizardModel.loadedDriverString)) {
									page = TESTING_REMOTE_DETAILS_SETUP;
									wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_DRIVER_CLASS_REQ, null);
									renderTemplate(page, referenceMap, httpResponse, wizardModel);
									return;
								}

								wizardModel.databaseName = InitializationWizardModel.DEFAULT_DATABASE_NAME;
								page = WIZARD_COMPLETE;
							} else {
								page = DATABASE_SETUP;
								wizardModel.currentStepNumber = 2;
							}
							wizardModel.msgs.put("install.testing.testingModuleFound", null);
						} else {
							renderTemplate(page, referenceMap, httpResponse, wizardModel);
							return;
						}
					} else {
						wizardModel.errors.put("install.testing.noTestingModule", null);
					}
				} else {
					wizardModel.errors.put("install.testing.invalidProductionUrl", new Object[] { wizardModel.remoteUrl });
				}
			}

			renderTemplate(page, referenceMap, httpResponse, wizardModel);
		}
	}

	/**
	 * Starts an install with the values of the given wizard model, unless an install is running
	 *
	 * @param wizardModel the wizard model of the session starting the install
	 */
	protected void startInstallation(InitializationWizardModel wizardModel) {
		InitializationCompletion job;
		synchronized (INSTALLATION_LOCK) {
			// only one install runs at a time
			if (isInstallationStarted()) {
				return;
			}
			job = new InitializationCompletion(wizardModel);
			initJob = job;
			setInstallationStarted(true);
		}
		job.start();
	}

	private void createTablesTask(InitializationWizardModel wizardModel) {
		if (wizardModel.createTables) {
			wizardModel.tasksToExecute.add(WizardTask.CREATE_TABLES);
			wizardModel.tasksToExecute.add(WizardTask.ADD_CORE_DATA);
		}
	}

	private void createDatabaseTask(InitializationWizardModel wizardModel) {
		if (!wizardModel.hasCurrentOpenmrsDatabase) {
			wizardModel.tasksToExecute.add(WizardTask.CREATE_SCHEMA);
		}
		if (wizardModel.createDatabaseUser) {
			wizardModel.tasksToExecute.add(WizardTask.CREATE_DB_USER);
		}
	}

	private void createSimpleSetup(InitializationWizardModel wizardModel, String databaseRootPassword) {
		setDatabaseNameIfInTestMode(wizardModel);
		wizardModel.databaseConnection = Context.getRuntimeProperties().getProperty("connection.url",
		    wizardModel.databaseConnection);

		wizardModel.createDatabaseUsername = Context.getRuntimeProperties().getProperty("connection.username",
		    wizardModel.createDatabaseUsername);

		wizardModel.createUserUsername = wizardModel.createDatabaseUsername;

		wizardModel.databaseRootPassword = databaseRootPassword;
		checkForEmptyValue(wizardModel.databaseRootPassword, wizardModel.errors, ErrorMessageConstants.ERROR_DB_PSDW_REQ);

		wizardModel.hasCurrentOpenmrsDatabase = false;
		wizardModel.createTables = true;
		// default wizardModel.databaseName is openmrs
		// default wizardModel.createDatabaseUsername is root
		wizardModel.createDatabasePassword = wizardModel.databaseRootPassword;
		wizardModel.hasCurrentDatabaseUser = false;
		wizardModel.createDatabaseUser = true;
		// default wizardModel.createUserUsername is root
		wizardModel.createUserPassword = wizardModel.databaseRootPassword;

		wizardModel.moduleWebAdmin = true;
		wizardModel.autoUpdateDatabase = false;

		wizardModel.adminUserPassword = InitializationWizardModel.ADMIN_DEFAULT_PASSWORD;
	}

	private void setDatabaseNameIfInTestMode(InitializationWizardModel wizardModel) {
		if (OpenmrsUtil.isTestMode()) {
			wizardModel.databaseName = OpenmrsUtil.getOpenMRSVersionInTestMode();
		}
	}

	private void autoRunOpenMRS(HttpServletRequest httpRequest, InitializationWizardModel wizardModel) {
		File runtimeProperties = getRuntimePropertiesFile();
		wizardModel.runtimePropertiesPath = runtimeProperties.getAbsolutePath();

		if (!InitializationWizardModel.INSTALL_METHOD_AUTO.equals(wizardModel.installMethod)) {
			if (httpRequest.getParameter("database_user_name") != null) {
				wizardModel.createDatabaseUsername = httpRequest.getParameter("database_user_name");
			}

			createSimpleSetup(wizardModel, httpRequest.getParameter("database_root_password"));
		}

		checkLocaleAttributes(httpRequest, wizardModel);
		try {
			wizardModel.loadedDriverString = DatabaseUtil.loadDatabaseDriver(wizardModel.databaseConnection,
			    wizardModel.databaseDriver);
		} catch (ClassNotFoundException e) {
			wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_DRIVER_CLASS_REQ, null);
			return;
		}
		if (!isValidDatabaseName(wizardModel.databaseName)) {
			wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_NAME_INVALID, null);
			log.error(
			    "Not installing OpenMRS, because the configured database name does not consist of letters, digits "
			            + "and underscores, with single hyphens or dots between them, or is longer than {} characters",
			    DATABASE_NAME_MAX_LENGTH);
			return;
		}
		if (isRootWebApplicationContextSet()) {
			wizardModel.errors.put(ErrorMessageConstants.ERROR_RESTART_REQUIRED, null);
			log.error("Not installing OpenMRS again, because an earlier install failed while starting it. "
			        + "Restart OpenMRS to install again.");
			return;
		}
		try {
			OpenmrsUtil.validatePassword("admin", wizardModel.adminUserPassword, "admin");
		} catch (PasswordException e) {
			wizardModel.errors.put(ErrorMessageConstants.ERROR_DB_ADM_PSDW_WEAK, null);
			log.error("Not installing OpenMRS, because the configured admin password does not meet the password policy");
			return;
		}
		wizardModel.tasksToExecute = new ArrayList<>();
		createDatabaseTask(wizardModel);
		createTablesTask(wizardModel);
		if (wizardModel.importTestData) {
			wizardModel.tasksToExecute.add(WizardTask.IMPORT_TEST_DATA);
			wizardModel.tasksToExecute.add(WizardTask.ADD_MODULES);
		}
		wizardModel.tasksToExecute.add(WizardTask.UPDATE_TO_LATEST);
		startInstallation(wizardModel);
	}

	/**
	 * This method should be called after the user has left wizard's first page (i.e. choose language).
	 * It checks if user has changed any of locale related parameters and makes appropriate corrections
	 * with the session's wizard model or/and with locale attribute inside user's session.
	 *
	 * @param httpRequest the http request object
	 * @param wizardModel the session's wizard model
	 */
	private void checkLocaleAttributes(HttpServletRequest httpRequest, InitializationWizardModel wizardModel) {
		String localeParameter = httpRequest.getParameter(FilterUtil.LOCALE_ATTRIBUTE);
		Boolean rememberLocale = false;
		// we need to check if user wants that system will remember his selection of language
		if (httpRequest.getParameter(FilterUtil.REMEMBER_ATTRIBUTE) != null) {
			rememberLocale = true;
		}
		if (localeParameter != null) {
			String storedLocale = null;
			if (httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE) != null) {
				storedLocale = httpRequest.getSession().getAttribute(FilterUtil.LOCALE_ATTRIBUTE).toString();
			}
			// if user has changed locale parameter to new one
			// or chooses it parameter at first page loading
			if (storedLocale == null || !storedLocale.equals(localeParameter)) {
				log.info("Stored locale parameter to session " + localeParameter);
				httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, localeParameter);
			}
			if (rememberLocale) {
				httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, localeParameter);
				httpRequest.getSession().setAttribute(FilterUtil.REMEMBER_ATTRIBUTE, true);
				wizardModel.localeToSave = localeParameter;
			} else {
				// we need to reset it if it was set before
				httpRequest.getSession().setAttribute(FilterUtil.REMEMBER_ATTRIBUTE, null);
				wizardModel.localeToSave = null;
			}
		}
	}

	/**
	 * It sets locale parameter for current session when user is making first GET http request to
	 * application. It retrieves user locale from request object and checks if this locale is supported
	 * by application. If not, it uses {@link Locale#ENGLISH} by default
	 *
	 * @param httpRequest the http request object
	 */
	public void checkLocaleAttributesForFirstTime(HttpServletRequest httpRequest) {
		Locale locale = httpRequest.getLocale();
		if (CustomResourceLoader.getInstance(httpRequest).getAvailablelocales().contains(locale)) {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, locale.toString());
		} else {
			httpRequest.getSession().setAttribute(FilterUtil.LOCALE_ATTRIBUTE, Locale.ENGLISH.toString());
		}
	}

	/**
	 * Convenience method to load the runtime properties file.
	 *
	 * @return the runtime properties file.
	 */
	private File getRuntimePropertiesFile() {
		File file;

		String pathName = OpenmrsUtil.getRuntimePropertiesFilePathName(WebConstants.WEBAPP_NAME);
		if (pathName != null) {
			file = new File(pathName);
		} else {
			file = new File(OpenmrsUtil.getApplicationDataDirectory(), getRuntimePropertiesFileName());
		}

		log.debug("Using file: " + file.getAbsolutePath());

		return file;
	}

	private String getRuntimePropertiesFileName() {
		String fileName = OpenmrsUtil.getRuntimePropertiesFileNameInTestMode();
		if (fileName == null) {
			fileName = WebConstants.WEBAPP_NAME + "-runtime.properties";
		}
		return fileName;
	}

	/**
	 * @see org.openmrs.web.filter.StartupFilter#getTemplatePrefix()
	 */
	@Override
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/initialization/";
	}

	/**
	 * Every page of this wizard is rendered with a model for its own session and shows that model's
	 * errors and msgs, so this default model, and the errors and msgs inherited from StartupFilter, are
	 * not rendered
	 *
	 * @see org.openmrs.web.filter.StartupFilter#getUpdateFilterModel()
	 */
	@Override
	protected Object getUpdateFilterModel() {
		return new InitializationWizardModel();
	}

	/**
	 * @see org.openmrs.web.filter.StartupFilter#skipFilter(HttpServletRequest)
	 */
	@Override
	public boolean skipFilter(HttpServletRequest httpRequest) {
		if (initializationRequired()) {
			return false;
		}
		// Once initialization completes, only the progress page's final poll still needs this filter; without it
		// progress.vm never leaves the wizard. Check the servlet path first so other requests aren't parsed.
		if (!("/" + WebConstants.SETUP_PAGE_URL).equals(httpRequest.getServletPath())) {
			return true;
		}
		return !PROGRESS_VM_AJAXREQUEST.equals(httpRequest.getParameter("page"));
	}

	/**
	 * Public method that returns true if database+runtime properties initialization is required
	 *
	 * @return true if this initialization wizard needs to run
	 */
	public static boolean initializationRequired() {
		return !isInitializationComplete();
	}

	/**
	 * @param isInstallationStarted the value to set
	 */
	protected static void setInstallationStarted(boolean isInstallationStarted) {
		InitializationFilter.isInstallationStarted = isInstallationStarted;
	}

	/**
	 * @return true if installation has been started
	 */
	public static boolean isInstallationStarted() {
		return isInstallationStarted;
	}

	/**
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		DatabaseDetective databaseDetective = new DatabaseDetective();
		//set whether need to do initialization work
		if (databaseDetective.isDatabaseEmpty(OpenmrsUtil.getRuntimeProperties(WebConstants.WEBAPP_NAME))) {
			//if runtime-properties file doesn't exist, have to do initialization work
			setInitializationComplete(false);
		} else {
			//if database is not empty, then let UpdaterFilter to judge whether need database update
			setInitializationComplete(true);
		}
	}

	private void importTestDataSet(InputStream in, String connectionUrl, String connectionUsername,
	        String connectionPassword, String databaseName) throws IOException {
		File tempFile = null;
		FileOutputStream fileOut = null;
		try {
			ZipInputStream zipIn = new ZipInputStream(in);
			zipIn.getNextEntry();

			tempFile = File.createTempFile("testDataSet", "dump");
			fileOut = new FileOutputStream(tempFile);

			IOUtils.copy(zipIn, fileOut);

			fileOut.close();
			zipIn.close();

			//Cater for the stand-alone connection url with has :mxj:
			if (connectionUrl.contains(":mxj:")) {
				connectionUrl = connectionUrl.replace(":mxj:", ":");
			}

			URI uri = URI.create(connectionUrl.substring(5)); //remove 'jdbc:' prefix to conform to the URI format
			String host = uri.getHost();
			int port = uri.getPort();

			TestInstallUtil.addTestData(host, port, databaseName, connectionUsername, connectionPassword,
			    tempFile.getAbsolutePath());
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fileOut);

			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}

	/**
	 * @param wizardModel the wizard model holding the database connection
	 * @param database the name of a database, such as mysql
	 * @return true if the model's database connection is for the given database
	 */
	protected boolean isCurrentDatabase(InitializationWizardModel wizardModel, String database) {
		return wizardModel.databaseConnection.contains(database);
	}

	/**
	 * Convenience variable to know if this wizard has completed successfully and that this wizard does
	 * not need to be executed again
	 *
	 * @return true if this has been run already
	 */
	private static boolean isInitializationComplete() {
		return initializationComplete;
	}

	/**
	 * Checks if the given string value is empty or contains only whitespace. If it is, an error is
	 * added to the provided errors map with the specified error message code.
	 *
	 * @param value the string to check
	 * @param errors the list of errors to append the errorMessage to if value is empty
	 * @param errorMessageCode the string with code of error message translation to append if value is
	 *            empty
	 */
	private void checkForEmptyValue(String value, Map<String, Object[]> errors, String errorMessageCode) {
		if (!StringUtils.hasText(value)) {
			errors.put(errorMessageCode, null);
		}
	}

	/**
	 * @return true once an earlier install has started the root web application context and then failed
	 *         to start OpenMRS, or failed while starting that context, either of which leaves the
	 *         context or its failure on the servlet context; no second one can be started until OpenMRS
	 *         restarts
	 */
	private boolean isRootWebApplicationContextSet() {
		return filterConfig.getServletContext()
		        .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null;
	}

	/**
	 * Adds an error if a database name was entered that the wizard does not accept
	 *
	 * @param databaseName the name entered
	 * @param errors the errors to add to
	 */
	private void checkDatabaseName(String databaseName, Map<String, Object[]> errors) {
		if (StringUtils.hasText(databaseName) && !isValidDatabaseName(databaseName)) {
			errors.put(ErrorMessageConstants.ERROR_DB_NAME_INVALID, null);
		}
	}

	/**
	 * @param databaseName a database name
	 * @return true if the wizard can use the name, and the user name it derives from it, in the SQL it
	 *         runs
	 */
	private static boolean isValidDatabaseName(String databaseName) {
		return databaseName != null && databaseName.length() <= DATABASE_NAME_MAX_LENGTH
		        && DATABASE_NAME_PATTERN.matcher(databaseName).matches();
	}

	/**
	 * Drivers take credentials in a connection URL in several forms, such as a password parameter or
	 * user info before an @, so rather than parse them out, the wizard shows no URL that names a
	 * password or holds an @ other than the one in its @DBNAME@ placeholder.
	 *
	 * @param connectionUrl a connection URL
	 * @return true if the URL contains "pass", ignoring case, or an @ outside @DBNAME@
	 */
	static boolean mayCarryCredentials(String connectionUrl) {
		String url = connectionUrl.replace("@DBNAME@", "");
		return url.toLowerCase(Locale.ROOT).contains("pass") || url.contains("@");
	}

	/**
	 * Separate thread that will run through all tasks to complete the initialization. The database is
	 * created, user's created, etc here
	 */
	class InitializationCompletion {

		/**
		 * The wizard model of the session that started this install, the only model it reads
		 */
		final InitializationWizardModel wizardModel;

		/**
		 * The work of the install, which {@link #start()} submits
		 */
		private final Runnable installation;

		private Future<Void> future;

		private int steps = 0;

		private String message = "";

		private Map<String, Object[]> errors = new HashMap<>();

		private String errorPage = null;

		private boolean erroneous = false;

		private int completedPercentage = 0;

		private WizardTask executingTask;

		private List<WizardTask> executedTasks = new ArrayList<>();

		/**
		 * The runtime properties file, once the install has opened it to write it
		 */
		private File openedRuntimePropertiesFile;

		/**
		 * Set once every step before starting OpenMRS has succeeded, the runtime properties file included
		 */
		private boolean setupComplete = false;

		/**
		 * The runtime properties OpenMRS had before this install added its own, once it has added them
		 */
		private Properties runtimePropertiesBeforeInstall;

		public synchronized void reportError(String error, String errorPage, Object... params) {
			errors.put(error, params);
			this.errorPage = errorPage;
			erroneous = true;
		}

		public synchronized boolean hasErrors() {
			return erroneous;
		}

		public synchronized String getErrorPage() {
			return errorPage;
		}

		public synchronized Map<String, Object[]> getErrors() {
			return errors;
		}

		/**
		 * Start the completion stage. This fires up the thread to do all the work.
		 */
		public void start() {
			setStepsComplete(0);
			setInitializationComplete(false);
			future = OpenmrsThreadPoolHolder.threadExecutor.submit(() -> {
				installation.run();
				return null;
			});
		}

		public void waitForCompletion() {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		protected synchronized void setStepsComplete(int steps) {
			this.steps = steps;
		}

		protected synchronized int getStepsComplete() {
			return steps;
		}

		public synchronized String getMessage() {
			return message;
		}

		public synchronized void setMessage(String message) {
			log.debug(message);
			this.message = message;
			setStepsComplete(getStepsComplete() + 1);
		}

		/**
		 * @return the executingTask
		 */
		protected synchronized WizardTask getExecutingTask() {
			return executingTask;
		}

		/**
		 * @return the completedPercentage
		 */
		protected synchronized int getCompletedPercentage() {
			return completedPercentage;
		}

		/**
		 * @param completedPercentage the completedPercentage to set
		 */
		protected synchronized void setCompletedPercentage(int completedPercentage) {
			this.completedPercentage = completedPercentage;
		}

		/**
		 * Adds a task that has been completed to the list of executed tasks
		 *
		 * @param task
		 */
		protected synchronized void addExecutedTask(WizardTask task) {
			this.executedTasks.add(task);
		}

		/**
		 * @param executingTask the executingTask to set
		 */
		protected synchronized void setExecutingTask(WizardTask executingTask) {
			this.executingTask = executingTask;
		}

		/**
		 * @return the executedTasks
		 */
		protected synchronized List<WizardTask> getExecutedTasks() {
			return this.executedTasks;
		}

		/**
		 * This class does all the work of creating the desired database, user, updates, etc
		 *
		 * @param wizardModel the wizard model of the session starting the install
		 */
		InitializationCompletion(InitializationWizardModel wizardModel) {
			this.wizardModel = wizardModel;
			installation = new Runnable() {

				/**
				 * TODO split this up into multiple testable methods
				 *
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					boolean succeeded = false;
					try {
						String connectionUsername;
						StringBuilder connectionPassword = new StringBuilder();
						ChangeLogDetective changeLogDetective = ChangeLogDetective.getInstance();
						ChangeLogVersionFinder changeLogVersionFinder = new ChangeLogVersionFinder();

						if (!wizardModel.hasCurrentOpenmrsDatabase) {
							setMessage("Create database");
							setExecutingTask(WizardTask.CREATE_SCHEMA);
							// connect via jdbc and create a database
							String sql;
							if (isCurrentDatabase(wizardModel, DATABASE_MYSQL)
							        || isCurrentDatabase(wizardModel, DATABASE_MARIADB)) {
								sql = "create database if not exists ? default character set utf8";
							} else if (isCurrentDatabase(wizardModel, DATABASE_POSTGRESQL)) {
								sql = "create database ? encoding 'utf8'";
							} else if (isCurrentDatabase(wizardModel, DATABASE_H2)) {
								sql = null;
							} else {
								sql = "create database ?";
							}

							int result;
							if (sql != null) {
								result = executeStatement(false, wizardModel.createDatabaseUsername,
								    wizardModel.createDatabasePassword, sql,
								    SqlArgument.identifier(wizardModel.databaseName));
							} else {
								result = 1;
							}
							// throw the user back to the main screen if this error occurs
							if (result < 0) {
								reportError(ErrorMessageConstants.ERROR_DB_CREATE_NEW, DEFAULT_PAGE);
								return;
							} else {
								wizardModel.workLog.add("Created database " + wizardModel.databaseName);
							}

							addExecutedTask(WizardTask.CREATE_SCHEMA);
						}

						if (wizardModel.createDatabaseUser) {
							setMessage("Create database user");
							setExecutingTask(WizardTask.CREATE_DB_USER);
							// the database name was checked before the install started, and the user name built from it
							// passes the same check
							connectionUsername = wizardModel.databaseName + "_user";
							if (connectionUsername.length() > 16) {
								connectionUsername = wizardModel.databaseName.substring(0, 11) + "_user"; // trim off enough to leave space for _user at the end
							}

							connectionPassword.append("");
							// generate random password from this subset of alphabet
							// intentionally left out these characters: ufsb$() to prevent certain words forming randomly
							String chars = "acdeghijklmnopqrtvwxyzACDEGHIJKLMNOPQRTVWXYZ0123456789.|~@#^&";
							Random r = new Random();
							StringBuilder randomStr = new StringBuilder("");
							for (int x = 0; x < 12; x++) {
								randomStr.append(chars.charAt(r.nextInt(chars.length())));
							}
							connectionPassword.append(randomStr);

							// connect via jdbc with root user and create an openmrs user
							String host = "'%'";
							if (wizardModel.databaseConnection.contains("localhost")
							        || wizardModel.databaseConnection.contains("127.0.0.1")) {
								host = "'localhost'";
							}

							// MySQL and MariaDB name a user with a string, PostgreSQL with an identifier
							SqlArgument user = isCurrentDatabase(wizardModel, DATABASE_POSTGRESQL)
							        ? SqlArgument.identifier(connectionUsername)
							        : SqlArgument.literal(connectionUsername);

							String sql = "";
							if (isCurrentDatabase(wizardModel, DATABASE_MYSQL)
							        || isCurrentDatabase(wizardModel, DATABASE_MARIADB)) {
								sql = "drop user ?@" + host;
							} else if (isCurrentDatabase(wizardModel, DATABASE_POSTGRESQL)) {
								sql = "drop user ?";
							}

							executeStatement(true, wizardModel.createUserUsername, wizardModel.createUserPassword, sql,
							    user);

							if (isCurrentDatabase(wizardModel, DATABASE_MYSQL)
							        || isCurrentDatabase(wizardModel, DATABASE_MARIADB)) {
								sql = "create user ?@" + host + " identified by ?";
							} else if (isCurrentDatabase(wizardModel, DATABASE_POSTGRESQL)) {
								sql = "create user ? with password ?";
							}

							if (-1 != executeStatement(false, wizardModel.createUserUsername, wizardModel.createUserPassword,
							    sql, user, SqlArgument.literal(connectionPassword.toString()))) {
								wizardModel.workLog.add("Created user " + connectionUsername);
							} else {
								// if error occurs stop
								reportError(ErrorMessageConstants.ERROR_DB_CREATE_DB_USER, DEFAULT_PAGE);
								return;
							}

							// grant the roles
							int result = 1;
							if (isCurrentDatabase(wizardModel, DATABASE_MYSQL)
							        || isCurrentDatabase(wizardModel, DATABASE_MARIADB)) {
								sql = "GRANT ALL ON ?.* TO ?@" + host;
								result = executeStatement(false, wizardModel.createUserUsername,
								    wizardModel.createUserPassword, sql, SqlArgument.identifier(wizardModel.databaseName),
								    user);
							} else if (isCurrentDatabase(wizardModel, DATABASE_POSTGRESQL)) {
								sql = "ALTER USER ? WITH SUPERUSER";
								result = executeStatement(false, wizardModel.createUserUsername,
								    wizardModel.createUserPassword, sql, user);
							}

							// throw the user back to the main screen if this error occurs
							if (result < 0) {
								reportError(ErrorMessageConstants.ERROR_DB_GRANT_PRIV, DEFAULT_PAGE);
								return;
							} else {
								wizardModel.workLog.add("Granted user " + connectionUsername + " all privileges to database "
								        + wizardModel.databaseName);
							}

							addExecutedTask(WizardTask.CREATE_DB_USER);
						} else {
							connectionUsername = wizardModel.currentDatabaseUsername;
							connectionPassword.setLength(0);
							connectionPassword.append(wizardModel.currentDatabasePassword);
						}

						String finalDatabaseConnectionString = wizardModel.databaseConnection.replace("@DBNAME@",
						    wizardModel.databaseName);

						finalDatabaseConnectionString = finalDatabaseConnectionString.replace("@APPLICATIONDATADIR@",
						    OpenmrsUtil.getApplicationDataDirectory().replace("\\", "/"));

						// verify that the database connection works
						if (!verifyConnection(connectionUsername, connectionPassword.toString(),
						    finalDatabaseConnectionString)) {
							setMessage("Verify that the database connection works");
							// redirect to setup page if we got an error
							reportError("Unable to connect to database", DEFAULT_PAGE);
							return;
						}

						// save the properties for startup purposes
						Properties runtimeProperties = new Properties();

						runtimeProperties.put("connection.url", finalDatabaseConnectionString);
						runtimeProperties.put("connection.username", connectionUsername);
						runtimeProperties.put("connection.password", connectionPassword.toString());
						if (StringUtils.hasText(wizardModel.databaseDriver)) {
							runtimeProperties.put("connection.driver_class", wizardModel.databaseDriver);
						}
						runtimeProperties.put("module.allow_web_admin", "" + wizardModel.moduleWebAdmin);
						runtimeProperties.put("auto_update_database", "" + wizardModel.autoUpdateDatabase);
						final Encoder base64 = Base64.getEncoder();
						runtimeProperties.put(OpenmrsConstants.ENCRYPTION_VECTOR_RUNTIME_PROPERTY,
						    new String(base64.encode(Security.generateNewInitVector()), StandardCharsets.UTF_8));
						runtimeProperties.put(OpenmrsConstants.ENCRYPTION_KEY_RUNTIME_PROPERTY,
						    new String(base64.encode(Security.generateNewSecretKey()), StandardCharsets.UTF_8));

						runtimeProperties.putAll(wizardModel.additionalPropertiesFromInstallationScript);

						runtimeProperties = addRuntimeProperties(runtimeProperties);

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
							 * @see ChangeSetExecutorCallback#executing(liquibase.changelog.ChangeSet, int)
							 */
							@Override
							public void executing(ChangeSet changeSet, int numChangeSetsToRun) {
								setMessage(message + " (" + i++ + "/" + numChangeSetsToRun + "): Author: "
								        + changeSet.getAuthor() + " Comments: " + changeSet.getComments() + " Description: "
								        + changeSet.getDescription());
								float numChangeSetsToRunFloat = (float) numChangeSetsToRun;
								float j = (float) i;
								setCompletedPercentage(Math.round(j * 100 / numChangeSetsToRunFloat));
							}

						}

						if (wizardModel.createTables) {
							log.debug("Creating tables");
							// use liquibase to create core data + tables
							try {
								String liquibaseSchemaFileName = changeLogVersionFinder.getLatestSchemaSnapshotFilename()
								        .get();
								String liquibaseCoreDataFileName = changeLogVersionFinder.getLatestCoreDataSnapshotFilename()
								        .get();

								setMessage("Executing " + liquibaseSchemaFileName);
								setExecutingTask(WizardTask.CREATE_TABLES);

								log.debug("executing Liquibase file '{}' ", liquibaseSchemaFileName);

								DatabaseUpdater.executeChangelog(liquibaseSchemaFileName,
								    new PrintingChangeSetExecutorCallback("OpenMRS schema file"));
								addExecutedTask(WizardTask.CREATE_TABLES);

								//reset for this task
								setCompletedPercentage(0);
								setExecutingTask(WizardTask.ADD_CORE_DATA);

								log.debug("executing Liquibase file '{}' ", liquibaseCoreDataFileName);

								DatabaseUpdater.executeChangelog(liquibaseCoreDataFileName,
								    new PrintingChangeSetExecutorCallback("OpenMRS core data file"));
								wizardModel.workLog.add("Created database tables and added core data");
								addExecutedTask(WizardTask.ADD_CORE_DATA);

							} catch (Exception e) {
								reportError(ErrorMessageConstants.ERROR_DB_CREATE_TABLES_OR_ADD_DEMO_DATA, DEFAULT_PAGE,
								    e.getMessage());
								log.warn("Error while trying to create tables and demo data. If the database already "
								        + "has the tables of an OpenMRS database, install again without creating them "
								        + "(create_tables=false for an automatic install)",
								    e);
								return;
							}
						}

						if (wizardModel.importTestData) {
							try {
								setMessage("Importing test data");
								setExecutingTask(WizardTask.IMPORT_TEST_DATA);
								setCompletedPercentage(0);

								try {
									InputStream inData = TestInstallUtil.getResourceInputStream(
									    wizardModel.remoteUrl + RELEASE_TESTING_MODULE_PATH + "generateTestDataSet.form",
									    wizardModel.remoteUsername, wizardModel.remotePassword);

									setCompletedPercentage(40);
									setMessage("Loading imported test data...");
									importTestDataSet(inData, finalDatabaseConnectionString, connectionUsername,
									    connectionPassword.toString(), wizardModel.databaseName);
									wizardModel.workLog.add("Imported test data");
									addExecutedTask(WizardTask.IMPORT_TEST_DATA);

									//reset the progress for the next task
									setCompletedPercentage(0);
									setMessage("Importing modules from remote server...");
									setExecutingTask(WizardTask.ADD_MODULES);

									InputStream inModules = TestInstallUtil.getResourceInputStream(
									    wizardModel.remoteUrl + RELEASE_TESTING_MODULE_PATH + "getModules.htm",
									    wizardModel.remoteUsername, wizardModel.remotePassword);

									setCompletedPercentage(90);
									setMessage("Adding imported modules...");
									if (!TestInstallUtil.addZippedTestModules(inModules)) {
										reportError(ErrorMessageConstants.ERROR_DB_UNABLE_TO_ADD_MODULES, DEFAULT_PAGE, "");
										return;
									} else {
										wizardModel.workLog.add("Added Modules");
										addExecutedTask(WizardTask.ADD_MODULES);
									}
								} catch (AccessDeniedException e) {
									log.warn("Unable to authenticate as a User with the System Developer role");
									reportError(ErrorMessageConstants.UPDATE_ERROR_UNABLE_AUTHENTICATE,
									    TESTING_REMOTE_DETAILS_SETUP, "");
									return;
								}
							} catch (Exception e) {
								reportError(ErrorMessageConstants.ERROR_DB_IMPORT_TEST_DATA, DEFAULT_PAGE, e.getMessage());
								log.warn("Error while trying to import test data", e);
								return;
							}
						}

						// update the database to the latest version
						try {
							setMessage("Updating the database to the latest version");
							setCompletedPercentage(0);
							setExecutingTask(WizardTask.UPDATE_TO_LATEST);

							String version = null;

							if (wizardModel.createTables) {
								version = changeLogVersionFinder.getLatestSnapshotVersion().get();
							} else {
								version = changeLogDetective.getInitialLiquibaseSnapshotVersion(DatabaseUpdater.CONTEXT,
								    new DatabaseUpdaterLiquibaseProvider());
							}

							log.debug(
							    "updating the database with versions of liquibase-update-to-latest files greater than '{}'",
							    version);

							List<String> changelogs = changeLogVersionFinder
							        .getUpdateFileNames(changeLogVersionFinder.getUpdateVersionsGreaterThan(version));

							for (String changelog : changelogs) {
								log.debug("applying Liquibase changelog '{}'", changelog);

								DatabaseUpdater.executeChangelog(changelog,
								    new PrintingChangeSetExecutorCallback("executing Liquibase changelog " + changelog));
							}
							addExecutedTask(WizardTask.UPDATE_TO_LATEST);
						} catch (Exception e) {
							reportError(ErrorMessageConstants.ERROR_DB_UPDATE_TO_LATEST, DEFAULT_PAGE, e.getMessage());
							log.warn("Error while trying to update to the latest database version", e);
							return;
						}

						setExecutingTask(null);
						setMessage("Starting OpenMRS");

						// start spring
						// if setting up fails after this point, the web application context is closed again; if starting
						// it or OpenMRS fails, it or its failure stays on the servlet context, and a later install is
						// refused until a restart
						// logic copied from org.springframework.web.context.ContextLoaderListener
						log.debug("Initializing WAC");
						ContextLoader contextLoader = new ContextLoader();
						contextLoader.initWebApplicationContext(filterConfig.getServletContext());
						log.debug("Done initializing WAC");

						Context.openSession();

						if (!completeSetup(runtimeProperties)) {
							Context.shutdown();
							WebModuleUtil.shutdownModules(filterConfig.getServletContext());
							contextLoader.closeWebApplicationContext(filterConfig.getServletContext());
							return;
						}

						// set this so that the wizard isn't run again on next page load
						Context.closeSession();

						// start openmrs
						try {
							UpdateFilter.setUpdatesRequired(false);
							WebDaemon.startOpenmrs(filterConfig.getServletContext());
						} catch (DatabaseUpdateException updateEx) {
							log.warn("Error while running the database update file", updateEx);
							reportError(ErrorMessageConstants.ERROR_DB_UPDATE, DEFAULT_PAGE, updateEx.getMessage());
							return;
						} catch (InputRequiredException inputRequiredEx) {
							// TODO display a page looping over the required input and ask the user for each.
							// 		When done and the user and put in their say, call DatabaseUpdater.update(Map);
							//		with the user's question/answer pairs
							log.warn(
							    "Unable to continue because user input is required for the db updates and we cannot do anything about that right now");
							reportError(ErrorMessageConstants.ERROR_INPUT_REQ, DEFAULT_PAGE);
							return;
						} catch (MandatoryModuleException mandatoryModEx) {
							log.warn(
							    "A mandatory module failed to start. Fix the error or unmark it as mandatory to continue.",
							    mandatoryModEx);
							reportError(ErrorMessageConstants.ERROR_MANDATORY_MOD_REQ, DEFAULT_PAGE,
							    mandatoryModEx.getMessage());
							return;
						}

						// TODO catch openmrs errors here and drop the user back out to the setup screen

						succeeded = true;
					} catch (Exception e) {
						reportError(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, DEFAULT_PAGE, e.getMessage());
						log.warn("Unable to complete the installation", e);
					} finally {
						finishInstallation(succeeded);
					}
				}
			};
		}

		/**
		 * Adds the runtime properties of this install to the ones OpenMRS has, which the steps that follow
		 * read, and keeps a copy of the ones OpenMRS had, which {@link #finishInstallation(boolean)} puts
		 * back if the install does not succeed
		 *
		 * @param installRuntimeProperties the runtime properties of this install
		 * @return the runtime properties OpenMRS has now
		 */
		Properties addRuntimeProperties(Properties installRuntimeProperties) {
			runtimePropertiesBeforeInstall = Context.getRuntimeProperties();
			Properties runtimeProperties = Context.getRuntimeProperties();
			runtimeProperties.putAll(installRuntimeProperties);
			Context.setRuntimeProperties(runtimeProperties);
			return runtimeProperties;
		}

		/**
		 * Runs the steps between starting the web application context and starting OpenMRS, in a session
		 * the caller opened, unless an earlier step of the install has reported an error. The runtime
		 * properties file, which marks OpenMRS as installed, is written last, so a step that fails before
		 * it leaves the file as it was.
		 *
		 * @param runtimeProperties the runtime properties of the install, holding its database connection
		 * @return true if every step succeeded, false once a step has reported an error
		 */
		boolean completeSetup(Properties runtimeProperties) {
			if (hasErrors() || !replaceSeededAdminPassword() || !setImplementationId()) {
				return false;
			}

			try {
				// Update PostgreSQL Sequences after insertion of core data
				Context.getAdministrationService().updatePostgresSequence();
			} catch (Exception e) {
				log.warn("Not able to update PostgreSQL sequence. Startup failed for PostgreSQL", e);
				reportError(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, DEFAULT_PAGE, e.getMessage());
				return false;
			}

			// output properties to the openmrs runtime properties file so that this wizard is not run again
			try {
				writeRuntimeProperties(runtimeProperties);
			} catch (IOException e) {
				reportError(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, DEFAULT_PAGE, e.getMessage());
				log.warn("Unable to write the runtime properties file", e);
				return false;
			}
			setupComplete = true;
			return true;
		}

		/**
		 * Replaces the password the core data gives the admin user with the one this install was given, if
		 * user 1 still has it, which it tells from user 1's stored password hash
		 *
		 * @return false once an error has been reported
		 */
		private boolean replaceSeededAdminPassword() {
			try {
				if (!isAdminPasswordSeeded()) {
					return true;
				}
				if (!StringUtils.hasText(wizardModel.adminUserPassword)) {
					reportError(ErrorMessageConstants.ERROR_DB_ADM_PSDW_DEFAULT, DEFAULT_PAGE);
					log.warn("The admin user still has the seeded password, and this install has none to replace it with");
					return false;
				}

				Properties props = Context.getRuntimeProperties();
				String initValue = props.getProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY);
				props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "false");
				Context.setRuntimeProperties(props);
				try {
					// no user is authenticated in the install thread
					Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
					Context.addProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
					UserService userService = Context.getUserService();
					userService.changePassword(userService.getUser(1), wizardModel.adminUserPassword);
				} finally {
					Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
					Context.removeProxyPrivilege(PrivilegeConstants.EDIT_USER_PASSWORDS);
					if (initValue == null) {
						props.remove(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY);
					} else {
						props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, initValue);
					}
					Context.setRuntimeProperties(props);
				}

				if (isAdminPasswordSeeded()) {
					reportError(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, DEFAULT_PAGE,
					    "The admin user still has the seeded password.");
					log.warn("The admin user still has the seeded password after replacing it");
					return false;
				}
				log.warn("Replaced the password the core data gives the admin user with the admin password this install "
				        + "was given");
				wizardModel.workLog.add("Replaced the default admin password");
				return true;
			} catch (Exception e) {
				reportError(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, DEFAULT_PAGE, e.getMessage());
				log.warn("Unable to replace the seeded admin password", e);
				return false;
			}
		}

		/**
		 * Reads user 1's password over DatabaseUpdater.getConnection(), which the install's Liquibase steps
		 * also use, as UpdateFilter does to check a login before OpenMRS has started
		 *
		 * @return true if user 1, the admin user, still has the password the core data gives it
		 */
		private boolean isAdminPasswordSeeded() throws Exception {
			try (Connection connection = DatabaseUpdater.getConnection();
			        PreparedStatement statement = connection
			                .prepareStatement("select password, salt from users where user_id = 1");
			        ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return false;
				}
				String passwordOnRecord = resultSet.getString(1);
				String saltOnRecord = resultSet.getString(2);
				// checked the way a login checks it
				return passwordOnRecord != null
				        && Security.checkPassword(passwordOnRecord, SEEDED_ADMIN_PASSWORD + saltOnRecord);
			}
		}

		/**
		 * Sets the implementation id the wizard was given, if any
		 *
		 * @return false if setting it failed
		 */
		private boolean setImplementationId() {
			if ("".equals(wizardModel.implementationId)) {
				return true;
			}
			try {
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
				Context.addProxyPrivilege(GET_GLOBAL_PROPERTIES);
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_CONCEPT_SOURCES);
				Context.addProxyPrivilege(PrivilegeConstants.GET_CONCEPT_SOURCES);
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID);

				ImplementationId implId = new ImplementationId();
				implId.setName(wizardModel.implementationIdName);
				implId.setImplementationId(wizardModel.implementationId);
				implId.setPassphrase(wizardModel.implementationIdPassPhrase);
				implId.setDescription(wizardModel.implementationIdDescription);

				Context.getAdministrationService().setImplementationId(implId);
				return true;
			} catch (Exception e) {
				reportError(ErrorMessageConstants.ERROR_SET_INPL_ID, DEFAULT_PAGE, e.getMessage());
				log.warn("Implementation ID could not be set.", e);
				return false;
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
				Context.removeProxyPrivilege(GET_GLOBAL_PROPERTIES);
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_CONCEPT_SOURCES);
				Context.removeProxyPrivilege(PrivilegeConstants.GET_CONCEPT_SOURCES);
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_IMPLEMENTATION_ID);
			}
		}

		/**
		 * Writes the runtime properties file, which marks OpenMRS as installed
		 *
		 * @param runtimeProperties the properties to write
		 * @throws IOException if the file could not be written
		 */
		void writeRuntimeProperties(Properties runtimeProperties) throws IOException {
			File runtimePropertiesFile = getRuntimePropertiesFile();
			// not OpenmrsUtil.storeProperties, which logs and swallows an IOException: a failed write has to
			// fail the setup
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(runtimePropertiesFile),
			        StandardCharsets.UTF_8)) {
				// opening it has truncated any file that was there
				openedRuntimePropertiesFile = runtimePropertiesFile;
				runtimeProperties.store(writer, "Auto generated by OpenMRS initialization wizard");
			}
			wizardModel.workLog.add("Saved runtime properties file " + runtimePropertiesFile);

			/*
			 * Fix file readability permissions:
			 * first revoke read permission from everyone, then set read permissions for only the user
			 * there is no function to set specific readability for only one user
			 * and revoke everyone else's, therefore this is the only way to accomplish this.
			 */
			wizardModel.workLog.add("Adjusting file posix properties to user readonly");
			if (runtimePropertiesFile.setReadable(false, false) && runtimePropertiesFile.setReadable(true)) {
				wizardModel.workLog.add("Successfully adjusted RuntimePropertiesFile to disallow world to read it");
			} else {
				wizardModel.workLog.add("Unable to adjust RuntimePropertiesFile to disallow world to read it");
			}
		}

		/**
		 * Ends the install, in the finally of the install thread. Only an install that got through starting
		 * OpenMRS without an error sets initialization complete, which lets requests past this filter. Any
		 * other install puts back the runtime properties OpenMRS had before the install added its own, so
		 * the wizard pages of the sessions that walk the wizard next do not read this install's values from
		 * them. One that failed before its setup was complete removes the runtime properties file it opened
		 * to write it, so the next start comes back to this wizard; opening a file empties it, so this
		 * holds for a file that was there before the install too. A runtime properties file the install
		 * never opened stays, since whoever wrote it owns it. Once the setup is complete the database no
		 * longer has the seeded admin password, so the file stays when only starting OpenMRS failed.
		 *
		 * @param succeeded whether the install got through starting OpenMRS
		 */
		void finishInstallation(boolean succeeded) {
			try {
				if (succeeded && !hasErrors()) {
					// set this so that the wizard isn't run again on next page load
					setInitializationComplete(true);
					// we should also try to store selected by user language
					// if user wants to system will do it for him
					FilterUtil.storeLocale(wizardModel.localeToSave);
					return;
				}

				// before the finally clears installation started, which lets sessions walk the wizard again
				if (runtimePropertiesBeforeInstall != null) {
					Context.setRuntimeProperties(runtimePropertiesBeforeInstall);
				}
				if (!hasErrors()) {
					reportError(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, DEFAULT_PAGE, "");
				}
				File runtimePropertiesFile = openedRuntimePropertiesFile;
				if (setupComplete || runtimePropertiesFile == null || !runtimePropertiesFile.exists()) {
					return;
				}
				if (runtimePropertiesFile.delete()) {
					log.warn("Deleted {}, which the failed install had begun to write",
					    runtimePropertiesFile.getAbsolutePath());
				} else {
					log.error("Unable to delete {} after the install failed. Delete it before starting OpenMRS again.",
					    runtimePropertiesFile.getAbsolutePath());
				}
			} finally {
				setInstallationStarted(false);
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
		private boolean verifyConnection(String connectionUsername, String connectionPassword,
		        String databaseConnectionFinalUrl) {
			try {
				// verify connection
				DatabaseUtil.loadDatabaseDriver(databaseConnectionFinalUrl, wizardModel.loadedDriverString);
				try (Connection ignored = DriverManager.getConnection(databaseConnectionFinalUrl, connectionUsername,
				    connectionPassword)) {
					return true;
				}
			} catch (Exception e) {
				reportError(ErrorMessageConstants.ERROR_DB_CONN_VERIFY, DEFAULT_PAGE, connectionUsername, e.getMessage());
				log.warn("Error while checking the connection user account", e);
				return false;
			}
		}

		/**
		 * @param silent if this statement fails do not log the stack trace or report an error for this
		 *            install.
		 * @param user username to connect with
		 * @param pw password to connect with
		 * @param sql String containing sql and question marks
		 * @param args the identifiers and literals to fill into the question marks in the given sql
		 * @return result of executeUpdate or -1 for error
		 */
		private int executeStatement(boolean silent, String user, String pw, String sql, SqlArgument... args) {

			Connection connection = null;
			Statement statement = null;
			try {
				// TODO how to get the driver for the other dbs...
				if (isCurrentDatabase(wizardModel, DATABASE_MYSQL)) {
					Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
				} else if (isCurrentDatabase(wizardModel, DATABASE_MARIADB)) {
					Class.forName("org.mariadb.jdbc.Driver").newInstance();
				} else if (isCurrentDatabase(wizardModel, DATABASE_POSTGRESQL)) {
					Class.forName("org.postgresql.Driver").newInstance();
				}

				String tempDatabaseConnection;
				if (sql.contains("create database")) {
					tempDatabaseConnection = wizardModel.databaseConnection.replace("@DBNAME@", ""); // make this dbname agnostic so we can create the db
				} else {
					tempDatabaseConnection = wizardModel.databaseConnection.replace("@DBNAME@", wizardModel.databaseName);
				}

				connection = DriverManager.getConnection(tempDatabaseConnection, user, pw);

				// run the sql statement
				statement = connection.createStatement();

				return statement.executeUpdate(fillIn(statement, sql, args));

			} catch (SQLException sqlex) {
				if (!silent) {
					// log and add error
					log.warn("error executing sql: " + sql, sqlex);
					reportError(ErrorMessageConstants.ERROR_DB_EXEC_SQL, DEFAULT_PAGE, sql, sqlex.getMessage());
				}
			} catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
				log.error("Error generated", e);
			} finally {
				try {
					if (statement != null) {
						statement.close();
					}
				} catch (SQLException e) {
					log.warn("Error while closing statement");
				}
				try {

					if (connection != null) {
						connection.close();
					}
				} catch (Exception e) {
					log.warn("Error while closing connection", e);
				}
			}

			return -1;
		}

		/**
		 * Replaces the question marks in the sql with the arguments in order, each quoted by the driver of
		 * the statement that runs it
		 */
		private String fillIn(Statement statement, String sql, SqlArgument... args) throws SQLException {
			StringBuilder filledIn = new StringBuilder();
			int next = 0;
			for (char c : sql.toCharArray()) {
				if (c == '?' && next < args.length) {
					filledIn.append(args[next++].quote(statement));
				} else {
					filledIn.append(c);
				}
			}
			return filledIn.toString();
		}
	}

	/**
	 * A database or user name, or a password, that the wizard puts into the sql it runs
	 */
	@FunctionalInterface
	private interface SqlArgument {

		/**
		 * @param statement the statement that will run the sql
		 * @return the argument quoted for the statement's driver
		 */
		String quote(Statement statement) throws SQLException;

		static SqlArgument identifier(String name) {
			return statement -> statement.enquoteIdentifier(name, true);
		}

		static SqlArgument literal(String value) {
			return statement -> statement.enquoteLiteral(value);
		}
	}

	/**
	 * Convenience method that loads the database driver
	 *
	 * @param connection the database connection string
	 * @param databaseDriver the database driver class name to load
	 * @return the loaded driver string
	 */
	public static String loadDriver(String connection, String databaseDriver) {
		String loadedDriverString = null;
		try {
			loadedDriverString = DatabaseUtil.loadDatabaseDriver(connection, databaseDriver);
			log.info("using database driver :" + loadedDriverString);
		} catch (ClassNotFoundException e) {
			log.error("The given database driver class was not found. "
			        + "Please ensure that the database driver jar file is on the class path "
			        + "(like in the webapp's lib folder)");
		}

		return loadedDriverString;
	}

	/**
	 * Utility method that checks if there is a runtime properties file containing database connection
	 * credentials
	 *
	 * @return
	 */
	private static boolean skipDatabaseSetupPage() {
		Properties props = OpenmrsUtil.getRuntimeProperties(WebConstants.WEBAPP_NAME);
		return (props != null && StringUtils.hasText(props.getProperty("connection.url"))
		        && StringUtils.hasText(props.getProperty("connection.username"))
		        && StringUtils.hasText(props.getProperty("connection.password")));
	}

	/**
	 * Utility methods that checks if the user clicked the back image
	 *
	 * @param httpRequest
	 * @return
	 */
	private static boolean goBack(HttpServletRequest httpRequest) {
		return "Back".equals(httpRequest.getParameter("back"))
		        || (httpRequest.getParameter("back.x") != null && httpRequest.getParameter("back.y") != null);
	}

	private String normalizeEnvVariableKey(String envVarKey) {
		if (NON_NORMALIZED_KEYS.contains(envVarKey)) {
			return envVarKey.toLowerCase();
		}
		return envVarKey.toLowerCase().replace('_', '.');
	}

	protected Map<String, String> getEnvironmentVariables() {
		return System.getenv();
	}

	/**
	 * Convenience method to get custom installation script
	 *
	 * @return Properties from custom installation script or empty if none specified
	 * @throws RuntimeException if path to installation script is invalid
	 */
	protected Properties getInstallationScript() {
		Properties prop = new Properties();

		String fileName = System.getProperty("OPENMRS_INSTALLATION_SCRIPT");
		if (fileName == null) {
			return prop;
		}
		if (fileName.startsWith("classpath:")) {
			fileName = fileName.substring(10);
			InputStream input = null;
			try {
				input = getClass().getClassLoader().getResourceAsStream(fileName);
				if (input == null) {
					return null;
				}
				prop.load(input);
				log.info("Using installation script from classpath: {}", fileName);

				input.close();
			} catch (IOException ex) {
				log.error("Failed to load installation script from classpath: {}", fileName, ex);
				throw new RuntimeException(ex);
			} finally {
				IOUtils.closeQuietly(input);
			}
		} else {
			File file = new File(fileName);
			if (file.exists()) {
				InputStream input = null;
				try {
					input = new FileInputStream(fileName);
					prop.load(input);
					log.info("Using installation script from absolute path: {}", file.getAbsolutePath());

					input.close();
				} catch (IOException ex) {
					log.error("Failed to load installation script from absolute path: {}", file.getAbsolutePath(), ex);
					throw new RuntimeException(ex);
				} finally {
					IOUtils.closeQuietly(input);
				}
			}
		}
		return prop;
	}

}
