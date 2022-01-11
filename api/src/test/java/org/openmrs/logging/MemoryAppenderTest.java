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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemoryAppenderTest {
	
	private MemoryAppender memoryAppender;
	private Logger logger;
	
	@BeforeEach
	public void setup() {
		memoryAppender = MemoryAppender.newBuilder()
			.setLayout(PatternLayout.newBuilder().withPattern("%m").build())
			.build();
		memoryAppender.start();
		
		setupLogger();
	}
	
	@AfterEach
	public void tearDown() {
		logger.removeAppender(memoryAppender);
		memoryAppender.stop();
		((Logger) LogManager.getRootLogger()).getContext().updateLoggers();

		memoryAppender = null;
		logger = null;
	}
	
	@Test
	void memoryAppender_shouldAppendAMessage() {
		logger.warn("Logging message");

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, notNullValue());
		assertThat(logLines, hasSize(greaterThanOrEqualTo(1)));
		assertThat(logLines, hasItem(equalTo("Logging message")));
	}

	@Test
	void memoryAppender_shouldAppendMultipleMessages() {
		int nTimes = 12;
		for (int i = 0; i < nTimes; i++) {
			logger.warn("Logging message");
		}

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, notNullValue());
		assertThat(logLines, hasSize(greaterThanOrEqualTo(1)));
		for (int i = logLines.size() - nTimes; i < nTimes; i++) {
			assertThat(logLines.get(i), equalTo("Logging message"));
		}
	}
	
	@Test
	void memoryAppender_shouldOnlyKeepBufferSizeItems() {
		// clear setup() results
		logger.removeAppender(memoryAppender);
		
		memoryAppender = MemoryAppender.newBuilder()
			.setLayout(PatternLayout.newBuilder().withPattern("%m").build())
			.setBufferSize(4)
			.build();
		memoryAppender.start();
		
		setupLogger();
		
		for (int i = 0; i < 12; i++) {
			logger.warn("Logging message");
		}
		
		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, notNullValue());
		assertThat(logLines.size(), equalTo(4));
	}
	
	private void setupLogger() {
		logger = (Logger) LogManager.getLogger("MemoryAppenderTest");
		// NB This needs to come before the setLevel() call
		logger.setAdditive(false);
		logger.setLevel(Level.ALL);
		logger.addAppender(memoryAppender);
	}

}
