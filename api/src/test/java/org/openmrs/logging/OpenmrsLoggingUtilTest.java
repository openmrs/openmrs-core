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
import org.mockito.MockedStatic;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

	// --- applyLogLevels ---

	@Test
	void applyLogLevels_shouldApplySingleLogLevel() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			AdministrationService adminService = mock(AdministrationService.class);
			contextMock.when(Context::getAdministrationService).thenReturn(adminService);
			when(adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, ""))
			        .thenReturn("org.openmrs.logging.test.applylevels:debug");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger = (Logger) LogManager.getLogger("org.openmrs.logging.test.applylevels");
			assertThat(logger.getLevel(), equalTo(Level.DEBUG));
		}
	}

	@Test
	void applyLogLevels_shouldApplyDefaultLogLevelWhenNoClassSpecified() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			AdministrationService adminService = mock(AdministrationService.class);
			contextMock.when(Context::getAdministrationService).thenReturn(adminService);
			when(adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "")).thenReturn("warn");

			OpenmrsLoggingUtil.applyLogLevels();

			Logger logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
			assertThat(logger.getLevel(), equalTo(Level.WARN));
		}
	}

	@Test
	void applyLogLevels_shouldApplyMultipleLogLevels() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			AdministrationService adminService = mock(AdministrationService.class);
			contextMock.when(Context::getAdministrationService).thenReturn(adminService);
			when(adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, ""))
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
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			AdministrationService adminService = mock(AdministrationService.class);
			contextMock.when(Context::getAdministrationService).thenReturn(adminService);
			when(adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "")).thenReturn("");

			// Should not throw
			OpenmrsLoggingUtil.applyLogLevels();
		}
	}

	@Test
	void applyLogLevels_shouldAddAndRemoveProxyPrivilege() {
		try (MockedStatic<Context> contextMock = mockStatic(Context.class)) {
			AdministrationService adminService = mock(AdministrationService.class);
			contextMock.when(Context::getAdministrationService).thenReturn(adminService);
			when(adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "")).thenReturn("");

			OpenmrsLoggingUtil.applyLogLevels();

			contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
			contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		}
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
