/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextMockTest;

/**
 * Tests methods in {@link org.openmrs.liquibase.LiquibaseSnapshotGenerator}.
 */
public class LiquibaseSnapshotGeneratorTest extends BaseContextMockTest {

	private static Path tempDir;
	private static final String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=LEGACY;NON_KEYWORDS=VALUE";
	private static final String username = "sa";
	private static final String password = "";
	
	@BeforeEach
	public void before() throws Exception {
		tempDir = Files.createTempDirectory("liquibase-snapshots");
		LiquibaseSnapshotGenerator.setPath(tempDir.toString() + "/");

		// create a dummy table so the snapshot isn't empty
		try (Connection conn = DriverManager.getConnection(url, username, password);
			 Statement stmt = conn.createStatement()) {
			stmt.execute("DROP TABLE IF EXISTS person");
			stmt.execute("CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(255));");
		}
	}
	
	/**
	 * @see org.openmrs.liquibase.LiquibaseSnapshotGenerator#execute(String, String, String)
	 */
	@Test
	public void execute_shouldGenerateSchemaSnapshot() throws Exception {
		LiquibaseSnapshotGenerator.execute(url, username, password);

		File schemaFile = new File(tempDir.toString(), "liquibase-schema-only-SNAPSHOT.xml");

		assertTrue(schemaFile.exists());
	}

	/**
	 * @see org.openmrs.liquibase.LiquibaseSnapshotGenerator#execute(String, String, String)
	 */
	@Test
	public void execute_shouldGenerateDataSnapshot() throws Exception {
		LiquibaseSnapshotGenerator.execute(url, username, password);
		
		File dataFile = new File(tempDir.toString(), "liquibase-core-data-SNAPSHOT.xml");
		
		assertTrue(dataFile.exists());
	}
}
