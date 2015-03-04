/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import java.sql.BatchUpdateException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.DatabaseUpdater;

/**
 * Inserts core concept map types into the concept map type table
 */
public class AddConceptMapTypesChangeset implements CustomTaskChange {
	
	private static final Log log = LogFactory.getLog(AddConceptMapTypesChangeset.class);
	
	/**
	 * The "visibleConceptMapTypes" parameter defined in the liquibase xml changeSet element that is
	 * calling this class, it value is expected to be a comma separated list of concept map type
	 * names to add as the visible ones
	 */
	private String visibleConceptMapTypes;
	
	/**
	 * The "hiddenConceptMapTypess" parameter defined in the liquibase xml changeSet element that is
	 * calling this class, it value is expected to be a comma separated list of concept map type
	 * names to add as the hidden ones
	 */
	private String hiddenConceptMapTypes;
	
	private String[] visibleConceptMapTypeArray;
	
	private String[] hiddenConceptMapTypeArray;
	
	/**
	 * Does the work of adding UUIDs to all rows.
	 *
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	public void execute(Database database) throws CustomChangeException {
		runBatchInsert((JdbcConnection) database.getConnection());
	}
	
	/**
	 * Executes all the changes to the concept names as a batch update.
	 *
	 * @param connection The database connection
	 */
	private void runBatchInsert(JdbcConnection connection) throws CustomChangeException {
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			
			Integer userId = DatabaseUpdater.getAuthenticatedUserId();
			//if we have no authenticated user(for API users), set as Daemon
			if (userId == null || userId < 1) {
				userId = getInt(connection, "SELECT min(user_id) FROM users");
				//leave it as null rather than setting it to 0
				if (userId < 1) {
					userId = null;
				}
			}
			
			//userId is not a param, because it's easier this way if it's null
			pStmt = connection.prepareStatement("INSERT INTO concept_map_type "
			        + "(concept_map_type_id, name, is_hidden, retired, creator, date_created, uuid) VALUES(?,?,?,?,"
			        + userId + ",?,?)");
			
			int mapTypeId = 1;
			
			for (String map : visibleConceptMapTypeArray) {
				String[] mapTypeAndUuid = map.trim().split("\\|");
				String mapType = mapTypeAndUuid[0];
				String mapUuid = mapTypeAndUuid[1];
				
				pStmt.setInt(1, mapTypeId);
				pStmt.setString(2, mapType);
				pStmt.setBoolean(3, false);
				pStmt.setBoolean(4, false);
				pStmt.setDate(5, new Date(Calendar.getInstance().getTimeInMillis()));
				pStmt.setString(6, mapUuid);
				pStmt.addBatch();
				
				mapTypeId++;
			}
			
			for (String map : hiddenConceptMapTypeArray) {
				String[] mapTypeAndUuid = map.trim().split("\\|");
				String mapType = mapTypeAndUuid[0];
				String mapUuid = mapTypeAndUuid[1];
				
				pStmt.setInt(1, mapTypeId);
				pStmt.setString(2, mapType);
				pStmt.setBoolean(3, true);
				pStmt.setBoolean(4, false);
				pStmt.setDate(5, new Date(Calendar.getInstance().getTimeInMillis()));
				pStmt.setString(6, mapUuid);
				pStmt.addBatch();
				
				mapTypeId++;
			}
			
			try {
				int[] updateCounts = pStmt.executeBatch();
				for (int i = 0; i < updateCounts.length; i++) {
					if (updateCounts[i] > -1) {
						log.debug("Successfully executed: updateCount=" + updateCounts[i]);
					} else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
						log.debug("Successfully executed; No Success info");
					} else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
						log.warn("Failed to execute insert");
					}
				}
				
				log.debug("Committing inserts...");
				connection.commit();
			}
			catch (BatchUpdateException be) {
				log.warn("Error generated while processsing batch insert", be);
				int[] updateCounts = be.getUpdateCounts();
				
				for (int i = 0; i < updateCounts.length; i++) {
					if (updateCounts[i] > -1) {
						log.warn("Executed with exception: insertCount=" + updateCounts[i]);
					} else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
						log.warn("Executed with exception; No Success info");
					} else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
						log.warn("Failed to execute insert with exception");
					}
				}
				
				try {
					log.debug("Rolling back batch", be);
					connection.rollback();
				}
				catch (Exception rbe) {
					log.warn("Error generated while rolling back batch insert", be);
				}
				
				//marks the changeset as a failed one
				throw new CustomChangeException("Failed to insert one or more concept map types", be);
			}
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Failed to insert one or more concept map types:", e);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Failed to insert one or more concept map types:", e);
		}
		finally {
			//reset to auto commit mode
			try {
				connection.setAutoCommit(true);
			}
			catch (DatabaseException e) {
				log.warn("Failed to reset auto commit back to true", e);
			}
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the resultset object");
				}
			}
			
			if (pStmt != null) {
				try {
					pStmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the prepared statement object");
				}
			}
		}
	}
	
	/**
	 * returns an integer resulting from the execution of an sql statement
	 *
	 * @param connection a DatabaseConnection
	 * @param sql the sql statement to execute
	 * @return integer resulting from the execution of the sql statement
	 */
	private int getInt(JdbcConnection connection, String sql) {
		Statement stmt = null;
		int result = 0;
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			if (rs.next()) {
				result = rs.getInt(1);
			} else {
				log.warn("No row returned by getInt() method");
			}
			
			if (rs.next()) {
				log.warn("Multiple rows returned by getInt() method");
			}
			
			return result;
		}
		catch (DatabaseException e) {
			log.warn("Error generated", e);
		}
		catch (SQLException e) {
			log.warn("Error generated", e);
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the statement object");
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Get the comma separated value of the concept map types names passed in as values for
	 * parameters
	 *
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	public void setUp() throws SetupException {
		if (StringUtils.isNotBlank(visibleConceptMapTypes)) {
			visibleConceptMapTypeArray = StringUtils.split(visibleConceptMapTypes, ",");
		}
		if (StringUtils.isNotBlank(hiddenConceptMapTypes)) {
			hiddenConceptMapTypeArray = StringUtils.split(hiddenConceptMapTypes, ",");
		}
	}
	
	/**
	 * @param visibleConceptMapTypes the visibleConceptMapTypes to set
	 */
	public void setVisibleConceptMapTypes(String visibleConceptMapTypes) {
		this.visibleConceptMapTypes = visibleConceptMapTypes;
	}
	
	/**
	 * @param hiddenConceptMapTypes the hiddenConceptMapTypes to set
	 */
	public void setHiddenConceptMapTypes(String hiddenConceptMapTypes) {
		this.hiddenConceptMapTypes = hiddenConceptMapTypes;
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	public String getConfirmationMessage() {
		return "Finished inserting core concept map types";
	}
	
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
