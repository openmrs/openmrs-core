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

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

class MemoryAppenderTest {

	private MemoryAppender memoryAppender;

	private Logger logger;

	@BeforeEach
	public void setup() {
		memoryAppender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
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

		memoryAppender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build())
		        .setBufferSize(4).build();
		memoryAppender.start();

		setupLogger();

		for (int i = 0; i < 12; i++) {
			logger.warn("Logging message");
		}

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, notNullValue());
		assertThat(logLines.size(), equalTo(4));
	}

	@Test
	void memoryAppender_shouldUseDefaultBufferSizeWhenNotSet() {
		MemoryAppender appender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build())
		        .build();

		assertThat(appender.getBufferSize(), equalTo(100));
	}

	@Test
	void memoryAppender_builderShouldUseDefaultName() {
		MemoryAppender appender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build())
		        .build();

		assertThat(appender.getName(), equalTo(OpenmrsConstants.MEMORY_APPENDER_NAME));
	}

	@Test
	void memoryAppender_builderShouldSetCustomBufferSize() {
		MemoryAppender appender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build())
		        .setBufferSize(50).build();

		assertThat(appender.getBufferSize(), equalTo(50));
	}

	@Test
	void memoryAppender_builderShouldUseDefaultBufferSizeWhenSetToZero() {
		MemoryAppender appender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build())
		        .setBufferSize(0).build();

		assertThat(appender.getBufferSize(), equalTo(100));
	}

	@Test
	void memoryAppender_builderShouldThrowForNegativeBufferSize() {
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
		    () -> MemoryAppender.newBuilder().setBufferSize(-1));
	}

	@Test
	void memoryAppender_builderShouldThrowForNonStringLayout() {
		@SuppressWarnings("unchecked")
		org.apache.logging.log4j.core.Layout<java.io.Serializable> nonStringLayout = mock(
		    org.apache.logging.log4j.core.Layout.class);
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
		    () -> MemoryAppender.newBuilder().setLayout(nonStringLayout));
	}

	@Test
	void getLogLines_shouldReturnEmptyListWhenNoMessages() {
		// Use a uniquely-named appender to avoid stale buffer data from other tests
		logger.removeAppender(memoryAppender);
		MemoryAppender freshAppender = MemoryAppender.newBuilder().setName("test-empty-buffer")
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		freshAppender.start();

		logger.addAppender(freshAppender);
		try {
			List<String> logLines = freshAppender.getLogLines();
			assertThat(logLines, notNullValue());
			assertThat(logLines, empty());
		} finally {
			logger.removeAppender(freshAppender);
			freshAppender.stop();
		}
	}

	@Test
	void getLogLines_shouldFormatLogMessagesWithLayout() {
		logger.warn("Test message");

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, hasItem(equalTo("Test message")));
	}

	@Test
	void memoryAppender_shouldMaintainInsertionOrder() {
		// Use a uniquely-named appender to avoid stale buffer data from other tests
		logger.removeAppender(memoryAppender);
		MemoryAppender orderAppender = MemoryAppender.newBuilder().setName("test-insertion-order-" + System.nanoTime())
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		orderAppender.start();

		logger.addAppender(orderAppender);
		try {
			logger.warn("First");
			logger.warn("Second");
			logger.warn("Third");

			List<String> logLines = orderAppender.getLogLines();
			assertThat(logLines, contains("First", "Second", "Third"));
		} finally {
			logger.removeAppender(orderAppender);
			orderAppender.stop();
		}
	}

	@Test
	void memoryAppender_shouldOverwriteOldestMessagesWhenFull() {
		logger.removeAppender(memoryAppender);

		memoryAppender = MemoryAppender.newBuilder().setLayout(PatternLayout.newBuilder().withPattern("%m").build())
		        .setBufferSize(3).build();
		memoryAppender.start();
		setupLogger();

		logger.warn("A");
		logger.warn("B");
		logger.warn("C");
		logger.warn("D");

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, contains("B", "C", "D"));
	}

	@Test
	void memoryAppender_shouldStartAutomaticallyWhenCreatedViaPluginFactory() {
		// The builder's build() doesn't auto-start, but the @PluginFactory does
		MemoryAppender appender = MemoryAppender.createAppender("test-plugin-appender", 10, true, null,
		    PatternLayout.newBuilder().withPattern("%m").build());

		assertThat(appender.isStarted(), equalTo(true));
		assertThat(appender.getBufferSize(), equalTo(10));
	}

	@Test
	void memoryAppender_pluginFactoryShouldUseDefaultBufferSizeForZero() {
		MemoryAppender appender = MemoryAppender.createAppender("test-plugin-zero", 0, true, null,
		    PatternLayout.newBuilder().withPattern("%m").build());

		assertThat(appender.getBufferSize(), equalTo(100));
	}

	@Test
	void memoryAppender_pluginFactoryShouldUseDefaultBufferSizeForNegative() {
		MemoryAppender appender = MemoryAppender.createAppender("test-plugin-neg", -5, true, null,
		    PatternLayout.newBuilder().withPattern("%m").build());

		assertThat(appender.getBufferSize(), equalTo(100));
	}

	@Test
	void memoryAppender_shouldNotFilterByDefault() {
		// Use a uniquely-named appender to avoid stale buffer data from other tests
		logger.removeAppender(memoryAppender);
		MemoryAppender filterAppender = MemoryAppender.newBuilder().setName("test-filter-levels")
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		filterAppender.start();

		logger.addAppender(filterAppender);
		logger.setLevel(Level.ALL);

		try {
			logger.trace("trace");
			logger.debug("debug");
			logger.info("info");
			logger.warn("warn");
			logger.error("error");
			logger.fatal("fatal");

			List<String> logLines = filterAppender.getLogLines();
			assertThat(logLines, contains("trace", "debug", "info", "warn", "error", "fatal"));
		} finally {
			logger.removeAppender(filterAppender);
			filterAppender.stop();
		}
	}

	private void setupLogger() {
		logger = (Logger) LogManager.getLogger("MemoryAppenderTest");
		// NB This needs to come before the setLevel() call
		logger.setAdditive(false);
		logger.setLevel(Level.ALL);
		logger.addAppender(memoryAppender);
	}

	@Test
	void getBuffer_shouldMigrateEventsWhenBufferSizeDecreases() {
		String appenderName = "test-migration-buffer-" + System.nanoTime();

		// Create appender with buffer size 5
		MemoryAppender appender1 = MemoryAppender.newBuilder().setName(appenderName)
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).setBufferSize(5).build();
		appender1.start();

		Logger migrationLogger = (Logger) LogManager.getLogger(appenderName);
		migrationLogger.setAdditive(false);
		migrationLogger.setLevel(Level.ALL);
		migrationLogger.addAppender(appender1);

		try {
			migrationLogger.warn("A");
			migrationLogger.warn("B");
			migrationLogger.warn("C");
			migrationLogger.warn("D");
			migrationLogger.warn("E");

			// Create new appender with same name but smaller buffer size
			MemoryAppender appender2 = MemoryAppender.newBuilder().setName(appenderName)
			        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).setBufferSize(3).build();
			appender2.start();

			// The new appender should have migrated the 3 most recent events
			List<String> logLines = appender2.getLogLines();
			assertThat(logLines, contains("C", "D", "E"));
			assertThat(appender2.getBufferSize(), equalTo(3));
		} finally {
			migrationLogger.removeAppender(appender1);
			migrationLogger.setLevel(null);
			((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
		}
	}

	@Test
	void getBuffer_shouldPreserveAllEventsWhenBufferSizeIncreases() {
		String appenderName = "test-migration-grow-" + System.nanoTime();

		// Create appender with buffer size 3
		MemoryAppender appender1 = MemoryAppender.newBuilder().setName(appenderName)
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).setBufferSize(3).build();
		appender1.start();

		Logger migrationLogger = (Logger) LogManager.getLogger(appenderName);
		migrationLogger.setAdditive(false);
		migrationLogger.setLevel(Level.ALL);
		migrationLogger.addAppender(appender1);

		try {
			migrationLogger.warn("A");
			migrationLogger.warn("B");
			migrationLogger.warn("C");

			// Create new appender with same name but larger buffer size
			MemoryAppender appender2 = MemoryAppender.newBuilder().setName(appenderName)
			        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).setBufferSize(10).build();
			appender2.start();

			// All events should be preserved
			List<String> logLines = appender2.getLogLines();
			assertThat(logLines, contains("A", "B", "C"));
			assertThat(appender2.getBufferSize(), equalTo(10));
		} finally {
			migrationLogger.removeAppender(appender1);
			migrationLogger.setLevel(null);
			((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
		}
	}

}
