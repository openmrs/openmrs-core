/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.util.H2DatabaseIT;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DatabaseDetectiveIT extends H2DatabaseIT {
	
	private static final String LIQUIBASE_SCHEMA_ONLY_1_9_X = "org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-1.9.x.xml";

	private static final String LIQUIBASE_CHANGE_LOG_TABLES = "org/openmrs/liquibase/liquibase-changelog-tables.xml";

	private DatabaseDetective databaseDetective;
	
	private Properties properties;
	
	@BeforeEach
	public void setup() {
		databaseDetective = new DatabaseDetective();

		properties = new Properties();
		properties.put("connection.url", super.CONNECTION_URL);
		properties.put("connection.username", super.USER_NAME);
		properties.put("connection.password", super.PASSWORD);
	}
	
	@Test
	public void shouldRecogniseDatabaseWithoutAnyTables() throws Exception {
		assertTrue( databaseDetective.isDatabaseEmpty( properties ) );
	}
	
	@Test
	public void shouldIgnoreLiquibaseChangeLogTables() throws Exception {
		updateDatabase( LIQUIBASE_CHANGE_LOG_TABLES );
		DatabaseDetective databaseDetective = new DatabaseDetective();
		assertTrue( databaseDetective.isDatabaseEmpty( properties ) );
	}

	@Test
	public void shouldRecogniseDatabaseContainsOpenmrsTables() throws Exception {
		updateDatabase( LIQUIBASE_SCHEMA_ONLY_1_9_X );
		assertFalse( databaseDetective.isDatabaseEmpty( properties ) );
	}
}
