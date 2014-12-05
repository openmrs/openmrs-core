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
package org.openmrs.util.databasechange;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Marking in concept at least one name in a locale as locale_preferred
 * Setting FULLY_SPECIFIED if is not set
 */
public class ConceptNamesValidatorChangeSet implements CustomTaskChange {
	
	private final static Log log = LogFactory.getLog(ConceptNamesValidatorChangeSet.class);
	
	HashMap<Integer, Boolean> ids = new HashMap<Integer, Boolean>();
	
	LinkedList<Integer> withoutFullySpecified = new LinkedList<Integer>();
	
	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		String locale = "";
		int conceptNameId = 0, conceptId = 0, localePreferred = 0;
		boolean hasFullySpecified = true;
		Statement stmt = null;
		
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT concept_name_id, concept_id, locale, locale_preferred, "
			        + "concept_name_type FROM concept_name ORDER BY concept_id, locale, concept_name_type;");
			
			while (rs.next()) {
				
				if (rs.getInt("concept_id") > conceptId || !locale.equals(rs.getString("locale"))) {
					
					if (conceptNameId > 0) {
						ids.put(conceptNameId, hasFullySpecified);
						conceptNameId = 0;
					} else if (!hasFullySpecified) {
						withoutFullySpecified.add(localePreferred);
					}
					
					conceptId = rs.getInt("concept_id");
					locale = rs.getString("locale");
					localePreferred = 0;
					hasFullySpecified = false;
				}
				
				if (!hasFullySpecified && "FULLY_SPECIFIED".equals(rs.getString("concept_name_type"))) {
					hasFullySpecified = true;
				}
				
				if (localePreferred == 0) {
					if (rs.getBoolean("locale_preferred")) {
						localePreferred = rs.getInt("concept_name_id");
						conceptNameId = 0;
					} else if (conceptNameId == 0 || "FULLY_SPECIFIED".equals(rs.getString("concept_name_type"))) {
						conceptNameId = rs.getInt("concept_name_id");
					}
				}
			}
			
			if (conceptNameId > 0) {
				ids.put(conceptNameId, hasFullySpecified);
			} else if (!hasFullySpecified) {
				withoutFullySpecified.add(localePreferred);
			}
			
			updateConceptsNames(connection);
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
	}
	
	private void updateConceptsNames(JdbcConnection connection) {
		PreparedStatement typeStmt = null, localeStmt = null, localeAndTypeStmt = null;
		
		try {
			connection.setAutoCommit(false);
			typeStmt = connection
			        .prepareStatement("UPDATE concept_name SET concept_name_type = 'FULLY_SPECIFIED' WHERE concept_name_id = ?");
			localeStmt = connection
			        .prepareStatement("UPDATE concept_name SET locale_preferred = 1 WHERE concept_name_id = ?");
			localeAndTypeStmt = connection
			        .prepareStatement("UPDATE concept_name SET locale_preferred = 1, concept_name_type = 'FULLY_SPECIFIED' WHERE concept_name_id = ?");
			
			for (Map.Entry<Integer, Boolean> entry : ids.entrySet()) {
				if (entry.getValue()) {
					localeStmt.setInt(1, entry.getKey());
					localeStmt.addBatch();
				} else {
					localeAndTypeStmt.setInt(1, entry.getKey());
					localeAndTypeStmt.addBatch();
				}
			}
			
			for (Integer i : withoutFullySpecified) {
				typeStmt.setInt(1, i);
				typeStmt.addBatch();
			}
			
			try {
				tryUpdate(typeStmt);
				tryUpdate(localeStmt);
				tryUpdate(localeAndTypeStmt);
				
				log.debug("Committing updates...");
				connection.commit();
			}
			catch (BatchUpdateException be) {
				log.warn("Error generated while processsing batch update", be);
				int[] updateCounts = be.getUpdateCounts();
				
				for (int i = 0; i < updateCounts.length; i++) {
					if (updateCounts[i] > -1) {
						log.warn("Executed with exception: updateCount=" + updateCounts[i]);
					} else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
						log.warn("Executed with exception; No Success info");
					} else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
						log.warn("Failed to execute update with exception");
					}
				}
				
				try {
					log.warn("Rolling back batch", be);
					connection.rollback();
				}
				catch (Exception rbe) {
					log.warn("Error generated while rolling back batch update", be);
				}
			}
		}
		catch (DatabaseException e) {
			log.warn("Error generated", e);
		}
		catch (SQLException e) {
			log.warn("Error generated", e);
		}
		finally {
			try {
				connection.setAutoCommit(true);
			}
			catch (DatabaseException e) {
				log.warn("Failed to reset auto commit back to true", e);
			}
			closeStatement(typeStmt);
			closeStatement(localeStmt);
			closeStatement(localeAndTypeStmt);
		}
	}
	
	private void tryUpdate(PreparedStatement stmt) throws SQLException {
		int[] updateCounts = stmt.executeBatch();
		for (int i = 0; i < updateCounts.length; i++) {
			if (updateCounts[i] > -1) {
				log.debug("Successfully executed: updateCount=" + updateCounts[i]);
			} else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
				log.debug("Successfully executed; No Success info");
			} else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
				log.warn("Failed to execute update");
			}
		}
	}
	
	private void closeStatement(PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (SQLException e) {
				log.warn("Failed to close the prepared statement object");
			}
		}
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished validating concepts names locales";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.resource.ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
}
