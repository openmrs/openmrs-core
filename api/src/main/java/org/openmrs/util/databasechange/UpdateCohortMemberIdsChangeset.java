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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
 * This change set is run to update cohort member ids
 */
public class UpdateCohortMemberIdsChangeset implements CustomTaskChange {
	
	private static final Logger log = LoggerFactory.getLogger(UpdateCohortMemberIdsChangeset.class);

	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		Statement stmt = null;
		PreparedStatement pStmt = null;
		
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM cohort_member");
			
			pStmt = connection
					.prepareStatement("UPDATE cohort_member SET cohort_member_id = ?"
							+ " WHERE cohort_id = ? AND patient_id = ?");
			int i = 0;
			while (rs.next()) {
				int cohortId = rs.getInt("cohort_id");
				int patientId = rs.getInt("patient_id");
				pStmt.setInt(1, ++i);
				pStmt.setInt(2, cohortId);
				pStmt.setInt(3, patientId);
				pStmt.addBatch();
			}
			pStmt.executeBatch();
		}
		catch (DatabaseException | SQLException e) {
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
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished updating cohort member ids";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(Database)
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
