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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This changeset creates concept reference terms from existing rows in the existing concept_map
 * table. This should be able to run successfully for all OpenMRS supported database.
 */
public class ConceptReferenceTermChangeSet implements CustomTaskChange {
	
	private final static Log log = LogFactory.getLog(ConceptValidatorChangeSet.class);
	
	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		if (log.isInfoEnabled())
			log.debug("Generating and inserting concept reference terms");
		//insert the reference terms
		insertRows(connection, getPropertyObjectsMap(connection, true));
	}
	
	/**
	 * Convenience method that generates concept reference terms or maps from the existing
	 * concept_map table
	 * 
	 * @param connection the current database connection
	 * @param generateTerms boolean flag to specify if we are generating concept reference terms or
	 *            maps
	 * @return
	 * @throws CustomChangeException
	 */
	private List<Map<String, Object>> getPropertyObjectsMap(JdbcConnection connection, boolean generateTerms)
	        throws CustomChangeException {
		List<Map<String, Object>> listOfPropertyValueMaps = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			if (generateTerms) {
				rs = stmt
				        .executeQuery("SELECT cm.concept_map_id, cm.source, cm.source_code, cm.comment, cm.creator, cm.date_created, hl7_code "
				                + "FROM concept_reference_map cm, concept_reference_source crs WHERE cm.source = crs.concept_source_id");
			} else {
				rs = stmt
				        .executeQuery("SELECT concept_map_id, concept_id, creator, date_created, uuid FROM concept_reference_map");
			}
			
			listOfPropertyValueMaps = new LinkedList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> propertyValueMap = new HashMap<String, Object>();
				propertyValueMap.put("termId", rs.getInt("concept_map_id"));
				propertyValueMap.put("sourceId", rs.getInt("source"));
				propertyValueMap.put("code", rs.getString("source_code"));
				propertyValueMap.put("description", rs.getString("comment"));
				propertyValueMap.put("creator", rs.getInt("creator"));
				propertyValueMap.put("dateCreated", rs.getDate("date_created"));
				propertyValueMap.put("uuid", rs.getString("hl7_code").concat("-").concat(rs.getString("source_code")));
				listOfPropertyValueMaps.add(propertyValueMap);
			}
			rs.close();
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Error generated", e);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Error generated", e);
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
		
		return listOfPropertyValueMaps;
	}
	
	/**
	 * Convenience method that inserts rows into the concept reference term table. The
	 * concept_map_id values becomes the concept_reference_term_id values
	 * 
	 * @param connection the current database connection
	 * @param listOfPropertyValueMaps a list of property and value maps for the objects to insert
	 * @throws CustomChangeException
	 */
	private void insertRows(JdbcConnection connection, List<Map<String, Object>> listOfPropertyValueMaps)
	        throws CustomChangeException {
		if (CollectionUtils.isNotEmpty(listOfPropertyValueMaps)) {
			PreparedStatement pStmt = null;
			try {
				connection.setAutoCommit(false);
				pStmt = connection
				        .prepareStatement("INSERT INTO concept_reference_term"
				                + "(concept_reference_term_id, concept_source_id, code, description, creator, date_created, retired, uuid) "
				                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
				
				for (Map<String, Object> propertyValueMap : listOfPropertyValueMaps) {
					pStmt.setInt(1, (Integer) propertyValueMap.get("termId"));
					pStmt.setInt(2, (Integer) propertyValueMap.get("sourceId"));
					pStmt.setString(3, propertyValueMap.get("code").toString());
					pStmt.setString(4, (propertyValueMap.get("description") == null) ? null : propertyValueMap.get(
					    "description").toString());
					pStmt.setInt(5, (Integer) propertyValueMap.get("creator"));
					pStmt.setDate(6, (Date) propertyValueMap.get("dateCreated"));
					pStmt.setBoolean(7, false);
					pStmt.setString(8, propertyValueMap.get("uuid").toString());
					
					pStmt.addBatch();
				}
				
				try {
					int[] updateCounts = pStmt.executeBatch();
					for (int i = 0; i < updateCounts.length; i++) {
						if (updateCounts[i] > -1) {
							log.debug("Successfully executed: updateCount=" + updateCounts[i]);
						} else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
							log.debug("Successfully executed; No Success info");
						} else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
							log.warn("Failed to execute update");
						}
					}
					
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
					
					//marks the changeset as a failed one
					throw new CustomChangeException(
					        "Failed to generate concept reference terms from existing concept mappings.");
				}
			}
			catch (DatabaseException e) {
				throw new CustomChangeException("Error generated", e);
			}
			catch (SQLException e) {
				throw new CustomChangeException("Error generated", e);
			}
			finally {
				//reset to auto commit mode
				try {
					connection.setAutoCommit(true);
				}
				catch (DatabaseException e) {
					log.warn("Failed to reset auto commit back to true", e);
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
		} else
			log.error("List of property value maps is null or empty");
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	public String getConfirmationMessage() {
		return "Finished generating concept reference terms from existing concept mappings.";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	public void setUp() throws SetupException {
	}
	
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
