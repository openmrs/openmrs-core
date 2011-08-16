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
import java.util.UUID;

import liquibase.change.custom.CustomChange;
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
import org.openmrs.util.FormConstants;

/**
 * This change set copies form metadata from the form table into the form attributes table
 */
public class CopyFormMetadataChangeSet implements CustomTaskChange {
	
	protected final static Log log = LogFactory.getLog(CopyFormMetadataChangeSet.class);
	
	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		String getFormMetadataSql = "SELECT form_id, xslt, template FROM form";
		
		StringBuilder insertFormMetadataSql = new StringBuilder();
		insertFormMetadataSql.append("INSERT INTO form_resource");
		insertFormMetadataSql.append(" (form_id, owner, name, value, uuid)");
		insertFormMetadataSql.append(" VALUES (?, ?, ?, ?, ?)");
		
		PreparedStatement insertStatement = null;
		
		try {
			insertStatement = connection.prepareStatement(insertFormMetadataSql.toString());
			
			// iterate over deleted HL7s
			ResultSet forms = connection.createStatement().executeQuery(getFormMetadataSql);
			while (forms.next()) {
				
				// define the xslt object
				byte[] xslt = forms.getString(2) == null ? null : forms.getString(2).getBytes();
				
				// define the template object
				byte[] template = forms.getString(3) == null ? null : forms.getString(3).getBytes();
				
				// add xslt
				if (xslt != null) {
					insertStatement.setInt(1, forms.getInt(1));
					insertStatement.setString(2, FormConstants.FORM_RESOURCE_FORMENTRY_OWNER);
					insertStatement.setString(3, FormConstants.FORM_RESOURCE_FORMENTRY_XSLT);
					insertStatement.setBytes(4, xslt);
					insertStatement.setString(5, UUID.randomUUID().toString());
					insertStatement.executeUpdate();
				}
				
				// add template
				if (template != null) {
					insertStatement.setInt(1, forms.getInt(1));
					insertStatement.setString(2, FormConstants.FORM_RESOURCE_FORMENTRY_OWNER);
					insertStatement.setString(3, FormConstants.FORM_RESOURCE_FORMENTRY_TEMPLATE);
					insertStatement.setBytes(4, template);
					insertStatement.setString(5, UUID.randomUUID().toString());
					insertStatement.executeUpdate();
				}
			}
			
			// cleanup
			if (insertStatement != null)
				insertStatement.close();
			
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to copy form metadata to form attributes table", e);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to copy form metadata to form attributes table", e);
		}
	}
	
	/**
	 * @see CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished copying form metadata";
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
