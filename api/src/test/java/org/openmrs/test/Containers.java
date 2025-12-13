/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.util.Collections;
import java.util.Properties;

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.openmrs.api.context.Context;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class Containers {

	private static MySQLContainer<?> mysql;
	private static PostgreSQLContainer<?> postgres;
	
	private static final String USERNAME =  "test";
	private static final String  PASSWORD = "test";
	private static final String  DATABASE = "openmrs";
	
	private static boolean schemaCreated = false;

	
	public static void ensureDatabaseRunning() {

		if(mysql != null || postgres != null) {
			return;
		}
		
		if ("postgres".equals(System.getProperty("database"))) {
			ensurePostgreSQLRunning();
		}
		else {
			ensureMySQLRunning();
		}
	}
	
	
    private static void ensureMySQLRunning() {
    	
        if (mysql == null) {
        	mysql = newMySQLContainer();
        }
        
        if (!mysql.isRunning()) {
        	try {
        		mysql.start();
        	} catch (Exception e) {
        		String msg = "Failed to start MySQL container. This usually means Docker is not available or not running. "
        				+ "Please ensure Docker is installed and running, or use -DuseInMemoryDatabase=true to use in-memory database. "
        				+ "Original error: " + e.getMessage();
        		throw new IllegalStateException(msg, e);
        	}
        }
        
        // Always set system properties if container is running, not just when starting
        if (mysql.isRunning()) {
        	System.setProperty("databaseUrl", mysql.getJdbcUrl());
    		System.setProperty("databaseName", DATABASE);
    		System.setProperty("databaseUsername", USERNAME);
    		System.setProperty("databasePassword", PASSWORD);
    		System.setProperty("databaseDriver", mysql.getDriverClassName());
    		System.setProperty("databaseDialect", MySQLDialect.class.getName());
			System.setProperty("database", "mysql");
    		
    		// Only create schema once
    		if (!schemaCreated) {
    			createSchema();
    			schemaCreated = true;
    		}
        } else {
        	throw new IllegalStateException("MySQL container failed to start. Please check Docker configuration.");
        }
    }

	public static MariaDBContainer<?> newMariaDBContainer() {
		return new MariaDBContainer<>("mariadb:10.11.7")
			.withUsername(USERNAME)
			.withPassword(PASSWORD)
			.withDatabaseName(DATABASE)
			.withUrlParam("autoReconnect", "true")
			.withUrlParam("sessionVariables", "default_storage_engine=InnoDB")
			.withUrlParam("useUnicode", "true")
			.withUrlParam("characterEncoding", "UTF-8")
			.withCommand("mysqld --character-set-server=utf8 --collation-server=utf8_general_ci")
			.withTmpFs(Collections.singletonMap("/var/lib/mysql", "rw"))
			.withReuse(true);
	}

	public static MySQLContainer<?> newMySQLContainer() {
		return new MySQLContainer<>("mysql:5.7.39")
			.withUsername(USERNAME)
			.withPassword(PASSWORD)
			.withDatabaseName(DATABASE)
			.withUrlParam("autoReconnect", "true")
			.withUrlParam("sessionVariables", "default_storage_engine=InnoDB")
			.withUrlParam("useUnicode", "true")
			.withUrlParam("characterEncoding", "UTF-8")
			.withCommand("mysqld --character-set-server=utf8 --collation-server=utf8_general_ci")
			.withTmpFs(Collections.singletonMap("/var/lib/mysql", "rw"))
			.withReuse(true);
	}
	
	private static void ensurePostgreSQLRunning() {
        if (postgres == null) {
			postgres = newPostgreSQLContainer();
		}
        
        if (!postgres.isRunning()) {
        	try {
        		postgres.start();
        	} catch (Exception e) {
        		String msg = "Failed to start PostgreSQL container. This usually means Docker is not available or not running. "
        				+ "Please ensure Docker is installed and running, or use -DuseInMemoryDatabase=true to use in-memory database. "
        				+ "Original error: " + e.getMessage();
        		throw new IllegalStateException(msg, e);
        	}
        }
        
        // Always set system properties if container is running, not just when starting
        if (postgres.isRunning()) {
            System.setProperty("databaseUrl", postgres.getJdbcUrl());
    		System.setProperty("databaseName", DATABASE);
    		System.setProperty("databaseUsername", USERNAME);
    		System.setProperty("databasePassword", PASSWORD);
    		System.setProperty("databaseDriver", postgres.getDriverClassName());
    		System.setProperty("databaseDialect", PostgreSQLDialect.class.getName());
			
    		// Only create schema once
    		if (!schemaCreated) {
    			createSchema();
    			schemaCreated = true;
    		}
        } else {
        	throw new IllegalStateException("PostgreSQL container failed to start. Please check Docker configuration.");
        }
    }

	public static PostgreSQLContainer<?> newPostgreSQLContainer() {
		return new PostgreSQLContainer<>("postgres:14.5")
			.withUsername(USERNAME)
			.withPassword(PASSWORD)
			.withDatabaseName(DATABASE)
			.withReuse(true);
	}

	private static void createSchema() {
    	
    	//needed for running liquibase changesets
		Properties runtimeProperties = TestUtil.getRuntimeProperties("openmrs");
		runtimeProperties.setProperty("connection.username", USERNAME);
		runtimeProperties.setProperty("connection.password", PASSWORD);
		runtimeProperties.setProperty("connection.url", System.getProperty("databaseUrl"));
		Context.setRuntimeProperties(runtimeProperties);
    			
    	LiquibaseUtil.ensureSchemaCreated();
    }
}
