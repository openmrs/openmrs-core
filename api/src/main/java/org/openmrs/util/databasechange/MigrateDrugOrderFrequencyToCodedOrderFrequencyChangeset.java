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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MigrateDrugOrderFrequencyToCodedOrderFrequencyChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		try {
			List<String> uniqueFrequencies = getUniqueFrequencies(connection);
			migrateFrequenciesToCodedValue(connection, uniqueFrequencies);
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void migrateFrequenciesToCodedValue(JdbcConnection connection, List<String> uniqueFrequencies)
	        throws CustomChangeException, SQLException, DatabaseException {
		PreparedStatement updateDrugOrderStatement = null;
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			updateDrugOrderStatement = connection
			        .prepareStatement("update drug_order set frequency = ? where frequency_text = ?");
			for (String frequency : uniqueFrequencies) {
				Integer conceptIdForFrequency = DatabaseUtil.getConceptIdForUnits(connection.getUnderlyingConnection(),
				    frequency);
				if (conceptIdForFrequency == null) {
					throw new CustomChangeException("No concept mapping found for frequency: " + frequency);
				}
				int orderFrequencyId = getOrderFrequencyIdForConceptId(connection, conceptIdForFrequency);
				
				updateDrugOrderStatement.setInt(1, orderFrequencyId);
				updateDrugOrderStatement.setString(2, frequency);
				updateDrugOrderStatement.executeUpdate();
				updateDrugOrderStatement.clearParameters();
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
			if (updateDrugOrderStatement != null) {
				updateDrugOrderStatement.close();
			}
		}
	}
	
	private int getOrderFrequencyIdForConceptId(JdbcConnection connection, Integer conceptIdForFrequency)
	        throws DatabaseException, SQLException, CustomChangeException {
		PreparedStatement orderFrequencyIdQuery = connection
		        .prepareStatement("select order_frequency_id from order_frequency where concept_id = ?");
		orderFrequencyIdQuery.setInt(1, conceptIdForFrequency);
		ResultSet orderFrequencyIdResultSet = orderFrequencyIdQuery.executeQuery();
		if (!orderFrequencyIdResultSet.next()) {
			throw new CustomChangeException("No order frequency found for concept: " + conceptIdForFrequency);
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
		return "Finished migrating drug order frequencies to coded order frequencies";
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
