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
import org.hibernate.dialect.MySQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openmrs.liquibase.LiquibaseProvider;
import org.openmrs.test.Containers;
import org.openmrs.util.databasechange.H2LessStrictDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseIT implements LiquibaseProvider {
	
	private static final Logger log = LoggerFactory.getLogger(DatabaseIT.class);

	@Deprecated
	public static final String CONNECTION_URL = "jdbc:h2:mem:openmrs;DB_CLOSE_DELAY=-1";

	public static String JDBC_CONNECTION_URL = "";
	
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
		if(useInMemoryDatabase()) {
			String driver = "org.h2.Driver";
			Class.forName(driver);
			JDBC_CONNECTION_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
			return;
		}
		setupContainerDB();
	}

	private void setupContainerDB() throws ClassNotFoundException {
		Containers.ensureDatabaseRunning();
		Class.forName(Containers.getDatabaseDriverClassName());
		JDBC_CONNECTION_URL = Containers.getDatabaseURL();
	}

	protected void updateDatabase(String filename) throws Exception {
		Liquibase liquibase = getLiquibase(filename);
		liquibase.update(new Contexts(CONTEXT));
		liquibase.getDatabase().getConnection().commit();
	}

	protected void dropAllDatabaseObjects() throws SQLException {
		Connection connection = getConnection();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			if(useInMemoryDatabase()) {
				String query = "DROP ALL OBJECTS";
				statement.execute(query);
			}
			else {
				if ("postgres".equals(System.getProperty("database"))) {
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
				} else if ("mysql".equals(System.getProperty("database"))) {
					statement.execute("DROP DATABASE " + Containers.getDatabaseName() + ";");
					statement.execute("CREATE DATABASE " + Containers.getDatabaseName() + ";");
				}
			}
		} catch (JdbcSQLNonTransientException e) {
			log.error("connection is already closed, most likely a test method already dropped all database objects");
		} finally {
			connection.close();
		}
	}

	protected void updateDatabase( List<String> filenames) throws Exception {
		log.info("liquibase files used for creating and updating the OpenMRS database are: " + filenames);

		for (String filename : filenames) {
			log.info("updating database with '{}'", filename);
			this.updateDatabase(filename);
		}
	}

	protected Connection getConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, USER_NAME, PASSWORD);
		connection.setAutoCommit( false );
		return connection;
	}
	
	protected String getDatabaseDialectName() {
		if (useInMemoryDatabase()) {
			return H2LessStrictDialect.class.getName();
		} else {
			return MySQLDialect.class.getName();
		}
	}
	
	private Boolean useInMemoryDatabase() {
		return !"false".equals(System.getProperty("useInMemoryDatabase"));
	}
}
