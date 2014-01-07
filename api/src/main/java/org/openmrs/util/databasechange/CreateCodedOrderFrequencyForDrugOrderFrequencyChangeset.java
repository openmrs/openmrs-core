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

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.openmrs.util.DatabaseUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateCodedOrderFrequencyForDrugOrderFrequencyChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		try {
			List<String> uniqueFrequencies = getUniqueFrequencies(connection);
			insertUniqueFrequencies(connection, uniqueFrequencies);
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void insertUniqueFrequencies(JdbcConnection connection, List<String> uniqueFrequencies)
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
				Integer conceptIdForFrequency = DatabaseUtil.getConceptIdForUnits(connection.getUnderlyingConnection(),
				    frequency);
				if (conceptIdForFrequency == null) {
					throw new CustomChangeException("No concept mapping found for frequency: " + frequency);
				}
				
				Integer orderFrequencyId = getOrderFrequencyIdForConceptId(connection, conceptIdForFrequency);
				if (orderFrequencyId != null) {
					continue;
				}
				
				insertOrderFrequencyStatement.setInt(1, conceptIdForFrequency);
				insertOrderFrequencyStatement.setInt(2, 1);
				insertOrderFrequencyStatement.setDate(3, date);
				insertOrderFrequencyStatement.setBoolean(4, false);
				insertOrderFrequencyStatement.setString(5, UUID.randomUUID().toString());
				
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
	
	private Integer getOrderFrequencyIdForConceptId(JdbcConnection connection, Integer conceptIdForFrequency)
	        throws DatabaseException, SQLException, CustomChangeException {
		PreparedStatement orderFrequencyIdQuery = connection
		        .prepareStatement("select order_frequency_id from order_frequency where concept_id = ?");
		orderFrequencyIdQuery.setInt(1, conceptIdForFrequency);
		ResultSet orderFrequencyIdResultSet = orderFrequencyIdQuery.executeQuery();
		if (!orderFrequencyIdResultSet.next()) {
			return null;
		}
		return orderFrequencyIdResultSet.getInt("order_frequency_id");
	}
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
	}
	
	private List<String> getUniqueFrequencies(JdbcConnection connection) throws CustomChangeException, SQLException {
		List<String> uniqueFrequencies = new ArrayList<String>();
		PreparedStatement uniqueFrequenciesQuery = null;
		try {
			uniqueFrequenciesQuery = connection
			        .prepareStatement("select distinct frequency_text as unique_frequency from drug_order");
			ResultSet resultSet = uniqueFrequenciesQuery.executeQuery();
			while (resultSet.next()) {
				String unit = resultSet.getString("unique_frequency");
				if (unit != null) {
					uniqueFrequencies.add(unit);
				}
			}
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
		finally {
			if (uniqueFrequenciesQuery != null) {
				uniqueFrequenciesQuery.close();
			}
		}
		return uniqueFrequencies;
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
