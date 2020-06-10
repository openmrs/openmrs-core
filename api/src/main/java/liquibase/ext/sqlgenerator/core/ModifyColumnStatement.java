/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

/**
 * Modifies an existing column.
 *
 * @since 2.4
 */
public class ModifyColumnStatement extends AbstractSqlStatement {

	private String schemaName;

	private String tableName;

	private ColumnConfig[] columns;

	public ModifyColumnStatement(String schemaName, String tableName, ColumnConfig... columns) {
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.columns = columns;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getTableName() {
		return tableName;
	}

	public ColumnConfig[] getColumns() {
		return columns;
	}
}
