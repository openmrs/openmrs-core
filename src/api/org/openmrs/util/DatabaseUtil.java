/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

/**
 * Utility class that provides database related methods
 * 
 * @since 1.6
 */
public class DatabaseUtil {
	
	/**
	 * Load the jdbc driver clas for the database which is specified by the connectionUrl parameter <br/>
	 * <br/>
	 * This is only needed when loading up a jdbc connectino manually for the first time. This is
	 * not needed by most users and development practices with the openmrs API.
	 * 
	 * @param connectionUrl the connection url for the database, such as
	 *            "jdbc:mysql://localhost:3306/..."
	 * @throws ClassNotFoundException
	 */
	public static void loadDatabaseDriver(String connectionUrl) throws ClassNotFoundException {
		if (connectionUrl.contains("mysql"))
			Class.forName("com.mysql.jdbc.Driver");
		else if (connectionUrl.contains("hsqldb"))
			Class.forName("org.hsqldb.jdbcDriver");
		else if (connectionUrl.contains("postgresql"))
			Class.forName("org.postgresql.Driver");
		else if (connectionUrl.contains("oracle"))
			Class.forName("oracle.jdbc.driver.OracleDriver");
		else if (connectionUrl.contains("sqlserver"))
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
	}
}
