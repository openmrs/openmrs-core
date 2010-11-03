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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.hl7.HL7Constants;

/**
 * This change set moves "deleted" HL7s from the archive table to the queue table
 */
public class MoveDeletedHL7sChangeSet implements CustomTaskChange {
	
	protected final static Log log = LogFactory.getLog(MoveDeletedHL7sChangeSet.class);

	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
		DatabaseConnection connection = database.getConnection();

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
		
		PreparedStatement insertStatement = null;
		PreparedStatement deleteStatement = null;
		
		try {
			insertStatement = connection.prepareStatement(insertHL7Sql.toString());
			deleteStatement = connection.prepareStatement(
					"DELETE FROM hl7_in_archive WHERE hl7_in_archive_id=?");
			
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
			if (insertStatement != null)
				insertStatement.close();
			if (deleteStatement != null)
				deleteStatement.close();
			
		} catch (SQLException e) {
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
	 * @see CustomChange#setFileOpener(FileOpener)
	 */
	@Override
	public void setFileOpener(FileOpener fo) {
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
	public void validate(Database db) throws InvalidChangeDefinitionException {
	}
}
