/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Test;

/**
 * Class to ensure that we maintain some level of compatibility with Log4J 1.X
 * At some point in the future, this compatibility guarantee should be removed.
 */
class Log4JCompatibilityTest {

	@Test
	void OpenmrsCore_shouldAllowUseOfLog4j1xAPI() {
		final org.apache.logging.log4j.core.Logger compatibilityLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager.getLogger("Log4JCompatibility");
		final Level originalLevel = compatibilityLogger.getLevel();
		final boolean originalAdditive = compatibilityLogger.isAdditive();
		final org.openmrs.logging.MemoryAppender ma = MemoryAppender.newBuilder()
			.setLayout(PatternLayout.newBuilder()
				.withPattern("%m%n")
				.build())
			.setBufferSize(1)
			.build();
		
		try {
			// start the appender
			ma.start();
			compatibilityLogger.addAppender(ma);
			compatibilityLogger.setLevel(Level.ALL);
			compatibilityLogger.setAdditive(false);

			Logger logger = Logger.getLogger("Log4JCompatibility");
			logger.error("This message should be logged.");

			List<String> logLines = ma.getLogLines();
			assertThat(logLines.size(), greaterThan(0));
			assertThat(logLines.get(0), containsString("This message should be logged."));
		} finally {
			compatibilityLogger.removeAppender(ma);
			compatibilityLogger.setLevel(originalLevel);
			compatibilityLogger.setAdditive(originalAdditive);
		}
	}
	
}
