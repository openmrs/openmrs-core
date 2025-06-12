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

	@Override
	protected String columnType(int sqlTypeCode) {
		// H2Dialect incorrectly sets these to synonyms in H2
		//
		
		if (sqlTypeCode == Types.BIGINT) {
			return "integer";
		}

		// Liquibase incorrectly creates varchar for clob in H2 so we just tell Hibernate it's ok
		//
		if (sqlTypeCode == Types.CLOB) {
			return "varchar";
		}
		// H2 maps 'FLOAT' to 'double' as per http://www.h2database.com/html/datatypes.html#double_type
		//
		// Without mapping 'float' to 'double' the validation of Hibernate mappings fails:
		//   Schema-validation: 
		//     wrong column type encountered in column [sort_weight] in table [form_field]; 
		//     found [double (Types#DOUBLE)], but expecting [float (Types#FLOAT)]
		//
		if (sqlTypeCode == Types.FLOAT) {
			return "double";
		}
		//person.birthdate is not a timestamp, but date in db
		//
		if (sqlTypeCode == Types.TIMESTAMP) {
			return "date";
		}
		// UUIDs are created as char(38), but H2Dialect maps them to varchars
		//
		if (sqlTypeCode == Types.VARCHAR) {
			return "char($1)";
		}
		if (sqlTypeCode == Types.LONGVARCHAR) {
			return "clob";
		}

		
		return super.columnType(sqlTypeCode);
	}
}
