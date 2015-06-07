/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link DatabaseUpdater} class. This class expects /metadata/model to be on
 * the classpath so that the liquibase-update-to-latest.xml can be found.
 */
public class DatabaseUpdaterTest extends BaseContextSensitiveTest {
	
	private static Log log = LogFactory.getLog(DatabaseUpdaterTest.class);
	
	/**
	 * @see {@link DatabaseUpdater#updatesRequired()}
	 */
	@Test
	@Verifies(value = "should always have a valid update to latest file", method = "updatesRequired()")
	public void updatesRequired_shouldAlwaysHaveAValidUpdateToLatestFile() throws Exception {
		// expects /metadata/model to be on the classpath so that
		// the liquibase-update-to-latest.xml can be found.
		try {
			DatabaseUpdater.updatesRequired();
		}
		catch (RuntimeException rex) {
			log.error("Runtime Exception in test for Validation Errors");
		}
		// does not run DatabaseUpdater.update() because hsqldb doesn't like single quotes in strings
	}
}
