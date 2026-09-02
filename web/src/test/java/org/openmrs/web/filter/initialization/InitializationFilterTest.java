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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.UserService;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class InitializationFilterTest extends BaseWebContextSensitiveTest {

	private InitializationFilter filter;

	@Value("${hibernate.connection.url}")
	private String connectionUrl;

	@BeforeEach
	public void setup() {
		filter = new InitializationFilter();
		filter.wizardModel = new InitializationWizardModel();
	}

	@Test
	public void shouldLoadConnectionUrlFromProperties() {
		assertNotNull(connectionUrl);
		assertEquals(connectionUrl, "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true");
	}

	@AfterEach
	public void cleanup() {
		System.clearProperty("connection.url");
		System.clearProperty("install_method");
	}

	@Test
	public void shouldPrioritizeSystemPropertiesOverEnvironmentAndScript() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("CONNECTION_URL", "jdbc:mysql://env-var:3306/openmrs");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();

		Properties installScriptProps = new Properties();
		installScriptProps.setProperty("connection.url", "jdbc:mysql://file:3306/openmrs");
		doReturn(installScriptProps).when(spyFilter).getInstallationScript();

		System.setProperty("connection.url", "jdbc:mysql://system:3306/openmrs");

		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

		assertEquals("jdbc:mysql://system:3306/openmrs", spyFilter.wizardModel.databaseConnection);
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldNormalizeEnvironmentVariablesCorrectly() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("CONNECTION_URL", "jdbc:mysql://env-var:3306/openmrs");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

		assertEquals("jdbc:mysql://env-var:3306/openmrs", spyFilter.wizardModel.databaseConnection);
	}

	/**
	 *
	 */
	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldKeepSpecialEnvironmentKeysWithoutNormalization() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("INSTALL_METHOD", "env_auto");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

		assertEquals("env_auto", spyFilter.wizardModel.installMethod);
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldFallbackToInstallScriptWhenNotInSystemOrEnvironment() {
		InitializationFilter spyFilter = spy(filter);
		doReturn(new HashMap<String, String>()).when(spyFilter).getEnvironmentVariables();

		Properties installScriptProps = new Properties();
		installScriptProps.setProperty("database_name", "openmrs_file");
		doReturn(installScriptProps).when(spyFilter).getInstallationScript();

		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

		assertEquals("openmrs_file", spyFilter.wizardModel.databaseName);
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldReturnNullForNonExistingProperties() {
		InitializationFilter spyFilter = spy(filter);
		doReturn(new HashMap<String, String>()).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

		assertNull(spyFilter.wizardModel.additionalPropertiesFromInstallationScript.getProperty("non_existing_key"));
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldMergePropertiesFromSystemEnvAndInstallScript()
	        throws IOException {
		InitializationFilter spyFilter = spy(filter);
		System.setProperty("connection.url", "jdbc:mysql://system:3306/openmrs");

		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("INSTALL_METHOD", "env_auto");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();

		File tempScript = File.createTempFile("openmrs-install", ".properties");
		try {
			try (PrintWriter writer = new PrintWriter(tempScript)) {
				writer.println("connection.url=jdbc:mysql://script:3306/openmrs");
				writer.println("install_method=script_auto");
				writer.println("database_name=openmrs_script");
			}

			System.setProperty("OPENMRS_INSTALLATION_SCRIPT", tempScript.getAbsolutePath());

			spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

			assertEquals("jdbc:mysql://system:3306/openmrs", spyFilter.wizardModel.databaseConnection);
			assertEquals("env_auto", spyFilter.wizardModel.installMethod);
			assertEquals("openmrs_script", spyFilter.wizardModel.databaseName);
		} finally {
			if (!tempScript.delete()) {
				tempScript.deleteOnExit();
			}
		}
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldCorrectlyHandleSpecialEnvironmentVariables() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("CREATE_DATABASE_USERNAME", "db_user");
		fakeEnv.put("CREATE_DATABASE_PASSWORD", "db_pass");
		fakeEnv.put("CREATE_USER_USERNAME", "user_user");
		fakeEnv.put("CREATE_USER_PASSWORD", "user_pass");
		fakeEnv.put("CONNECTION_DRIVER_CLASS", "com.mysql.cj.jdbc.Driver");
		fakeEnv.put("ADMIN_PASSWORD_LOCKED", "true");
		fakeEnv.put("DATABASE_NAME", "openmrs_env");
		fakeEnv.put("INSTALL_METHOD", "env_auto");
		fakeEnv.put("CREATE_TABLES", "true");
		fakeEnv.put("MODULE_WEB_ADMIN", "false");
		fakeEnv.put("AUTO_UPDATE_DATABASE", "true");
		fakeEnv.put("ADMIN_USER_PASSWORD", "EnvPass123");
		fakeEnv.put("IMPORT_TEST_DATA", "true");
		fakeEnv.put("REMOTE_URL", "http://remote:8080/openmrs");
		fakeEnv.put("REMOTE_USERNAME", "remote_user");
		fakeEnv.put("REMOTE_PASSWORD", "remote_pass");

		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

		assertEquals("db_user", spyFilter.wizardModel.createDatabaseUsername);
		assertEquals("db_pass", spyFilter.wizardModel.createDatabasePassword);
		assertEquals("user_user", spyFilter.wizardModel.createUserUsername);
		assertEquals("user_pass", spyFilter.wizardModel.createUserPassword);
		assertEquals("com.mysql.cj.jdbc.Driver", spyFilter.wizardModel.databaseDriver);
		assertEquals("openmrs_env", spyFilter.wizardModel.databaseName);
		assertEquals("env_auto", spyFilter.wizardModel.installMethod);
		assertTrue(spyFilter.wizardModel.createTables);
		assertFalse(spyFilter.wizardModel.moduleWebAdmin);
		assertTrue(spyFilter.wizardModel.autoUpdateDatabase);
		assertEquals("EnvPass123", spyFilter.wizardModel.adminUserPassword);
		assertEquals("true", spyFilter.wizardModel.additionalPropertiesFromInstallationScript
		        .getProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY));
		assertTrue(spyFilter.wizardModel.importTestData);
		assertEquals("http://remote:8080/openmrs", spyFilter.wizardModel.remoteUrl);
		assertEquals("remote_user", spyFilter.wizardModel.remoteUsername);
		assertEquals("remote_pass", spyFilter.wizardModel.remotePassword);
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldHandleCustomPropertiesAndAliases() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("ADD_DEMO_DATA", "true");
		fakeEnv.put("PROPERTY_CUSTOM_RUNTIME_PROP", "custom_value");
		fakeEnv.put("PROPERTY_ANOTHER_PROP", "another_value");

		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		// System properties to test admin.password.locked and whitelisted/normalized keys
		System.setProperty("admin.password.locked", "true");
		System.setProperty("connection_driver_class", "org.postgresql.Driver");

		try {
			spyFilter.initializeWizardFromResolvedPropertiesIfPresent();

			assertTrue(spyFilter.wizardModel.importTestData, "ADD_DEMO_DATA env var should set importTestData");
			assertEquals("custom_value",
			    spyFilter.wizardModel.additionalPropertiesFromInstallationScript.getProperty("custom.runtime.prop"));
			assertEquals("another_value",
			    spyFilter.wizardModel.additionalPropertiesFromInstallationScript.getProperty("another.prop"));
			assertEquals("true",
			    spyFilter.wizardModel.additionalPropertiesFromInstallationScript.getProperty("admin.password.locked"));
			assertEquals("org.postgresql.Driver", spyFilter.wizardModel.databaseDriver);
		} finally {
			System.clearProperty("admin.password.locked");
			System.clearProperty("connection_driver_class");
		}
	}

	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldInitializeWizardModelCorrectlyFromProperties() {
		Properties installScript = getInstallScript();
		InitializationFilter spyFilter = spy(filter);
		doReturn(installScript).when(spyFilter).getInstallationScript();
		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();
		InitializationWizardModel model = spyFilter.wizardModel;

		assertEquals("auto", model.installMethod);
		assertEquals("jdbc:h2:@APPLICATIONDATADIR@/database/@DBNAME@;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1",
		    model.databaseConnection);
		assertEquals("org.h2.Driver", model.databaseDriver);
		assertEquals("sa", model.currentDatabaseUsername);
		assertEquals("sa", model.currentDatabasePassword);
		assertTrue(model.hasCurrentOpenmrsDatabase);
		assertFalse(model.createDatabaseUser);
		assertTrue(model.createTables);
		assertTrue(model.moduleWebAdmin);
		assertFalse(model.autoUpdateDatabase);
		assertEquals("Admin123", model.adminUserPassword);
	}

	private static Properties getInstallScript() {
		Properties installScript = new Properties();
		installScript.setProperty("install_method", "auto");
		installScript.setProperty("connection.url",
		    "jdbc:h2:@APPLICATIONDATADIR@/database/@DBNAME@;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1");
		installScript.setProperty("connection.driver_class", "org.h2.Driver");
		installScript.setProperty("connection.username", "sa");
		installScript.setProperty("connection.password", "sa");
		installScript.setProperty("database_name", "openmrs");
		installScript.setProperty("has_current_openmrs_database", "true");
		installScript.setProperty("create_database_user", "false");
		installScript.setProperty("create_tables", "true");
		installScript.setProperty("add_demo_data", "false");
		installScript.setProperty("module_web_admin", "true");
		installScript.setProperty("auto_update_database", "false");
		installScript.setProperty("admin_user_password", "Admin123");
		return installScript;
	}
}
