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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.OpenmrsConstants;

/**
 * This changeset creates provider accounts for orderers that have no providers accounts, and then
 * converts the orderer from being users to providers
 */
public class ConvertOrderersToProviders implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		try {
			List<List<Object>> usersAndProviders = getUsersAndProviders(connection);
			convertOrdererToProvider(connection, usersAndProviders);
		}
		catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}
	
	private List<List<Object>> getUsersAndProviders(JdbcConnection connection) throws CustomChangeException, SQLException {
		//Should only match on current users that are orderers
		final String query = "SELECT u.user_id AS userId, p.provider_id AS providerId FROM users u, provider p"
		        + " WHERE u.person_id = p.person_id AND u.user_id IN (select orderer from orders)";
		
		return DatabaseUtil.executeSQL(connection.getUnderlyingConnection(), query, true);
	}
	
	private void convertOrdererToProvider(JdbcConnection connection, List<List<Object>> usersAndProviders)
	        throws CustomChangeException, SQLException, DatabaseException {
		final int batchSize = 1000;
		int index = 0;
		PreparedStatement updateStatement = null;
		Statement statement = connection.createStatement();
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			updateStatement = connection.prepareStatement("UPDATE orders SET orderer = ? WHERE orderer = ?");
			boolean supportsBatchUpdate = connection.getMetaData().supportsBatchUpdates();
			for (List<Object> row : usersAndProviders) {
				updateStatement.setInt(1, (Integer) row.get(1));
				updateStatement.setInt(2, (Integer) row.get(0));
				if (supportsBatchUpdate) {
					updateStatement.addBatch();
					index++;
					if (index % batchSize == 0) {
						updateStatement.executeBatch();
					}
				} else {
					updateStatement.executeUpdate();
				}
			}
			
			if (supportsBatchUpdate) {
				updateStatement.executeBatch();
			}
			
			//Set the orderer for orders with null orderer to Unknown Provider
			statement.execute("UPDATE orders SET orderer = " + "(SELECT provider_id FROM provider WHERE uuid ="
			        + "(SELECT property_value FROM global_property WHERE property = '" + ""
			        + OpenmrsConstants.GP_UNKNOWN_PROVIDER_UUID + "')) " + "WHERE orderer IS NULL");
			
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
			if (updateStatement != null) {
				updateStatement.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	@Override
	public String getConfirmationMessage() {
		return "Finished converting orders.orderer from user_id to provider_id";
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
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
	}
}
