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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.util.ErrorMessageConstants;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that each client walks the setup wizard with its own values, that an install runs with the
 * values of the client that started it, and that no wizard page renders a stored password. Pages
 * are rendered through Velocity, as the wizard serves them.
 */
class InitializationFilterSessionTest {

	private static final String TABLES_AND_USER_PAGE = "databasetablesanduser.vm";

	/**
	 * A password input rendered with a value, whatever the value
	 */
	private static final Pattern PREFILLED_PASSWORD_INPUT = Pattern
	        .compile("<input(?=[^>]*\\stype=\"password\")(?=[^>]*\\svalue=)[^>]*>");

	@TempDir
	Path applicationDataDirectory;

	private String originalApplicationDataDirectory;

	private Properties originalRuntimeProperties;

	private RecordingInitializationFilter filter;

	private MockServletContext servletContext;

	/**
	 * Renders pages as the wizard does, and records the installs it is asked to start. Unless told to
	 * run them, it creates each install without running it, so a test can look at the wizard while an
	 * install is in progress.
	 */
	static class RecordingInitializationFilter extends InitializationFilter {

		final List<InitializationWizardModel> installsStarted = new ArrayList<>();

		final Map<String, String> environment = new HashMap<>();

		final Properties installationScript = new Properties();

		boolean runInstalls = false;

		@Override
		protected void startInstallation(InitializationWizardModel model) {
			installsStarted.add(model);
			if (runInstalls) {
				super.startInstallation(model);
			} else {
				initJob = new InitializationCompletion(model);
				setInstallationStarted(true);
			}
		}

		@Override
		protected Map<String, String> getEnvironmentVariables() {
			return environment;
		}

		@Override
		protected Properties getInstallationScript() {
			return installationScript;
		}
	}

	@BeforeEach
	void setUp() throws ServletException {
		// created first so logging is set up before OpenmrsConstants is first read, which fails the other way round
		filter = new RecordingInitializationFilter();
		originalApplicationDataDirectory = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		OpenmrsUtil.setApplicationDataDirectory(applicationDataDirectory.toString());
		originalRuntimeProperties = Context.getRuntimeProperties();
		Context.setRuntimeProperties(new Properties());

		servletContext = new MockServletContext();
		filter.init(new MockFilterConfig(servletContext));
		filter.setInitializationComplete(false);
		InitializationFilter.setInstallationStarted(false);
	}

	@AfterEach
	void tearDown() {
		InitializationFilter.InitializationCompletion job = filter.initJob;
		if (filter.runInstalls && job != null) {
			job.waitForCompletion();
		}
		InitializationFilter.setInstallationStarted(false);
		filter.setInitializationComplete(false);
		Context.setRuntimeProperties(originalRuntimeProperties);
		OpenmrsUtil.setApplicationDataDirectory(originalApplicationDataDirectory);
	}

	@Test
	void shouldNotShowTheValuesOneSessionEnteredToAnotherSession() throws Exception {
		MockHttpSession first = startSession();
		post(first, "installmethod.vm", "install_method", "advanced");
		post(first, "databasesetup.vm", "database_connection", "jdbc:mysql://first-session-host:3306/@DBNAME@",
		    "database_driver", "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "yes",
		    "openmrs_current_database_name", "first_session_db");

		MockHttpSession second = startSession();
		String page = post(second, "installmethod.vm", "install_method", "advanced");

		assertThat(page, containsString("name=\"database_connection\""));
		assertThat(page, not(containsString("first-session-host")));
		assertThat(page, not(containsString("first_session_db")));
	}

	@Test
	void shouldNotShowTheErrorsOfOneSessionToAnotherSession() throws Exception {
		MockHttpSession first = startSession();
		post(first, "installmethod.vm", "install_method", "advanced");
		String firstPage = post(first, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		    "database_driver", "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "no", "openmrs_new_database_name", "",
		    "create_database_username", "root", "create_database_password", "secret");
		assertThat(firstPage, containsString("New database name is required"));

		MockHttpSession second = startSession();
		String secondPage = get(second, "installmethod.vm");

		assertThat(secondPage, containsString("name=\"install_method\""));
		assertThat(secondPage, not(containsString("New database name is required")));
	}

	@Test
	void shouldMoveOnOnceAPageWithAnErrorIsPostedAgainWithValidValues() throws Exception {
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "advanced");
		String withError = post(session, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		    "database_driver", "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "yes",
		    "openmrs_current_database_name", "");
		assertThat(withError, containsString("Current database name is required"));

		String next = post(session, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		    "database_driver", "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "yes",
		    "openmrs_current_database_name", "openmrs");

