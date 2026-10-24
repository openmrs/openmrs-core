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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for the setup wizard flow in {@link InitializationFilter}. These tests verify
 * the complete page-to-page navigation and model state changes for each installation path (simple,
 * advanced, testing) without requiring a database or Spring context.
 */
class InitializationFilterE2ETest {

	private TestableInitializationFilter filter;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private Properties originalRuntimeProperties;

	private String originalApplicationDataDirectory;

	private MockServletContext servletContext;

	@TempDir
	Path applicationDataDirectory;

	/**
	 * Testable subclass that overrides renderTemplate to capture which template was rendered instead of
	 * actually invoking Velocity, and stubs startInstallation so the real InitializationCompletion
	 * background thread (which performs the real setup and overwrites Context.runtimeProperties with
	 * wizard-defaulted values) doesn't race with subsequent tests in the same JVM. It takes a scripted
	 * install from the test rather than from the environment of the machine running it.
	 */
	static class TestableInitializationFilter extends InitializationFilter {

		String lastRenderedTemplate;

		final Properties installationScript = new Properties();

		/**
		 * The model of the last install this filter was asked to start
		 */
		InitializationWizardModel installed;

		/**
		 * Configures the filter without init, which checks the database of any runtime properties this
		 * machine has
		 */
		TestableInitializationFilter(ServletContext servletContext) {
			filterConfig = new MockFilterConfig(servletContext);
		}

		@Override
		protected void renderTemplate(String templateName, Map<String, Object> referenceMap,
		        HttpServletResponse httpResponse, Object model, Map<String, Object[]> errors, Map<String, Object[]> msgs)
		        throws IOException {
			this.lastRenderedTemplate = templateName;
		}

		@Override
		protected void startInstallation(InitializationWizardModel model) {
			installed = model;
			setInstallationStarted(true);
		}

		@Override
		protected Properties getInstallationScript() {
			return installationScript;
		}

		@Override
		protected Map<String, String> getEnvironmentVariables() {
			return Collections.emptyMap();
		}
	}

	@BeforeEach
	void setup() {
		servletContext = new MockServletContext();
		filter = new TestableInitializationFilter(servletContext);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		// Save and clear runtime properties to avoid state leaking from other test classes.
		// Subsequent context-loading tests in the same JVM rely on these being restored,
		// since SchedulerConfig.dataSource() reads them at bean-creation time.
		originalRuntimeProperties = Context.getRuntimeProperties();
		Context.setRuntimeProperties(new Properties());
		originalApplicationDataDirectory = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		OpenmrsUtil.setApplicationDataDirectory(applicationDataDirectory.toString());
		filter.setInitializationComplete(false);
		InitializationFilter.setInstallationStarted(false);
	}

	@AfterEach
	void cleanup() {
		InitializationFilter.setInstallationStarted(false);
		filter.setInitializationComplete(false);
		Context.setRuntimeProperties(originalRuntimeProperties);
		OpenmrsUtil.setApplicationDataDirectory(originalApplicationDataDirectory);
	}

	// ========== Language Selection (chooselang.vm) ==========

	@Test
	void chooseLangPage_shouldRenderInstallMethodPageOnPost() throws Exception {
		request.setParameter("page", "chooselang.vm");
		request.setParameter("locale", "en");

		filter.doPost(request, response);

		assertEquals("installmethod.vm", filter.lastRenderedTemplate);
	}

	@Test
	void chooseLangPage_shouldStoreLocaleInSession() throws Exception {
		request.setParameter("page", "chooselang.vm");
		request.setParameter("locale", "fr");

		filter.doPost(request, response);

		assertEquals("fr", request.getSession().getAttribute("locale"));
	}

	// ========== Install Method Selection (installmethod.vm) ==========

