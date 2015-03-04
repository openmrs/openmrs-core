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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

public class MigrateDrugOrderFrequencyToCodedOrderFrequencyChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		try {
			Set<String> uniqueFrequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency_text", "drug_order",
			    String.class, connection.getUnderlyingConnection());
			migrateFrequenciesToCodedValue(connection, uniqueFrequencies);
		}
		catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void migrateFrequenciesToCodedValue(JdbcConnection connection, Set<String> uniqueFrequencies)
	        throws CustomChangeException, SQLException, DatabaseException {
		PreparedStatement updateDrugOrderStatement = null;
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			updateDrugOrderStatement = connection
			        .prepareStatement("update drug_order set frequency = ? where frequency_text = ?");
			
			updateDrugOrderStatement.setNull(1, Types.INTEGER);
			updateDrugOrderStatement.setNull(2, Types.VARCHAR);
			updateDrugOrderStatement.executeUpdate();
			updateDrugOrderStatement.clearParameters();
			
			for (String frequency : uniqueFrequencies) {
				if (StringUtils.isBlank(frequency)) {
					updateDrugOrderStatement.setNull(1, Types.INTEGER);
				} else {
					Integer conceptIdForFrequency = UpgradeUtil.getConceptIdForUnits(frequency);
					if (conceptIdForFrequency == null) {
						throw new CustomChangeException("No concept mapping found for frequency: " + frequency);
					}
					Integer orderFrequencyId = UpgradeUtil.getOrderFrequencyIdForConceptId(connection
					        .getUnderlyingConnection(), conceptIdForFrequency);
					if (orderFrequencyId == null) {
						throw new CustomChangeException("No order frequency found for concept " + conceptIdForFrequency);
					}
					
					updateDrugOrderStatement.setInt(1, orderFrequencyId);
				}
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
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
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
