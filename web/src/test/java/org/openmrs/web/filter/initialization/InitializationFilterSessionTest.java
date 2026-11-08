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
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmrs.api.context.Context;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.Security;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.util.ErrorMessageConstants;
import org.springframework.mock.web.MockFilterChain;
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
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests that each client walks the setup wizard with its own values, that an install runs with the
 * values of the client that started it, and that no wizard page renders a stored password. Pages
 * are rendered through Velocity, as the wizard serves them.
 */
class InitializationFilterSessionTest {

	private static final String TABLES_AND_USER_PAGE = "databasetablesanduser.vm";

	/**
	 * User 1's password and salt as the core data seeds them, the hash of "test"
	 */
	private static final String SEEDED_PASSWORD_HASH = "4a1750c8607dfa237de36c6305715c223415189";

	private static final String SEEDED_SALT = "c788c6ad82a157b712392ca695dfcf2eed193d7f";

	/**
	 * A password input rendered with a value, whatever the value
	 */
	private static final Pattern PREFILLED_PASSWORD_INPUT = Pattern
	        .compile("<input(?=[^>]*\\stype=\"password\")(?=[^>]*\\svalue=)[^>]*>");

	/**
	 * A reference to a password or passphrase field, which the template data leaves out, so Velocity
	 * shows the reference as written
	 */
	private static final Pattern PASSWORD_FIELD_REFERENCE = Pattern.compile("\\$!?\\{?\\w*(?i:password|passphrase)");

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

		/**
		 * Runs once the install's checks have passed and before the install is started, as another request
		 * can
		 */
		Runnable beforeStart;

		@Override
		protected void startInstallation(InitializationWizardModel model, boolean automatic) {
			if (beforeStart != null) {
				beforeStart.run();
			}
			installsStarted.add(model);
			if (runInstalls) {
				super.startInstallation(model, automatic);
			} else {
				initJob = new InitializationCompletion(model, automatic);
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
	void shouldStartTheWizardAgainAndSayWhyOnceInstallIsClickedAfterTheSessionExpired() throws Exception {
		MockHttpSession expired = startSession();
		walkAdvancedWizardToConfirmation(expired, "clinic_db", "jdbc:mysql://localhost:3306/@DBNAME@");
		// the browser's session expired, so its next request starts a new one
		MockHttpSession session = new MockHttpSession();

		String page = post(session, "wizardcomplete.vm");

		assertThat(filter.installsStarted, empty());
		assertThat(page, containsString("name=\"page\" value=\"chooselang.vm\""));
		assertThat(page, containsString("This browser no longer has its setup session"));
	}

	@Test
	void shouldStartTheWizardAgainAndSayWhyOnceAPageIsPostedAfterTheSessionExpired() throws Exception {
		MockHttpSession session = new MockHttpSession();

		String page = post(session, TABLES_AND_USER_PAGE, "create_tables", "yes", "current_database_user", "yes",
		    "current_database_username", "posted_user", "current_database_password", "secret");

		// rather than moving on to the next page with the default values of the pages before
		assertThat(page, containsString("name=\"page\" value=\"chooselang.vm\""));
		assertThat(page, containsString("This browser no longer has its setup session"));
		// posted again, as after going back to it in the browser, or from another tab
		String again = post(session, TABLES_AND_USER_PAGE, "create_tables", "yes", "current_database_user", "yes",
		    "current_database_username", "posted_user", "current_database_password", "secret");
		assertThat(again, containsString("name=\"page\" value=\"chooselang.vm\""));
		assertThat(again, containsString("This browser no longer has its setup session"));
		assertNotEquals("posted_user", filter.getWizardModel(session).currentDatabaseUsername);
		String next = post(session, "chooselang.vm", "locale", "en");
		assertThat(next, containsString("name=\"page\" value=\"installmethod.vm\""));
		assertThat(next, not(containsString("This browser no longer has its setup session")));
	}

	@Test
	void shouldNotTakeAPageTheWizardDidNotShowTheSessionWhileItWalksTheWizard() throws Exception {
		// a new session, as a browser starts in another tab once its first session expired
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "advanced");

		// a page of the expired session
		String page = post(session, "adminusersetup.vm", "new_admin_password", "Posted123", "new_admin_password_confirm",
		    "Posted123");

		assertThat(page, containsString("name=\"page\" value=\"chooselang.vm\""));
		assertThat(page, containsString("This browser no longer has its setup session"));
		InitializationWizardModel model = filter.getWizardModel(session);
		assertNotEquals("Posted123", model.adminUserPassword);
		// the session's own walk goes on with what it entered
		String next = post(session, "databasesetup.vm", "database_connection", "jdbc:mysql://localhost:3306/@DBNAME@",
		    "database_driver", "", "current_openmrs_database", "yes", "openmrs_current_database_name", "clinic_db");
		assertThat(next, containsString("name=\"page\" value=\"databasetablesanduser.vm\""));
		assertEquals("advanced", model.installMethod);
	}

	@Test
	void shouldStartTheWizardAgainAndSayWhyOnceABrowserThatSendsNoCookiePostsAPage() throws Exception {
		MockHttpServletRequest request = request(new MockHttpSession(), "POST");
		request.setRequestedSessionId(null);
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doPost(request, response);

		String page = response.getContentAsString();
		assertThat(page, containsString("name=\"page\" value=\"chooselang.vm\""));
		assertThat(page, containsString("This browser no longer has its setup session"));
		assertThat(page, containsString("cookies for this site are cleared or blocked"));
	}

	@Test
	void shouldSayOnlyWhyTheWizardStartsAgainRatherThanWhatAPageTheSessionWasShownReported() throws Exception {
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "simple");
		String withError = post(session, "simplesetup.vm", "database_type", "mysql", "database_root_password", "");
		assertThat(withError, containsString("Database root password is required"));

		// a page of an expired session, posted from another tab
		String page = post(session, "adminusersetup.vm", "new_admin_password", "Posted123", "new_admin_password_confirm",
		    "Posted123");

		assertThat(page, containsString("This browser no longer has its setup session"));
		assertThat(page, not(containsString("Database root password is required")));
	}

