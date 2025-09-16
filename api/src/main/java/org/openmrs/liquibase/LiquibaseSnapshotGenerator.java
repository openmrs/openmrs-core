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

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Data;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.structure.core.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

/**
 * Utility class for generating Liquibase snapshots from an OpenMRS database.
 * <p>
 * This generator produces two types of snapshots:
 * <ul>
 * <li><b>Schema-only snapshot</b> — contains database structure (tables, columns, constraints,
 * etc.)</li>
 * <li><b>Core data snapshot</b> — contains baseline reference data (rows from essential tables)</li>
 * </ul>
 * The snapshots are written to the {@code liquibase/snapshots/} folder with the names:
 * <ul>
 * <li>{@code liquibase-schema-only-SNAPSHOT.xml}</li>
 * <li>{@code liquibase-core-data-SNAPSHOT.xml}</li>
 * </ul>
 * After generation, a Mozilla Public License header is prepended to each file. This class can be
 * run standalone (via {@link #main(String[])}), or programmatically through
 * {@link #execute(String, String, String)}
 */
public class LiquibaseSnapshotGenerator {
	
	private static final Logger log = LoggerFactory.getLogger(LiquibaseSnapshotGenerator.class);
	
	private static String PATH = "liquibase/snapshots/";
	
	private static final String SCHEMA_SNAPSHOT_FILE = "liquibase-schema-only-SNAPSHOT.xml";
	
	private static final String DATA_SNAPSHOT_FILE = "liquibase-core-data-SNAPSHOT.xml";
	
	/**
	 * To run this generator locally, update the database connection details below:
	 * <ul>
	 * <li><b>URL</b>: Change {@code 127.0.0.1} to {@code localhost} (Optional), and replace
	 * {@code openmrs} with your local DB name (Required).</li>
	 * <li><b>Username</b>: Use your local DB username (e.g. {@code root})</li>
	 * <li><b>Password</b>: Use your local DB password (e.g. {@code Admin123})</li>
	 * </ul>
	 * Example configuration:
	 * 
	 * <pre><code>
	 * String url = "jdbc:mysql://localhost:3306/@DBNAME?autoReconnect=true";
	 * String username = "root";
	 * String password = "Admin123";
	 * </code></pre>
	 * 
	 * After updating these values, you can run:
	 * 
	 * <pre><code>
	 *   mvn clean package -DskipTests=true dependency:copy-dependencies
	 *   java -cp "api/target/classes:api/target/dependency/*" org.openmrs.liquibase.LiquibaseSnapshotGenerator
	 * </code></pre>
	 * 
	 * This will generate the snapshots into the configured output directory.
	 */
	public static void main(String[] args) throws Exception {
		
		String url = "jdbc:mysql://127.0.0.1:3306/openmrs";
		String username = "test";
		String password = "test";
		
		execute(url, username, password);
	}
	
	public static void execute(String url, String username, String password) throws Exception {
		// Step 1: Clean old snapshots
		log.info("Deleting old snapshots...");
		deleteOldSnapshots();
		
		// Step 2: Generate schema changelog
		log.info("Generating schema-only snapshot...");
		generateSchemaSnapshot(url, username, password);
		
		// Step 3: Generate data changelog
		log.info("Generating core data snapshot...");
		generateDataSnapshot(url, username, password);
		
		// Step 4: Prepend license if files were created
		prependLicense(PATH + SCHEMA_SNAPSHOT_FILE);
		prependLicense(PATH + DATA_SNAPSHOT_FILE);
		
		log.info("Snapshots generated and license headers applied.");
	}
	
	private static void generateSchemaSnapshot(String referenceUrl, String username, String password) throws Exception {
		try (Connection connection = DriverManager.getConnection(referenceUrl, username, password)) {
			Database liquibaseDb = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));

			// Create snapshot for schema objects only
			SnapshotControl snapshotControl = new SnapshotControl(liquibaseDb, Schema.class, View.class, Table.class, 
				Column.class, PrimaryKey.class, ForeignKey.class, Index.class, UniqueConstraint.class, Catalog.class);

			DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(liquibaseDb.getDefaultSchema(),
				liquibaseDb, snapshotControl);

			DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(snapshot, null, 
				CompareControl.STANDARD);

			DiffToChangeLog changeLog = new DiffToChangeLog(diffResult, new DiffOutputControl(false, 
				false, true, null));

			ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer("xml");
			File file = new File(PATH + SCHEMA_SNAPSHOT_FILE);
			
			try (PrintStream out = new PrintStream(file)) {
				serializer.write(changeLog.generateChangeSets(), out);
			}

			log.info("INFO - Schema snapshot generated: {}", file.getPath());
		}
	}
	
	private static void generateDataSnapshot(String referenceUrl, String username, String password) throws Exception {
		try (Connection connection = DriverManager.getConnection(referenceUrl, username, password)) {
			Database liquibaseDb = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));
			
			// Create snapshot for data objects only
			DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(liquibaseDb.getDefaultSchema(),
				liquibaseDb, new SnapshotControl(liquibaseDb, Data.class));
			
			DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(snapshot, null,
				new CompareControl(new HashSet<>(Collections.singletonList(Data.class))));
			
			DiffToChangeLog changeLog = new DiffToChangeLog(diffResult, new DiffOutputControl(false, 
				false, true, null));
			
			ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer("xml");
			File file = new File(PATH, DATA_SNAPSHOT_FILE);
			
			try (PrintStream out = new PrintStream(file)) {
				serializer.write(changeLog.generateChangeSets(), out);
			}

			log.info("INFO - Data snapshot generated: {}", file.getPath());
		}
	}
	
	private static void deleteOldSnapshots() {
		File dir = new File(PATH);
		if (dir.exists() && dir.isDirectory()) {
			for (File file : Objects.requireNonNull(dir.listFiles())) {
				if (file.getName().contains("SNAPSHOT")) {
					file.delete();
				}
			}
		}
	}
	
	private static void prependLicense(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			try {
				File originalFile = new File(filePath);
				File tempFile = new File(filePath + ".tmp");

				String header = """
			<!--

			  This Source Code Form is subject to the terms of the Mozilla Public License,
			  v. 2.0. If a copy of the MPL was not distributed with this file, You can
			  obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
			  the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

			  Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS.
			  graphic logo is a trademark of OpenMRS Inc.

			-->
			""";

				try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
					 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

					String firstLine = reader.readLine();

					if (firstLine != null && firstLine.trim().startsWith("<?xml")) {
						writer.write(firstLine);
						writer.newLine();
						
						writer.write(header);
					} else {
						writer.write(header);
						
						if (firstLine != null) {
							writer.write(firstLine);
							writer.newLine();
						}
					}
					
					String line;
					while ((line = reader.readLine()) != null) {
						writer.write(line);
						writer.newLine();
					}
				}
				
				if (!originalFile.delete() || !tempFile.renameTo(originalFile)) {
					throw new IOException("Failed to replace original file with licensed file.");
				}

			} catch (IOException e) {
				log.error("Error: '{}' encountered while adding header licence. ", e.getCause(), e);
			}
		}
	}
	
	protected static void setPath(String path) {
		PATH = path;
	}
}