		assertThat(next, containsString("value=\"databasetablesanduser.vm\""));
		assertThat(next, not(containsString("Current database name is required")));
	}

	@Test
	void shouldNotStartAnInstallForASessionThatWasNotShownTheConfirmationPage() throws Exception {
		MockHttpSession first = startSession();
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:mysql://localhost:3306/@DBNAME@");

		MockHttpSession second = startSession();
		post(second, "wizardcomplete.vm");

		assertThat(filter.installsStarted, empty());
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	@Test
	void shouldInstallWithTheValuesOfTheSessionThatStartedTheInstall() throws Exception {
		MockHttpSession first = startSession();
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:mysql://localhost:3306/@DBNAME@");

		MockHttpSession second = startSession();
		post(second, "installmethod.vm", "install_method", "advanced");
		post(second, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@", "database_driver",
		    "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "yes", "openmrs_current_database_name",
		    "second_session_db");

		post(first, "wizardcomplete.vm");

		assertEquals(1, filter.installsStarted.size());
		InitializationWizardModel installed = filter.installsStarted.get(0);
		assertSame(filter.getWizardModel(first), installed);
		assertEquals("first_session_db", installed.databaseName);
	}

	@Test
	void shouldReportTheDetailsOfAFailedInstallOnlyToTheSessionThatStartedIt() throws Exception {
		filter.runInstalls = true;
		MockHttpSession first = startSession();
		// an in-memory database that does not exist, so the install fails at its first connection
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE");
		MockHttpSession second = startSession();

		post(first, "wizardcomplete.vm");
		filter.initJob.waitForCompletion();

		Map<String, Object> firstProgress = poll(first);
		assertEquals(true, firstProgress.get("hasErrors"));
		assertThat(firstProgress, hasKey("errorPage"));
		assertThat(firstProgress, hasKey("logLines"));
		assertThat(filter.getWizardModel(first).errors, hasKey("install.error.connVerify"));

		Map<String, Object> secondProgress = poll(second);
		assertEquals(true, secondProgress.get("hasErrors"));
		assertThat(secondProgress, not(hasKey("errorPage")));
		assertThat(secondProgress, not(hasKey("logLines")));
		assertTrue(filter.getWizardModel(second).errors.isEmpty());
	}

	@Test
	void shouldNotShowTheConnectionOfAFailedInstallToAnotherSession() throws Exception {
		// what a standalone launcher's runtime properties file gives the wizard when OpenMRS starts
		writeExistingRuntimeProperties("StandalonePassword1");
		Properties runtimePropertiesBeforeInstall = Context.getRuntimeProperties();
		filter.runInstalls = true;
		// test data with no server to import it from, so the install fails at the step after the one that checks
		// its database connection
		filter.installationScript.setProperty("import_test_data", "true");
		String firstConnection = "jdbc:h2:mem:first_session_" + UUID.randomUUID().toString().replace("-", "");
		MockHttpSession first = startSession();
		post(first, "installmethod.vm", "install_method", "advanced");
		post(first, "databasesetup.vm", "database_connection", firstConnection, "database_driver", "",
		    "current_openmrs_database", "yes", "openmrs_current_database_name", "first_session_db");
		post(first, TABLES_AND_USER_PAGE, "create_tables", "no", "current_database_user", "yes", "current_database_username",
		    "first_session_user", "current_database_password", "FirstSessionPassword1");
		post(first, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no");
		post(first, "adminusersetup.vm", "new_admin_password", "Admin123", "new_admin_password_confirm", "Admin123");
		post(first, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "", "pass_phrase", "",
		    "description", "");
		post(first, "wizardcomplete.vm");
		filter.initJob.waitForCompletion();
		assertThat(filter.initJob.getErrors(), hasKey(ErrorMessageConstants.ERROR_DB_IMPORT_TEST_DATA));

		MockHttpSession second = startSession();
		post(second, "chooselang.vm", "locale", "en");
		post(second, "installmethod.vm", "install_method", "simple");
		String confirmation = post(second, "simplesetup.vm", "database_type", "mysql", "database_root_password",
		    "SecondSessionRoot1");

		assertThat(confirmation, containsString("value=\"wizardcomplete.vm\""));
		assertThat(confirmation, not(containsString(firstConnection)));
		assertThat(confirmation, containsString("jdbc:mysql://localhost:3306/openmrs"));
		InitializationWizardModel secondModel = filter.getWizardModel(second);
		assertEquals("openmrs", secondModel.createDatabaseUsername);
		assertEquals("openmrs", secondModel.currentDatabaseUsername);
		assertEquals("StandalonePassword1", secondModel.currentDatabasePassword);
		assertEquals(runtimePropertiesBeforeInstall, Context.getRuntimeProperties());
	}

	@Test
	void shouldNotInstallAgainOnceAnEarlierInstallFailedWhileStartingOpenmrs() throws Exception {
		MockHttpSession session = startSession();
		walkAdvancedWizardToConfirmation(session, "openmrs", "jdbc:mysql://localhost:3306/@DBNAME@");
		// what an earlier install that started OpenMRS and then failed leaves behind
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, new Object());

		String page = post(session, "wizardcomplete.vm");

		assertThat(filter.installsStarted, empty());
		assertThat(page, containsString("value=\"wizardcomplete.vm\""));
		assertThat(page, containsString("Restart OpenMRS before you install again"));
	}

	@Test
	void shouldAskForTheConfirmationAgainOnceAnEarlierPageIsPosted() throws Exception {
		MockHttpSession session = startSession();
		walkAdvancedWizardToConfirmation(session, "openmrs", "jdbc:mysql://localhost:3306/@DBNAME@");
		// as from another tab of the same session, once the confirmation page was shown
		post(session, "adminusersetup.vm", "new_admin_password", "Other123", "new_admin_password_confirm", "Other123");

		post(session, "wizardcomplete.vm");
		assertThat(filter.installsStarted, empty());

		String confirmation = post(session, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "",
		    "pass_phrase", "", "description", "");
		assertThat(confirmation, containsString("value=\"wizardcomplete.vm\""));
		post(session, "wizardcomplete.vm");
		assertEquals(1, filter.installsStarted.size());
		assertEquals("Other123", filter.installsStarted.get(0).adminUserPassword);
	}

	@Test
	void shouldShowAnotherSessionTheTasksOfTheRunningInstall() throws Exception {
		MockHttpSession first = startSession();
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:mysql://localhost:3306/@DBNAME@");
		post(first, "wizardcomplete.vm");
		assertTrue(InitializationFilter.isInstallationStarted());
		// as the install records its steps
		filter.getWizardModel(first).workLog.add("Created database first_session_db");

		MockHttpSession second = startSession();
		String page = get(second, null);

		assertThat(page, containsString("UPDATE_TO_LATEST_task_loading"));
		assertThat(page, containsString("CREATE_TABLES_task_loading"));
		assertThat(page, not(containsString("first_session_db")));
		assertThat(get(first, null), containsString("first_session_db"));
	}

	@Test
	void shouldLeaveTheValuesOfTheRunningInstallAloneWhileItsProgressIsShown() throws Exception {
		MockHttpSession first = startSession();
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:mysql://localhost:3306/@DBNAME@");
		post(first, "wizardcomplete.vm");
		// a value the wizard would apply to the model of a session on its next page
		filter.installationScript.setProperty("database_name", "from_script");

		poll(first);
		get(first, null);
		MockHttpSession other = new MockHttpSession();
		poll(other);

		assertEquals("first_session_db", filter.installsStarted.get(0).databaseName);
		assertNull(other.getAttribute(InitializationFilter.WIZARD_MODEL_ATTRIBUTE));
	}

	@Test
	void shouldForgetTheWizardValuesOfEachSessionThatPollsOnceTheInstallCompletes() throws Exception {
		MockHttpSession first = startSession();
		MockHttpSession second = startSession();
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:mysql://localhost:3306/@DBNAME@");
		post(first, "wizardcomplete.vm");
		assertTrue(second.getAttribute(InitializationFilter.WIZARD_MODEL_ATTRIBUTE) instanceof InitializationWizardModel);
		filter.initJob.finishInstallation(true);

		assertEquals(true, poll(second).get("initializationComplete"));
		assertEquals(true, poll(first).get("initializationComplete"));

		assertNull(first.getAttribute(InitializationFilter.WIZARD_MODEL_ATTRIBUTE));
		assertNull(second.getAttribute(InitializationFilter.WIZARD_MODEL_ATTRIBUTE));
	}

	@Test
	void shouldSayOnTheAdminPageThatOnlyTheSeededAdminPasswordOfADatabaseWithTablesIsReplaced() throws Exception {
		String note = "only replaced if it is still the default one";
		MockHttpSession existingTables = startSession();
		post(existingTables, "installmethod.vm", "install_method", "advanced");
		post(existingTables, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		    "database_driver", "", "current_openmrs_database", "yes", "openmrs_current_database_name", "openmrs");
		post(existingTables, TABLES_AND_USER_PAGE, "create_tables", "no", "current_database_user", "yes",
		    "current_database_username", "openmrs", "current_database_password", "openmrs");
		String existingTablesPage = post(existingTables, "otherruntimeproperties.vm", "module_web_admin", "yes",
		    "auto_update_database", "no");

		MockHttpSession newTables = startSession();
		post(newTables, "installmethod.vm", "install_method", "advanced");
		post(newTables, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@", "database_driver",
		    "", "current_openmrs_database", "yes", "openmrs_current_database_name", "openmrs");
		post(newTables, TABLES_AND_USER_PAGE, "create_tables", "yes", "current_database_user", "yes",
		    "current_database_username", "openmrs", "current_database_password", "openmrs");
		String newTablesPage = post(newTables, "otherruntimeproperties.vm", "module_web_admin", "yes",
		    "auto_update_database", "no");

		assertThat(existingTablesPage, containsString("name=\"new_admin_password\""));
		assertThat(existingTablesPage, containsString(note));
		assertThat(newTablesPage, containsString("name=\"new_admin_password\""));
		assertThat(newTablesPage, not(containsString(note)));
	}

	@Test
	void shouldNotRenderAnyStoredPasswordOnAnyWizardPage() throws Exception {
		Map<String, String> sentinels = new LinkedHashMap<>();
		sentinels.put("environment create_database_password", "SentinelEnvCreateDb1");
		sentinels.put("environment create_user_password", "SentinelEnvCreateUser1");
		sentinels.put("environment remote_password", "SentinelEnvRemote1");
		sentinels.put("environment admin_user_password", "SentinelEnvAdmin1");
		sentinels.put("runtime properties connection.password", "SentinelRuntimeConnection1");
		sentinels.put("form database_root_password", "SentinelFormRoot1");
		sentinels.put("form create_database_password", "SentinelFormCreateDb1");
		sentinels.put("form create_user_password", "SentinelFormCreateUser1");
		sentinels.put("form current_database_password", "SentinelFormCurrent1");
		sentinels.put("form new_admin_password", "SentinelFormAdmin1");

		filter.environment.put("CREATE_DATABASE_PASSWORD", sentinels.get("environment create_database_password"));
		filter.environment.put("CREATE_USER_PASSWORD", sentinels.get("environment create_user_password"));
		filter.environment.put("REMOTE_PASSWORD", sentinels.get("environment remote_password"));
		filter.environment.put("ADMIN_USER_PASSWORD", sentinels.get("environment admin_user_password"));
		writeExistingRuntimeProperties(sentinels.get("runtime properties connection.password"));

		Map<String, String> pages = new LinkedHashMap<>();
		MockHttpSession session = new MockHttpSession();
		pages.put("chooselang.vm", get(session, null));
		pages.put("installmethod.vm", get(session, "installmethod.vm"));
		pages.put("simplesetup.vm", post(session, "installmethod.vm", "install_method", "simple"));
		pages.put("wizardcomplete.vm after simple setup", post(session, "simplesetup.vm", "database_type", "mysql",
		    "database_root_password", sentinels.get("form database_root_password")));
		pages.put("simplesetup.vm after back", post(session, "wizardcomplete.vm", "back", "Back"));
		pages.put("installmethod.vm after back", post(session, "simplesetup.vm", "back", "Back"));
		pages.put("remotedetails.vm", post(session, "installmethod.vm", "install_method", "testing"));
		pages.put("installmethod.vm after testing", post(session, "remotedetails.vm", "back", "Back"));
		// a wizard GET applies the environment's values to the session's model again
		pages.put("installmethod.vm reloaded", get(session, "installmethod.vm"));
		pages.put("databasesetup.vm", post(session, "installmethod.vm", "install_method", "advanced"));
		pages.put("databasesetup.vm with an error",
		    post(session, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		        "database_driver", "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "no", "openmrs_new_database_name",
		        "", "create_database_username", "root", "create_database_password",
		        sentinels.get("form create_database_password")));
		pages.put(TABLES_AND_USER_PAGE,
		    post(session, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		        "database_driver", "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "no", "openmrs_new_database_name",
		        "openmrs_walk", "create_database_username", "root", "create_database_password",
		        sentinels.get("form create_database_password")));
		pages.put(TABLES_AND_USER_PAGE + " with an error",
		    post(session, TABLES_AND_USER_PAGE, "current_database_user", "yes", "current_database_username", "",
		        "current_database_password", sentinels.get("form current_database_password")));
		pages.put(TABLES_AND_USER_PAGE + " with another error", post(session, TABLES_AND_USER_PAGE, "current_database_user",
		    "no", "create_user_username", "", "create_user_password", sentinels.get("form create_user_password")));
		pages.put("otherruntimeproperties.vm", post(session, TABLES_AND_USER_PAGE, "current_database_user", "no",
		    "create_user_username", "root", "create_user_password", sentinels.get("form create_user_password")));
		pages.put("adminusersetup.vm",
		    post(session, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no"));
		pages.put("adminusersetup.vm with an error", post(session, "adminusersetup.vm", "new_admin_password",
		    sentinels.get("form new_admin_password"), "new_admin_password_confirm", "different"));
		pages.put("implementationidsetup.vm",
		    post(session, "adminusersetup.vm", "new_admin_password", sentinels.get("form new_admin_password"),
		        "new_admin_password_confirm", sentinels.get("form new_admin_password")));
		pages.put("wizardcomplete.vm", post(session, "implementationidsetup.vm", "implementation_name", "",
		    "implementation_id", "", "pass_phrase", "", "description", ""));
		pages.put("progress.vm", post(session, "wizardcomplete.vm"));
		pages.put("progress.vm for another session", get(new MockHttpSession(), null));

		for (Map.Entry<String, String> page : pages.entrySet()) {
			assertThat("the walk did not render " + page.getKey(), page.getValue(), containsString("</html>"));
			// each page is labelled with its template first, and each but the progress page posts it back
			String template = page.getKey().split(" ")[0];
			if (!template.equals("progress.vm")) {
				assertThat("the walk did not reach " + page.getKey(), page.getValue(),
				    containsString("name=\"page\" value=\"" + template + "\""));
			}
			assertFalse(PREFILLED_PASSWORD_INPUT.matcher(page.getValue()).find(),
			    page.getKey() + " fills in a password field");
			for (Map.Entry<String, String> sentinel : sentinels.entrySet()) {
				assertFalse(page.getValue().contains(sentinel.getValue()),
				    page.getKey() + " rendered the " + sentinel.getKey());
			}
		}
	}

	/**
	 * Walks the advanced install of an existing database through to the confirmation page.
	 */
	private void walkAdvancedWizardToConfirmation(MockHttpSession session, String databaseName, String connectionUrl)
	        throws Exception {
		post(session, "installmethod.vm", "install_method", "advanced");
		post(session, "databasesetup.vm", "database_connection", connectionUrl, "database_driver", "",
		    "current_openmrs_database", "yes", "openmrs_current_database_name", databaseName);
		post(session, TABLES_AND_USER_PAGE, "create_tables", "yes", "current_database_user", "yes",
		    "current_database_username", "openmrs", "current_database_password", "openmrs");
		post(session, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no");
		post(session, "adminusersetup.vm", "new_admin_password", "Admin123", "new_admin_password_confirm", "Admin123");
		String confirmation = post(session, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "",
		    "pass_phrase", "", "description", "");
		assertThat(confirmation, containsString("value=\"wizardcomplete.vm\""));
	}

	private void writeExistingRuntimeProperties(String connectionPassword) throws IOException {
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty("connection.url", "jdbc:mysql://localhost:3306/openmrs");
		runtimeProperties.setProperty("connection.username", "openmrs");
		runtimeProperties.setProperty("connection.password", connectionPassword);
		File file = new File(applicationDataDirectory.toFile(), WebConstants.WEBAPP_NAME + "-runtime.properties");
		try (OutputStream out = new FileOutputStream(file)) {
			runtimeProperties.store(out, null);
		}
		Context.setRuntimeProperties(runtimeProperties);
	}

	/**
	 * A browser's first request to the wizard
	 */
	private MockHttpSession startSession() throws Exception {
		MockHttpSession session = new MockHttpSession();
		get(session, null);
		return session;
	}

	private String get(MockHttpSession session, String page) throws Exception {
		MockHttpServletRequest request = request(session, "GET");
		if (page != null) {
			request.setParameter("page", page);
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doGet(request, response);
		return response.getContentAsString();
	}

	private String post(MockHttpSession session, String page, String... parameters) throws Exception {
		MockHttpServletRequest request = request(session, "POST");
		request.setParameter("page", page);
		for (int i = 0; i < parameters.length; i += 2) {
			request.setParameter(parameters[i], parameters[i + 1]);
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doPost(request, response);
		return response.getContentAsString();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> poll(MockHttpSession session) throws Exception {
		String json = get(session, "progress.vm.ajaxRequest");
		return new ObjectMapper().readValue(json, Map.class);
	}

	private MockHttpServletRequest request(MockHttpSession session, String method) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, "/" + WebConstants.SETUP_PAGE_URL);
		request.setServletPath("/" + WebConstants.SETUP_PAGE_URL);
		request.setSession(session);
		return request;
	}
}
