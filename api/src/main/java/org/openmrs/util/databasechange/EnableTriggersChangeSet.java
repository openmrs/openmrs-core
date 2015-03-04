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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Enables all triggers for the current schema
 * Postgres does not have a call to enable all foreign key constraints. This
 * changeset alters table and enable triggers for all tables.
 */
public class EnableTriggersChangeSet implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		DatabaseMetaData metadata;
		try {
			metadata = connection.getMetaData();
			String[] types = { "TABLE" };
			ResultSet rs = metadata.getTables(null, null, "%", types);
			
			while (rs.next()) {
				String tableName = rs.getString(3);
				connection.prepareStatement("ALTER TABLE " + tableName + " ENABLE TRIGGER ALL").execute();
			}
			
		}
		catch (DatabaseException ex) {
			throw new CustomChangeException("Error enabling trigger: " + ex);
		}
		catch (SQLException ex) {
			throw new CustomChangeException("Error enabling trigger: " + ex);
		}
	}
	
	@Override
	public String getConfirmationMessage() {
		return "Finished enabling triggers for all tables";
	}
	
	@Override
	public void setUp() throws SetupException {
	}
	
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
}
