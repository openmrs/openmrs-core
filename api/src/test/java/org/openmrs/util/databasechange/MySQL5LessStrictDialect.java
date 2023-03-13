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
import org.hibernate.dialect.MySQL5Dialect;

public class MySQL5LessStrictDialect extends MySQL5Dialect {
	
	public MySQL5LessStrictDialect() {
		super();
		
		// MySQL5Dialect incorrectly sets these to synonyms in Maria DB
		registerColumnType(Types.BIGINT, "integer");
		
		// Our UUIDs are created as char(38), but MySQL5Dialect maps them to varchars
		registerColumnType(Types.VARCHAR, 38, "char($1)");
	}
}
