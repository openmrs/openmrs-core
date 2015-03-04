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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

/**
 * Creates concept reference terms from existing rows in the concept_reference_map table.
 * <p>
 * The terms are created only for a unique source and code.
 */
public class MigrateConceptReferenceTermChangeSet implements CustomTaskChange {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public static final String DEFAULT_CONCEPT_MAP_TYPE = "NARROWER-THAN";
	
	/**
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		final JdbcConnection connection = (JdbcConnection) database.getConnection();
		Boolean prevAutoCommit = null;
		
		PreparedStatement selectTypes = null;
		PreparedStatement batchUpdateMap = null;
		PreparedStatement selectMap = null;
		PreparedStatement updateMapTerm = null;
		PreparedStatement insertTerm = null;
		PreparedStatement updateMapType = null;
		
		try {
			prevAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			//Prepare a list of types and their ids.
			Map<String, Integer> typesToIds = new HashMap<String, Integer>();
			
			selectTypes = connection.prepareStatement("select * from concept_map_type");
			selectTypes.execute();
			ResultSet selectTypeResult = selectTypes.getResultSet();
			
			while (selectTypeResult.next()) {
				typesToIds.put(selectTypeResult.getString("name").trim().toUpperCase(), selectTypeResult
				        .getInt("concept_map_type_id"));
			}
			selectTypes.close();
			
			//The FK on concept_reference_term_id is not yet created so we are safe to copy over IDs. 
			//The trims are done to be able to compare properly.
			batchUpdateMap = connection.prepareStatement("update concept_reference_map set"
			        + " concept_reference_term_id = concept_map_id,"
			        + " source_code = trim(source_code), comment = trim(comment)");
			batchUpdateMap.execute();
			batchUpdateMap.close();
			
			//Preparing statements for use in the loop.
			updateMapTerm = connection.prepareStatement("update concept_reference_map set"
			        + " concept_reference_term_id = ? where concept_map_id = ?");
			insertTerm = connection.prepareStatement("insert into concept_reference_term"
			        + " (concept_reference_term_id, uuid, concept_source_id, code, creator, date_created, description)"
			        + " values (?, ?, ?, ?, ?, ?, ?)");
			updateMapType = connection.prepareStatement("update concept_reference_map set"
			        + " concept_map_type_id = ? where concept_map_id = ?");
			
			int prevSource = -1;
			String prevSourceCode = null;
			String prevComment = null;
			int prevInsertedTerm = -1;
			
			//In addition to source and source_code we order by UUID to always insert the same term if run on different systems.
			selectMap = connection.prepareStatement("select * from concept_reference_map"
			        + " order by source, source_code, uuid");
			selectMap.execute();
			
			final ResultSet selectMapResult = selectMap.getResultSet();
			
			while (selectMapResult.next()) {
				final int conceptMapId = selectMapResult.getInt("concept_map_id");
				final int source = selectMapResult.getInt("source");
				final String sourceCode = selectMapResult.getString("source_code");
				final String comment = selectMapResult.getString("comment");
				final int creator = selectMapResult.getInt("creator");
				final Date dateCreated = selectMapResult.getDate("date_created");
				final String uuid = selectMapResult.getString("uuid");
				
				final Integer mapTypeId = determineMapTypeId(comment, typesToIds);
				final int updatedMapTypeId = (mapTypeId == null) ? typesToIds.get(DEFAULT_CONCEPT_MAP_TYPE) : mapTypeId;
				updateMapType.setInt(1, updatedMapTypeId);
				updateMapType.setInt(2, conceptMapId);
				updateMapType.execute();
				if (updateMapType.getUpdateCount() != 1) {
					throw new CustomChangeException("Failed to set map type: " + mapTypeId + " for map: " + conceptMapId
					        + ", updated rows: " + updateMapType.getUpdateCount());
				}
				
				if (source == prevSource
				        && (sourceCode == prevSourceCode || (sourceCode != null && sourceCode.equals(prevSourceCode)))) {
					if (mapTypeId == null && comment != null && !comment.equals(prevComment)) {
						log.warn("Lost comment '" + comment + "' for map " + conceptMapId + ". Preserved comment "
						        + prevComment);
					}
					
					//We need to use the last inserted term.
					updateMapTerm.setInt(1, prevInsertedTerm);
					updateMapTerm.setInt(2, conceptMapId);
					
					updateMapTerm.execute();
					if (updateMapTerm.getUpdateCount() != 1) {
						throw new CustomChangeException("Failed to set reference term: " + prevInsertedTerm + " for map: "
						        + conceptMapId + ", updated rows: " + updateMapTerm.getUpdateCount());
					}
				} else {
					insertTerm.setInt(1, conceptMapId);
					//We need to guaranty that UUIDs are always the same when run on different systems.
					insertTerm.setString(2, UUID.nameUUIDFromBytes(uuid.getBytes()).toString());
					insertTerm.setInt(3, source);
					insertTerm.setString(4, sourceCode);
					insertTerm.setInt(5, creator);
					insertTerm.setDate(6, dateCreated);
					if (mapTypeId == null) {
						insertTerm.setString(7, comment);
					} else {
						insertTerm.setString(7, null);
					}
					
					insertTerm.execute();
					
					prevInsertedTerm = conceptMapId;
				}
				
				prevSource = source;
				prevSourceCode = sourceCode;
				prevComment = comment;
			}
			selectMap.close();
			updateMapType.close();
			updateMapTerm.close();
			insertTerm.close();
			
			connection.commit();
		}
		catch (Exception e) {
			try {
				if (connection != null) {
					connection.rollback();
				}
			}
			catch (Exception ex) {
				log.error("Failed to rollback", ex);
			}
			
			throw new CustomChangeException(e);
		}
		finally {
			closeStatementQuietly(selectTypes);
			closeStatementQuietly(batchUpdateMap);
			closeStatementQuietly(selectMap);
			closeStatementQuietly(updateMapTerm);
			closeStatementQuietly(insertTerm);
			closeStatementQuietly(updateMapType);
			
			if (connection != null && prevAutoCommit != null) {
				try {
					connection.setAutoCommit(prevAutoCommit);
				}
				catch (DatabaseException e) {
					log.error("Failed to reset auto commit", e);
				}
			}
		}
	}
	
	/**
	 * Closes the statement quietly.
	 * 
	 * @param statement
	 */
	private void closeStatementQuietly(PreparedStatement statement) {
		if (statement != null) {
			try {
				statement.close();
			}
			catch (SQLException e) {
				log.error("Failed to close statement", e);
			}
		}
	}
	
	/**
	 * Determines the map type based on the given comment.
	 * 
	 * @param comment
	 * @param typesToIds 
	 * @return map type id or null if not recognized
	 */
	protected Integer determineMapTypeId(String comment, Map<String, Integer> typesToIds) {
		Integer mapTypeId = null;
		
		if (!StringUtils.isBlank(comment)) {
			comment = comment.toUpperCase();
			
			if (comment.startsWith("MAP TYPE:")) {
				comment = comment.substring(9).trim();
				
				if (comment.equals("SAME-AS FROM RXNORM")) {
					comment = "SAME-AS";
				}
				
				mapTypeId = typesToIds.get(comment);
			}
		}
		return mapTypeId;
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished migrating concept reference terms";
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
		return null;
	}
	
}
