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

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link OpenmrsLoggingUtil}.
 * <p/>
 * Tests are organized by method. Where possible, tests directly manipulate the Log4J2 context
 * without mocking, to provide integration-style coverage. The {@code applyLogLevels()} tests that
 * depend on configuration sources drive the real collaborators rather than mocking statics: system
 * properties feed the system-property branch, {@link Context#setRuntimeProperties(Properties)} feeds
 * the runtime branch, an open {@link UserContext} enables the session-scoped global branch, and global
 * values are seeded into {@link ConfigUtil}'s cache so they resolve without a backing service.
 */
class OpenmrsLoggingUtilTest {

	private LoggerContext loggerContext;

	private Level originalRootLevel;

	private Properties originalRuntimeProperties;

	@BeforeEach
	void setUp() {
		loggerContext = (LoggerContext) LogManager.getContext(false);
		originalRootLevel = ((Logger) LogManager.getRootLogger()).getLevel();

		// applyLogLevels() resolves log.level from system, then runtime, then (if a session is open)
		// global properties; start every test from a clean slate so these sources do not leak between
		// tests sharing the JVM fork
		originalRuntimeProperties = Context.getRuntimeProperties();
		Context.setRuntimeProperties(new Properties());
		System.clearProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
	}

	@AfterEach
	void tearDown() {
		// Restore root logger level
		LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
		rootConfig.setLevel(originalRootLevel);
		loggerContext.updateLoggers();

		System.clearProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		new ConfigUtil().globalPropertyDeleted(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		Context.clearUserContext();
		Context.setRuntimeProperties(originalRuntimeProperties);
	}

	private static void openSession() {
		// the UserContext never authenticates here, so a no-op authentication scheme is sufficient
		Context.setUserContext(new UserContext(credentials -> null));
	}

	private static void seedGlobalLogLevel(String value) {
		new ConfigUtil().globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, value));
	}

	// --- Constructor ---

	@Test
	void constructor_shouldBePrivate() throws Exception {
		Constructor<OpenmrsLoggingUtil> constructor = OpenmrsLoggingUtil.class.getDeclaredConstructor();
		assertThat(constructor.isAccessible(), equalTo(false));

		constructor.setAccessible(true);
		OpenmrsLoggingUtil instance = constructor.newInstance();
		assertThat(instance, notNullValue());
	}

	// --- applyLogLevel ---

	@Test
	void applyLogLevel_shouldSetLevelToTrace() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "trace");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.TRACE));
	}

	@Test
	void applyLogLevel_shouldSetLevelToDebug() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "debug");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.DEBUG));
	}

	@Test
	void applyLogLevel_shouldSetLevelToInfo() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "info");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.INFO));
	}

	@Test
	void applyLogLevel_shouldSetLevelToWarn() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "warn");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.WARN));
	}

	@Test
	void applyLogLevel_shouldSetLevelToError() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "error");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.ERROR));
	}

	@Test
	void applyLogLevel_shouldSetLevelToFatal() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "fatal");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.FATAL));
	}

	@Test
	void applyLogLevel_shouldHandleUpperCaseLogLevel() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "DEBUG");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.DEBUG));
	}

	@Test
	void applyLogLevel_shouldHandleMixedCaseLogLevel() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test", "WaRn");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test");
		assertThat(logger.getLevel(), equalTo(Level.WARN));
	}

	@Test
	void applyLogLevel_shouldUseDefaultClassWhenLogClassIsEmpty() {
		OpenmrsLoggingUtil.applyLogLevel("", "debug");

		Logger logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		assertThat(logger.getLevel(), equalTo(Level.DEBUG));
	}

	@Test
	void applyLogLevel_shouldUseDefaultClassWhenLogClassIsNull() {
		OpenmrsLoggingUtil.applyLogLevel(null, "error");

		Logger logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		assertThat(logger.getLevel(), equalTo(Level.ERROR));
	}

	@Test
	void applyLogLevel_shouldNotChangeLevelWhenLogLevelIsBlank() {
		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.unchanged");
		Level levelBefore = logger.getLevel();

		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test.unchanged", "");

		// Level should not have changed
		assertThat(logger.getLevel(), equalTo(levelBefore));
	}

	@Test
	void applyLogLevel_shouldNotChangeLevelWhenLogLevelIsNull() {
		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.unchanged2");
		Level levelBefore = logger.getLevel();

		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test.unchanged2", null);

		assertThat(logger.getLevel(), equalTo(levelBefore));
	}

	@Test
	void applyLogLevel_shouldFallbackToInfoForInvalidLevelOnDefaultClass() {
		OpenmrsLoggingUtil.applyLogLevel("", "invalid");

		Logger logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		assertThat(logger.getLevel(), equalTo(Level.INFO));
	}

	@Test
	void applyLogLevel_shouldFallbackToWarnForInvalidLevelOnCustomClass() {
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test.invalid", "invalid");

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.invalid");
		assertThat(logger.getLevel(), equalTo(Level.WARN));
	}

	@Test
	void applyLogLevel_shouldUpdateExistingLoggerConfig() {
		// Set level first
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test.update", "debug");
		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.update");
		assertThat(logger.getLevel(), equalTo(Level.DEBUG));

		// Update to a different level
		OpenmrsLoggingUtil.applyLogLevel("org.openmrs.logging.test.update", "error");
		assertThat(logger.getLevel(), equalTo(Level.ERROR));
	}

	/**
	 * Regression test for the bug where {@code applyLogLevel} would mutate an ancestor logger's level
	 * when the targeted child logger had no exact match. The fix in {@code applyLogLevelInternal} (see
	 * the {@code !configuration.getName().equals(logClass)} branch) creates a fresh
	 * {@code LoggerConfig} for the child rather than calling {@code setLevel} on the inherited
	 * ancestor.
	 */
	@Test
	void applyLogLevel_shouldNotMutateAncestorLoggerLevel() {
		String parent = "org.openmrs.logging.test.isolation";
		String child = parent + ".child";

		OpenmrsLoggingUtil.applyLogLevel(parent, "warn");
		assertThat(((Logger) LogManager.getLogger(parent)).getLevel(), equalTo(Level.WARN));

		// Setting the child must not touch the parent
		OpenmrsLoggingUtil.applyLogLevel(child, "trace");

		assertThat("Child logger should be at TRACE", ((Logger) LogManager.getLogger(child)).getLevel(),
		    equalTo(Level.TRACE));
		assertThat("Parent logger must remain at WARN, not be downgraded to TRACE",
		    ((Logger) LogManager.getLogger(parent)).getLevel(), equalTo(Level.WARN));
	}

	/**
	 * Regression test that mirrors {@link #applyLogLevel_shouldNotMutateAncestorLoggerLevel()} but
	 * exercises the multi-entry {@code applyLogLevels()} parsing path. Setting a child via the
	 * global-property syntax must not change a parent that was set in the same call.
	 */
	@Test
	void applyLogLevels_shouldNotMutateAncestorLoggerLevel() {
		String parent = "org.openmrs.logging.test.isolation2";
		String child = parent + ".child";

		openSession();
		seedGlobalLogLevel(parent + ":warn," + child + ":trace");

		OpenmrsLoggingUtil.applyLogLevels();

		assertThat(((Logger) LogManager.getLogger(parent)).getLevel(), equalTo(Level.WARN));
		assertThat(((Logger) LogManager.getLogger(child)).getLevel(), equalTo(Level.TRACE));
	}

	// --- applyLogLevels ---

	@Test
	void applyLogLevels_shouldApplySingleLogLevel() {
		openSession();
		seedGlobalLogLevel("org.openmrs.logging.test.applylevels:debug");

		OpenmrsLoggingUtil.applyLogLevels();

		Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.applylevels");
		assertThat(logger.getLevel(), equalTo(Level.DEBUG));
	}

	@Test
	void applyLogLevels_shouldApplyDefaultLogLevelWhenNoClassSpecified() {
		openSession();
		seedGlobalLogLevel("warn");

		OpenmrsLoggingUtil.applyLogLevels();

		Logger logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		assertThat(logger.getLevel(), equalTo(Level.WARN));
	}

	@Test
	void applyLogLevels_shouldApplyMultipleLogLevels() {
		openSession();
		seedGlobalLogLevel("org.openmrs.logging.test.multi1:debug,org.openmrs.logging.test.multi2:error");

		OpenmrsLoggingUtil.applyLogLevels();

		Logger logger1 = (Logger) LogManager.getLogger("org.openmrs.logging.test.multi1");
		Logger logger2 = (Logger) LogManager.getLogger("org.openmrs.logging.test.multi2");
		assertThat(logger1.getLevel(), equalTo(Level.DEBUG));
		assertThat(logger2.getLevel(), equalTo(Level.ERROR));
	}

	@Test
	void applyLogLevels_shouldHandleEmptyLogLevelProperty() {
		openSession();
		seedGlobalLogLevel("");

		// Should not throw
		OpenmrsLoggingUtil.applyLogLevels();
	}

	/**
	 * A system property wins over a conflicting runtime property. Setting both for the same logger and
	 * getting the system-supplied level back proves the precedence order behaviourally.
	 */
	@Test
	void applyLogLevels_shouldUseSystemPropertyWhenSet() {
		String loggerName = "org.openmrs.logging.test.sysprop";
		System.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":debug");
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":error");
		Context.setRuntimeProperties(runtimeProperties);

		OpenmrsLoggingUtil.applyLogLevels();

		Logger logger = (Logger) LogManager.getLogger(loggerName);
		assertThat(logger.getLevel(), equalTo(Level.DEBUG));
	}

	/**
	 * With no system property, a runtime property wins over a conflicting session-scoped global property.
	 */
	@Test
	void applyLogLevels_shouldFallBackToRuntimePropertyWhenSystemPropertyMissing() {
		String loggerName = "org.openmrs.logging.test.runtimeprop";
		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":error");
		Context.setRuntimeProperties(runtimeProperties);

		openSession();
		seedGlobalLogLevel(loggerName + ":trace");

		OpenmrsLoggingUtil.applyLogLevels();

		Logger logger = (Logger) LogManager.getLogger(loggerName);
		assertThat(logger.getLevel(), equalTo(Level.ERROR));
	}

	/**
	 * When no session is open, the session-scoped global property must not be consulted — so a logger
	 * whose only configured source is the global property keeps its inherited level.
	 */
	@Test
	void applyLogLevels_shouldNotReadGlobalPropertyWhenSessionClosed() {
		String loggerName = "org.openmrs.logging.test.sessionclosed";
		Logger logger = (Logger) LogManager.getLogger(loggerName);
		Level levelBefore = logger.getLevel();

		// session is closed by default; only the global property carries a value for this logger
		seedGlobalLogLevel(loggerName + ":debug");

		OpenmrsLoggingUtil.applyLogLevels();

		assertThat(logger.getLevel(), equalTo(levelBefore));
	}

	/**
	 * With a session open and no system/runtime override, the level comes from the global property. This
	 * exercises the full session-scoped path (proxy-privilege bracket plus global read) end-to-end.
	 */
	@Test
	void applyLogLevels_shouldReadGlobalPropertyWhenSessionOpen() {
		String loggerName = "org.openmrs.logging.test.sessionopen";
		openSession();
		seedGlobalLogLevel(loggerName + ":warn");

		OpenmrsLoggingUtil.applyLogLevels();

		Logger logger = (Logger) LogManager.getLogger(loggerName);
		assertThat(logger.getLevel(), equalTo(Level.WARN));
	}

	// --- stringToLevel ---

	@Test
	void stringToLevel_shouldReturnWarnForNullLogLevel() {
		assertThat(OpenmrsLoggingUtil.stringToLevel((String) null), equalTo(Level.WARN));
	}

	@Test
	void stringToLevel_shouldReturnWarnForNullLogLevelWithNullLogClass() {
		assertThat(OpenmrsLoggingUtil.stringToLevel(null, null), equalTo(Level.WARN));
	}

	@Test
	void stringToLevel_shouldReturnWarnForInvalidLevelWithNullLogClass() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("invalid", null), equalTo(Level.WARN));
	}

	@Test
	void stringToLevel_shouldReturnInfoForInvalidLevelWithDefaultLogClass() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("invalid", OpenmrsConstants.LOG_CLASS_DEFAULT), equalTo(Level.INFO));
	}

	@Test
	void stringToLevel_shouldReturnTraceForTraceLevel() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("trace"), equalTo(Level.TRACE));
	}

	@Test
	void stringToLevel_shouldReturnDebugForDebugLevel() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("DEBUG"), equalTo(Level.DEBUG));
	}

	@Test
	void stringToLevel_shouldReturnInfoForInfoLevel() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("Info"), equalTo(Level.INFO));
	}

	@Test
	void stringToLevel_shouldReturnErrorForErrorLevel() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("error"), equalTo(Level.ERROR));
	}

	@Test
	void stringToLevel_shouldReturnFatalForFatalLevel() {
		assertThat(OpenmrsLoggingUtil.stringToLevel("fatal"), equalTo(Level.FATAL));
	}

	// --- getOpenmrsLogLocation ---

	@Test
	void getOpenmrsLogLocation_shouldReturnNullWhenNoFileAppenderFound() {
		// The default test log4j2 configuration has no OPENMRS FILE APPENDER
		String location = OpenmrsLoggingUtil.getOpenmrsLogLocation();
		assertThat(location, nullValue());
	}

	// --- reloadLoggingConfiguration ---

	@Test
	void reloadLoggingConfiguration_shouldNotThrow() {
		// This exercises the reconfigure path; we just verify it doesn't throw
		OpenmrsLoggingUtil.reloadLoggingConfiguration();
	}
}
