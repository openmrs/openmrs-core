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
import org.openmrs.util.OpenmrsConstants;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Applies quantity units to drug orders created pre 1.10.x based on a concept specified in a global property.
 *
 * @see OpenmrsConstants#GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS
 */
public class MigrateDrugOrderQuantityToCodedQuantityUnitsChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		Integer conceptId = DatabaseUtil.getConceptIdForUnits(connection.getUnderlyingConnection(),
		    "drug_order_quantity_units");
		
		try {
			PreparedStatement update = connection
			        .prepareStatement("update drug_order set quantity_units = ? where quantity_units is NULL");
			if (conceptId == null) {
				update.setNull(1, Types.INTEGER);
			} else {
				update.setInt(1, conceptId);
			}
			update.executeUpdate();
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
	}
	
	@Override
	public String getConfirmationMessage() {
		return "Finished migrating drug order quantity to coded quantity units";
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