	@ParameterizedTest
	@ValueSource(strings = { "chooselang.vm", "adminusersetup.vm" })
	void shouldGiveASessionWhoseFirstRequestIsAPostTheConfiguration(String firstPage) throws Exception {
		filter.installationScript.setProperty("property.clinic.setting", "configured");
		MockHttpSession session = new MockHttpSession();

		post(session, firstPage, "locale", "en");

		assertEquals("configured",
		    filter.getWizardModel(session).additionalPropertiesFromInstallationScript.get("clinic.setting"));
	}

	@Test
	void shouldTakeTheFirstPageFromASessionThatHasNoValuesYet() throws Exception {
		// nothing is entered before the first page, so a session that expired there loses nothing
		MockHttpSession session = new MockHttpSession();

		String page = post(session, "chooselang.vm", "locale", "en");

		assertThat(page, containsString("name=\"page\" value=\"installmethod.vm\""));
		assertThat(page, not(containsString("This browser no longer has its setup session")));
	}

	@Test
	void shouldSayWhyAnInstallDidNotStartOnceTheSetupWasOpenedAgainAfterItsConfirmationPage() throws Exception {
		MockHttpSession session = startSession();
		walkAdvancedWizardToConfirmation(session, "clinic_db", "jdbc:mysql://localhost:3306/@DBNAME@");
		// as from another tab of the same browser
		get(session, null);

		String page = post(session, "wizardcomplete.vm");

		assertThat(filter.installsStarted, empty());
		assertThat(page, containsString("name=\"page\" value=\"chooselang.vm\""));
		assertThat(page, containsString("The install did not start"));
	}

