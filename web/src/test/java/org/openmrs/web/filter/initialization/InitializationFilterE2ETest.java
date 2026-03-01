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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmrs.api.context.Context;
import org.openmrs.web.filter.StartupFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * End-to-end tests for the setup wizard flow in {@link InitializationFilter}.
 * These tests verify the complete page-to-page navigation and model state changes
 * for each installation path (simple, advanced, testing) without requiring a database
 * or Spring context.
 */
class InitializationFilterE2ETest {
	
	private TestableInitializationFilter filter;
	
	private MockHttpServletRequest request;
	
	private MockHttpServletResponse response;
	
	/**
	 * Testable subclass that overrides renderTemplate to capture which template was rendered
	 * instead of actually invoking Velocity.
	 */
	static class TestableInitializationFilter extends InitializationFilter {
		
		String lastRenderedTemplate;
		
		@Override
		protected void renderTemplate(String templateName, Map<String, Object> referenceMap,
				HttpServletResponse httpResponse) throws IOException {
			this.lastRenderedTemplate = templateName;
		}
	}
	
	@BeforeEach
	void setup() {
		filter = new TestableInitializationFilter();
		filter.wizardModel = new InitializationWizardModel();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		// Clear runtime properties to avoid state leaking from other test classes
		Context.setRuntimeProperties(new Properties());
	}
	
	@AfterEach
	void cleanup() {
		InitializationFilter.setInstallationStarted(false);
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
		
		assertEquals("simple", filter.wizardModel.installMethod);
		assertEquals("simplesetup.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void installMethodPage_shouldNavigateToDatabaseSetupWhenAdvancedSelected() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");
		
		filter.doPost(request, response);
		
		assertEquals("advanced", filter.wizardModel.installMethod);
		assertEquals(1, filter.wizardModel.currentStepNumber);
		assertEquals(5, filter.wizardModel.numberOfSteps);
		assertEquals("databasesetup.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void installMethodPage_shouldNavigateToRemoteDetailsWhenTestingSelected() throws Exception {
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "testing");
		
		filter.doPost(request, response);
		
		assertEquals("testing", filter.wizardModel.installMethod);
		assertEquals(1, filter.wizardModel.currentStepNumber);
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
		
		assertEquals("mysql", filter.wizardModel.databaseType);
		assertEquals("rootpass", filter.wizardModel.databaseRootPassword);
		assertFalse(filter.wizardModel.hasCurrentOpenmrsDatabase);
		assertTrue(filter.wizardModel.createTables);
		assertTrue(filter.wizardModel.createDatabaseUser);
		assertTrue(filter.wizardModel.moduleWebAdmin);
		assertFalse(filter.wizardModel.autoUpdateDatabase);
		assertEquals(InitializationWizardModel.ADMIN_DEFAULT_PASSWORD, filter.wizardModel.adminUserPassword);
	}
	
	@Test
	void simpleSetup_shouldSetPostgresqlDefaultsWhenPostgresqlSelected() throws Exception {
		request.setParameter("page", "simplesetup.vm");
		request.setParameter("database_type", "postgresql");
		request.setParameter("database_root_password", "pgpass");
		
		filter.doPost(request, response);
		
		assertEquals("postgresql", filter.wizardModel.databaseType);
		assertEquals(InitializationWizardModel.DEFAULT_POSTGRESQL_CONNECTION, filter.wizardModel.databaseConnection);
	}
	
	// ========== Database Setup - Advanced (databasesetup.vm) ==========
	
