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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.db.DAOException;
import org.springframework.util.StringUtils;

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
	 *            "jdbc:mysql://localhost:3306/..."
	 * @throws ClassNotFoundException
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
	 *            "jdbc:mysql://localhost:3306/..."
	 * @param connectionDriver the database driver class name, such as "com.mysql.jdbc.Driver"
	 * @throws ClassNotFoundException
	 */
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
	
	/**
	 * Returns conceptId for the given units from OpenmrsConstants#GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS
	 * global property.
	 *
	 * @param connection
	 * @param units
	 * @return conceptId
	 * @throws DAOException
	 * @should return concept_id for drug_order_quantity_units
	 * @should fail if units is not specified
	 */
	public static Integer getConceptIdForUnits(Connection connection, String units) throws DAOException {
		PreparedStatement unitsToConceptsQuery;
		try {
			unitsToConceptsQuery = connection
			        .prepareStatement("select property_value from global_property where property = ?");
			unitsToConceptsQuery.setString(1, OpenmrsConstants.GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS);
			ResultSet unitsToConceptsResult = unitsToConceptsQuery.executeQuery();
			if (!unitsToConceptsResult.next()) {
				throw new DAOException(
				        OpenmrsConstants.GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS
				                + " global property must be specified before upgrading. Please refer to upgrade instructions for more details.");
			}
			
			String unitsToConceptsGP = unitsToConceptsResult.getString(1);
			String[] unitsToConcepts = unitsToConceptsGP.split(",");
			for (String unitsToConcept : unitsToConcepts) {
				if (unitsToConcept.startsWith(units)) {
					String concept = unitsToConcept.substring(units.length() + 1);// '+ 1' stands for ':'
					
					if (concept.toLowerCase().equals("null")) {
						return null;
					}
					
					try {
						return Integer.valueOf(concept);
					}
					catch (NumberFormatException e) {
						throw new DAOException(OpenmrsConstants.GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS
						        + " global property contains invalid mapping from " + units + " to concept ID " + concept
						        + ". ID must be an integer or null. Please refer to upgrade instructions for more details.",
						        e);
					}
				}
			}
		}
		catch (SQLException e) {
			throw new DAOException(e);
		}
		
		throw new DAOException(OpenmrsConstants.GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS
		        + " global property does not have mapping for " + units
		        + ". Please refer to upgrade instructions for more details.");
	}
	
	public static String getConceptUuid(Connection connection, int conceptId) throws SQLException {
		PreparedStatement select = connection.prepareStatement("select uuid from concept where concept_id = ?");
		try {
			select.setInt(1, conceptId);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				throw new IllegalArgumentException("Concept not found " + conceptId);
			}
		}
		finally {
			select.close();
		}
	}
	
	public static Integer getOrderFrequencyIdForConceptId(Connection connection, Integer conceptIdForFrequency)
	        throws SQLException {
		PreparedStatement orderFrequencyIdQuery = connection
		        .prepareStatement("select order_frequency_id from order_frequency where concept_id = ?");
		orderFrequencyIdQuery.setInt(1, conceptIdForFrequency);
		ResultSet orderFrequencyIdResultSet = orderFrequencyIdQuery.executeQuery();
		if (!orderFrequencyIdResultSet.next()) {
			return null;
		}
		return orderFrequencyIdResultSet.getInt("order_frequency_id");
	}
	
	/**
	 * Gets all unique values excluding nulls in the specified column and table
	 * 
	 * @param columnName the column
	 * @param tableName the table
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
