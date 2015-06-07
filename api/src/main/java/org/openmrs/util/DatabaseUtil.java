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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.openmrs.api.db.DAOException;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class that provides database related methods
 *
 * @since 1.6
 */
public class DatabaseUtil {
	
	private final static Log log = LogFactory.getLog(DatabaseUtil.class);
	
	/**
	 * Load the jdbc driver class for the database which is specified by the connectionUrl parameter <br/>
	 * This is only needed when loading up a jdbc connection manually for the first time. This is
	 * not needed by most users and development practices with the openmrs API.
	 *
	 * @param connectionUrl the connection url for the database, such as
	 *                      "jdbc:mysql://localhost:3306/..."
	 * @throws ClassNotFoundException
	 * @deprecated
	 */
	@Deprecated
	public static void loadDatabaseDriver(String connectionUrl) throws ClassNotFoundException {
		loadDatabaseDriver(connectionUrl, null);
	}
	
	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set Load the jdbc
	 * driver class for the database which is specified by the connectionUrl and connectionDriver
	 * parameters <br/>
	 * <br/>
	 * This is only needed when loading up a jdbc connection manually for the first time. This is
	 * not needed by most users and development practices with the openmrs API.
	 *
	 * @param connectionUrl the connection url for the database, such as
	 * "jdbc:mysql://localhost:3306/..."
	 * @param connectionDriver the database driver class name, such as "com.mysql.jdbc.Driver"
	 * @throws ClassNotFoundException
	 */
	
	public final static String ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME = "order_entry_upgrade_settings.txt";
	
	public static String loadDatabaseDriver(String connectionUrl, String connectionDriver) throws ClassNotFoundException {
		if (StringUtils.hasText(connectionDriver)) {
			Class.forName(connectionDriver);
			log.debug("set user defined Database driver class: " + connectionDriver);
		} else {
			if (connectionUrl.contains("mysql")) {
				Class.forName("com.mysql.jdbc.Driver");
				connectionDriver = "com.mysql.jdbc.Driver";
			} else if (connectionUrl.contains("hsqldb")) {
				Class.forName("org.hsqldb.jdbcDriver");
				connectionDriver = "org.hsqldb.jdbcDriver";
			} else if (connectionUrl.contains("postgresql")) {
				Class.forName("org.postgresql.Driver");
				connectionDriver = "org.postgresql.Driver";
			} else if (connectionUrl.contains("oracle")) {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				connectionDriver = "oracle.jdbc.driver.OracleDriver";
			} else if (connectionUrl.contains("jtds")) {
				Class.forName("net.sourceforge.jtds.jdbc.Driver");
				connectionDriver = "net.sourceforge.jtds.jdbc.Driver";
			} else if (connectionUrl.contains("sqlserver")) {
				Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
				connectionDriver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			}
		}
		log.info("Set database driver class as " + connectionDriver);
		return connectionDriver;
	}
	
	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set for given Session
	 */
	public static List<List<Object>> executeSQL(Session session, String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = checkQueryForManipulationCommands(sql, selectOnly);
		
		final List<List<Object>> result = new ArrayList<List<Object>>();
		final String query = sql;
		final boolean sessionDataManipulation = dataManipulation;
		
		//todo replace with lambdas after moving on to Java 8
		session.doWork(new Work() {
			
			@Override
			public void execute(Connection conn) {
				populateResultsFromSQLQuery(conn, query, sessionDataManipulation, result);
			}
		});
		
		return result;
	}
	
	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set for given Connection
	 */
	public static List<List<Object>> executeSQL(Connection conn, String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = checkQueryForManipulationCommands(sql, selectOnly);
		List<List<Object>> result = new ArrayList<List<Object>>();
		populateResultsFromSQLQuery(conn, sql, dataManipulation, result);
		return result;
	}
	
	private static boolean checkQueryForManipulationCommands(String sql, boolean selectOnly) {
		boolean dataManipulation = false;
		
		String sqlLower = sql.toLowerCase();
		if (sqlLower.startsWith("insert") || sqlLower.startsWith("update") || sqlLower.startsWith("delete")
		        || sqlLower.startsWith("alter") || sqlLower.startsWith("drop") || sqlLower.startsWith("create")
		        || sqlLower.startsWith("rename")) {
			dataManipulation = true;
		}
		
		if (selectOnly && dataManipulation) {
			throw new IllegalArgumentException("Illegal command(s) found in query string");
		}
		return dataManipulation;
	}
	
	private static void populateResultsFromSQLQuery(Connection conn, String sql, boolean dataManipulation,
	        List<List<Object>> results) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			if (dataManipulation) {
				Integer i = ps.executeUpdate();
				List<Object> row = new ArrayList<Object>();
				row.add(i);
				results.add(row);
			} else {
				ResultSet resultSet = ps.executeQuery();
				
				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();
				
				while (resultSet.next()) {
					List<Object> rowObjects = new ArrayList<Object>();
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
	}
	
	/**
	 * Gets all unique values excluding nulls in the specified column and table
	 *
	 * @param columnName the column
	 * @param tableName  the table
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	public static <T> Set<T> getUniqueNonNullColumnValues(String columnName, String tableName, Class<T> type,
	        Connection connection) throws Exception {
		Set<T> uniqueValues = new HashSet<T>();
		final String alias = "unique_values";
		String select = "SELECT DISTINCT " + columnName + " AS " + alias + " FROM " + tableName + " WHERE " + columnName
		        + " IS NOT NULL";
		List<List<Object>> rows = DatabaseUtil.executeSQL(connection, select, true);
		for (List<Object> row : rows) {
			//There can only be one column since we are selecting one
			uniqueValues.add((T) row.get(0));
		}
		
		return uniqueValues;
	}
}
