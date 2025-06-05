/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package liquibase.ext.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.BooleanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL uses BOOLEAN type for boolean values, while MySQL uses BIT(1).
 * This class ensures proper type conversion for PostgreSQL.
 * 
 * @since 2.8.0
 */
public class PostgreSQLBooleanType extends BooleanType {

	private static final Logger log = LoggerFactory.getLogger(PostgreSQLBooleanType.class);

	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		if (database instanceof PostgresDatabase) {
			DatabaseDataType result = new DatabaseDataType("BOOLEAN");

			log.debug("boolean type for PostgreSQL is '{}' ", result.getType());

			return result;
		}

		log.debug("delegating the choice of boolean type for database '{}' to super class of PostgreSQLBooleanType",
			database.getDatabaseProductName());

		return super.toDatabaseDataType(database);
	}

	@Override
	public int getPriority() {
		return super.getPriority() + 2;
	}
}
