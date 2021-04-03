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
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.BooleanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL (and MariaDB) represent boolean variables as TINYINT(1). Liquibase changed the representation of boolean
 * types in these databases to BIT(1) or BIT as of Liquibase version 3. This custom type ensures that TINYINT(1) is 
 * used instead.
 * 
 * @since 2.4
 */
public class MySQLBooleanType extends BooleanType {
	
	private static final Logger log = LoggerFactory.getLogger(MySQLBooleanType.class);
	
	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		if (database instanceof MySQLDatabase) {
			DatabaseDataType result = new DatabaseDataType("TINYINT", 1);
			
			log.debug("boolean type for MySQL is '{}' ", result.getType());
			
			return result;
		}
		
		log.debug("delegating the choice of boolean type for database '{}' to super class of MySQLBooleanType",
		    database.getDatabaseProductName());
		
		return super.toDatabaseDataType(database);
	}
	
	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}
}
