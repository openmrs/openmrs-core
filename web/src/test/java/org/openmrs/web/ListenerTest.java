/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.Security;
import org.openmrs.web.filter.initialization.InitializationFilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests how the Listener decides whether OpenMRS starts or the setup wizard does, against an
 * in-memory database with the tables that decision reads
 */
class ListenerTest {

	/**
	 * User 1's password and salt as the core data seeds them, the hash of "test"
	 */
	private static final String SEEDED_PASSWORD_HASH = "4a1750c8607dfa237de36c6305715c223415189";

	private static final String SEEDED_SALT = "c788c6ad82a157b712392ca695dfcf2eed193d7f";

	@TempDir
	Path applicationDataDirectory;

	private String originalApplicationDataDirectory;

	private Properties originalRuntimeProperties;

	private boolean runtimePropertiesFoundBefore;

	private Listener listener;

	@BeforeEach
	void setUp() {
		// created first so logging is set up before OpenmrsConstants is first read, which fails the other way round
		listener = new Listener();
		originalApplicationDataDirectory = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		OpenmrsUtil.setApplicationDataDirectory(applicationDataDirectory.toString());
		originalRuntimeProperties = Context.getRuntimeProperties();
		runtimePropertiesFoundBefore = Listener.runtimePropertiesFound();
		Listener.setRuntimePropertiesFound(true);
	}

	@AfterEach
	void tearDown() {
		Listener.setRuntimePropertiesFound(runtimePropertiesFoundBefore);
		Context.setRuntimeProperties(originalRuntimeProperties);
		OpenmrsUtil.setApplicationDataDirectory(originalApplicationDataDirectory);
	}

	@Test
	void setupNeeded_shouldBeFalseForAnUpToDateDatabaseWithoutAnInstallMarkedIncomplete() throws Exception {
		startWith(installedDatabase(SEEDED_PASSWORD_HASH));

		assertFalse(listener.setupNeeded());
	}

	@Test
	void setupNeeded_shouldBeTrueWhileAnInstallMarkedIncompleteLeftTheSeededAdminPassword() throws Exception {
		startWith(installedDatabase(SEEDED_PASSWORD_HASH));
		markInstallIncomplete();

		assertTrue(listener.setupNeeded());
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void setupNeeded_shouldBeTrueForAnEmptyDatabaseWhileAnInstallIsMarkedIncomplete() throws Exception {
		// as after only the database of a failed install was emptied to install again
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty("connection.url",
		    "jdbc:h2:mem:listener_empty_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
		runtimeProperties.setProperty("connection.username", "openmrs");
		runtimeProperties.setProperty("connection.password", "Password1");
		startWith(runtimeProperties);
		markInstallIncomplete();

		assertTrue(listener.setupNeeded());
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void setupNeeded_shouldStartAsBeforeOnceTheAdminUserOfAnInstallMarkedIncompleteHasAnotherPassword() throws Exception {
		startWith(installedDatabase(Security.encodePassword("Admin123" + SEEDED_SALT)));
		markInstallIncomplete();

		assertFalse(listener.setupNeeded());
		assertFalse(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void setupNeeded_shouldNotStartOpenmrsWhileTheFileOfAnInstallThatNoLongerAppliesCannotBeDeleted() throws Exception {
		startWith(installedDatabase(Security.encodePassword("Admin123" + SEEDED_SALT)));
		markInstallIncomplete();
		File directory = applicationDataDirectory.toFile();
		// root can write to a directory whose write bit is cleared, so this runs only as another user
		assumeTrue(directory.setWritable(false) && !directory.canWrite());
		try {
			assertThrows(SQLException.class, listener::setupNeeded);
		} finally {
			directory.setWritable(true);
		}

		// the filter, which starts next, would otherwise start the wizard in front of a running OpenMRS
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void setupNeeded_shouldNotStartOpenmrsWhenTheDatabaseOfAnInstallMarkedIncompleteCannotBeReached() throws Exception {
		Properties reachable = installedDatabase(SEEDED_PASSWORD_HASH);
		startWith(reachable);
		// the runtime properties file names a database that cannot be reached while the rest of the start up
		// reaches it, as when the database comes back between the check and the connections after it
		Properties unreachable = new Properties();
		unreachable.putAll(reachable);
		unreachable.setProperty("connection.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";IFEXISTS=TRUE");
		writeRuntimePropertiesFile(unreachable);
		markInstallIncomplete();

		assertThrows(SQLException.class, listener::setupNeeded);
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	/**
	 * An in-memory database at the version this code is, whose user 1 has the given stored password and
	 * the seeded salt
	 *
	 * @return the runtime properties that name it
	 */
	private Properties installedDatabase(String passwordHash) throws SQLException {
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty("connection.url",
		    "jdbc:h2:mem:listener_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
		runtimeProperties.setProperty("connection.username", "openmrs");
		runtimeProperties.setProperty("connection.password", "Password1");
		runtimeProperties.setProperty("connection.driver_class", "org.h2.Driver");
		try (Connection connection = DriverManager.getConnection(runtimeProperties.getProperty("connection.url"), "openmrs",
		    "Password1")) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("create table users (user_id int primary key, password varchar(255), salt varchar(255))");
				statement.execute(
				    "create table global_property (property varchar(255) primary key, " + "property_value varchar(255))");
			}
			try (PreparedStatement insert = connection.prepareStatement("insert into users values (1, ?, ?)")) {
				insert.setString(1, passwordHash);
				insert.setString(2, SEEDED_SALT);
				insert.executeUpdate();
			}
			try (PreparedStatement insert = connection
			        .prepareStatement("insert into global_property values ('core.version', ?)")) {
				insert.setString(1, OpenmrsConstants.OPENMRS_VERSION_SHORT);
				insert.executeUpdate();
			}
		}
		return runtimeProperties;
	}

	/**
	 * Gives OpenMRS these runtime properties, as a runtime properties file and as the ones it read from
	 * it
	 */
	private void startWith(Properties runtimeProperties) throws Exception {
		writeRuntimePropertiesFile(runtimeProperties);
		Context.setRuntimeProperties(runtimeProperties);
	}

	private void writeRuntimePropertiesFile(Properties runtimeProperties) throws Exception {
		try (OutputStream out = new FileOutputStream(
		        applicationDataDirectory.resolve(WebConstants.WEBAPP_NAME + "-runtime.properties").toFile())) {
			runtimeProperties.store(out, null);
		}
	}

	private void markInstallIncomplete() throws Exception {
		Files.write(InitializationFilter.getInstallIncompleteFile().toPath(), new byte[0]);
	}
}
