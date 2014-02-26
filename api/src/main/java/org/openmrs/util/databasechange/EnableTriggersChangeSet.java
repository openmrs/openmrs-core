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