	@ParameterizedTest
	@CsvSource({
		"advanced, databasesetup.vm, installmethod.vm",
		"testing, databasesetup.vm, remotedetails.vm",
		"simple, wizardcomplete.vm, simplesetup.vm",
		"advanced, wizardcomplete.vm, implementationidsetup.vm"
	})
	void backNavigation_shouldReturnToCorrectPageForInstallMethod(String installMethod, String page,
			String expectedTemplate) throws Exception {
		filter.wizardModel.installMethod = installMethod;
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
		
		assertEquals("openmrs_existing", filter.wizardModel.databaseName);
		assertTrue(filter.wizardModel.hasCurrentOpenmrsDatabase);
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
		
		assertEquals("openmrs_new", filter.wizardModel.databaseName);
		assertFalse(filter.wizardModel.hasCurrentOpenmrsDatabase);
		assertTrue(filter.wizardModel.createTables);
		assertEquals("admin", filter.wizardModel.createDatabaseUsername);
		assertEquals("adminpass", filter.wizardModel.createDatabasePassword);
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
		assertEquals(2, filter.wizardModel.currentStepNumber);
	}
	
	@Test
	void databaseSetup_shouldSetStepNumber3ForTestingInstall() throws Exception {
		filter.wizardModel.installMethod = "testing";
		request.setParameter("page", "databasesetup.vm");
		request.setParameter("database_connection", "jdbc:h2:mem:test");
		request.setParameter("database_driver", "org.h2.Driver");
		request.setParameter("current_openmrs_database", "yes");
		request.setParameter("openmrs_current_database_name", "openmrs");
		
		filter.doPost(request, response);
		
		assertEquals("databasetablesanduser.vm", filter.lastRenderedTemplate);
		assertEquals(3, filter.wizardModel.currentStepNumber);
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
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("create_tables", "yes");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "openmrs_user");
		request.setParameter("current_database_password", "openmrs_pass");
		
		filter.doPost(request, response);
		
