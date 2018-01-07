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

import org.openmrs.hl7.HL7Constants;

import liquibase.change.custom.CustomChange;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * This change set moves "deleted" HL7s from the archive table to the queue table
 */
public class MoveDeletedHL7sChangeSet implements CustomTaskChange {

	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		StringBuilder getDeletedHL7sSql = new StringBuilder();
		getDeletedHL7sSql.append("SELECT hl7_source, hl7_source_key, hl7_data, date_created, uuid, hl7_in_archive_id");
		getDeletedHL7sSql.append(" FROM hl7_in_archive WHERE message_state=");
		getDeletedHL7sSql.append(HL7Constants.HL7_STATUS_DELETED);
		
		StringBuilder insertHL7Sql = new StringBuilder();
		insertHL7Sql.append("INSERT INTO hl7_in_queue");
		insertHL7Sql.append(" (hl7_source, hl7_source_key, hl7_data, date_created, uuid, message_state)");
		insertHL7Sql.append(" VALUES (?, ?, ?, ?, ?, ");
		insertHL7Sql.append(HL7Constants.HL7_STATUS_DELETED);
		insertHL7Sql.append(")");
		
		PreparedStatement insertStatement;
		PreparedStatement deleteStatement;
		
		try {
			insertStatement = connection.prepareStatement(insertHL7Sql.toString());
			deleteStatement = connection.prepareStatement("DELETE FROM hl7_in_archive WHERE hl7_in_archive_id=?");
			
			// iterate over deleted HL7s
			ResultSet archives = connection.createStatement().executeQuery(getDeletedHL7sSql.toString());
			while (archives.next()) {
				
				// add to the queue
				insertStatement.setString(1, archives.getString(1)); // set hl7_source
				insertStatement.setString(2, archives.getString(2)); // set hl7_source_key
				insertStatement.setString(3, archives.getString(3)); // set hl7_data
				insertStatement.setDate(4, archives.getDate(4)); // set date_created
				insertStatement.setString(5, archives.getString(5)); // set uuid
				insertStatement.executeUpdate();
				
				// remove from the archives
				deleteStatement.setInt(1, archives.getInt(6));
				deleteStatement.executeUpdate();
			}
			
			// cleanup
			if (insertStatement != null) {
				insertStatement.close();
			}
			if (deleteStatement != null) {
				deleteStatement.close();
			}
			
		}
		catch (SQLException | DatabaseException e) {
			throw new CustomChangeException("Unable to move deleted HL7s from archive table to queue table", e);
		}
	}
	
	/**
	 * @see CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished moving deleted changesets";
	}
	
	/**
	 * @see CustomChange#setFileOpener(ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor fo) {
	}
	
	/**
	 * @see CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see CustomChange#validate(Database)
	 */
	@Override
	public ValidationErrors validate(Database db) {
		return new ValidationErrors();
	}
}
