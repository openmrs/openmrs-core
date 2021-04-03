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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

import org.openmrs.util.DatabaseUtil;

public class DatabaseDetective {
	
	private static final String CONNECTION_URL = "connection.url";
	
	private static final String CONNECTION_USERNAME = "connection.username";
	
	private static final String CONNECTION_PASSWORD = "connection.password";
	
	/**
	 * Check whether openmrs database is empty. Having just one non-liquibase table in the given
	 * database qualifies this as a non-empty database.
	 *
	 * @param props the runtime properties
	 * @return true if the openmrs database is empty or does not exist yet
	 */
	public boolean isDatabaseEmpty(Properties props) {
		if (props == null) {
			return true;
		}
		
		Connection connection = null;
		
		try {
			DatabaseUtil.loadDatabaseDriver(props.getProperty(CONNECTION_URL), null);
			
			connection = DriverManager.getConnection(props.getProperty(CONNECTION_URL), props
			        .getProperty(CONNECTION_USERNAME), props.getProperty(CONNECTION_PASSWORD));
			
			DatabaseMetaData dbMetaData = connection.getMetaData();
			
			String[] types = { "TABLE" };
			
			//get all tables
			ResultSet tbls = dbMetaData.getTables(null, null, null, types);
			
			while (tbls.next()) {
				String tableName = tbls.getString("TABLE_NAME");
				//if any table exist besides "liquibasechangelog" or "liquibasechangeloglock", return false
				if (!("liquibasechangelog".equals(tableName.toLowerCase()))
				        && !("liquibasechangeloglock".equals(tableName.toLowerCase()))) {
					return false;
				}
			}
			return true;
		}
		catch (Exception e) {
			// consider the database to be empty
			return true;
		}
		finally {
			try {
				if (connection != null) {
					connection.close();
				}
			}
			catch (Exception e) {
				// consider the database to be empty
				return true;
			}
		}
	}
}
