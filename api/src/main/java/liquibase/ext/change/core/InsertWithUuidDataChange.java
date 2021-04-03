/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.change.core;

import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.core.InsertDataChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Column;

import static java.util.UUID.randomUUID;

@DatabaseChange(name = "insertWithUuid", description = "Inserts data into an existing table and generates and inserts an uuid", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class InsertWithUuidDataChange extends InsertDataChange {
	
	private static final String UUID = "uuid";

	@Override
	public SqlStatement[] generateStatements(final Database database) {
		
		// Check if the insert change set specifies a value for the uuid column. If that is the case nothing else needs to be done.
		//
		for (final ColumnConfig column : getColumns()) {
			if (column.getName().equalsIgnoreCase(UUID)) {
				return super.generateStatements(database);
			}
		}
		
		// Add the uuid column to the insert statement.
		//
		ColumnConfig uuid = new ColumnConfig(new Column(UUID));
		uuid.setValue(randomUUID().toString());
		addColumn(uuid);
		return super.generateStatements(database);
	}
}
