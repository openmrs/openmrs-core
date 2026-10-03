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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

/**
 * Tests for {@link OpenmrsLoggingUtil}.
 * <p/>
 * Tests are organized by method. Where possible, tests directly manipulate the Log4J2 context
 * without mocking, to provide integration-style coverage. Tests that require
 * {@link Context#getAdministrationService()} use mocked statics.
 */
class OpenmrsLoggingUtilTest {

	private LoggerContext loggerContext;

	private Level originalRootLevel;

	@BeforeEach
	void setUp() {
		loggerContext = (LoggerContext) LogManager.getContext(false);
		originalRootLevel = ((Logger) LogManager.getRootLogger()).getLevel();
	}

	@AfterEach
	void tearDown() {
		// Restore root logger level
		LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
		rootConfig.setLevel(originalRootLevel);
		loggerContext.updateLoggers();
	}

	// --- Constructor ---

	@Test
	void constructor_shouldBePrivate() throws Exception {
		Constructor<OpenmrsLoggingUtil> constructor = OpenmrsLoggingUtil.class.getDeclaredConstructor();
		assertThat(constructor.canAccess(null), equalTo(false));

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

		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(parent + ":warn," + child + ":trace");

			OpenmrsLoggingUtil.applyLogLevels();

			assertThat(((Logger) LogManager.getLogger(parent)).getLevel(), equalTo(Level.WARN));
			assertThat(((Logger) LogManager.getLogger(child)).getLevel(), equalTo(Level.TRACE));
		}
	}

	// --- applyLogLevels ---

	@Test
	void applyLogLevels_shouldApplySingleLogLevel() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("org.openmrs.logging.test.applylevels:debug");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.applylevels");
			assertThat(logger.getLevel(), equalTo(Level.DEBUG));
		}
	}

	@Test
	void applyLogLevels_shouldApplyDefaultLogLevelWhenNoClassSpecified() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("warn");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
			assertThat(logger.getLevel(), equalTo(Level.WARN));
		}
	}

	@Test
	void applyLogLevels_shouldApplyMultipleLogLevels() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("org.openmrs.logging.test.multi1:debug,org.openmrs.logging.test.multi2:error");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger1 = (Logger) LogManager.getLogger("org.openmrs.logging.test.multi1");
			Logger logger2 = (Logger) LogManager.getLogger("org.openmrs.logging.test.multi2");
			assertThat(logger1.getLevel(), equalTo(Level.DEBUG));
			assertThat(logger2.getLevel(), equalTo(Level.ERROR));
		}
	}

	@Test
	void applyLogLevels_shouldHandleEmptyLogLevelProperty() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("");

			// Should not throw
			OpenmrsLoggingUtil.applyLogLevels();
		}
	}

	@Test
	void applyLogLevels_shouldUseSystemPropertyWhenSet() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("org.openmrs.logging.test.sysprop:debug");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.sysprop");
			assertThat(logger.getLevel(), equalTo(Level.DEBUG));

			// System property wins — runtime and global must not be consulted, and no privilege bracket
			configUtilMock.verify(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());
		}
	}

	@Test
	void applyLogLevels_shouldFallBackToRuntimePropertyWhenSystemPropertyMissing() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("org.openmrs.logging.test.runtimeprop:error");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.runtimeprop");
			assertThat(logger.getLevel(), equalTo(Level.ERROR));

			// Verify the precedence ORDER, not just that calls happened: system must be consulted
			// before runtime. A future re-ordering of ConfigUtil calls would fail this test.
			InOrder inOrder = Mockito.inOrder(ConfigUtil.class);
			inOrder.verify(configUtilMock, () -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL));
			inOrder.verify(configUtilMock, () -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL));

			// Runtime property wins over the global property
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());
		}
	}

	@Test
	void applyLogLevels_shouldNotReadGlobalPropertyWhenSessionClosed() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(false);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);

			OpenmrsLoggingUtil.applyLogLevels();

			// With no session, the global-property branch must not run
			configUtilMock.verify(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), never());
			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), never());
		}
	}

	@Test
	void applyLogLevels_shouldAddAndRemoveProxyPrivilege() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn("");

			OpenmrsLoggingUtil.applyLogLevels();

			// The privilege must be acquired BEFORE the global-property read and released AFTER it,
			// not just present somewhere. Order matters: missing-privilege errors only surface in the
			// (add → read → remove) sequence.
			InOrder inOrder = Mockito.inOrder(Context.class, ConfigUtil.class);
			inOrder.verify(contextMock, () -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
			inOrder.verify(configUtilMock, () -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL));
			inOrder.verify(contextMock, () -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		}
	}

	/**
	 * Regression test for the re-entrant logging initialization that broke downstream modules: reading
	 * the {@code log.level} global property reaches into the service layer, which may be unavailable or
	 * mid-initialization and therefore throw an unchecked exception. {@code applyLogLevels} must
	 * swallow that failure (and still release the proxy privilege) rather than letting it propagate.
	 * Without the broadened catch the exception escapes.
	 */
	@Test
	void applyLogLevels_shouldNotPropagateRuntimeExceptionWhenReadingGlobalPropertyFails() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class);
		        MockedStatic<ConfigUtil> configUtilMock = mockStatic(ConfigUtil.class)) {
			contextMock.when(Context::isSessionOpen).thenReturn(true);
			configUtilMock.when(() -> ConfigUtil.getSystemProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getRuntimeProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenReturn(null);
			configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL))
			        .thenThrow(new NullPointerException("ServiceContext not initialized"));

			// Must not throw
			OpenmrsLoggingUtil.applyLogLevels();

			// The proxy-privilege bracket must still be balanced even when the read fails
			contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		}
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
