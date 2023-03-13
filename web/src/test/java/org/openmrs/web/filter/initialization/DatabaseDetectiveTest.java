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
}
