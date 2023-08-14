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
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.openmrs.api.context.Context;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class Containers {

	private static MySQLContainer<?> mysql;
	private static PostgreSQLContainer<?> postgres;
	
	private static final String USERNAME =  "test";
	private static final String  PASSWORD = "test";
	private static final String  DATABASE = "openmrs";

	
	public static void ensureDatabaseRunning() {
		
		if (mysql != null || postgres != null) {
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
        	
        	mysql = new MySQLContainer<>("mysql:5.7.39")
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
        
        if (!mysql.isRunning()) {
        	
        	mysql.start();
        	
        	System.setProperty("databaseUrl", mysql.getJdbcUrl());
    		System.setProperty("databaseName", DATABASE);
    		System.setProperty("databaseUsername", USERNAME);
    		System.setProperty("databasePassword", PASSWORD);
    		System.setProperty("databaseDriver", mysql.getDriverClassName());
    		System.setProperty("databaseDialect", MySQLDialect.class.getName());
    		
    		createSchema();
        }
    }
    
    private static void ensurePostgreSQLRunning() {

        if (postgres == null) {
        	
            postgres = new PostgreSQLContainer<>("postgres:14.5")
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withDatabaseName(DATABASE)
                .withReuse(true);
        }
        
        if (!postgres.isRunning()) {
        	
            postgres.start();
            
            System.setProperty("databaseUrl", postgres.getJdbcUrl());
    		System.setProperty("databaseName", DATABASE);
    		System.setProperty("databaseUsername", USERNAME);
    		System.setProperty("databasePassword", PASSWORD);
    		System.setProperty("databaseDriver", postgres.getDriverClassName());
    		System.setProperty("databaseDialect", PostgreSQL82Dialect.class.getName());
    		
    		createSchema();
        }
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
