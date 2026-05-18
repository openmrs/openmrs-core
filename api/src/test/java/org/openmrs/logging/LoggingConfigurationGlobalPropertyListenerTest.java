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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.GlobalProperty;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Tests for {@link LoggingConfigurationGlobalPropertyListener}.
 */
class LoggingConfigurationGlobalPropertyListenerTest {

	private LoggingConfigurationGlobalPropertyListener listener;

	@BeforeEach
	void setUp() {
		listener = new LoggingConfigurationGlobalPropertyListener();
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
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "debug");
			listener.globalPropertyChanged(gp);

			utilMock.verify(OpenmrsLoggingUtil::applyLogLevels);
		}
	}

	@Test
	void globalPropertyChanged_shouldNotReloadConfigurationWhenLogLevelChanges() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL, "debug");
			listener.globalPropertyChanged(gp);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, never());
		}
	}

	// --- globalPropertyChanged for GP_LOG_LAYOUT ---

	@Test
	void globalPropertyChanged_shouldReloadConfigurationWhenLogLayoutChanges() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");
			listener.globalPropertyChanged(gp);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration);
		}
	}

	@Test
	void globalPropertyChanged_shouldNotReloadWhenLogLayoutIsUnchanged() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			// Set initial value
			GlobalProperty gp1 = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");
			listener.globalPropertyChanged(gp1);

			// Same value again
			GlobalProperty gp2 = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");
			listener.globalPropertyChanged(gp2);

			// Should only have been called once (from the first change)
			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, times(1));
		}
	}

	@Test
	void globalPropertyChanged_shouldReloadWhenLogLayoutChangesToDifferentValue() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp1 = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");
			listener.globalPropertyChanged(gp1);

			GlobalProperty gp2 = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%p - %m%n");
			listener.globalPropertyChanged(gp2);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, times(2));
		}
	}

	// --- globalPropertyChanged for GP_LOG_LOCATION ---

	@Test
	void globalPropertyChanged_shouldReloadConfigurationWhenLogLocationChanges() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs");
			listener.globalPropertyChanged(gp);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration);
		}
	}

	@Test
	void globalPropertyChanged_shouldNotReloadWhenLogLocationIsUnchanged() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp1 = new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs");
			listener.globalPropertyChanged(gp1);

			GlobalProperty gp2 = new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs");
			listener.globalPropertyChanged(gp2);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, times(1));
		}
	}

	// --- globalPropertyDeleted ---

	@Test
	void globalPropertyDeleted_shouldReturnEarlyForLogLevelProperty() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			listener.globalPropertyDeleted(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);

			// log.level deletion should not trigger reload
			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, never());
		}
	}

	@Test
	void globalPropertyDeleted_shouldReloadConfigurationWhenLogLayoutDeleted() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LAYOUT);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration);
		}
	}

	@Test
	void globalPropertyDeleted_shouldReloadConfigurationWhenLogLocationDeleted() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LOCATION);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration);
		}
	}

	@Test
	void globalPropertyDeleted_shouldResetLogLayoutCacheWhenDeleted() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			// Set a value first
			GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");
			listener.globalPropertyChanged(gp);

			// Delete resets the cached value
			listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LAYOUT);

			// Now setting the same value again should trigger reload (cache was cleared)
			GlobalProperty gp2 = new GlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");
			listener.globalPropertyChanged(gp2);

			// Once from first change, once from deletion, once from re-setting after cache clear
			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, times(3));
		}
	}

	@Test
	void globalPropertyDeleted_shouldResetLogLocationCacheWhenDeleted() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs");
			listener.globalPropertyChanged(gp);

			listener.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LOCATION);

			// Re-setting same value should trigger reload since cache was cleared
			GlobalProperty gp2 = new GlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs");
			listener.globalPropertyChanged(gp2);

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, times(3));
		}
	}

	@Test
	void globalPropertyDeleted_shouldReturnEarlyForUnknownProperty() {
		try (MockedStatic<OpenmrsLoggingUtil> utilMock = mockStatic(OpenmrsLoggingUtil.class)) {
			listener.globalPropertyDeleted("unknown.property");

			utilMock.verify(OpenmrsLoggingUtil::reloadLoggingConfiguration, never());
		}
	}
}
