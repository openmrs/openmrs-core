/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openmrs.liquibase.LiquibaseProvider;
import org.openmrs.test.Containers;
import org.openmrs.util.databasechange.H2LessStrictDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseIT implements LiquibaseProvider {
	
	private static final Logger log = LoggerFactory.getLogger(DatabaseIT.class);

	public static String CONNECTION_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=LEGACY;NON_KEYWORDS=VALUE";
	
	private static final String CONTEXT = "some context";
	
	protected static final String USER_NAME = "test";
	
	protected static final String PASSWORD = "test";
	
	@BeforeEach
	public void setup() throws SQLException, ClassNotFoundException {
		this.initializeDatabase();
	}
	
	@AfterEach
	public void tearDown() throws SQLException {
		this.dropAllDatabaseObjects();
	}
	
	public Liquibase getLiquibase(String filename) throws LiquibaseException, SQLException {
		Database liquibaseConnection = DatabaseFactory.getInstance()
		        .findCorrectDatabaseImplementation(new JdbcConnection(getConnection()));
		
		liquibaseConnection.setDatabaseChangeLogTableName("liquibasechangelog");
		liquibaseConnection.setDatabaseChangeLogLockTableName("liquibasechangeloglock");
		
		return new Liquibase(filename, new ClassLoaderResourceAccessor(getClass().getClassLoader()), liquibaseConnection);
	}
	
	protected void initializeDatabase() throws ClassNotFoundException {
		if (!useInMemoryDatabase()) {
			setupContainerDB();
		} else {
			System.setProperty("databaseDialect", H2LessStrictDialect.class.getName());
		}
	}

	private void setupContainerDB() throws ClassNotFoundException {
		Containers.ensureDatabaseRunning();
		CONNECTION_URL = System.getProperty("databaseUrl");
	}

	protected void updateDatabase(String filename) throws Exception {
		Liquibase liquibase = getLiquibase(filename);
		liquibase.update(new Contexts(CONTEXT));
		liquibase.getDatabase().getConnection().commit();
	}

	protected void dropAllDatabaseObjects() throws SQLException {
		try (Connection connection = getConnection()) {
			Statement statement = connection.createStatement();
			if (useInMemoryDatabase()) {
				String query = "DROP ALL OBJECTS";
				statement.execute(query);
			} else {
				if ("postgres".equalsIgnoreCase(System.getProperty("database"))) {
					connection.setAutoCommit(true);
					String dropTables =
						"DO $$ DECLARE " +
							"    r RECORD; " +
							"BEGIN " +
							"    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') " +
							"    LOOP " +
							"        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE'; " +
							"    END LOOP; " +
							"END $$;";
					statement.execute(dropTables);
					connection.setAutoCommit(false);
				} else if ("mysql".equalsIgnoreCase(System.getProperty("database"))) {
					statement.execute("DROP DATABASE " + System.getProperty("databaseName") + ";");
					statement.execute("CREATE DATABASE " + System.getProperty("databaseName") + ";");
				}
			}
		} catch (JdbcSQLNonTransientException e) {
			log.error("connection is already closed, most likely a test method already dropped all database objects");
		}
	}

	protected void updateDatabase(List<String> filenames) throws Exception {
		log.info("liquibase files used for creating and updating the OpenMRS database are: " + filenames);

		for (String filename : filenames) {
			log.info("updating database with '{}'", filename);
			this.updateDatabase(filename);
		}
	}

	protected Connection getConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD);
		connection.setAutoCommit( false );
		return connection;
	}
	
	private Boolean useInMemoryDatabase() {
		return !"false".equalsIgnoreCase(System.getProperty("useInMemoryDatabase"));
	}
}
