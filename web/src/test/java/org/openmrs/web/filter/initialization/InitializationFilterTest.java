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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
	public void resolveInitializationProperties_shouldPrioritizeSystemPropertiesOverEnvironmentAndScript() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("CONNECTION_URL", "jdbc:mysql://env-var:3306/openmrs");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();

		Properties installScriptProps = new Properties();
		installScriptProps.setProperty("connection.url", "jdbc:mysql://file:3306/openmrs");
		doReturn(installScriptProps).when(spyFilter).getInstallationScript();

		System.setProperty("connection.url", "jdbc:mysql://system:3306/openmrs");
		Properties mergedProps = spyFilter.resolveInitializationProperties();

		assertEquals("jdbc:mysql://system:3306/openmrs", mergedProps.getProperty("connection.url"));
	}

	@Test
	public void resolveInitializationProperties_shouldNormalizeEnvironmentVariablesCorrectly() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("CONNECTION_URL", "jdbc:mysql://env-var:3306/openmrs");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		Properties mergedProps = spyFilter.resolveInitializationProperties();
		assertEquals("jdbc:mysql://env-var:3306/openmrs", mergedProps.getProperty("connection.url"));
	}

	@Test
	public void resolveInitializationProperties_shouldKeepSpecialEnvironmentKeysWithoutNormalization() {
		InitializationFilter spyFilter = spy(filter);
		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("INSTALL_METHOD", "env_auto");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		Properties mergedProps = spyFilter.resolveInitializationProperties();
		assertEquals("env_auto", mergedProps.getProperty("install_method"));
	}

	@Test
	public void resolveInitializationProperties_shouldFallbackToInstallScriptWhenNotInSystemOrEnvironment() {
		InitializationFilter spyFilter = spy(filter);
		doReturn(new HashMap<String, String>()).when(spyFilter).getEnvironmentVariables();

		Properties installScriptProps = new Properties();
		installScriptProps.setProperty("database_name", "openmrs_file");
		doReturn(installScriptProps).when(spyFilter).getInstallationScript();

		Properties mergedProps = spyFilter.resolveInitializationProperties();
		assertEquals("openmrs_file", mergedProps.getProperty("database_name"));
	}

	@Test
	public void resolveInitializationProperties_shouldReturnNullForNonExistingProperties() {
		InitializationFilter spyFilter = spy(filter);
		doReturn(new HashMap<String, String>()).when(spyFilter).getEnvironmentVariables();
		doReturn(new Properties()).when(spyFilter).getInstallationScript();

		Properties mergedProps = spyFilter.resolveInitializationProperties();
		assertNull(mergedProps.getProperty("non_existing_key"));
	}

	@Test
	public void shouldMergePropertiesFromSystemEnvAndInstallScript() throws IOException {
		InitializationFilter spyFilter = spy(filter);
		System.setProperty("connection.url", "jdbc:mysql://system:3306/openmrs");

		Map<String, String> fakeEnv = new HashMap<>();
		fakeEnv.put("INSTALL_METHOD", "env_auto");
		doReturn(fakeEnv).when(spyFilter).getEnvironmentVariables();

		File tempScript = File.createTempFile("openmrs-install", ".properties");
		try (PrintWriter writer = new PrintWriter(tempScript)) {
			writer.println("connection.url=jdbc:mysql://script:3306/openmrs");
			writer.println("install_method=script_auto");
			writer.println("database_name=openmrs_script");
		}

		System.setProperty("OPENMRS_INSTALLATION_SCRIPT", tempScript.getAbsolutePath());

		Properties merged = spyFilter.resolveInitializationProperties();

		assertEquals("jdbc:mysql://system:3306/openmrs", merged.getProperty("connection.url"));
		assertEquals("env_auto", merged.getProperty("install_method"));
		assertEquals("openmrs_script", merged.getProperty("database_name"));

		tempScript.delete();
	}


	@Test
	public void initializeWizardFromResolvedPropertiesIfPresent_shouldInitializeWizardModelCorrectlyFromProperties() {
		Properties installScript = getInstallScript();
		InitializationFilter spyFilter = spy(filter);
		doReturn(installScript).when(spyFilter).getInstallationScript();
		spyFilter.initializeWizardFromResolvedPropertiesIfPresent();
		InitializationWizardModel model = spyFilter.wizardModel;

		assertEquals("auto", model.installMethod);
		assertEquals("jdbc:h2:@APPLICATIONDATADIR@/database/@DBNAME@;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1", model.databaseConnection);
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
		installScript.setProperty("connection.url", "jdbc:h2:@APPLICATIONDATADIR@/database/@DBNAME@;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1");
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
