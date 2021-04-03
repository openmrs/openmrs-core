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
import liquibase.datatype.core.CharType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL returns white spaces for UUIDs since their actual length is 36 and we have set it
 * everywhere as 38 in consideration to ticket https://issues.openmrs.org/browse/TRUNK-1283. This
 * leads to validation issues. Thus we convert it to CHAR(36).
 * 
 * @since 2.4
 */
public class PostgreSQLUuidType extends CharType {
	
	private static final Logger log = LoggerFactory.getLogger(PostgreSQLUuidType.class);
	
	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		if (database instanceof PostgresDatabase && getSize() == 38) {
			DatabaseDataType result = new DatabaseDataType("CHARACTER", 36);
			
			log.debug("uuid type for PostgreSQL is '{}' ", result.getType());
			
			return result;
		}
		
		log.debug("delegating the choice of char(38) type for database '{}' to super class of PostgreSQLUuidType",
		    database.getDatabaseProductName());
		
		return super.toDatabaseDataType(database);
	}
	
	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}
}
