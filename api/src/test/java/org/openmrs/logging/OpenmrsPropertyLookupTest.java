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

import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link OpenmrsPropertyLookup}.
 * <p/>
 * This class handles two distinct operational phases:
 * <ul>
 * <li>Normal operations: a session is open and ConfigUtil can reach the AdministrationService</li>
 * <li>Startup / initialization: no session is open, returns hardcoded defaults</li>
 * </ul>
 * <p/>
 * Rather than mocking statics (which requires Mockito's inline mock maker), these tests drive the real
 * collaborators: system properties feed {@link ConfigUtil#getSystemProperty(String)}, an open
 * {@link UserContext} makes {@link Context#isSessionOpen()} return true and enables the proxy-privilege
 * bracket, and global-property values are seeded into {@link ConfigUtil}'s cache so they resolve without
 * a backing service.
 */
class OpenmrsPropertyLookupTest {

	@TempDir
	Path tempDir;

	private OpenmrsPropertyLookup lookup;

	private Properties originalRuntimeProperties;

	@BeforeEach
	void setUp() {
		lookup = new OpenmrsPropertyLookup();
		originalRuntimeProperties = Context.getRuntimeProperties();
		Context.setRuntimeProperties(new Properties());
		clearLoggingState();
	}

	@AfterEach
	void tearDown() {
		clearLoggingState();
		Context.clearUserContext();
		Context.setRuntimeProperties(originalRuntimeProperties);
		OpenmrsUtil.setApplicationDataDirectory(null);
	}

	/**
	 * Removes any global-property values this test seeded into {@link ConfigUtil}'s static cache and any
	 * system properties that would otherwise leak into other tests sharing the JVM fork.
	 */
	private static void clearLoggingState() {
		ConfigUtil configUtil = new ConfigUtil();
		configUtil.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LOCATION);
		configUtil.globalPropertyDeleted(OpenmrsConstants.GP_LOG_LAYOUT);
		System.clearProperty(OpenmrsConstants.GP_LOG_LOCATION);
		System.clearProperty(OpenmrsConstants.GP_LOG_LAYOUT);
	}

	private static void openSession() {
		// the UserContext never authenticates here, so a no-op authentication scheme is sufficient
		Context.setUserContext(new UserContext(credentials -> null));
	}

	private static void seedGlobalProperty(String name, String value) {
		new ConfigUtil().globalPropertyChanged(new GlobalProperty(name, value));
	}

	// --- applicationDirectory ---

	@Test
	void lookup_shouldReturnApplicationDirectoryWhenSet() {
		OpenmrsUtil.setApplicationDataDirectory(tempDir.toString());

		String result = lookup.lookup(null, "applicationDirectory");

		assertThat(result, equalTo(tempDir.toFile().toString()));
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
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo("/var/log/openmrs"));
	}

	@Test
	void lookup_shouldStripTrailingSlashFromLogLocation() {
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "/var/log/openmrs/");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, equalTo("/var/log/openmrs"));
	}

	@Test
	void lookup_shouldReturnNullForNullLogLocation() {
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, null);

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, nullValue());
	}

	@Test
	void lookup_shouldReturnNullForBlankLogLocation() {
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LOCATION, "   ");

		String result = lookup.lookup(null, "logLocation");

		assertThat(result, nullValue());
	}

	// --- logLayout during startup (no session open) ---

	@Test
	void lookup_shouldReturnDefaultLayoutDuringStartup() {
		// isSessionOpen() defaults to false; system/runtime properties default to null
		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n"));
	}

	// --- logLayout during normal operations ---

	@Test
	void lookup_shouldReturnLogLayoutFromGlobalProperty() {
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%d %m%n"));
	}

	@Test
	void lookup_shouldReturnDefaultLayoutWhenGlobalPropertyIsNull() {
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, null);

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n"));
	}

	@Test
	void lookup_shouldReturnDefaultLayoutWhenGlobalPropertyIsBlank() {
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "   ");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n"));
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

	// --- system property precedence ---

	/**
	 * A system property takes precedence over a session-scoped global property. Setting both and getting
	 * the system value back proves the precedence order without needing to verify which collaborator was
	 * (or was not) consulted.
	 */
	@Test
	void lookup_shouldPreferSystemPropertyOverGlobalProperty() {
		System.setProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%m%n");
		openSession();
		seedGlobalProperty(OpenmrsConstants.GP_LOG_LAYOUT, "%d %m%n");

		String result = lookup.lookup(null, "logLayout");

		assertThat(result, equalTo("%m%n"));
	}
}
