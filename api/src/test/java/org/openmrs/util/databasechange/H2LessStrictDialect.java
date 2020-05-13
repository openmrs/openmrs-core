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

import java.sql.Types;
import org.hibernate.dialect.H2Dialect;

/**
 * H2 version we use behaves differently from H2Dialect bundled with Hibernate so we provide a
 * custom implementation for the purpose of validation in {@link DatabaseUpgradeTestUtil}
 */
public class H2LessStrictDialect extends H2Dialect {
	
	public H2LessStrictDialect() {
		super();
		
		// H2Dialect incorrectly sets these to synonyms in H2
		//
		registerColumnType(Types.BIGINT, "integer");
		
		// Liquibase incorrectly creates varchar for clob in H2 so we just tell Hibernate it's ok
		//
		registerColumnType(Types.CLOB, "varchar");
		
		// H2 maps 'FLOAT' to 'double' as per http://www.h2database.com/html/datatypes.html#double_type
		//
		// Without mapping 'float' to 'double' the validation of Hibernate mappings fails:
		//   Schema-validation: 
		//     wrong column type encountered in column [sort_weight] in table [form_field]; 
		//     found [double (Types#DOUBLE)], but expecting [float (Types#FLOAT)]
		//
		registerColumnType(Types.FLOAT, "double");
		
		//person.birthdate is not a timestamp, but date in db
		//
		registerColumnType(Types.TIMESTAMP, "date");
		
		// UUIDs are created as char(38), but H2Dialect maps them to varchars
		//
		registerColumnType(Types.VARCHAR, 38, "char($1)");
		
		// These mappings are required for "long" fields of type java.lang.String that are declared as 'text' 
		// in Hibernate change sets.
		//
		registerColumnType(Types.VARCHAR, 250, "clob");
		registerColumnType(Types.VARCHAR, 500, "clob");
		registerColumnType(Types.VARCHAR, 1024, "clob");
		registerColumnType(Types.VARCHAR, 65535, "clob");
		registerColumnType(Types.VARCHAR, 16777215, "clob");
		registerColumnType(Types.VARCHAR, 2147483647, "clob");
		registerColumnType(Types.LONGVARCHAR, 2147483647, "clob");
	}
}