	@Test
	void installMethodPage_shouldNavigateToSimpleSetupWhenSimpleSelected() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "simple");

		filter.doPost(request, response);

		assertEquals("simple", model().installMethod);
		assertEquals("simplesetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void installMethodPage_shouldNavigateToDatabaseSetupWhenAdvancedSelected() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");

		filter.doPost(request, response);

		assertEquals("advanced", model().installMethod);
		assertEquals(1, model().currentStepNumber);
		assertEquals(5, model().numberOfSteps);
		assertEquals("databasesetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void installMethodPage_shouldNavigateToRemoteDetailsWhenTestingSelected() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "testing");

		filter.doPost(request, response);

		assertEquals("testing", model().installMethod);
		assertEquals(1, model().currentStepNumber);
		assertEquals("remotedetails.vm", filter.lastRenderedTemplate);
	}

	@Test
	void installMethodPage_shouldGoBackToChooseLangWhenBackClicked() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("chooselang.vm", filter.lastRenderedTemplate);
	}

	// ========== Simple Setup (simplesetup.vm) ==========

	@Test
	void simpleSetup_shouldGoBackToInstallMethodWhenBackClicked() throws Exception {
		request.setParameter("page", "simplesetup.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("installmethod.vm", filter.lastRenderedTemplate);
	}

	@Test
	void simpleSetup_shouldShowErrorWhenDatabaseRootPasswordEmpty() throws Exception {
		request.setParameter("page", "simplesetup.vm");
		request.setParameter("database_root_password", "");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbPasswd"));
		assertEquals("simplesetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void simpleSetup_shouldSetMysqlDefaultsWhenMysqlSelected() throws Exception {
		request.setParameter("page", "simplesetup.vm");
		request.setParameter("database_type", "mysql");
		request.setParameter("database_root_password", "rootpass");

		filter.doPost(request, response);

		assertEquals("mysql", model().databaseType);
		assertEquals("rootpass", model().databaseRootPassword);
		assertFalse(model().hasCurrentOpenmrsDatabase);
		assertTrue(model().createTables);
		assertTrue(model().createDatabaseUser);
		assertTrue(model().moduleWebAdmin);
		assertFalse(model().autoUpdateDatabase);
		assertEquals(InitializationWizardModel.ADMIN_DEFAULT_PASSWORD, model().adminUserPassword);
	}

	@Test
	void simpleSetup_shouldSetPostgresqlDefaultsWhenPostgresqlSelected() throws Exception {
		request.setParameter("page", "simplesetup.vm");
		request.setParameter("database_type", "postgresql");
		request.setParameter("database_root_password", "pgpass");

		filter.doPost(request, response);

		assertEquals("postgresql", model().databaseType);
		assertEquals(InitializationWizardModel.DEFAULT_POSTGRESQL_CONNECTION, model().databaseConnection);
	}

	// ========== Database Setup - Advanced (databasesetup.vm) ==========

	@ParameterizedTest
	@CsvSource({ "advanced, databasesetup.vm, installmethod.vm", "testing, databasesetup.vm, remotedetails.vm",
	        "simple, wizardcomplete.vm, simplesetup.vm", "advanced, wizardcomplete.vm, implementationidsetup.vm" })
	void backNavigation_shouldReturnToCorrectPageForInstallMethod(String installMethod, String page, String expectedTemplate)
	        throws Exception {
		model().installMethod = installMethod;
		request.setParameter("page", page);
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals(expectedTemplate, filter.lastRenderedTemplate);
	}

	@Test
	void databaseSetup_shouldShowErrorWhenConnectionStringEmpty() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "");
		request.setParameter("database_driver", "");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbConn"));
	}

	@Test
	void databaseSetup_shouldSetExistingDatabaseWhenYesSelected() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "openmrs_existing");

		filter.doPost(request, response);

		assertEquals("openmrs_existing", model().databaseName);
		assertTrue(model().hasCurrentOpenmrsDatabase);
	}

	@Test
	void databaseSetup_shouldSetNewDatabaseWhenNoSelected() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "no");
		request.setParameter("openmrs_new_database_name", "openmrs_new");
		request.setParameter("create_database_username", "admin");
		request.setParameter("create_database_password", "adminpass");

		filter.doPost(request, response);

		assertEquals("openmrs_new", model().databaseName);
		assertFalse(model().hasCurrentOpenmrsDatabase);
		assertTrue(model().createTables);
		assertEquals("admin", model().createDatabaseUsername);
		assertEquals("adminpass", model().createDatabasePassword);
	}

	@Test
	void databaseSetup_shouldRequireNewDatabaseNameWhenCreatingNew() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "no");
		request.setParameter("openmrs_new_database_name", "");
		request.setParameter("create_database_username", "admin");
		request.setParameter("create_database_password", "adminpass");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbNewName"));
	}

	@Test
	void databaseSetup_shouldNavigateToDatabaseTablesAndUserOnSuccess() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "openmrs");

		filter.doPost(request, response);

		assertEquals("databasetablesanduser.vm", filter.lastRenderedTemplate);
		assertEquals(2, model().currentStepNumber);
	}

	@Test
	void databaseSetup_shouldSetStepNumber3ForTestingInstall() throws Exception {
		model().installMethod = "testing";
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "openmrs");

		filter.doPost(request, response);

		assertEquals("databasetablesanduser.vm", filter.lastRenderedTemplate);
		assertEquals(3, model().currentStepNumber);
	}

	// ========== Database Tables and User (databasetablesanduser.vm) ==========

	@Test
	void databaseTablesAndUser_shouldGoBackToDatabaseSetup() throws Exception {
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("databasesetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void databaseTablesAndUser_shouldSetCreateTablesWhenYesSelected() throws Exception {
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("create_tables", "yes");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "openmrs_user");
		request.setParameter("current_database_password", "openmrs_pass");

		filter.doPost(request, response);

		assertTrue(model().createTables);
		assertEquals("openmrs_user", model().currentDatabaseUsername);
		assertEquals("openmrs_pass", model().currentDatabasePassword);
		assertTrue(model().hasCurrentDatabaseUser);
		assertFalse(model().createDatabaseUser);
	}

	@Test
	void databaseTablesAndUser_shouldSetCreateUserWhenNoExistingUser() throws Exception {
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("create_tables", "yes");
		request.setParameter("current_database_user", "no");
		request.setParameter("create_user_username", "new_user");
		request.setParameter("create_user_password", "new_pass");

		filter.doPost(request, response);

		assertFalse(model().hasCurrentDatabaseUser);
		assertTrue(model().createDatabaseUser);
		assertEquals("new_user", model().createUserUsername);
		assertEquals("new_pass", model().createUserPassword);
	}

	@Test
	void databaseTablesAndUser_shouldRequireUsernameWhenExistingUserSelected() throws Exception {
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "");
		request.setParameter("current_database_password", "pass");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbCurUserName"));
	}

	@Test
	void databaseTablesAndUser_shouldRequirePasswordWhenExistingUserSelected() throws Exception {
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "user");
		request.setParameter("current_database_password", "");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbCurUserPswd"));
	}

	@Test
	void databaseTablesAndUser_shouldNavigateToOtherRuntimePropsForAdvancedInstall() throws Exception {
		model().installMethod = "advanced";
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "user");
		request.setParameter("current_database_password", "pass");

		filter.doPost(request, response);

		assertEquals("otherruntimeproperties.vm", filter.lastRenderedTemplate);
	}

	@Test
	void databaseTablesAndUser_shouldNavigateToWizardCompleteForTestingInstall() throws Exception {
		model().installMethod = "testing";
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "user");
		request.setParameter("current_database_password", "pass");

		filter.doPost(request, response);

		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);
	}

	// ========== Other Runtime Properties (otherruntimeproperties.vm) ==========

	@Test
	void otherRuntimeProps_shouldGoBackToDatabaseTablesAndUser() throws Exception {
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("databasetablesanduser.vm", filter.lastRenderedTemplate);
	}

	@Test
	void otherRuntimeProps_shouldSetModuleWebAdminWhenYes() throws Exception {
		model().createTables = true;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");

		filter.doPost(request, response);

		assertTrue(model().moduleWebAdmin);
		assertFalse(model().autoUpdateDatabase);
	}

	@Test
	void otherRuntimeProps_shouldSetAutoUpdateDatabaseWhenYes() throws Exception {
		model().createTables = true;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "no");
		request.setParameter("auto_update_database", "yes");

		filter.doPost(request, response);

		assertFalse(model().moduleWebAdmin);
		assertTrue(model().autoUpdateDatabase);
	}

	@Test
	void otherRuntimeProps_shouldNavigateToAdminUserSetupWhenCreatingTables() throws Exception {
		model().createTables = true;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");

		filter.doPost(request, response);

		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void otherRuntimeProps_shouldNavigateToAdminUserSetupWhenNotCreatingTables() throws Exception {
		model().createTables = false;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");

		filter.doPost(request, response);

		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}

	// ========== Admin User Setup (adminusersetup.vm) ==========

	@Test
	void adminUserSetup_shouldGoBackToOtherRuntimeProps() throws Exception {
		request.setParameter("page", "adminusersetup.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("otherruntimeproperties.vm", filter.lastRenderedTemplate);
	}

	@ParameterizedTest
	@CsvSource({ "Admin123, DifferentPass1, install.error.adminPswdMatch", "'', '', install.error.adminPswdEmpty",
	        "weak, weak, install.error.adminPswdWeak" })
	void adminUserSetup_shouldShowErrorForInvalidPassword(String password, String confirmPassword, String errorKey)
	        throws Exception {
		request.setParameter("page", "adminusersetup.vm");
		request.setParameter("new_admin_password", password);
		request.setParameter("new_admin_password_confirm", confirmPassword);

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey(errorKey));
		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void adminUserSetup_shouldNavigateToImplementationIdSetupOnSuccess() throws Exception {
		request.setParameter("page", "adminusersetup.vm");
		request.setParameter("new_admin_password", "Admin123");
		request.setParameter("new_admin_password_confirm", "Admin123");

		filter.doPost(request, response);

		assertEquals("Admin123", model().adminUserPassword);
		assertEquals("implementationidsetup.vm", filter.lastRenderedTemplate);
	}

	// ========== Implementation ID Setup (implementationidsetup.vm) ==========

	@Test
	void implementationIdSetup_shouldGoBackToAdminUserSetupWhenCreatingTables() throws Exception {
		model().createTables = true;
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void implementationIdSetup_shouldGoBackToAdminUserSetupWhenNotCreatingTables() throws Exception {
		model().createTables = false;
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("back", "Back");

		filter.doPost(request, response);

		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void implementationIdSetup_shouldSetImplementationDetails() throws Exception {
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "Test Clinic");
		request.setParameter("implementation_id", "TESTCLINIC");
		request.setParameter("pass_phrase", "secret");
		request.setParameter("description", "A test clinic implementation");

		filter.doPost(request, response);

		assertEquals("Test Clinic", model().implementationIdName);
		assertEquals("TESTCLINIC", model().implementationId);
		assertEquals("secret", model().implementationIdPassPhrase);
		assertEquals("A test clinic implementation", model().implementationIdDescription);
		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);
	}

	@Test
	void implementationIdSetup_shouldRejectIdWithCaret() throws Exception {
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "Test");
		request.setParameter("implementation_id", "INVALID^ID");
		request.setParameter("pass_phrase", "secret");
		request.setParameter("description", "desc");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.implID"));
		assertEquals("implementationidsetup.vm", filter.lastRenderedTemplate);
	}

	@Test
	void implementationIdSetup_shouldRejectIdWithPipe() throws Exception {
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "Test");
		request.setParameter("implementation_id", "INVALID|ID");
		request.setParameter("pass_phrase", "secret");
		request.setParameter("description", "desc");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.implID"));
	}

	@Test
	void implementationIdSetup_shouldAcceptEmptyImplementationId() throws Exception {
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "");
		request.setParameter("implementation_id", "");
		request.setParameter("pass_phrase", "");
		request.setParameter("description", "");

		filter.doPost(request, response);

		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);
	}

	// ========== Wizard Complete (wizardcomplete.vm) ==========

	@Test
	void wizardComplete_shouldBuildCorrectTaskListForAdvancedWithNewDatabase() throws Exception {
		model().installMethod = "advanced";
		model().databaseName = "openmrs";
		model().hasCurrentOpenmrsDatabase = false;
		model().createDatabaseUser = true;
		model().createTables = true;
		model().wizardCompletePageShown = true;
		request.setParameter("page", "wizardcomplete.vm");

		filter.doPost(request, response);

		assertNotNull(model().tasksToExecute);
		assertTrue(model().tasksToExecute.contains(WizardTask.CREATE_SCHEMA));
		assertTrue(model().tasksToExecute.contains(WizardTask.CREATE_DB_USER));
		assertTrue(model().tasksToExecute.contains(WizardTask.CREATE_TABLES));
		assertTrue(model().tasksToExecute.contains(WizardTask.ADD_CORE_DATA));
		assertTrue(model().tasksToExecute.contains(WizardTask.UPDATE_TO_LATEST));
	}

	@Test
	void wizardComplete_shouldBuildCorrectTaskListForExistingDatabase() throws Exception {
		model().installMethod = "advanced";
		model().databaseName = "openmrs";
		model().hasCurrentOpenmrsDatabase = true;
		model().createDatabaseUser = false;
		model().createTables = false;
		model().wizardCompletePageShown = true;
		request.setParameter("page", "wizardcomplete.vm");

		filter.doPost(request, response);

		assertNotNull(model().tasksToExecute);
		assertFalse(model().tasksToExecute.contains(WizardTask.CREATE_SCHEMA));
		assertFalse(model().tasksToExecute.contains(WizardTask.CREATE_DB_USER));
		assertFalse(model().tasksToExecute.contains(WizardTask.CREATE_TABLES));
		assertTrue(model().tasksToExecute.contains(WizardTask.UPDATE_TO_LATEST));
	}

	@Test
	void wizardComplete_shouldSetTestingTasksForTestingInstall() throws Exception {
		model().installMethod = "testing";
		model().databaseName = "openmrs";
		model().hasCurrentOpenmrsDatabase = false;
		model().createDatabaseUser = false;
		model().wizardCompletePageShown = true;
		request.setParameter("page", "wizardcomplete.vm");

		filter.doPost(request, response);

		assertTrue(model().importTestData);
		assertFalse(model().createTables);
		// the testing install has no admin password page, so it sets no admin password, not even ADMIN_DEFAULT_PASSWORD
		assertEquals("", model().adminUserPassword);
		assertNotNull(model().tasksToExecute);
		assertTrue(model().tasksToExecute.contains(WizardTask.IMPORT_TEST_DATA));
		assertTrue(model().tasksToExecute.contains(WizardTask.ADD_MODULES));
		assertTrue(model().tasksToExecute.contains(WizardTask.UPDATE_TO_LATEST));
	}

	@Test
	void wizardComplete_shouldRenderProgressPage() throws Exception {
		model().installMethod = "advanced";
		model().databaseName = "openmrs";
		model().hasCurrentOpenmrsDatabase = true;
		model().createDatabaseUser = false;
		model().createTables = false;
		model().wizardCompletePageShown = true;
		request.setParameter("page", "wizardcomplete.vm");

		filter.doPost(request, response);

		assertEquals("progress.vm", filter.lastRenderedTemplate);
	}

	@Test
	void wizardComplete_shouldNotStartAnInstallOnceTheWizardWasStartedAgain() throws Exception {
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "");
		request.setParameter("implementation_id", "");
		request.setParameter("pass_phrase", "");
		request.setParameter("description", "");
		filter.doPost(request, response);
		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);

		resetRequest();
		filter.doGet(request, response);
		assertEquals("chooselang.vm", filter.lastRenderedTemplate);

		resetRequest();
		request.setParameter("page", "wizardcomplete.vm");
		filter.doPost(request, response);

		assertEquals("chooselang.vm", filter.lastRenderedTemplate);
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	@Test
	void wizardComplete_shouldNotStartAnInstallWithAnInvalidDatabaseName() throws Exception {
		// the simple install has no database name field, so a scripted name is first checked on confirmation
		filter.installationScript.setProperty("database_name", "open mrs");
		filter.doGet(request, response);
		resetRequest();
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "simple");
		filter.doPost(request, response);
		resetRequest();
		request.setParameter("page", "simplesetup.vm");
		request.setParameter("database_type", "mysql");
		request.setParameter("database_root_password", "rootpass");
		filter.doPost(request, response);
		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);
		resetRequest();
		request.setParameter("page", "wizardcomplete.vm");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbNameInvalid"));
		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	// ========== Installation Already Started ==========

	@Test
	void startInstallation_shouldNotStartASecondInstallWhileOneIsRunning() {
		InitializationFilter.setInstallationStarted(true);
		InitializationFilter otherFilter = new InitializationFilter();
		InitializationWizardModel model = new InitializationWizardModel();
		// a database that does not exist, so an install started by mistake ends at its first connection
		model.databaseConnection = "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE";
		model.hasCurrentOpenmrsDatabase = true;
		model.createDatabaseUser = false;

		try {
			otherFilter.startInstallation(model);

			assertNull(otherFilter.initJob);
			assertTrue(InitializationFilter.isInstallationStarted());
		} finally {
			if (otherFilter.initJob != null) {
				otherFilter.initJob.waitForCompletion();
			}
		}
	}

	@Test
	void startInstallation_shouldIgnoreAnotherStartUntilTheRunningInstallEnds() throws Exception {
		CountDownLatch installRunning = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		InitializationFilter installingFilter = new InitializationFilter() {

			@Override
			protected boolean isCurrentDatabase(InitializationWizardModel wizardModel, String database) {
				// holds the install at its first step
				installRunning.countDown();
				try {
					release.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return super.isCurrentDatabase(wizardModel, database);
			}
		};
		installingFilter.startInstallation(installEndingAtItsFirstConnection());
		InitializationFilter.InitializationCompletion job = installingFilter.initJob;
		assertNotNull(job);
		try {
			assertTrue(installRunning.await(30, TimeUnit.SECONDS));
			assertTrue(InitializationFilter.isInstallationStarted());
			installingFilter.startInstallation(installEndingAtItsFirstConnection());
			assertSame(job, installingFilter.initJob);
		} finally {
			release.countDown();
			job.waitForCompletion();
			if (installingFilter.initJob != job) {
				installingFilter.initJob.waitForCompletion();
			}
		}

		assertFalse(InitializationFilter.isInstallationStarted());
	}

	/**
	 * A model for an install on a database that does not exist, which ends at its first connection
	 */
	private InitializationWizardModel installEndingAtItsFirstConnection() {
		InitializationWizardModel model = new InitializationWizardModel();
		model.databaseConnection = "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE";
		model.databaseName = "openmrs";
		model.hasCurrentOpenmrsDatabase = false;
		model.createDatabaseUser = false;
		return model;
	}

	@Test
	void doPost_shouldRenderProgressWhenInstallationAlreadyStarted() throws Exception {
		InitializationFilter.setInstallationStarted(true);
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "simple");

		filter.doPost(request, response);

		assertEquals("progress.vm", filter.lastRenderedTemplate);
	}

	// ========== Full Flow: Advanced Installation Path ==========

	@Test
	void fullAdvancedFlow_shouldNavigateCorrectlyThroughAllSteps() throws Exception {
		// Step 1: Choose Language -> Install Method
		request.setParameter("page", "chooselang.vm");
		request.setParameter("locale", "en");
		filter.doPost(request, response);
		assertEquals("installmethod.vm", filter.lastRenderedTemplate);

		// Step 2: Install Method (advanced) -> Database Setup
		resetRequest();
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("databasesetup.vm", filter.lastRenderedTemplate);
		assertEquals(1, model().currentStepNumber);
		assertEquals(5, model().numberOfSteps);

		// Step 3: Database Setup -> Database Tables and User
		resetRequest();
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "openmrs");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("databasetablesanduser.vm", filter.lastRenderedTemplate);

		// Step 4: Database Tables and User -> Other Runtime Props
		resetRequest();
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "openmrs");
		request.setParameter("current_database_password", "openmrs");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("otherruntimeproperties.vm", filter.lastRenderedTemplate);

		// Step 5: Other Runtime Props -> Admin User Setup
		resetRequest();
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);

		// Step 6: Admin User Setup -> Implementation ID
		resetRequest();
		request.setParameter("page", "adminusersetup.vm");
		request.setParameter("new_admin_password", "Admin123");
		request.setParameter("new_admin_password_confirm", "Admin123");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("implementationidsetup.vm", filter.lastRenderedTemplate);

		// Step 7: Implementation ID -> Wizard Complete
		resetRequest();
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "");
		request.setParameter("implementation_id", "");
		request.setParameter("pass_phrase", "");
		request.setParameter("description", "");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("wizardcomplete.vm", filter.lastRenderedTemplate);

		// Step 8: Wizard Complete -> Progress (starts installation)
		resetRequest();
		request.setParameter("page", "wizardcomplete.vm");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("progress.vm", filter.lastRenderedTemplate);
	}

	// ========== Full Flow: Advanced with Back Navigation ==========

	@Test
	void fullAdvancedFlow_shouldSupportBackNavigationThroughSteps() throws Exception {
		// Navigate forward: chooselang -> installmethod -> databasesetup
		request.setParameter("page", "chooselang.vm");
		filter.doPost(request, response);

		resetRequest();
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");
		clearErrors();
		filter.doPost(request, response);

		// Now go back from databasesetup -> installmethod
		resetRequest();
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("back", "Back");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("installmethod.vm", filter.lastRenderedTemplate);

		// Go back from installmethod -> chooselang
		resetRequest();
		request.setParameter("page", "installmethod.vm");
		request.setParameter("back", "Back");
		clearErrors();
		filter.doPost(request, response);
		assertEquals("chooselang.vm", filter.lastRenderedTemplate);
	}

	// ========== Full Flow: Advanced with Existing DB (no table creation) ==========

	@Test
	void advancedExistingDb_shouldNavigateToAdminUserSetupWhenNotCreatingTables() throws Exception {
		model().installMethod = "advanced";
		model().hasCurrentOpenmrsDatabase = true;
		model().createTables = false;

		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");

		filter.doPost(request, response);

		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}

	// ========== Wizard Model State Preservation ==========

	@Test
	void wizardModel_shouldPreserveStateAcrossSteps() throws Exception {
		// Step 1: Install method
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");
		filter.doPost(request, response);
		assertEquals("advanced", model().installMethod);

		// Step 2: Database setup
		resetRequest();
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "my_openmrs");
		clearErrors();
		filter.doPost(request, response);

		// Verify state from both steps
		assertEquals("advanced", model().installMethod);
		assertEquals("jdbc:h2:mem:test", model().databaseConnection);
		assertEquals("my_openmrs", model().databaseName);
		assertTrue(model().hasCurrentOpenmrsDatabase);
	}

	@Test
	void isCurrentDatabase_shouldReturnTrueForMariaDBUrl() {
		model().databaseConnection = "jdbc:mariadb://localhost:3306/openmrs";
		assertTrue(filter.isCurrentDatabase(model(), "mariadb"));
		assertFalse(filter.isCurrentDatabase(model(), "mysql"));
	}

	@Test
	void isCurrentDatabase_shouldReturnTrueForMySQLUrl() {
		model().databaseConnection = "jdbc:mysql://localhost:3306/openmrs";
		assertTrue(filter.isCurrentDatabase(model(), "mysql"));
		assertFalse(filter.isCurrentDatabase(model(), "mariadb"));
	}

	// ========== goBack via image click (back.x / back.y) ==========

	@Test
	void backNavigation_shouldWorkWithImageClickCoordinates() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("back.x", "10");
		request.setParameter("back.y", "20");

		filter.doPost(request, response);

		assertEquals("chooselang.vm", filter.lastRenderedTemplate);
	}

	// ========== Database Tables and User - Create User Validation ==========

	@Test
	void databaseTablesAndUser_shouldRequireCreateUserUsernameWhenCreatingNewUser() throws Exception {
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "no");
		request.setParameter("create_user_username", "");
		request.setParameter("create_user_password", "pass");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbUserName"));
	}

	@Test
	void databaseTablesAndUser_shouldRequireCreateUserPasswordWhenCreatingNewUser() throws Exception {
		model().hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "no");
		request.setParameter("create_user_username", "user");
		request.setParameter("create_user_password", "");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbUserPswd"));
	}

	// ========== Database Setup - Validation for Existing DB ==========

	@Test
	void databaseSetup_shouldRequireCurrentDatabaseNameWhenExistingSelected() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbCurrName"));
	}

	@Test
	void databaseSetup_shouldRequireCreateUsernameWhenCreatingNewDb() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "no");
		request.setParameter("openmrs_new_database_name", "newdb");
		request.setParameter("create_database_username", "");
		request.setParameter("create_database_password", "pass");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbUserName"));
	}

	@Test
	void databaseSetup_shouldRequireCreatePasswordWhenCreatingNewDb() throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "no");
		request.setParameter("openmrs_new_database_name", "newdb");
		request.setParameter("create_database_username", "admin");
		request.setParameter("create_database_password", "");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbUserPswd"));
	}

	// ========== skipFilter ==========

	@Test
	void skipFilter_shouldReturnFalseWhenInitializationRequired() {
		filter.setInitializationComplete(false);

		boolean result = filter.skipFilter(request);

		assertFalse(result);
	}

	@Test
	void skipFilter_shouldReturnTrueWhenInitializationComplete() {
		filter.setInitializationComplete(true);

		boolean result = filter.skipFilter(request);

		assertTrue(result);
	}

	@Test
	void skipFilter_shouldReturnFalseForProgressPollOnSetupPage() {
		filter.setInitializationComplete(true);
		request.setServletPath("/" + WebConstants.SETUP_PAGE_URL);
		request.setParameter("page", "progress.vm.ajaxRequest");

		boolean result = filter.skipFilter(request);

		assertFalse(result);
	}

	@Test
	void skipFilter_shouldReturnTrueForProgressPollOffSetupPage() {
		filter.setInitializationComplete(true);
		request.setServletPath("/openmrs");
		request.setParameter("page", "progress.vm.ajaxRequest");

		boolean result = filter.skipFilter(request);

		assertTrue(result);
	}

	@Test
	void skipFilter_shouldReturnTrueForSetupPageRequestThatIsNotTheProgressPoll() {
		filter.setInitializationComplete(true);
		request.setServletPath("/" + WebConstants.SETUP_PAGE_URL);

		boolean result = filter.skipFilter(request);

		assertTrue(result);
	}

	@Test
	void skipFilter_shouldNotReadRequestParametersOnceInitializationIsComplete() {
		filter.setInitializationComplete(true);
		MockHttpServletRequest req = new MockHttpServletRequest() {

			@Override
			public String getParameter(String name) {
				throw new AssertionError("skipFilter parsed request parameters after initialization completed");
			}
		};
		req.setServletPath("/index.htm");

		assertTrue(filter.skipFilter(req));
	}

	// ========== Database names ==========

	static Stream<String> invalidDatabaseNames() {
		return Stream.of("open`mrs", "open'mrs", "open\"mrs", "openmrs--x", "openmrs/*x", "openmrs#x", "openmrs;x",
		    "open$mrs", "open\\mrs", "open mrs", "-openmrs", "openmrs-", "openmrs..db", ".openmrs", "openmrs.",
		    "openmrs.-db", "a".repeat(65));
	}

	@ParameterizedTest
	@MethodSource("invalidDatabaseNames")
	void databaseSetup_shouldRejectAnInvalidNewDatabaseName(String databaseName) throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "no");
		request.setParameter("openmrs_new_database_name", databaseName);
		request.setParameter("create_database_username", "root");
		request.setParameter("create_database_password", "secret");

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbNameInvalid"));
		assertEquals("databasesetup.vm", filter.lastRenderedTemplate);
	}

	@ParameterizedTest
	@MethodSource("invalidDatabaseNames")
	void databaseSetup_shouldRejectAnInvalidCurrentDatabaseName(String databaseName) throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", databaseName);

		filter.doPost(request, response);

		assertTrue(getErrors().containsKey("install.error.dbNameInvalid"));
		assertEquals("databasesetup.vm", filter.lastRenderedTemplate);
	}

	@ParameterizedTest
	@MethodSource("validDatabaseNames")
	void databaseSetup_shouldAcceptAValidDatabaseName(String databaseName) throws Exception {
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "no");
		request.setParameter("openmrs_new_database_name", databaseName);
		request.setParameter("create_database_username", "root");
		request.setParameter("create_database_password", "secret");

		filter.doPost(request, response);

		assertTrue(getErrors().isEmpty());
		assertEquals("databasetablesanduser.vm", filter.lastRenderedTemplate);
	}

	static Stream<String> validDatabaseNames() {
		// the OpenMRS SDK names a server's database openmrs-<server id>
		return Stream.of("openmrs", "openmrs_2", "openmrs-server", "openmrs-my-server_1", "openmrs.db",
		    "openmrs-refapp-2.13", "a".repeat(64));
	}

	// ========== Scripted (auto) install ==========

	@Test
	void autoInstall_shouldStartWhenTheScriptedValuesAreValid() throws Exception {
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("database_name", "openmrs-server");
		filter.installationScript.setProperty("admin_user_password", "Admin123");

		filter.doGet(request, response);

		assertTrue(InitializationFilter.isInstallationStarted());
		assertTrue(getErrors().isEmpty());
		assertSame(model(), filter.installed);
		assertEquals("openmrs-server", filter.installed.databaseName);
	}

	@Test
	void autoInstall_shouldNotStartWithAnInvalidScriptedDatabaseName() throws Exception {
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("database_name", "open mrs");
		filter.installationScript.setProperty("admin_user_password", "Admin123");

		filter.doGet(request, response);

		assertFalse(InitializationFilter.isInstallationStarted());
		assertTrue(getErrors().containsKey("install.error.dbNameInvalid"));
	}

	@Test
	void autoInstall_shouldNotStartOnceAnEarlierInstallFailedWhileStartingOpenmrs() throws Exception {
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("database_name", "openmrs");
		filter.installationScript.setProperty("admin_user_password", "Admin123");
		// what an earlier install that started OpenMRS and then failed leaves behind
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, new Object());

		filter.doGet(request, response);

		assertFalse(InitializationFilter.isInstallationStarted());
		assertTrue(getErrors().containsKey("install.error.restartRequired"));
	}

	@Test
	void autoInstall_shouldNotStartWithAnAdminPasswordThePasswordPolicyRejects() throws Exception {
		filter.installationScript.setProperty("install_method", "auto");
		filter.installationScript.setProperty("database_name", "openmrs");
		filter.installationScript.setProperty("admin_user_password", "weak");

		filter.doGet(request, response);

		assertFalse(InitializationFilter.isInstallationStarted());
		assertTrue(getErrors().containsKey("install.error.adminPswdWeak"));
	}

	// ========== Helper Methods ==========

	/**
	 * The wizard model of the session the current request belongs to.
	 */
	private InitializationWizardModel model() {
		return filter.getWizardModel(request.getSession());
	}

	/**
	 * The errors shown to the session the current request belongs to.
	 */
	private Map<String, Object[]> getErrors() {
		return model().errors;
	}

	/**
	 * Clear the errors map between steps in multi-step flow tests.
	 */
	private void clearErrors() throws Exception {
		getErrors().clear();
	}

	/**
	 * Reset request and response objects for the next step in a multi-step test, in the same session.
	 */
	private void resetRequest() {
		HttpSession session = request.getSession();
		request = new MockHttpServletRequest();
		request.setSession(session);
		response = new MockHttpServletResponse();
		filter.lastRenderedTemplate = null;
	}
}
