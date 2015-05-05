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
import java.sql.Types;
import java.util.List;
import java.util.Set;

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
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.UpgradeUtil;

public class MigrateDrugOrderUnitsToCodedDoseUnitsChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		try {
			Set<String> uniqueUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", String.class,
			    connection.getUnderlyingConnection());
			migrateUnitsToCodedValue(connection, uniqueUnits);
		}
		catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void migrateUnitsToCodedValue(JdbcConnection connection, Set<String> uniqueUnits) throws CustomChangeException,
	        SQLException, DatabaseException {
		PreparedStatement updateDrugOrderStatement = null;
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			updateDrugOrderStatement = connection.prepareStatement("update drug_order set dose_units = ? where units = ?");
			
			updateDrugOrderStatement.setNull(1, Types.INTEGER);
			updateDrugOrderStatement.setNull(2, Types.VARCHAR);
			updateDrugOrderStatement.executeUpdate();
			updateDrugOrderStatement.clearParameters();
			
			for (String unit : uniqueUnits) {
				if (StringUtils.isBlank(unit)) {
					updateDrugOrderStatement.setNull(1, Types.INTEGER);
				} else {
					Integer conceptIdForUnit = UpgradeUtil.getConceptIdForUnits(unit);
					if (conceptIdForUnit == null) {
						throw new CustomChangeException("No concept mapping found for unit: " + unit);
					}
					String dosingUnitsConceptSetUuid = UpgradeUtil.getGlobalProperty(connection.getUnderlyingConnection(),
					    OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID);
					List<Integer> dosingUnitsconceptIds = UpgradeUtil.getMemberSetIds(connection.getUnderlyingConnection(),
					    dosingUnitsConceptSetUuid);
					if (!dosingUnitsconceptIds.contains(conceptIdForUnit)) {
						throw new CustomChangeException("Dosing unit '" + unit
						        + "' is not among valid concepts defined in global property "
						        + OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID);
					}
					
					updateDrugOrderStatement.setInt(1, conceptIdForUnit);
				}
				updateDrugOrderStatement.setString(2, unit);
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
		return "Finished migrating drug order units to coded dose units";
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
