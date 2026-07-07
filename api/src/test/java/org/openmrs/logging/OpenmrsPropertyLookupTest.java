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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.openmrs.api.context.Context;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mockStatic;

/**
 * Tests for {@link OpenmrsPropertyLookup}.
 * <p/>
 * This class handles two distinct operational phases:
 * <ul>
 * <li>Normal operations: a session is open and ConfigUtil can reach the AdministrationService</li>
 * <li>Startup / initialization: no session is open, returns hardcoded defaults</li>
 * </ul>
 */
class OpenmrsPropertyLookupTest {

	private OpenmrsPropertyLookup lookup;

	private MockedStatic<Context> contextMock;

	private MockedStatic<OpenmrsUtil> openmrsUtilMock;

	private MockedStatic<ConfigUtil> configUtilMock;

	@BeforeEach
	void setUp() {
		lookup = new OpenmrsPropertyLookup();
		contextMock = mockStatic(Context.class);
		openmrsUtilMock = mockStatic(OpenmrsUtil.class);
		configUtilMock = mockStatic(ConfigUtil.class);
	}

	@AfterEach
	void tearDown() {
		configUtilMock.close();
		contextMock.close();
		openmrsUtilMock.close();
	}

	// --- applicationDirectory ---

	@Test
	void lookup_shouldReturnApplicationDirectoryWhenSet() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn("/opt/openmrs");

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, equalTo("/opt/openmrs"));
	}

	@Test
	void lookup_shouldReturnNullWhenApplicationDirectoryIsEmpty() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn("");

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, nullValue());
	}

	@Test
	void lookup_shouldReturnNullWhenApplicationDirectoryIsNull() {
		openmrsUtilMock.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn(null);

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, nullValue());
	}

	// --- logLocation during startup (no session open) ---

	@Test
	void lookup_shouldReturnNullForLogLocationDuringStartup() {
		// isSessionOpen() defaults to false; system/runtime properties default to null
		String result = lookup.lookup(null, "logLocation");

		assertThat(result, nullValue());
	}

	// --- logLocation during normal operations ---

	@Test
	void lookup_shouldReturnLogLocationFromGlobalProperty() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION))
		        .thenReturn("/var/log/openmrs");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo("/var/log/openmrs"));
	}

	@Test
	void lookup_shouldStripTrailingSlashFromLogLocation() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION))
		        .thenReturn("/var/log/openmrs/");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo("/var/log/openmrs"));
	}

	@Test
	void lookup_shouldReturnNullForNullLogLocation() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION)).thenReturn(null);

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, nullValue());
	}

	@Test
	void lookup_shouldReturnNullForBlankLogLocation() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION)).thenReturn("   ");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, nullValue());
	}

	// --- logLayout during startup (no session open) ---

	@Test
	void lookup_shouldReturnDefaultLayoutDuringStartup() {
		// isSessionOpen() defaults to false; system/runtime properties default to null
		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN));
	}

	// --- logLayout during normal operations ---

	@Test
	void lookup_shouldReturnLogLayoutFromGlobalProperty() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn("%d %m%n");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%d %m%n"));
	}

	@Test
	void lookup_shouldReturnDefaultLayoutWhenGlobalPropertyIsNull() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn(null);

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN));
	}

	@Test
	void lookup_shouldReturnDefaultLayoutWhenGlobalPropertyIsBlank() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn("   ");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN));
	}

	// --- unknown key ---

	/**
	 * Unknown lookup keys must not throw — Log4J2's {@code StrLookup} contract is to return null when
	 * no value can be resolved, so a typo in a layout pattern degrades gracefully (the literal
	 * {@code ${openmrs:typo}} surfaces in output) rather than crashing the substitutor.
	 */
	@Test
	void lookup_shouldReturnNullForUnknownKey() {
		assertThat(lookup.lookup(null, "unknownKey"), nullValue());
	}

	// --- privilege management ---

	@Test
	void lookup_shouldAddAndRemoveProxyPrivilegeWhenFetchingGlobalProperty() {
		contextMock.when(Context::isSessionOpen).thenReturn(true);
		configUtilMock.when(() -> ConfigUtil.getGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT)).thenReturn("%m%n");

		lookup.lookup(null, "logLayout");

		contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
		contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
	}

	// --- PatternLayout sanitization ---

	@Test
	void layoutPattern_shouldReplaceControlCharactersWithUnderscores() {
		String message = "before\nafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event).toString();
		assertThat(result, containsString("before_after"));
		assertThat(result, not(containsString("before\n")));
	}

	@Test
	void layoutPattern_shouldReplaceCarriageReturnWithUnderscores() {
		String message = "before\rafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event).toString();
		assertThat(result, containsString("before_after"));
		assertThat(result, not(containsString("before\r")));
	}

	@Test
	void layoutPattern_shouldNotReplaceTabs() {
		String message = "before\tafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event).toString();
		assertThat(result, containsString("before\tafter"));
	}
}
