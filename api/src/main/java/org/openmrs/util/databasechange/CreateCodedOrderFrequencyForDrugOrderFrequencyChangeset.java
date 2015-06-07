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

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.UpgradeUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public class CreateCodedOrderFrequencyForDrugOrderFrequencyChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		try {
			Set<String> uniqueFrequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency_text", "drug_order",
			    String.class, connection.getUnderlyingConnection());
			insertUniqueFrequencies(connection, uniqueFrequencies);
		}
		catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void insertUniqueFrequencies(JdbcConnection connection, Set<String> uniqueFrequencies)
	        throws CustomChangeException, SQLException, DatabaseException {
		PreparedStatement insertOrderFrequencyStatement = null;
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			insertOrderFrequencyStatement = connection.prepareStatement("insert into order_frequency "
			        + "(concept_id, creator, date_created, retired, uuid) values (?, ?, ?, ?, ?)");
			
			Date date = new Date(new java.util.Date().getTime());
			
			for (String frequency : uniqueFrequencies) {
				if (StringUtils.isBlank(frequency)) {
					continue;
				}
				
				Integer conceptIdForFrequency = UpgradeUtil.getConceptIdForUnits(frequency);
				if (conceptIdForFrequency == null) {
					throw new CustomChangeException("No concept mapping found for frequency: " + frequency);
				}
				
				Integer orderFrequencyId = UpgradeUtil.getOrderFrequencyIdForConceptId(connection.getUnderlyingConnection(),
				    conceptIdForFrequency);
				if (orderFrequencyId != null) {
					//a single concept is mapped to more than one text or there is an order frequency already
					continue;
				}
				
				//Generating UUID for order frequency. Generated UUIDs will be the same if concepts UUIDs are the same.
				String uuid = UpgradeUtil.getConceptUuid(connection.getUnderlyingConnection(), conceptIdForFrequency);
				uuid += "-6925ebb0-7c69-11e3-baa7-0800200c9a66"; //Adding random value for order frequency
				uuid = UUID.nameUUIDFromBytes(uuid.getBytes()).toString();
				
				insertOrderFrequencyStatement.setInt(1, conceptIdForFrequency);
				insertOrderFrequencyStatement.setInt(2, 1);
				insertOrderFrequencyStatement.setDate(3, date);
				insertOrderFrequencyStatement.setBoolean(4, false);
				insertOrderFrequencyStatement.setString(5, uuid);
				
				insertOrderFrequencyStatement.executeUpdate();
				insertOrderFrequencyStatement.clearParameters();
			}
			connection.commit();
		}
		catch (DatabaseException e) {
			handleError(connection, e);
		}
		catch (SQLException e) {
			handleError(connection, e);
		}
		finally {
			if (autoCommit != null) {
				connection.setAutoCommit(autoCommit);
			}
			if (insertOrderFrequencyStatement != null) {
				insertOrderFrequencyStatement.close();
			}
		}
	}
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
	}
	
	@Override
	public String getConfirmationMessage() {
		return "Finished creating coded order frequencies for drug order frequencies";
	}
	
	@Override
	public void setUp() throws SetupException {
	}
	
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
