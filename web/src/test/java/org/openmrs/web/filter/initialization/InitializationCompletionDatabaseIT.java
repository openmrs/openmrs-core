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

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmrs.test.Containers;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.filter.util.ErrorMessageConstants;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs the steps of an install that create its database and database user, for a database name with
 * a hyphen and a dot, against a MariaDB server through the MySQL and the MariaDB JDBC drivers. The
 * server is a container the test starts, so the test runs where Docker is available and is skipped
 * elsewhere. It does not use the database of {@link org.openmrs.util.DatabaseIT}, which is H2
 * unless the build is told otherwise, and an install on H2 skips the statement that creates the
 * database.
 */
class InitializationCompletionDatabaseIT {

	/**
	 * A name like the OpenMRS SDK gives a server's database
	 */
	private static final String DATABASE_NAME = "openmrs-ref.app-2";

	/**
	 * The user the install names after the database: the first 11 characters of the name and _user,
	 * since the name with _user is longer than 16 characters
	 */
	private static final String DATABASE_USER = "openmrs-ref_user";

	private static final String STOPPED = "stopped by the test once the database and its user were created";

	private static MariaDBContainer<?> container;

	@TempDir
	Path applicationDataDirectory;

	private String originalApplicationDataDirectory;

	private boolean initializationRequiredBefore;

	private InitializationFilter filter;

	@BeforeEach
	void setUp() throws SQLException {
		dropDatabaseAndUser();
		originalApplicationDataDirectory = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		OpenmrsUtil.setApplicationDataDirectory(applicationDataDirectory.toString());
		initializationRequiredBefore = InitializationFilter.initializationRequired();
		filter = new InitializationFilter();
	}

	@AfterEach
	void tearDown() throws SQLException {
		if (filter == null) {
			// skipped before setting anything up
			return;
		}
		InitializationFilter.setInstallationStarted(false);
		filter.setInitializationComplete(!initializationRequiredBefore);
		OpenmrsUtil.setApplicationDataDirectory(originalApplicationDataDirectory);
		dropDatabaseAndUser();
	}

	@AfterAll
	static void stopContainer() {
		if (container != null) {
			container.stop();
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "mysql", "mariadb" })
	void install_shouldCreateTheDatabaseAndAUserThatCanConnectToIt(String driver) throws Exception {
		InitializationWizardModel model = new InitializationWizardModel();
		model.databaseConnection = connection(driver);
		model.databaseName = DATABASE_NAME;
		model.hasCurrentOpenmrsDatabase = false;
		model.createDatabaseUsername = adminUsername();
		model.createDatabasePassword = adminPassword();
		model.createDatabaseUser = true;
		model.createUserUsername = adminUsername();
		model.createUserPassword = adminPassword();
		Properties runtimeProperties = new Properties();
		InitializationFilter.InitializationCompletion install = filter.new InitializationCompletion(
		                                                                                            model) {

			@Override
			Properties addRuntimeProperties(Properties installRuntimeProperties) {
				// called once the install has checked that its database user can connect, before it creates the
				// tables and starts OpenMRS, which this test leaves out
				runtimeProperties.putAll(installRuntimeProperties);
				throw new IllegalStateException(STOPPED);
			}
		};

		install.start();
		install.waitForCompletion();

		assertEquals(Map.of(ErrorMessageConstants.ERROR_COMPLETE_STARTUP, List.of(STOPPED)), errors(install));
		assertEquals(List.of(WizardTask.CREATE_SCHEMA, WizardTask.CREATE_DB_USER), install.getExecutedTasks());
		assertEquals(DATABASE_USER, runtimeProperties.getProperty("connection.username"));
		try (Connection connection = DriverManager.getConnection(runtimeProperties.getProperty("connection.url"),
		    DATABASE_USER, runtimeProperties.getProperty("connection.password"));
		        Statement statement = connection.createStatement();
		        ResultSet resultSet = statement.executeQuery("select database()")) {
			assertTrue(resultSet.next());
			assertEquals(DATABASE_NAME, resultSet.getString(1));
		}
	}

	/**
	 * The connection the wizard is given, to the server in the container. It names the container's host
	 * in capitals, which resolves to the same address but keeps the install from creating its database
	 * user for 'localhost', which it does for a connection naming localhost: the server sees a
	 * connection to the container's published port come from Docker's network.
	 *
	 * @param driver mysql or mariadb: the JDBC driver, and the branch of the install's database steps
	 * @return the connection, with @DBNAME@ where the install puts the database name
	 */
	String connection(String driver) {
		MariaDBContainer<?> server = container();
		return "jdbc:" + driver + "://" + server.getHost().toUpperCase(Locale.ROOT) + ":" + server.getMappedPort(3306)
		        + "/@DBNAME@";
	}

	/**
	 * @return the account the wizard is given to create the database and its user
	 */
	String adminUsername() {
		return container().getUsername();
	}

	/**
	 * @return the password of the account the wizard is given to create the database and its user
	 */
	String adminPassword() {
		return container().getPassword();
	}

	/**
	 * @return the container of the MariaDB server, started on first use with root as its account
	 */
	private static MariaDBContainer<?> container() {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available to start a database server");
		if (container == null) {
			container = Containers.newMariaDBContainer().withUsername("root");
			container.start();
		}
		return container;
	}

	/**
	 * Drops the database and the user that an install of this test creates
	 */
	private void dropDatabaseAndUser() throws SQLException {
		try (Connection connection = DriverManager.getConnection(connection("mysql").replace("@DBNAME@", ""),
		    adminUsername(), adminPassword()); Statement statement = connection.createStatement()) {
			statement.execute("drop database if exists `" + DATABASE_NAME + "`");
			statement.execute("drop user if exists '" + DATABASE_USER + "'@'%'");
		}
	}

	/**
	 * @return the errors the install reported, with their parameters, which a failed comparison prints
	 */
	private static Map<String, List<Object>> errors(InitializationFilter.InitializationCompletion install) {
		Map<String, List<Object>> errors = new HashMap<>();
		install.getErrors().forEach((error, parameters) -> errors.put(error, Arrays.asList(parameters)));
		return errors;
	}
}
