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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.db.DAOException;

/**
 * Utility class that provides database related methods
 * 
 * @since 1.6
 */
public class DatabaseUtil {
	
	private final static Log log = LogFactory.getLog(DatabaseUtil.class);
	
	/**
	 * Load the jdbc driver clas for the database which is specified by the connectionUrl parameter <br/>
	 * <br/>
	 * This is only needed when loading up a jdbc connection manually for the first time. This is
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
		else if (connectionUrl.contains("jtds"))
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
		else if (connectionUrl.contains("sqlserver"))
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
	}
	
	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set
	 */
	public static List<List<Object>> executeSQL(Connection conn, String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = false;
		
		String sqlLower = sql.toLowerCase();
		if (sqlLower.startsWith("insert") || sqlLower.startsWith("update") || sqlLower.startsWith("delete")
		        || sqlLower.startsWith("alter") || sqlLower.startsWith("drop") || sqlLower.startsWith("create")
		        || sqlLower.startsWith("rename")) {
			dataManipulation = true;
		}
		
		if (selectOnly && dataManipulation)
			throw new IllegalArgumentException("Illegal command(s) found in query string");
		
		PreparedStatement ps = null;
		List<List<Object>> results = new Vector<List<Object>>();
		
		try {
			ps = conn.prepareStatement(sql);
			
			if (dataManipulation == true) {
				Integer i = ps.executeUpdate();
				List<Object> row = new Vector<Object>();
				row.add(i);
				results.add(row);
			} else {
				ResultSet resultSet = ps.executeQuery();
				
				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();
				
				while (resultSet.next()) {
					List<Object> rowObjects = new Vector<Object>();
					for (int x = 1; x <= columnCount; x++) {
						rowObjects.add(resultSet.getObject(x));
					}
					results.add(rowObjects);
				}
			}
		}
		catch (Exception e) {
			log.debug("Error while running sql: " + sql, e);
			throw new DAOException("Error while running sql: " + sql + " . Message: " + e.getMessage(), e);
		}
		finally {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException e) {
					log.error("Error generated while closing statement", e);
				}
			}
		}
		
		return results;
	}
}
