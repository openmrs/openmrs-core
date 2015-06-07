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
		
		//H2Dialect incorrectly sets these to synonyms in H2
		registerColumnType(Types.LONGVARCHAR, "varchar");
		registerColumnType(Types.BIGINT, "integer");
		
		//Liquibase incorrectly creates varchar for clob in H2 so we just tell Hibernate it's ok
		registerColumnType(Types.CLOB, "varchar");
		
		//Our UUIDs are created as char(38), but H2Dialect maps them to varchars
		registerColumnType(Types.VARCHAR, 38, "char($1)");
		
		//Float is mapped to real per H2 docs at http://www.h2database.com/html/datatypes.html#real_type
		registerColumnType(Types.FLOAT, "real");
		
		//person.birthdate is not a timestamp, but date in db
		registerColumnType(Types.TIMESTAMP, "date");
	}
}
