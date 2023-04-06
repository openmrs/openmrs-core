/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.util.SQLHelper;
import org.openmrs.util.OpenmrsConstants;

public class OpenmrsMetadataHandler extends DefaultMetadataHandler {

	@Override
	public boolean tableExists(DatabaseMetaData databaseMetaData, String schemaName, String tableName) throws SQLException {
		ResultSet tableRs = databaseMetaData.getTables(OpenmrsConstants.DATABASE_NAME, schemaName, tableName, null);
        try 
        {
            return tableRs.next();
        }
        finally
        {
            SQLHelper.close(tableRs);
        }
	}

	@Override
	public ResultSet getTables(DatabaseMetaData databaseMetaData, String schemaName, String[] tableTypes) throws SQLException {
		return databaseMetaData.getTables(OpenmrsConstants.DATABASE_NAME, schemaName, "%", tableTypes);
	}
}