		assertTrue(filter.wizardModel.createTables);
		assertEquals("openmrs_user", filter.wizardModel.currentDatabaseUsername);
		assertEquals("openmrs_pass", filter.wizardModel.currentDatabasePassword);
		assertTrue(filter.wizardModel.hasCurrentDatabaseUser);
		assertFalse(filter.wizardModel.createDatabaseUser);
	}
	
	@Test
	void databaseTablesAndUser_shouldSetCreateUserWhenNoExistingUser() throws Exception {
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("create_tables", "yes");
		request.setParameter("current_database_user", "no");
		request.setParameter("create_user_username", "new_user");
		request.setParameter("create_user_password", "new_pass");
		
		filter.doPost(request, response);
		
		assertFalse(filter.wizardModel.hasCurrentDatabaseUser);
		assertTrue(filter.wizardModel.createDatabaseUser);
		assertEquals("new_user", filter.wizardModel.createUserUsername);
		assertEquals("new_pass", filter.wizardModel.createUserPassword);
	}
	
	@Test
	void databaseTablesAndUser_shouldRequireUsernameWhenExistingUserSelected() throws Exception {
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "");
		request.setParameter("current_database_password", "pass");
		
		filter.doPost(request, response);
		
		assertTrue(getErrors().containsKey("install.error.dbCurUserName"));
	}
	
	@Test
	void databaseTablesAndUser_shouldRequirePasswordWhenExistingUserSelected() throws Exception {
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "user");
		request.setParameter("current_database_password", "");
		
		filter.doPost(request, response);
		
		assertTrue(getErrors().containsKey("install.error.dbCurUserPswd"));
	}
	
	@Test
	void databaseTablesAndUser_shouldNavigateToOtherRuntimePropsForAdvancedInstall() throws Exception {
		filter.wizardModel.installMethod = "advanced";
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "yes");
		request.setParameter("current_database_username", "user");
		request.setParameter("current_database_password", "pass");
		
		filter.doPost(request, response);
		
		assertEquals("otherruntimeproperties.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void databaseTablesAndUser_shouldNavigateToWizardCompleteForTestingInstall() throws Exception {
		filter.wizardModel.installMethod = "testing";
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
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
		filter.wizardModel.createTables = true;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");
		
		filter.doPost(request, response);
		
		assertTrue(filter.wizardModel.moduleWebAdmin);
		assertFalse(filter.wizardModel.autoUpdateDatabase);
	}
	
	@Test
	void otherRuntimeProps_shouldSetAutoUpdateDatabaseWhenYes() throws Exception {
		filter.wizardModel.createTables = true;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "no");
		request.setParameter("auto_update_database", "yes");
		
		filter.doPost(request, response);
		
		assertFalse(filter.wizardModel.moduleWebAdmin);
		assertTrue(filter.wizardModel.autoUpdateDatabase);
	}
	
	@Test
	void otherRuntimeProps_shouldNavigateToAdminUserSetupWhenCreatingTables() throws Exception {
		filter.wizardModel.createTables = true;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");
		
		filter.doPost(request, response);
		
		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void otherRuntimeProps_shouldSkipAdminSetupWhenNotCreatingTables() throws Exception {
		filter.wizardModel.createTables = false;
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");
		
		filter.doPost(request, response);
		
		assertEquals("implementationidsetup.vm", filter.lastRenderedTemplate);
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
	@CsvSource({
		"Admin123, DifferentPass1, install.error.adminPswdMatch",
		"'', '', install.error.adminPswdEmpty",
		"weak, weak, install.error.adminPswdWeak"
	})
	void adminUserSetup_shouldShowErrorForInvalidPassword(String password, String confirmPassword,
			String errorKey) throws Exception {
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
		
		assertEquals("Admin123", filter.wizardModel.adminUserPassword);
		assertEquals("implementationidsetup.vm", filter.lastRenderedTemplate);
	}
	
	// ========== Implementation ID Setup (implementationidsetup.vm) ==========
	
	@Test
	void implementationIdSetup_shouldGoBackToAdminUserSetupWhenCreatingTables() throws Exception {
		filter.wizardModel.createTables = true;
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("back", "Back");
		
		filter.doPost(request, response);
		
		assertEquals("adminusersetup.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void implementationIdSetup_shouldGoBackToOtherRuntimePropsWhenNotCreatingTables() throws Exception {
		filter.wizardModel.createTables = false;
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("back", "Back");
		
		filter.doPost(request, response);
		
		assertEquals("otherruntimeproperties.vm", filter.lastRenderedTemplate);
	}
	
	@Test
	void implementationIdSetup_shouldSetImplementationDetails() throws Exception {
		request.setParameter("page", "implementationidsetup.vm");
		request.setParameter("implementation_name", "Test Clinic");
		request.setParameter("implementation_id", "TESTCLINIC");
		request.setParameter("pass_phrase", "secret");
		request.setParameter("description", "A test clinic implementation");
		
		filter.doPost(request, response);
		
		assertEquals("Test Clinic", filter.wizardModel.implementationIdName);
		assertEquals("TESTCLINIC", filter.wizardModel.implementationId);
		assertEquals("secret", filter.wizardModel.implementationIdPassPhrase);
		assertEquals("A test clinic implementation", filter.wizardModel.implementationIdDescription);
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
		filter.wizardModel.installMethod = "advanced";
		filter.wizardModel.hasCurrentOpenmrsDatabase = false;
		filter.wizardModel.createDatabaseUser = true;
		filter.wizardModel.createTables = true;
		request.setParameter("page", "wizardcomplete.vm");
		
		filter.doPost(request, response);
		
		assertNotNull(filter.wizardModel.tasksToExecute);
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.CREATE_SCHEMA));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.CREATE_DB_USER));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.CREATE_TABLES));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.ADD_CORE_DATA));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.UPDATE_TO_LATEST));
	}
	
	@Test
	void wizardComplete_shouldBuildCorrectTaskListForExistingDatabase() throws Exception {
		filter.wizardModel.installMethod = "advanced";
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		filter.wizardModel.createDatabaseUser = false;
		filter.wizardModel.createTables = false;
		request.setParameter("page", "wizardcomplete.vm");
		
		filter.doPost(request, response);
		
		assertNotNull(filter.wizardModel.tasksToExecute);
		assertFalse(filter.wizardModel.tasksToExecute.contains(WizardTask.CREATE_SCHEMA));
		assertFalse(filter.wizardModel.tasksToExecute.contains(WizardTask.CREATE_DB_USER));
		assertFalse(filter.wizardModel.tasksToExecute.contains(WizardTask.CREATE_TABLES));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.UPDATE_TO_LATEST));
	}
	
	@Test
	void wizardComplete_shouldSetTestingTasksForTestingInstall() throws Exception {
		filter.wizardModel.installMethod = "testing";
		filter.wizardModel.hasCurrentOpenmrsDatabase = false;
		filter.wizardModel.createDatabaseUser = false;
		request.setParameter("page", "wizardcomplete.vm");
		
		filter.doPost(request, response);
		
		assertTrue(filter.wizardModel.importTestData);
		assertFalse(filter.wizardModel.createTables);
		assertNotNull(filter.wizardModel.tasksToExecute);
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.IMPORT_TEST_DATA));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.ADD_MODULES));
		assertTrue(filter.wizardModel.tasksToExecute.contains(WizardTask.UPDATE_TO_LATEST));
	}
	
	@Test
	void wizardComplete_shouldRenderProgressPage() throws Exception {
		filter.wizardModel.installMethod = "advanced";
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		filter.wizardModel.createDatabaseUser = false;
		filter.wizardModel.createTables = false;
		request.setParameter("page", "wizardcomplete.vm");
		
		filter.doPost(request, response);
		
		assertEquals("progress.vm", filter.lastRenderedTemplate);
	}
	
	// ========== Installation Already Started ==========
	
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
		assertEquals(1, filter.wizardModel.currentStepNumber);
		assertEquals(5, filter.wizardModel.numberOfSteps);
		
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
		filter.wizardModel.createTables = true;
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
	void advancedExistingDb_shouldSkipAdminSetupWhenNotCreatingTables() throws Exception {
		filter.wizardModel.installMethod = "advanced";
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		filter.wizardModel.createTables = false;
		
		request.setParameter("page", "otherruntimeproperties.vm");
		request.setParameter("module_web_admin", "yes");
		request.setParameter("auto_update_database", "no");
		
		filter.doPost(request, response);
		
		assertEquals("implementationidsetup.vm", filter.lastRenderedTemplate);
	}
	
	// ========== Wizard Model State Preservation ==========
	
	@Test
	void wizardModel_shouldPreserveStateAcrossSteps() throws Exception {
		// Step 1: Install method
		request.setParameter("page", "installmethod.vm");
		request.setParameter("install_method", "advanced");
		filter.doPost(request, response);
		assertEquals("advanced", filter.wizardModel.installMethod);
		
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
		assertEquals("advanced", filter.wizardModel.installMethod);
		assertEquals("jdbc:h2:mem:test", filter.wizardModel.databaseConnection);
		assertEquals("my_openmrs", filter.wizardModel.databaseName);
		assertTrue(filter.wizardModel.hasCurrentOpenmrsDatabase);
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
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
		request.setParameter("page", "databasetablesanduser.vm");
		request.setParameter("current_database_user", "no");
		request.setParameter("create_user_username", "");
		request.setParameter("create_user_password", "pass");
		
		filter.doPost(request, response);
		
		assertTrue(getErrors().containsKey("install.error.dbUserName"));
	}
	
	@Test
	void databaseTablesAndUser_shouldRequireCreateUserPasswordWhenCreatingNewUser() throws Exception {
		filter.wizardModel.hasCurrentOpenmrsDatabase = true;
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
	
	/**
	 * Access the errors map from the filter via reflection since it is protected in StartupFilter.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object[]> getErrors() throws Exception {
		Field errorsField = StartupFilter.class.getDeclaredField("errors");
		errorsField.setAccessible(true);
		return (Map<String, Object[]>) errorsField.get(filter);
	}
	
	/**
	 * Clear the errors map between steps in multi-step flow tests.
	 */
	private void clearErrors() throws Exception {
		getErrors().clear();
	}
	
	/**
	 * Reset request and response objects for the next step in a multi-step test.
	 */
	private void resetRequest() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
}
