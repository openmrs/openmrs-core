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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before_after"));
		assertThat(result, not(containsString("before\n")));
	}

	@Test
	void layoutPattern_shouldReplaceCarriageReturnWithUnderscores() {
		String message = "before\rafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before_after"));
		assertThat(result, not(containsString("before\r")));
	}

	@Test
	void layoutPattern_shouldNotReplaceTabs() {
		String message = "before\tafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before\tafter"));
	}

	@Test
	void layoutPattern_shouldReplaceLineAndParagraphSeparatorsWithUnderscores() {
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();

		String nelMessage = "before\u0085after";
		LogEvent nelEvent = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(nelMessage)).build();
		assertThat(layout.toSerializable(nelEvent), containsString("before_after"));

		String lsMessage = "before\u2028after";
		LogEvent lsEvent = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(lsMessage)).build();
		assertThat(layout.toSerializable(lsEvent), containsString("before_after"));

		String psMessage = "before\u2029after";
		LogEvent psEvent = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(psMessage)).build();
		assertThat(layout.toSerializable(psEvent), containsString("before_after"));
	}

	// --- layout pattern copy pinning ---

	/**
	 * The on-disk pattern copies must stay byte-for-byte equivalent to
	 * {@link OpenmrsConstants#DEFAULT_LOG_LAYOUT_PATTERN}. This file and the liquibase changeset below
	 * carry the {@code %replace} regex as raw text rather than as a Java string literal, so a doubled
	 * backslash there is read by log4j2 as a literal backslash plus literal characters and silently
	 * disables the defense.
	 * <p>
	 * The test classpath has its own {@code log4j2.xml}, so the main copy is read by file path relative
	 * to the module directory rather than as a classpath resource.
	 */
	@Test
	void log4j2xml_shouldCarryTheSameSanitizingLayoutAsTheDefaultConstant() throws Exception {
		String xml = Files.readString(Path.of("src/main/resources/log4j2.xml"));
		String pattern = extractDefaultPattern(xml);

		assertThat(pattern, equalTo(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN));
		assertHeaderSanitizes(pattern);
	}

	/**
	 * The value written into {@code log.layout} for existing installs must behave like the constant: it
	 * is persisted verbatim by liquibase, so the same doubled-backslash mistake would be shipped to
	 * every upgraded database. Read as a classpath resource since nothing on the test classpath shadows
	 * it.
	 */
	@Test
	void liquibaseChangelog_shouldCarryTheSameSanitizingLayoutAsTheDefaultConstant() throws Exception {
		String xml = readClasspathResource("org/openmrs/liquibase/updates/liquibase-update-to-latest-3.0.x.xml");
		String pattern = extractChangelogPattern(xml);

		assertThat(pattern, equalTo(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN));
		assertHeaderSanitizes(pattern);
	}

	private static String extractDefaultPattern(String xml) {
		return unescapeXml(extractElementContent(xml, "Property", "name", "defaultPattern"));
	}

	private static String extractChangelogPattern(String xml) {
		Matcher changeset = Pattern.compile("<changeSet id=\"TRUNK-6549-2026-06-07\"[^>]*>.*?</changeSet>", Pattern.DOTALL)
		        .matcher(xml);
		assertTrue(changeset.find(), "expected a TRUNK-6549 changelog changeset");
		Matcher column = Pattern.compile("<column name=\"property_value\"\\s+value=\"([^\"]*)\"\\s*/>")
		        .matcher(changeset.group());
		assertTrue(column.find(), "expected a property_value column in the TRUNK-6549 changeset");
		return unescapeXml(column.group(1));
	}

	private static String extractElementContent(String xml, String tag, String attr, String attrValue) {
		Matcher match = Pattern
		        .compile("<" + tag + " " + attr + "=\"" + Pattern.quote(attrValue) + "\">([^<]*)</" + tag + ">")
		        .matcher(xml);
		assertTrue(match.find(), "expected element <" + tag + " " + attr + "=\"" + attrValue + "\"> in:\n" + xml);
		return match.group(1);
	}

	private static String unescapeXml(String value) {
		return value.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
		        .replace("&apos;", "'");
	}

	private static String readClasspathResource(String resource) throws Exception {
		try (InputStream in = OpenmrsPropertyLookupTest.class.getClassLoader().getResourceAsStream(resource)) {
			assertNotNull(in, "missing classpath resource " + resource);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static void assertHeaderSanitizes(String pattern) {
		PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage("before\nafter")).build();
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before_after"));
		assertThat(result, not(containsString("before\n")));
	}
}
