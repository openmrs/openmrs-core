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
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Every concept should have at least one non-voided description with non-empty text for at least
 * one locale. 
 * The ValidateConceptDescriptionChangeset will validate existing concepts and add a new
 * default ConceptDescription for concepts that do not satisfy these requirements
 */
public class ConceptDescriptionValidatorChangeSet implements CustomTaskChange {
	
	private final static Log log = LogFactory.getLog(ConceptDescriptionValidatorChangeSet.class);
	
	@Override
	public String getConfirmationMessage() {
		return null;
	}
	
	@Override
	public void setFileOpener(ResourceAccessor arg0) {
	}
	
	@Override
	public void setUp() throws SetupException {
	}
	
	@Override
	public ValidationErrors validate(Database arg0) {
		return null;
	}
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		Statement stmt = null;
		Statement validateStmt = null;
		String property = null;
		PreparedStatement updateStatement = null;
		
		//The default description to add
		String description = "Auto Generated Description";
		try {
			stmt = connection.createStatement();
			
			//get the locale.allowed.list from the global properties table, and use the first locale in this list
			//to create the new default ConceptDescription objects
			ResultSet rs_allowedLocales = stmt.executeQuery("SELECT property_value FROM global_property WHERE property = '"
			        + OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST + "'");
			
			if (rs_allowedLocales.next()) {
				String allowedLocaleStr = rs_allowedLocales.getString("property_value");
				if (!StringUtils.isBlank(allowedLocaleStr)) {
					String[] localesArray = allowedLocaleStr.split(",");
					property = localesArray[0];
				}
			} else {
				log.info("The global property '" + OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST + "' isn't set");
				//If the locale.allowed.list is empty, then we will use 'en' as the default ?
				property = "en";
			}
			
			validateStmt = connection.createStatement();
			//Iterate through the concept and concept_description tables to find concepts without the necessary concept descriptions
			ResultSet invalidResultSet = validateStmt
			        .executeQuery("select * from concept a where a.concept_id not in (select b.concept_id from concept_description b where a.concept_id = b.concept_id)");
			
			while (invalidResultSet.next()) {
				//For each invalid concept discovered above, add a new ConceptDescription
				int conceptId = invalidResultSet.getInt("concept_id");
				updateStatement = connection
				        .prepareStatement("INSERT INTO concept_description (concept_id, description, locale, creator, date_created, changed_by, date_changed, uuid) values (?,?,?,1,NOW(),?,?,?)");
				
				updateStatement.setInt(1, conceptId);
				updateStatement.setString(2, description);
				updateStatement.setString(3, property);
				updateStatement.setString(4, null);
				updateStatement.setDate(5, null);
				updateStatement.setString(6, UUID.randomUUID().toString());
				updateStatement.executeUpdate();
			}
			
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Error validating ConceptDescription objects: " + e);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Error validating ConceptDescription objects: " + e);
		}
		
	}
	
}
