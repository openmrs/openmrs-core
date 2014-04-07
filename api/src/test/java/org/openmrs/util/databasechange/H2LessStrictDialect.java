/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