	@Test
	void shouldSendARequestForAutoRunOpenmrsToTheWizardRatherThanInstall() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auto_run_openmrs");
		request.setServletPath("/auto_run_openmrs");
		request.setSession(new MockHttpSession());
		request.setParameter("database_root_password", "RootPassword1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertEquals("/" + WebConstants.WEBAPP_NAME + "/" + WebConstants.SETUP_PAGE_URL, response.getRedirectedUrl());
		assertThat(filter.installsStarted, empty());
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	@Test
	void shouldNotStartAnInstallForAClientThatPostsTheAutomaticInstallMethod() throws Exception {
		// the install method page offers no automatic install, which starts without the confirmation page
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "auto");

		get(session, null);

		assertThat(filter.installsStarted, empty());
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	@Test
	void shouldInstallWithTheValuesItCheckedThoughAnotherRequestOfTheSessionArrivesMeanwhile() throws Exception {
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("admin_user_password", "Admin123");
		MockHttpSession session = new MockHttpSession();
		CountDownLatch otherRequestDone = new CountDownLatch(1);
		Thread[] otherRequest = new Thread[1];
		filter.beforeStart = () -> {
			filter.beforeStart = null;
			otherRequest[0] = new Thread(() -> {
				try {
					post(session, "adminusersetup.vm", "new_admin_password", "Other123", "new_admin_password_confirm",
					    "Other123");
				} catch (Exception e) {
					throw new IllegalStateException(e);
				} finally {
					otherRequestDone.countDown();
				}
			});
			otherRequest[0].start();
			// the other request either changes the model now, after the checks, or waits for this one to finish
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			while (otherRequestDone.getCount() > 0 && otherRequest[0].getState() != Thread.State.BLOCKED
			        && System.nanoTime() < deadline) {
				Thread.onSpinWait();
			}
		};

		get(session, null);
		otherRequest[0].join(TimeUnit.SECONDS.toMillis(10));

		assertEquals(1, filter.installsStarted.size());
		assertEquals("Admin123", filter.installsStarted.get(0).adminUserPassword);
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
		writeExistingRuntimeProperties("jdbc:mysql://localhost:3306/openmrs", "StandalonePassword1");
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
	void shouldShowEverySessionWhyTheLastAutomaticInstallFailed() throws Exception {
		filter.runInstalls = true;
		String database = configureAutomaticInstallThatCannotConnect();
		// started by a request no browser makes, as the health check's
		get(new MockHttpSession(), null);
		filter.initJob.waitForCompletion();

		String page = get(new MockHttpSession(), null);

		assertThat(page, containsString("does not work"));
		assertThat(page, containsString(database));
		// each error is shown through a message, which the page marks with ??? when it has none
		assertThat(page, not(containsString("???")));
	}

	@Test
	void shouldGiveTheServerLogLinesOfAnAutomaticInstallOnlyToTheSessionThatStartedIt() throws Exception {
		configureAutomaticInstallThatCannotConnect();
		MockHttpSession starter = new MockHttpSession();
		get(starter, null);

		assertThat(poll(starter), hasKey("logLines"));
		assertThat(poll(new MockHttpSession()), not(hasKey("logLines")));
	}

	@Test
	void shouldShowEverySessionTheStepsTheLastAutomaticInstallCompletedBeforeItFailed() throws Exception {
		filter.runInstalls = true;
		configureAutomaticInstallThatCannotConnect();
		// each install creates the database, which needs no statement on H2, before its connection fails
		filter.installationScript.setProperty("has_current_openmrs_database", "false");
		// two installs, as the health check starts one every few seconds
		for (int i = 0; i < 2; i++) {
			get(new MockHttpSession(), null);
			filter.initJob.waitForCompletion();
		}
		// then refused, so no install of the page's own lists steps on it
		filter.installationScript.setProperty("admin_user_password", "weak");

		String page = get(new MockHttpSession(), null);

		assertEquals(2, filter.installsStarted.size());
		// each install lists only its own steps
		assertEquals(List.of("Created database openmrs"), filter.installsStarted.get(1).workLog);
		assertEquals(2, page.split("<li>Created database openmrs</li>", -1).length);
	}

	@Test
	void shouldHaveAnotherRequestOfTheSessionWaitWhileItsAutomaticInstallStarts() throws Exception {
		configureAutomaticInstallThatCannotConnect();
		MockHttpSession session = new MockHttpSession();
		Thread[] otherRequest = new Thread[1];
		Thread.State[] otherRequestState = new Thread.State[1];
		filter.beforeStart = () -> {
			filter.beforeStart = null;
			// as from a second tab of the same browser
			otherRequest[0] = new Thread(() -> {
				try {
					get(session, null);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
			otherRequest[0].start();
			// the other request either waits for this one, or runs to its end meanwhile
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
			while (otherRequest[0].isAlive() && otherRequest[0].getState() != Thread.State.BLOCKED
			        && System.nanoTime() < deadline) {
				Thread.onSpinWait();
			}
			otherRequestState[0] = otherRequest[0].getState();
		};

		get(session, null);
		otherRequest[0].join(TimeUnit.SECONDS.toMillis(10));

		assertEquals(Thread.State.BLOCKED, otherRequestState[0]);
		assertEquals(1, filter.installsStarted.size());
		assertSame(filter.initJob.wizardModel, filter.getWizardModel(session));
		assertThat(poll(session), hasKey("logLines"));
	}

	@Test
	void shouldGiveTheFirstRequestsOfASessionTheSameLockThoughTheyArriveTogether() throws Exception {
		// a session in which the first request to find no lock waits a while for another request to look for it too
		CountDownLatch secondLookup = new CountDownLatch(1);
		AtomicInteger lookups = new AtomicInteger();
		MockHttpSession session = new MockHttpSession() {

			@Override
			public Object getAttribute(String name) {
				Object value = super.getAttribute(name);
				if (InitializationFilter.WIZARD_LOCK_ATTRIBUTE.equals(name) && value == null) {
					if (lookups.incrementAndGet() == 1) {
						try {
							secondLookup.await(2, TimeUnit.SECONDS);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					} else {
						secondLookup.countDown();
					}
				}
				return value;
			}
		};
		Object[] locks = new Object[2];
		Thread first = new Thread(() -> locks[0] = filter.getWizardLock(session));
		Thread second = new Thread(() -> locks[1] = filter.getWizardLock(session));

		first.start();
		second.start();
		first.join(TimeUnit.SECONDS.toMillis(10));
		second.join(TimeUnit.SECONDS.toMillis(10));

		assertSame(locks[0], locks[1]);
	}

	@Test
	void shouldSendARequestThatFindsTheInstallCompletedToTheProgressPage() throws Exception {
		configureAutomaticInstallThatCannotConnect();
		// what a successful install leaves for a request that got past the filter while it ran
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, new Object());
		filter.setInitializationComplete(true);

		String page = get(new MockHttpSession(), null);

		// whose first poll sends the browser on
		assertThat(page, containsString("showProgress();"));
		assertThat(page, not(containsString("Restart OpenMRS before you install again")));
	}

	@Test
	void shouldShowWhyAnAutomaticInstallFailedWhileStartingOpenmrsOnAPageThatStopsPolling() throws Exception {
		filter.runInstalls = true;
		String database = configureAutomaticInstallThatCannotConnect();
		get(new MockHttpSession(), null);
		filter.initJob.waitForCompletion();
		// what an install that failed while starting OpenMRS leaves behind
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, new Object());

		String page = get(new MockHttpSession(), null);

		assertEquals(1, filter.installsStarted.size());
		assertThat(page, containsString("Restart OpenMRS before you install again"));
		assertThat(page, containsString(database));
		// polling would find the failed install's errors and load the setup page again, over and over
		assertThat(page, not(containsString("showProgress();")));
	}

	@Test
	void shouldLogOnlyOnceThatNoInstallStartsUntilOpenmrsRestarts() throws Exception {
		configureAutomaticInstallThatCannotConnect();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, new Object());
		// a name of its own, since memory appenders of the same name share their lines
		MemoryAppender appender = MemoryAppender.newBuilder().setName("restartRequired-" + UUID.randomUUID())
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		appender.start();
		// the wizard's own lines, and those of looking up the runtime properties file, which there is none of
		List<Logger> loggers = List.of((Logger) LogManager.getLogger(InitializationFilter.class),
		    (Logger) LogManager.getLogger(OpenmrsUtil.class));
		List<Level> levels = new ArrayList<>();
		for (Logger logger : loggers) {
			levels.add(logger.getLevel());
			// the test logging configuration turns logging off
			Configurator.setLevel(logger.getName(), Level.WARN);
			logger.addAppender(appender);
		}
		try {
			// as the health check sends one every few seconds
			for (int i = 0; i < 3; i++) {
				get(new MockHttpSession(), null);
			}

			assertThat(filter.installsStarted, empty());
			assertEquals(1, appender.getLogLines().size(), String.valueOf(appender.getLogLines()));
			assertThat(appender.getLogLines().get(0), startsWith("Not installing OpenMRS again"));
		} finally {
			for (int i = 0; i < loggers.size(); i++) {
				loggers.get(i).removeAppender(appender);
				Configurator.setLevel(loggers.get(i).getName(), levels.get(i));
			}
			appender.stop();
		}
	}

	@Test
	void shouldNotShowAnAutomaticInstallSessionWhyAnInstallFromTheWizardPagesFailed() throws Exception {
		filter.runInstalls = true;
		MockHttpSession first = startSession();
		String firstDatabase = "first_session_" + UUID.randomUUID().toString().replace("-", "");
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:h2:mem:" + firstDatabase + ";IFEXISTS=TRUE");
		post(first, "wizardcomplete.vm");
		filter.initJob.waitForCompletion();
		// then OpenMRS is configured for an automatic install that is refused, so none starts
		configureAutomaticInstallThatCannotConnect();
		filter.installationScript.setProperty("admin_user_password", "weak");

		String page = get(new MockHttpSession(), null);

		assertEquals(1, filter.installsStarted.size());
		assertThat(page, not(containsString(firstDatabase)));
	}

	@Test
	void shouldShowTheSessionThatStartedAnInstallFromTheWizardPagesWhyItFailedAfterAnAutomaticInstallIsConfigured()
	        throws Exception {
		filter.runInstalls = true;
		MockHttpSession first = startSession();
		String firstDatabase = "first_session_" + UUID.randomUUID().toString().replace("-", "");
		walkAdvancedWizardToConfirmation(first, "first_session_db", "jdbc:h2:mem:" + firstDatabase + ";IFEXISTS=TRUE");
		post(first, "wizardcomplete.vm");
		filter.initJob.waitForCompletion();
		// then OpenMRS is configured for an automatic install that is refused, so none starts
		configureAutomaticInstallThatCannotConnect();
		filter.installationScript.setProperty("admin_user_password", "weak");

		String page = get(first, null);

		assertEquals(1, filter.installsStarted.size());
		assertThat(page, containsString(firstDatabase));
	}

	@Test
	void shouldRunAnAutomaticInstallWithTheConfigurationAloneRatherThanValuesAClientPosted() throws Exception {
		filter.runInstalls = true;
		String database = configureAutomaticInstallThatCannotConnect();
		// the configuration names no database user and no implementation id, so the client's session keeps the ones
		// it posts
		MockHttpSession client = new MockHttpSession();
		post(client, TABLES_AND_USER_PAGE, "create_tables", "no", "current_database_user", "yes",
		    "current_database_username", "<b>client_user</b>", "current_database_password", "ClientPassword1");
		post(client, "implementationidsetup.vm", "implementation_name", "Client", "implementation_id", "client_id",
		    "pass_phrase", "ClientPhrase1", "description", "");

		get(client, null);
		filter.initJob.waitForCompletion();
		String otherPage = get(new MockHttpSession(), null);

		InitializationWizardModel installed = filter.installsStarted.get(0);
		assertThat(String.valueOf(installed.currentDatabaseUsername), not(containsString("client_user")));
		assertThat(String.valueOf(installed.implementationId), not(containsString("client_id")));
		assertThat(otherPage, containsString(database));
		assertThat(otherPage, not(containsString("client_user")));
	}

	@ParameterizedTest
	@CsvSource({ "database_name, my--clinic", "admin_user_password, weak",
	        "connection.driver_class, com.microsoft.jdbc.sqlserver.SQLServerDriver" })
	void shouldShowWhyAnAutomaticInstallWasRefusedOnAPageThatDoesNotPoll(String property, String value) throws Exception {
		configureAutomaticInstallThatCannotConnect();
		// a url that takes the database name, which the name rule then applies to
		filter.installationScript.setProperty("connection.url", "jdbc:h2:mem:@DBNAME@;IFEXISTS=TRUE");
		filter.installationScript.setProperty(property, value);

		String page = get(new MockHttpSession(), null);

		assertThat(filter.installsStarted, empty());
		assertThat(page, containsString("class=\"openmrs_error\""));
		assertThat(page, not(containsString("showProgress();")));
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
	void shouldAskForTheImplementationIdPassphraseAgainOnceTheConfirmationPageWentBackToIt() throws Exception {
		MockHttpSession session = startSession();
		walkAdvancedWizardToConfirmation(session, "openmrs", "jdbc:mysql://localhost:3306/@DBNAME@");
		post(session, "wizardcomplete.vm", "back", "Back");
		post(session, "implementationidsetup.vm", "implementation_name", "Clinic", "implementation_id", "CLINIC",
		    "pass_phrase", "ClinicPhrase1", "description", "A clinic");

		String implementationIdPage = post(session, "wizardcomplete.vm", "back", "Back");
		assertThat(implementationIdPage, containsString("value=\"CLINIC\""));
		// the passphrase field comes back empty, so Next posts it empty
		String withoutPassphrase = post(session, "implementationidsetup.vm", "implementation_name", "Clinic",
		    "implementation_id", "CLINIC", "pass_phrase", "", "description", "A clinic");

		assertThat(withoutPassphrase, containsString("name=\"page\" value=\"implementationidsetup.vm\""));
		assertThat(withoutPassphrase, containsString("The Pass Phrase cannot be empty"));
		String confirmation = post(session, "implementationidsetup.vm", "implementation_name", "Clinic", "implementation_id",
		    "CLINIC", "pass_phrase", "ClinicPhrase1", "description", "A clinic");
		assertThat(confirmation, containsString("name=\"page\" value=\"wizardcomplete.vm\""));
		assertEquals("ClinicPhrase1", filter.getWizardModel(session).implementationIdPassPhrase);
	}

	@Test
	void shouldMarkAnInstallIncompleteOnceItHasBegunToPutUsersIntoTheDatabase() throws Exception {
		writeExistingRuntimeProperties("jdbc:mysql://localhost:3306/openmrs", "StandalonePassword1");
		File runtimePropertiesFile = new File(applicationDataDirectory.toFile(),
		        WebConstants.WEBAPP_NAME + "-runtime.properties");
		byte[] runtimePropertiesBeforeInstall = Files.readAllBytes(runtimePropertiesFile.toPath());
		filter.runInstalls = true;
		// test data with no server to import it from, so the install fails importing it
		filter.installationScript.setProperty("import_test_data", "true");
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "advanced");
		post(session, "databasesetup.vm", "database_connection",
		    "jdbc:h2:mem:incomplete_" + UUID.randomUUID().toString().replace("-", ""), "database_driver", "",
		    "current_openmrs_database", "yes", "openmrs_current_database_name", "incomplete_db");
		post(session, TABLES_AND_USER_PAGE, "create_tables", "no", "current_database_user", "yes",
		    "current_database_username", "incomplete_user", "current_database_password", "IncompletePassword1");
		post(session, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no");
		post(session, "adminusersetup.vm", "new_admin_password", "Admin123", "new_admin_password_confirm", "Admin123");
		post(session, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "", "pass_phrase", "",
		    "description", "");

		post(session, "wizardcomplete.vm");
		filter.initJob.waitForCompletion();

		assertThat(filter.initJob.getErrors(), hasKey(ErrorMessageConstants.ERROR_DB_IMPORT_TEST_DATA));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
		assertArrayEquals(runtimePropertiesBeforeInstall, Files.readAllBytes(runtimePropertiesFile.toPath()));
	}

	@Test
	void shouldMarkAnInstallIncompleteBeforeItAddsTheCoreData() throws Exception {
		filter.runInstalls = true;
		String connectionUrl = "jdbc:h2:mem:core_data_" + UUID.randomUUID().toString().replace("-", "")
		        + ";DB_CLOSE_DELAY=-1";
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "advanced");
		post(session, "databasesetup.vm", "database_connection", connectionUrl, "database_driver", "",
		    "current_openmrs_database", "yes", "openmrs_current_database_name", "core_data_db");
		post(session, TABLES_AND_USER_PAGE, "create_tables", "yes", "current_database_user", "yes",
		    "current_database_username", "core_data_user", "current_database_password", "CoreDataPassword1");
		post(session, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no");
		post(session, "adminusersetup.vm", "new_admin_password", "Admin123", "new_admin_password_confirm", "Admin123");
		post(session, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "", "pass_phrase", "",
		    "description", "");

		post(session, "wizardcomplete.vm");
		filter.initJob.waitForCompletion();

		// the install fails starting the web application context, whose configuration this servlet context lacks,
		// after the tables and the core data are in
		assertTrue(filter.initJob.getExecutedTasks().contains(WizardTask.ADD_CORE_DATA));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldStartTheWizardForADatabaseWithTablesWhileAnInstallIsMarkedIncomplete() throws Exception {
		writeExistingRuntimeProperties(databaseWithAdminPassword(SEEDED_PASSWORD_HASH), "InstalledPassword1");
		filter.init(new MockFilterConfig(servletContext));
		assertFalse(InitializationFilter.initializationRequired());

		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);
		filter.init(new MockFilterConfig(servletContext));

		assertTrue(InitializationFilter.initializationRequired());
	}

	@Test
	void shouldFollowTheListenerOnAnInstallMarkedIncompleteRatherThanCheckingItsDatabaseAgain() throws Exception {
		// the Listener, which starts first, deletes the file once the admin user has another password, so a file it
		// left in place means it decided on the wizard, as when its own check failed
		String otherPasswordHash = Security.encodePassword("Admin123" + SEEDED_SALT);
		writeExistingRuntimeProperties(databaseWithAdminPassword(otherPasswordHash), "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);

		filter.init(new MockFilterConfig(servletContext));

		assertTrue(InitializationFilter.initializationRequired());
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldKeepAnInstallMarkedIncompleteWhileItsDatabaseHasTheSeededAdminPassword() throws Exception {
		writeExistingRuntimeProperties(databaseWithAdminPassword(SEEDED_PASSWORD_HASH), "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);

		assertTrue(InitializationFilter.isInstallIncomplete(Context.getRuntimeProperties()));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldNoLongerMarkAnInstallIncompleteOnceItsDatabaseHasAnotherAdminPassword() throws Exception {
		String otherPasswordHash = Security.encodePassword("Admin123" + SEEDED_SALT);
		writeExistingRuntimeProperties(databaseWithAdminPassword(otherPasswordHash), "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);

		assertFalse(InitializationFilter.isInstallIncomplete(Context.getRuntimeProperties()));
		assertFalse(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldKeepAnInstallMarkedIncompleteWhileItsDatabaseHasNoAdminUserYet() throws Exception {
		String connectionUrl = "jdbc:h2:mem:no_admin_" + UUID.randomUUID().toString().replace("-", "")
		        + ";DB_CLOSE_DELAY=-1";
		try (Connection connection = DriverManager.getConnection(connectionUrl, "openmrs", "InstalledPassword1");
		        Statement statement = connection.createStatement()) {
			statement.execute("create table users (user_id int primary key, password varchar(255), salt varchar(255))");
		}
		writeExistingRuntimeProperties(connectionUrl, "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);

		assertTrue(InitializationFilter.isInstallIncomplete(Context.getRuntimeProperties()));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldNotTellWhetherAnInstallMarkedIncompleteFinishedFromADatabaseWithoutUsers() throws Exception {
		String connectionUrl = "jdbc:h2:mem:no_users_" + UUID.randomUUID().toString().replace("-", "")
		        + ";DB_CLOSE_DELAY=-1";
		try (Connection connection = DriverManager.getConnection(connectionUrl, "openmrs", "InstalledPassword1");
		        Statement statement = connection.createStatement()) {
			statement.execute("create table location (location_id int primary key)");
		}
		writeExistingRuntimeProperties(connectionUrl, "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);
		Properties runtimeProperties = Context.getRuntimeProperties();

		assertThrows(SQLException.class, () -> InitializationFilter.isInstallIncomplete(runtimeProperties));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldNotTellWhetherAnInstallMarkedIncompleteFinishedWithoutItsDatabaseDriver() throws Exception {
		writeExistingRuntimeProperties(databaseWithAdminPassword(SEEDED_PASSWORD_HASH), "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);
		Properties runtimeProperties = Context.getRuntimeProperties();
		runtimeProperties.setProperty("connection.driver_class", "org.openmrs.NoSuchDriver");

		assertThrows(SQLException.class, () -> InitializationFilter.isInstallIncomplete(runtimeProperties));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldNotTellWhetherAnInstallMarkedIncompleteFinishedWithoutReachingItsDatabase() throws Exception {
		// an in-memory database that does not exist, as one that cannot be reached
		writeExistingRuntimeProperties("jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE", "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);
		Properties runtimeProperties = Context.getRuntimeProperties();

		assertThrows(SQLException.class, () -> InitializationFilter.isInstallIncomplete(runtimeProperties));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldCheckAnInstallMarkedIncompleteThroughADriverTheWizardDoesNotOffer() throws Exception {
		writeExistingRuntimeProperties(databaseWithAdminPassword(SEEDED_PASSWORD_HASH), "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);
		Properties runtimeProperties = Context.getRuntimeProperties();
		runtimeProperties.setProperty("connection.driver_class", UnofferedDriver.class.getName());

		assertTrue(InitializationFilter.isInstallIncomplete(runtimeProperties));
	}

	/**
	 * A database driver outside the ones the wizard offers, as a wrapping driver a runtime properties
	 * file can name
	 */
	public static class UnofferedDriver extends org.h2.Driver {}

	@Test
	void shouldNoLongerMarkAnInstallIncompleteOnceItsAdminUserHasNoPassword() throws Exception {
		// the core data adds the admin user with the seeded password, so no install leaves it with none
		writeExistingRuntimeProperties(databaseWithAdminPassword(null), "InstalledPassword1");
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);

		assertFalse(InitializationFilter.isInstallIncomplete(Context.getRuntimeProperties()));
		assertFalse(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void shouldStopAnInstallThatCannotMarkItselfIncompleteBeforeItAddsTheCoreData() throws Exception {
		filter.runInstalls = true;
		String connectionUrl = "jdbc:h2:mem:no_core_data_" + UUID.randomUUID().toString().replace("-", "")
		        + ";DB_CLOSE_DELAY=-1";
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "advanced");
		post(session, "databasesetup.vm", "database_connection", connectionUrl, "database_driver", "",
		    "current_openmrs_database", "yes", "openmrs_current_database_name", "no_core_data_db");
		post(session, TABLES_AND_USER_PAGE, "create_tables", "yes", "current_database_user", "yes",
		    "current_database_username", "no_core_data_user", "current_database_password", "NoCoreDataPassword1");
		post(session, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no");
		post(session, "adminusersetup.vm", "new_admin_password", "Admin123", "new_admin_password_confirm", "Admin123");
		post(session, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "", "pass_phrase", "",
		    "description", "");
		File directory = applicationDataDirectory.toFile();
		// root can write to a directory whose write bit is cleared, so this runs only as another user
		assumeTrue(directory.setWritable(false) && !directory.canWrite());
		try {
			post(session, "wizardcomplete.vm");
			filter.initJob.waitForCompletion();
		} finally {
			directory.setWritable(true);
		}

		assertThat(filter.initJob.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		assertTrue(filter.initJob.getExecutedTasks().contains(WizardTask.CREATE_TABLES));
		assertFalse(filter.initJob.getExecutedTasks().contains(WizardTask.ADD_CORE_DATA));
		try (Connection connection = DriverManager.getConnection(connectionUrl, "no_core_data_user", "NoCoreDataPassword1");
		        Statement statement = connection.createStatement();
		        ResultSet resultSet = statement.executeQuery("select count(*) from users")) {
			assertTrue(resultSet.next());
			assertEquals(0, resultSet.getInt(1));
		}
	}

	@Test
	void shouldStopAnInstallThatCannotMarkItselfIncompleteBeforePuttingUsersIntoTheDatabase() throws Exception {
		filter.runInstalls = true;
		// test data with no server to import it from, which the install would otherwise fail importing
		filter.installationScript.setProperty("import_test_data", "true");
		MockHttpSession session = startSession();
		post(session, "installmethod.vm", "install_method", "advanced");
		post(session, "databasesetup.vm", "database_connection",
		    "jdbc:h2:mem:unmarked_" + UUID.randomUUID().toString().replace("-", ""), "database_driver", "",
		    "current_openmrs_database", "yes", "openmrs_current_database_name", "unmarked_db");
		post(session, TABLES_AND_USER_PAGE, "create_tables", "no", "current_database_user", "yes",
		    "current_database_username", "unmarked_user", "current_database_password", "UnmarkedPassword1");
		post(session, "otherruntimeproperties.vm", "module_web_admin", "yes", "auto_update_database", "no");
		post(session, "adminusersetup.vm", "new_admin_password", "Admin123", "new_admin_password_confirm", "Admin123");
		post(session, "implementationidsetup.vm", "implementation_name", "", "implementation_id", "", "pass_phrase", "",
		    "description", "");
		File directory = applicationDataDirectory.toFile();
		// root can write to a directory whose write bit is cleared, so this runs only as another user
		assumeTrue(directory.setWritable(false) && !directory.canWrite());
		try {
			post(session, "wizardcomplete.vm");
			filter.initJob.waitForCompletion();
		} finally {
			directory.setWritable(true);
		}

		assertThat(filter.initJob.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		assertThat(filter.initJob.getErrors(), not(hasKey(ErrorMessageConstants.ERROR_DB_IMPORT_TEST_DATA)));
		assertFalse(InitializationFilter.getInstallIncompleteFile().exists());
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
		sentinels.put("runtime properties connection.url password", "SentinelRuntimeUrl1");
		sentinels.put("form database_root_password", "SentinelFormRoot1");
		sentinels.put("form create_database_password", "SentinelFormCreateDb1");
		sentinels.put("form create_user_password", "SentinelFormCreateUser1");
		sentinels.put("form current_database_password", "SentinelFormCurrent1");
		sentinels.put("form new_admin_password", "SentinelFormAdmin1");
		sentinels.put("form database_connection password", "SentinelFormUrl1");
		sentinels.put("form pass_phrase", "SentinelFormPassPhrase1");

		filter.environment.put("CREATE_DATABASE_PASSWORD", sentinels.get("environment create_database_password"));
		filter.environment.put("CREATE_USER_PASSWORD", sentinels.get("environment create_user_password"));
		filter.environment.put("REMOTE_PASSWORD", sentinels.get("environment remote_password"));
		filter.environment.put("ADMIN_USER_PASSWORD", sentinels.get("environment admin_user_password"));
		writeExistingRuntimeProperties(
		    "jdbc:mysql://localhost:3306/openmrs?password=" + sentinels.get("runtime properties connection.url password"),
		    sentinels.get("runtime properties connection.password"));
		String connectionWithPassword = "jdbc:mysql://localhost:3306/@DBNAME@?password="
		        + sentinels.get("form database_connection password");

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
		    post(session, "databasesetup.vm", "database_connection", connectionWithPassword, "database_driver",
		        "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "no", "openmrs_new_database_name", "",
		        "create_database_username", "root", "create_database_password",
		        sentinels.get("form create_database_password")));
		pages.put(TABLES_AND_USER_PAGE,
		    post(session, "databasesetup.vm", "database_connection", connectionWithPassword, "database_driver",
		        "com.mysql.cj.jdbc.Driver", "current_openmrs_database", "no", "openmrs_new_database_name", "openmrs_walk",
		        "create_database_username", "root", "create_database_password",
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
		    "implementation_id", "", "pass_phrase", sentinels.get("form pass_phrase"), "description", ""));
		pages.put("implementationidsetup.vm after back", post(session, "wizardcomplete.vm", "back", "Back"));
		pages.put("wizardcomplete.vm again", post(session, "implementationidsetup.vm", "implementation_name", "",
		    "implementation_id", "", "pass_phrase", sentinels.get("form pass_phrase"), "description", ""));
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
			assertFalse(PASSWORD_FIELD_REFERENCE.matcher(page.getValue()).find(),
			    page.getKey() + " shows a reference to a password field");
			for (Map.Entry<String, String> sentinel : sentinels.entrySet()) {
				assertFalse(page.getValue().contains(sentinel.getValue()),
				    page.getKey() + " rendered the " + sentinel.getKey());
			}
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "jdbc:mysql://localhost:3306/openmrs?user=openmrs&password=Secret1",
	        "jdbc:mysql://openmrs:Secret1@localhost:3306/openmrs", "jdbc:oracle:thin:openmrs/Secret1@localhost:1521/openmrs",
	        "jdbc:h2:mem:openmrs;USER=openmrs;PASSWORD=Secret1",
	        "jdbc:mysql://address=(host=localhost)(user=openmrs)(password=Secret1)/openmrs",
	        "jdbc:mysql://localhost:3306/@DBNAME@?trustCertificateKeyStorePassword=Secret1",
	        "jdbc:postgresql://localhost:5432/openmrs?sslpassword=Secret1" })
	void shouldNotShowAConnectionUrlThatNamesAPasswordOrHoldsUserInfo(String connectionUrl) {
		assertTrue(InitializationFilter.mayCarryCredentials(connectionUrl));
	}

	@ParameterizedTest
	@ValueSource(strings = { InitializationWizardModel.DEFAULT_MYSQL_CONNECTION,
	        InitializationWizardModel.DEFAULT_POSTGRESQL_CONNECTION, "jdbc:mysql://localhost:3306/openmrs?user=openmrs" })
	void shouldShowAConnectionUrlWithoutCredentials(String connectionUrl) {
		assertFalse(InitializationFilter.mayCarryCredentials(connectionUrl));
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

	/**
	 * An in-memory database with a users table whose user 1 has the given stored password, with the
	 * seeded salt
	 *
	 * @return its connection URL, for user openmrs with password InstalledPassword1
	 */
	private String databaseWithAdminPassword(String passwordHash) throws SQLException {
		String connectionUrl = "jdbc:h2:mem:installed_" + UUID.randomUUID().toString().replace("-", "")
		        + ";DB_CLOSE_DELAY=-1";
		try (Connection connection = DriverManager.getConnection(connectionUrl, "openmrs", "InstalledPassword1")) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("create table users (user_id int primary key, password varchar(255), salt varchar(255))");
			}
			try (PreparedStatement insert = connection.prepareStatement("insert into users values (1, ?, ?)")) {
				insert.setString(1, passwordHash);
				insert.setString(2, SEEDED_SALT);
				insert.executeUpdate();
			}
		}
		return connectionUrl;
	}

	private void writeExistingRuntimeProperties(String connectionUrl, String connectionPassword) throws IOException {
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty("connection.url", connectionUrl);
		runtimeProperties.setProperty("connection.username", "openmrs");
		runtimeProperties.setProperty("connection.password", connectionPassword);
		File file = new File(applicationDataDirectory.toFile(), WebConstants.WEBAPP_NAME + "-runtime.properties");
		try (OutputStream out = new FileOutputStream(file)) {
			runtimeProperties.store(out, null);
		}
		Context.setRuntimeProperties(runtimeProperties);
	}

	/**
	 * Configures an automatic install, as Docker and the OpenMRS SDK do, of an in-memory database that
	 * does not exist, so each install fails at its first connection
	 *
	 * @return the name of that database, which the connection error names
	 */
	private String configureAutomaticInstallThatCannotConnect() {
		String database = "automatic_" + UUID.randomUUID().toString().replace("-", "");
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("connection.url", "jdbc:h2:mem:" + database + ";IFEXISTS=TRUE");
		filter.installationScript.setProperty("connection.driver_class", "org.h2.Driver");
		filter.installationScript.setProperty("database_name", "openmrs");
		filter.installationScript.setProperty("has_current_openmrs_database", "true");
		filter.installationScript.setProperty("create_database_user", "false");
		filter.installationScript.setProperty("admin_user_password", "Admin123");
		return database;
	}

	/**
	 * A browser's first requests to the wizard: it opens the wizard and chooses a language
	 */
	private MockHttpSession startSession() throws Exception {
		MockHttpSession session = new MockHttpSession();
		get(session, null);
		post(session, "chooselang.vm", "locale", "en");
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
		// as a browser sends the cookie of its session
		request.setRequestedSessionId(session.getId());
		return request;
	}

	@Test
	void shouldStartAnAutomaticInstallWhoseDatabaseNameItPutsIntoNoSqlWhateverThatNameIs() throws Exception {
		// the name is not one the rule accepts, but this install reuses an existing database and user and its
		// url already names that database, so the name is never put into the sql the wizard runs
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("connection.url",
		    "jdbc:h2:mem:reuse_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
		filter.installationScript.setProperty("connection.driver_class", "org.h2.Driver");
		filter.installationScript.setProperty("database_name", "my--clinic");
		filter.installationScript.setProperty("has_current_openmrs_database", "true");
		filter.installationScript.setProperty("create_database_user", "false");
		filter.installationScript.setProperty("admin_user_password", "Admin123");

		MockHttpSession session = new MockHttpSession();
		get(session, null);

		assertEquals(1, filter.installsStarted.size());
		assertEquals("my--clinic", filter.installsStarted.get(0).databaseName);
		assertThat(filter.getWizardModel(session).errors, not(hasKey(ErrorMessageConstants.ERROR_DB_NAME_INVALID)));
	}

	@Test
	void shouldRejectAnAutomaticInstallWhoseInvalidDatabaseNameGoesIntoTheConnectionUrl() throws Exception {
		// the same name, but now the url carries @DBNAME@, so the install would put the name into the url
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("connection.url", "jdbc:h2:mem:@DBNAME@;DB_CLOSE_DELAY=-1");
		filter.installationScript.setProperty("connection.driver_class", "org.h2.Driver");
		filter.installationScript.setProperty("database_name", "my--clinic");
		filter.installationScript.setProperty("has_current_openmrs_database", "true");
		filter.installationScript.setProperty("create_database_user", "false");
		filter.installationScript.setProperty("admin_user_password", "Admin123");

		MockHttpSession session = new MockHttpSession();
		get(session, null);

		assertThat(filter.installsStarted, empty());
		assertThat(filter.getWizardModel(session).errors, hasKey(ErrorMessageConstants.ERROR_DB_NAME_INVALID));
	}

	@ParameterizedTest
	@CsvSource({ "false, false, false", "true, true, false", "true, false, true" })
	void shouldRejectAnAutomaticInstallThatPutsAnInvalidDatabaseNameIntoItsSql(boolean hasCurrentOpenmrsDatabase,
	        boolean createDatabaseUser, boolean importTestData) throws Exception {
		// the url already names a database, so only creating the database or its user, or importing test data, would
		// put the name into sql
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("connection.url",
		    "jdbc:h2:mem:reuse_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
		filter.installationScript.setProperty("connection.driver_class", "org.h2.Driver");
		filter.installationScript.setProperty("database_name", "my--clinic");
		filter.installationScript.setProperty("has_current_openmrs_database", String.valueOf(hasCurrentOpenmrsDatabase));
		filter.installationScript.setProperty("create_database_user", String.valueOf(createDatabaseUser));
		filter.installationScript.setProperty("import_test_data", String.valueOf(importTestData));
		filter.installationScript.setProperty("admin_user_password", "Admin123");

		MockHttpSession session = new MockHttpSession();
		get(session, null);

		assertThat(filter.installsStarted, empty());
		assertThat(filter.getWizardModel(session).errors, hasKey(ErrorMessageConstants.ERROR_DB_NAME_INVALID));
	}
}
