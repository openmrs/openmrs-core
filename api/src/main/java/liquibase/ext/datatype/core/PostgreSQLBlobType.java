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
import liquibase.datatype.core.BlobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL provides two types to handle binary large data which are oid and bytea. Liquibase maps
 * columns of type longblob to type bytea in PostgreSQL but Hibernate maps java.sql.Blob type to oid
 * in PostgreSQL. For consistency with Hibernate ORM we need to map longblob to oid type.
 * 
 * @since 2.4
 */
public class PostgreSQLBlobType extends BlobType {
	
	private static final Logger log = LoggerFactory.getLogger(PostgreSQLBlobType.class);
	
	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		if (database instanceof PostgresDatabase) {
			DatabaseDataType result = new DatabaseDataType("OID");
			
			log.debug("blob type for PostgreSQL is '{}' ", result.getType());
			
			return result;
		}
		
		log.debug("delegating the choice of blob type for database '{}' to super class of PostgreSQLBlobType",
		    database.getDatabaseProductName());
		
		return super.toDatabaseDataType(database);
	}
	
	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}
}
