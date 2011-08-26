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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
			
			// iterate over existing forms
			ResultSet forms = connection.createStatement().executeQuery(getFormMetadataSql);
			while (forms.next()) {
				
				int formId = forms.getInt(1);
				
				// get the xslt and template byte arrays
				byte[] xslt = null;
				byte[] template = null;
				
				try {
					xslt = forms.getString(2) == null ? null : convertToObjectStreamOutputByteArray(forms.getString(2));
				}
				catch (CustomChangeException e) {
					throw new CustomChangeException("error converting Form #" + formId
					        + " XSLT to ObjectStreamOutputByteArray", e);
				}
				
				try {
					template = forms.getString(3) == null ? null : convertToObjectStreamOutputByteArray(forms.getString(3));
				}
				catch (CustomChangeException e) {
					throw new CustomChangeException("error converting Form #" + formId
					        + " Template to ObjectStreamOutputByteArray", e);
				}
				
				// add xslt
				if (xslt != null) {
					insertStatement.setInt(1, formId);
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
			if (insertStatement != null) {
				insertStatement.close();
			}
			
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to copy form metadata to form attributes table", e);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to copy form metadata to form attributes table", e);
		}
	}
	
	/**
	 * convert a String object to an ObjectOutputStream byte array
	 * 
	 * @param obj the string to be converted
	 * @return the converted byte array
	 * @throws CustomChangeException 
	 */
	private byte[] convertToObjectStreamOutputByteArray(String obj) throws CustomChangeException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(obj);
			out.close();
		}
		catch (IOException e) {
			throw new CustomChangeException("could not serialize a form resource", e);
		}
		return bos.toByteArray();
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
