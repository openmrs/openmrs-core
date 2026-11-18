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

import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Tests for {@link LoggingConfigurationGlobalPropertyListener}.
 * <p/>
 * The listener's job is to react to logging-related global-property changes by either applying log
 * levels in-place ({@code log.level}) or fully reloading the logging configuration ({@code log.layout}
 * and {@code log.location}), while caching the latter two so an unchanged value does not trigger a
 * redundant reload.
 * <p/>
 * Instead of mocking {@link OpenmrsLoggingUtil} (which would require Mockito's inline mock maker), these
 * tests observe the real effects: applying a level changes the live logger, and a reload swaps the
 * Log4J2 {@link Configuration} instance. "Did a reload happen?" therefore reduces to "is the current
 * configuration a different instance than before?".
 */
class LoggingConfigurationGlobalPropertyListenerTest {

	private LoggingConfigurationGlobalPropertyListener listener;

	private LoggerContext loggerContext;

	private Properties originalRuntimeProperties;

	@BeforeEach
	void setUp() {
		listener = new LoggingConfigurationGlobalPropertyListener();
		// reloadLoggingConfiguration() reconfigures the context returned by getContext(true), which is a
		// different instance than getContext(false); observe the same one so a reload is visible here as
		// a swapped Configuration instance
		loggerContext = (LoggerContext) LogManager.getContext(true);

		// applyLogLevels() consults the system and runtime properties before the supplied value, so make
		// sure neither carries a stale log.level into these tests
		originalRuntimeProperties = Context.getRuntimeProperties();
		Context.setRuntimeProperties(new Properties());
		System.clearProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
	}

	@AfterEach
	void tearDown() {
		System.clearProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		Context.setRuntimeProperties(originalRuntimeProperties);
	}

	private Configuration currentConfiguration() {
		return loggerContext.getConfiguration();
	}

	// --- supportsPropertyName ---

	@Test
	void supportsPropertyName_shouldSupportLogLevelProperty() {
		assertThat(listener.supportsPropertyName(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL), is(true));
	}

	@Test
	void supportsPropertyName_shouldSupportLogLayoutProperty() {
		assertThat(listener.supportsPropertyName(OpenmrsConstants.GP_LOG_LAYOUT), is(true));
	}

	@Test
	void supportsPropertyName_shouldSupportLogLocationProperty() {
		assertThat(listener.supportsPropertyName(OpenmrsConstants.GP_LOG_LOCATION), is(true));
	}

	@Test
	void supportsPropertyName_shouldNotSupportArbitraryProperty() {
		assertThat(listener.supportsPropertyName("some.other.property"), is(false));
	}

	@Test
	void supportsPropertyName_shouldNotSupportNullProperty() {
		assertThat(listener.supportsPropertyName(null), is(false));
	}

	@Test
	void supportsPropertyName_shouldNotSupportEmptyProperty() {
		assertThat(listener.supportsPropertyName(""), is(false));
	}

	// --- globalPropertyChanged for GLOBAL_PROPERTY_LOG_LEVEL ---

	@Test
	void globalPropertyChanged_shouldApplyLogLevelsWhenLogLevelChanges() {
		String loggerName = "org.openmrs.logging.test.listener.applylevel";
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, loggerName + ":debug");

		listener.globalPropertyChanged(gp);

		assertThat(((Logger) LogManager.getLogger(loggerName)).getLevel(), equalTo(Level.DEBUG));
	}

	@Test
	void globalPropertyChanged_shouldNotReloadConfigurationWhenLogLevelChanges() {
		Configuration before = currentConfiguration();
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL,
		    "org.openmrs.logging.test.listener.noreload:debug");

		listener.globalPropertyChanged(gp);

		// applying a level updates loggers in place; it must not swap the configuration
		assertThat(currentConfiguration(), is(sameInstance(before)));
	}

	// --- globalPropertyChanged for GP_LOG_LAYOUT ---

	@Test
	void globalPropertyChanged_shouldReloadConfigurationWhenLogLayoutChanges() {
		Configuration before = currentConfiguration();

		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n"));

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyChanged_shouldNotReloadWhenLogLayoutIsUnchanged() {
		// establish the cached value (this first change reloads)
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n"));

		Configuration before = currentConfiguration();

		// same value again — should be a no-op
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n"));

		assertThat(currentConfiguration(), is(sameInstance(before)));
	}

	@Test
	void globalPropertyChanged_shouldReloadWhenLogLayoutChangesToDifferentValue() {
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n"));

		Configuration before = currentConfiguration();

		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%p - %m%n"));

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	// --- globalPropertyChanged for GP_LOG_LOCATION ---

	@Test
	void globalPropertyChanged_shouldReloadConfigurationWhenLogLocationChanges() {
		Configuration before = currentConfiguration();

		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs"));

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyChanged_shouldNotReloadWhenLogLocationIsUnchanged() {
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs"));

		Configuration before = currentConfiguration();

		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs"));

		assertThat(currentConfiguration(), is(sameInstance(before)));
	}

	// --- globalPropertyDeleted ---

	@Test
	void globalPropertyDeleted_shouldReloadConfigurationForLogLevelProperty() {
		Configuration before = currentConfiguration();

		listener.globalPropertyDeleted(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);

		// log.level deletion should trigger a reload
		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyDeleted_shouldReloadConfigurationWhenLogLayoutDeleted() {
		Configuration before = currentConfiguration();

		listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LAYOUT);

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyDeleted_shouldReloadConfigurationWhenLogLocationDeleted() {
		Configuration before = currentConfiguration();

		listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LOCATION);

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyDeleted_shouldResetLogLayoutCacheWhenDeleted() {
		// cache a value
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n"));

		// deletion resets the cache
		listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LAYOUT);

		Configuration before = currentConfiguration();

		// re-setting the same value now reloads again, proving the cache was cleared (otherwise this
		// would be treated as an unchanged value and skipped)
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n"));

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyDeleted_shouldResetLogLocationCacheWhenDeleted() {
		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs"));

		listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LOCATION);

		Configuration before = currentConfiguration();

		listener.globalPropertyChanged(new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs"));

		assertThat(currentConfiguration(), is(not(sameInstance(before))));
	}

	@Test
	void globalPropertyDeleted_shouldReturnEarlyForUnknownProperty() {
		Configuration before = currentConfiguration();

		listener.globalPropertyDeleted("unknown.property");

		assertThat(currentConfiguration(), is(sameInstance(before)));
	}
}
