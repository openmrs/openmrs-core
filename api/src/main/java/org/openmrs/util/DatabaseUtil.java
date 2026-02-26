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

import org.hibernate.Session;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class that provides database related methods
 *
 * @since 1.6
 */
public class DatabaseUtil {

	private DatabaseUtil() {
	}

	private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String MYSQL_LEGACY_DRIVER = "com.mysql.jdbc.Driver";
	private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
	private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
	private static final String H2_DRIVER = "org.h2.Driver";
	private static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
	private static final String SQLSERVER_DRIVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
	
	private static final Set<String> ALLOWED_JDBC_DRIVERS = Set.of(
		MYSQL_DRIVER, MYSQL_LEGACY_DRIVER, MARIADB_DRIVER, POSTGRESQL_DRIVER, H2_DRIVER, HSQLDB_DRIVER, ORACLE_DRIVER, SQLSERVER_DRIVER, JTDS_DRIVER
	);
	
	private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);

	public static final String ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME = "order_entry_upgrade_settings.txt";

	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set Load the jdbc
	 * driver class for the database which is specified by the connectionUrl and connectionDriver
	 * parameters <br>
	 * <br>
	 * This is only needed when loading up a jdbc connection manually for the first time. This is
	 * not needed by most users and development practices with the openmrs API.
	 *
	 * @param connectionUrl the connection url for the database, such as
	 * "jdbc:mysql://localhost:3306/..."
	 * @param connectionDriver the database driver class name, such as "com.mysql.cj.jdbc.Driver"
	 * @throws ClassNotFoundException
	 */
	public static String loadDatabaseDriver(String connectionUrl, String connectionDriver) throws ClassNotFoundException {
		if (StringUtils.hasText(connectionDriver)) {
			if (!ALLOWED_JDBC_DRIVERS.contains(connectionDriver)) {
				log.error("Attempted to load an unauthorized database driver: {}", connectionDriver);
				throw new IllegalArgumentException("Database driver '" + connectionDriver + "' is not an allowed driver.");
			}
			Class.forName(connectionDriver);
			log.debug("set user defined Database driver class: " + connectionDriver);
		} else {
			if (connectionUrl.contains("jdbc:mysql")) {
				Class.forName(MYSQL_DRIVER);
				connectionDriver = MYSQL_DRIVER;
			} else if (connectionUrl.contains("jdbc:mariadb")) {
				Class.forName(MARIADB_DRIVER);
				connectionDriver = MARIADB_DRIVER;
			} else if (connectionUrl.contains("jdbc:hsqldb")) {
				Class.forName(HSQLDB_DRIVER);
				connectionDriver = HSQLDB_DRIVER;
			} else if (connectionUrl.contains("jdbc:postgresql")) {
				Class.forName(POSTGRESQL_DRIVER);
				connectionDriver = POSTGRESQL_DRIVER;
			} else if (connectionUrl.contains("jdbc:oracle")) {
				Class.forName(ORACLE_DRIVER);
				connectionDriver = ORACLE_DRIVER;
			} else if (connectionUrl.contains("jdbc:jtds")) {
				Class.forName(JTDS_DRIVER);
				connectionDriver = JTDS_DRIVER;
			} else if (connectionUrl.contains("sqlserver")) {
				Class.forName(SQLSERVER_DRIVER);
				connectionDriver = SQLSERVER_DRIVER;
			} else if (connectionUrl.contains("jdbc:h2")) {
				Class.forName(H2_DRIVER);
				connectionDriver = H2_DRIVER;
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
		
		final List<List<Object>> result = new ArrayList<>();
		final String query = sql;
		final boolean sessionDataManipulation = dataManipulation;
		
		session.doWork(conn -> populateResultsFromSQLQuery(conn, query, sessionDataManipulation, result));
		
		return result;
	}
	
	/**
	 * Executes the passed SQL query, enforcing select only if that parameter is set for given Connection
	 */
	public static List<List<Object>> executeSQL(Connection conn, String sql, boolean selectOnly) throws DAOException {
		sql = sql.trim();
		boolean dataManipulation = checkQueryForManipulationCommands(sql, selectOnly);
		List<List<Object>> result = new ArrayList<>();
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
				List<Object> row = new ArrayList<>();
				row.add(i);
				results.add(row);
			} else {
				ResultSet resultSet = ps.executeQuery();
				
				ResultSetMetaData rmd = resultSet.getMetaData();
				int columnCount = rmd.getColumnCount();
				
				while (resultSet.next()) {
					List<Object> rowObjects = new ArrayList<>();
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
	 * @return set of unique values
	 * @throws Exception
	 */
	public static <T> Set<T> getUniqueNonNullColumnValues(String columnName, String tableName, Class<T> type,
	        Connection connection) throws Exception {
		Set<T> uniqueValues = new HashSet<>();
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
