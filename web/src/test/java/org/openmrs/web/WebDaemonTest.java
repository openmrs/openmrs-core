/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.openmrs.module.ModuleException;
import org.slf4j.LoggerFactory;

public class WebDaemonTest {
	
	@Test
	public void startOpenmrs_shouldThrowExceptionGivenNull() {
		LoggerFactory.getLogger(WebDaemonTest.class); //see TRUNK-6307
		assertThrows(ModuleException.class, () -> WebDaemon.startOpenmrs(null));
	}

}
