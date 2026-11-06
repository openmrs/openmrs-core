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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseDetectiveTest {

	private DatabaseDetective databaseDetective;

	@BeforeEach
	public void setup() {
		databaseDetective = new DatabaseDetective();
	}

	@Test
	public void shouldRecogniseNull() {
		assertTrue(databaseDetective.isDatabaseEmpty(null));
	}

	@Test
	public void shouldRecogniseInvalidConnectionParameters() {
		Properties properties = new Properties();
		assertTrue(databaseDetective.isDatabaseEmpty(properties));
	}

	@Test
	public void shouldNotReportConfiguredButUnreachableDatabaseAsEmpty() {
		// a configured instance whose database is unreachable must not be reported as empty,
		// otherwise the unauthenticated setup wizard would reopen on a production deployment
		Properties properties = new Properties();
		properties.setProperty("connection.url", "jdbc:h2:tcp://localhost:1/unreachable");
		properties.setProperty("connection.driver_class", "org.h2.Driver");
		properties.setProperty("connection.username", "sa");
		properties.setProperty("connection.password", "");
		assertFalse(databaseDetective.isDatabaseEmpty(properties));
	}
}
