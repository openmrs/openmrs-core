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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I did not want to throw away this utility which i used while working on TRUNK-6028
 * for my local database instances, before switching to the ones running in a docker container.
 * To use this utility, in org.openmrs.test.jupiter.BaseContextSensitiveTest 
 * and org.openmrs.test.BaseContextSensitiveTest, replace Containers.ensureDatabaseRunning()
 * with DbUtil.ensureDatabaseCreated() and then run: mvn test -DuseInMemoryDatabase=false
 * If you do not want to use the defaults, you can use the optional command line options below:
 * -DdatabaseName -DdatabaseUsername -DdatabasePassword -DdatabaseUrl
 * The default values are: database username=root, database password=test, 
 * database name=openmrs_test, database host=localhost and database port=3306
 * If you want to override the default database connection url, you would need to use
 * %s for the database name. To use PostgreSQL, you would need -Ddatabase=postgres
 * and then the default database port would change to 5432
 */
public class DbUtil {
	
	private static final Logger log = LoggerFactory.getLogger(DbUtil.class);
	
	private static boolean databaseCreated = false;
	
	private static boolean isMySql = true;
	
	public static void ensureDatabaseCreated() {
		if (databaseCreated) {
			return;
		}
		
		createDatabase();
		
		LiquibaseUtil.ensureSchemaCreated();
		
		databaseCreated = true;
	}
	
	private static void createDatabase() {
		
		isMySql = !"postgres".equals(System.getProperty("database"));
		
		String databaseName = System.getProperty("databaseName");
		if (StringUtils.isBlank(databaseName)) {
			databaseName = "openmrs_test";
		}
		
		String username = System.getProperty("databaseUsername");
		if (StringUtils.isBlank(username)) {
			username = "root";
		}
		
		String password = System.getProperty("databasePassword");
		if (StringUtils.isBlank(password)) {
			password = "test";
		}
		
		String url = System.getProperty("databaseUrl");
		if (StringUtils.isBlank(url)) {
			url = String.format(getConnectionUrl(), databaseName);
		}
		
		System.setProperty("databaseUrl", url);
		System.setProperty("databaseName", databaseName);
		System.setProperty("databaseUsername", username);
		System.setProperty("databasePassword", password);
		System.setProperty("databaseDriver", isMySql ? "com.mysql.cj.jdbc.Driver" : "org.postgresql.Driver");
		System.setProperty("databaseDialect", isMySql ? MySQLDialect.class.getName() : PostgreSQL82Dialect.class.getName());
		
		String sql = String.format(
		    isMySql ? "create database if not exists %s default character set utf8 collate utf8_general_ci"
		            : "create database %s encoding utf8", databaseName);
		createDatabase(username, password, sql, url.replace(databaseName, ""));
		
		//needed for running liquibase changesets
		Properties runtimeProperties = TestUtil.getRuntimeProperties("openmrs");
		runtimeProperties.setProperty("connection.username", username);
		runtimeProperties.setProperty("connection.password", password);
		runtimeProperties.setProperty("connection.url", url);
		Context.setRuntimeProperties(runtimeProperties);
		
		OpenmrsConstants.DATABASE_NAME = databaseName;
	}
	
	private static int createDatabase(String user, String pw, String sql, String url) {
		
		Connection connection = null;
		Statement statement = null;
		try {
			Class.forName(System.getProperty("databaseDriver")).newInstance();

			connection = DriverManager.getConnection(url, user, pw);

			statement = connection.createStatement();
			
			return statement.executeUpdate(sql);
			
		}
		catch (SQLException sqlex) {
			log.error("error executing sql: " + sql, sqlex);
		}
		catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
			log.error("Error generated", e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
			}
			catch (SQLException e) {
				log.error("Error while closing statement", e);
			}
			try {
				
				if (connection != null) {
					connection.close();
				}
			}
			catch (Exception e) {
				log.error("Error while closing connection", e);
			}
		}
		
		return -1;
	}
	
	private static String getConnectionUrl() {
		
		String url = "jdbc:mysql://localhost:3306/%s?autoReconnect=true&sessionVariables=default_storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8";
		
		if (!isMySql) {
			url = "jdbc:postgresql://localhost:5432/%s?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
		}
		
		return url;
	}
}
