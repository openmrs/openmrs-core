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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Liquibase custom changeset used to identify and resolve duplicate EncounterType names. If a
 * duplicate EncounterType name is identified, it will be edited to include a suffix term which
 * makes it unique, and identifies it as a value to be manually changed during later review
 */

public class DuplicateEncounterTypeNameChangeSet implements CustomTaskChange {
	
	private static final Logger log = LoggerFactory.getLogger(DuplicateEncounterTypeNameChangeSet.class);
	
	@Override
	public String getConfirmationMessage() {
		return "Completed updating duplicate EncounterType names";
	}
	
	@Override
	public void setFileOpener(ResourceAccessor arg0) {
		
	}
	
	@Override
	public void setUp() throws SetupException {
		// No setup actions
		
	}
	
	@Override
	public ValidationErrors validate(Database arg0) {
		return null;
	}
	
	/**
	 * Method to perform validation and resolution of duplicate EncounterType names
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		Map<String, HashSet<Integer>> duplicates = new HashMap<>();
		Statement stmt = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		Boolean initialAutoCommit = null;
		
		try {
			initialAutoCommit = connection.getAutoCommit();
			
			// set auto commit mode to false for UPDATE action
			connection.setAutoCommit(false);
			
			stmt = connection.createStatement();
			rs = stmt
			        .executeQuery("SELECT * FROM encounter_type INNER JOIN (SELECT name FROM encounter_type GROUP BY name HAVING count(name) > 1) dup ON encounter_type.name = dup.name");
			
			Integer id;
			String name;
			
			while (rs.next()) {
				id = rs.getInt("encounter_type_id");
				name = rs.getString("name");
				
				if (duplicates.get(name) == null) {
					HashSet<Integer> results = new HashSet<>();
					results.add(id);
					duplicates.put(name, results);
				} else {
					HashSet<Integer> results = duplicates.get(name);
					results.add(id);
				}
			}

			for (Object o : duplicates.entrySet()) {
				Map.Entry pairs = (Map.Entry) o;

				HashSet values = (HashSet) pairs.getValue();
				List<Integer> ids = new ArrayList<Integer>(values);

				int duplicateNameId = 1;
				for (int i = 1; i < ids.size(); i++) {
					String newName = pairs.getKey() + "_" + duplicateNameId;

					List<List<Object>> duplicateResult;
					boolean duplicateName;
					Connection con = DatabaseUpdater.getConnection();

					do {
						String sqlValidatorString = "select * from encounter_type where name = '" + newName + "'";
						duplicateResult = DatabaseUtil.executeSQL(con, sqlValidatorString, true);

						if (!duplicateResult.isEmpty()) {
							duplicateNameId += 1;
							newName = pairs.getKey() + "_" + duplicateNameId;
							duplicateName = true;
						} else {
							duplicateName = false;
						}
					} while (duplicateName);

					pStmt = connection.prepareStatement("update encounter_type set name = ? where encounter_type_id = ?");
					pStmt.setString(1, newName);
					pStmt.setInt(2, ids.get(i));

					duplicateNameId += 1;

					pStmt.executeUpdate();
				}
			}
		}
		catch (BatchUpdateException e) {
			log.warn("Error generated while processsing batch insert", e);
			
			try {
				log.debug("Rolling back batch", e);
				connection.rollback();
			}
			catch (Exception rbe) {
				log.warn("Error generated while rolling back batch insert", e);
			}
			
			// marks the changeset as a failed one
			throw new CustomChangeException("Failed to update one or more duplicate EncounterType names", e);
		}
		catch (Exception e) {
			throw new CustomChangeException(e);
		}
		finally {
			// set auto commit to its initial state
			try {
				if (initialAutoCommit != null) {
					connection.setAutoCommit(initialAutoCommit);
				}
			}
			catch (DatabaseException e) {
				log.warn("Failed to set auto commit to ids initial state", e);
			}
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the resultset object");
				}
			}
			
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the select statement used to identify duplicate EncounterType object names");
				}
			}
			
			if (pStmt != null) {
				try {
					pStmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the prepared statement used to update duplicate EncounterType object names");
				}
			}
		}
		
	}
	
}
