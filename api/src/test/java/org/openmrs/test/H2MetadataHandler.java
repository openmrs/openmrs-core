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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.dbunit.database.DefaultMetadataHandler;

/**
 * DBUnit metadata handler for the in-memory H2 test database.
 * <p>
 * H2's {@link DatabaseMetaData#getColumns} matches the table name with {@code LIKE} and unions in
 * synonyms, so it builds a row for every column of every table on each call (the {@code _} in most
 * OpenMRS table names is a wildcard). DBUnit calls it once per table per dataset, which makes it a
 * large part of the cost of {@code executeDataSet()}. This handler instead looks the table up by exact
 * name, which H2 can answer by reading only that table. The result has the same columns, in the same
 * order, as H2's own {@code getColumns}.
 */
public class H2MetadataHandler extends DefaultMetadataHandler {

	private static final String COLUMNS_QUERY = "SELECT TABLE_CATALOG TABLE_CAT, TABLE_SCHEMA TABLE_SCHEM, TABLE_NAME, "
	        + "COLUMN_NAME, DATA_TYPE, TYPE_NAME, CHARACTER_MAXIMUM_LENGTH COLUMN_SIZE, "
	        + "CHARACTER_MAXIMUM_LENGTH BUFFER_LENGTH, NUMERIC_SCALE DECIMAL_DIGITS, "
	        + "NUMERIC_PRECISION_RADIX NUM_PREC_RADIX, NULLABLE, REMARKS, COLUMN_DEFAULT COLUMN_DEF, "
	        + "DATA_TYPE SQL_DATA_TYPE, ZERO() SQL_DATETIME_SUB, CHARACTER_OCTET_LENGTH CHAR_OCTET_LENGTH, "
	        + "ORDINAL_POSITION, IS_NULLABLE IS_NULLABLE, CAST(SOURCE_DATA_TYPE AS VARCHAR) SCOPE_CATALOG, "
	        + "CAST(SOURCE_DATA_TYPE AS VARCHAR) SCOPE_SCHEMA, CAST(SOURCE_DATA_TYPE AS VARCHAR) SCOPE_TABLE, "
	        + "SOURCE_DATA_TYPE, CASE WHEN SEQUENCE_NAME IS NULL THEN 'NO' ELSE 'YES' END IS_AUTOINCREMENT, "
	        + "CASE WHEN IS_COMPUTED THEN 'YES' ELSE 'NO' END IS_GENERATEDCOLUMN "
	        + "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND (? IS NULL OR TABLE_SCHEMA = ?) "
	        + "ORDER BY TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION";

	/**
	 * @return a disconnected copy of the rows, so the statement can be closed before returning (H2 does
	 *         not support {@code closeOnCompletion()})
	 */
	@Override
	public ResultSet getColumns(DatabaseMetaData databaseMetaData, String schemaName, String tableName)
	        throws SQLException {
		try (PreparedStatement statement = databaseMetaData.getConnection().prepareStatement(COLUMNS_QUERY)) {
			statement.setString(1, tableName);
			statement.setString(2, schemaName);
			statement.setString(3, schemaName);
			try (ResultSet resultSet = statement.executeQuery()) {
				CachedRowSet rows = RowSetProvider.newFactory().createCachedRowSet();
				rows.populate(resultSet);
				return rows;
			}
		}
	}
}
