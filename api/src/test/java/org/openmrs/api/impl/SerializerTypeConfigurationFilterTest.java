/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit tests for the module scoping rules of serializer type configuration, exercised through
 * {@link AdministrationServiceImpl#filterSerializerTypeEntries(String, String, Map)},
 * {@link AdministrationServiceImpl#serializerTypeEntryProblem(String, String)} and
 * {@link AdministrationServiceImpl#sanitizeForLog(String)}.
 */
public class SerializerTypeConfigurationFilterTest {

	private static final String TEST_MODULE_PACKAGE = "org.openmrs.module.testmodule";

	private static Map<String, String> withTestModule() {
		return Map.of("testmodule", TEST_MODULE_PACKAGE, "serialization.xstream",
		    "org.openmrs.module.serialization.xstream");
	}

	@Test
	public void shouldIgnoreConfigurationWhosePrefixIsNotALoadedModule() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries("x.serializer.whitelist.types", "**", withTestModule());

		assertThat(result.getIgnoreReason(), is("no loaded module has this id"));
		assertThat(result.getAcceptedEntries(), is(emptyIterable()));
		assertThat(result.getRejectedEntries().size(), is(0));
	}

	@Test
	public void shouldHonourConfigurationWhosePrefixIsALoadedModule() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl.filterSerializerTypeEntries(
		    "testmodule.serializer.whitelist.types", "org.openmrs.module.testmodule.Foo", withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(), contains("org.openmrs.module.testmodule.Foo"));
	}

	@Test
	public void shouldIgnoreConfigurationWhosePrefixIsOnlyAPrefixOrSuperstringOfALoadedModuleId() {
		Map<String, String> moduleIds = Map.of("foo", "org.openmrs.module.foo", "testmodule", TEST_MODULE_PACKAGE);

		assertThat(AdministrationServiceImpl
		        .filterSerializerTypeEntries("foobar.serializer.whitelist.types", "org.openmrs.module.foobar.Foo", moduleIds)
		        .getIgnoreReason(),
		    is("no loaded module has this id"));
		assertThat(AdministrationServiceImpl
		        .filterSerializerTypeEntries("fo.serializer.whitelist.types", "org.openmrs.module.fo.Foo", moduleIds)
		        .getIgnoreReason(),
		    is("no loaded module has this id"));
		assertThat(AdministrationServiceImpl.filterSerializerTypeEntries("testmodulex.serializer.whitelist.types",
		    "org.openmrs.module.testmodulex.Foo", moduleIds).getIgnoreReason(), is("no loaded module has this id"));
	}

	@Test
	public void shouldComparePrefixesCaseSensitivelyInBothDirections() {
		Map<String, String> moduleIds = Map.of("testmodule", TEST_MODULE_PACKAGE);

		AdministrationServiceImpl.SerializerTypeFilterResult upperCaseName = AdministrationServiceImpl
		        .filterSerializerTypeEntries("TESTMODULE.serializer.whitelist.types", "org.openmrs.module.testmodule.Foo",
		            moduleIds);
		assertThat(upperCaseName.getIgnoreReason(), is("no loaded module has this id"));

		Map<String, String> upperCaseModuleIds = Map.of("TESTMODULE", TEST_MODULE_PACKAGE);
		AdministrationServiceImpl.SerializerTypeFilterResult upperCaseModule = AdministrationServiceImpl
		        .filterSerializerTypeEntries("testmodule.serializer.whitelist.types", "org.openmrs.module.testmodule.Foo",
		            upperCaseModuleIds);
		assertThat(upperCaseModule.getIgnoreReason(), is("no loaded module has this id"));
	}

	@Test
	public void shouldIgnoreConfigurationWithAnEmptyPrefix() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries(".serializer.whitelist.types", "**", withTestModule());

		assertThat(result.getIgnoreReason(), is("the name has no module id before the suffix"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "a.b.serializer.whitelist.types", "module.serializer.whitelist.types",
	        "testmoduleX.serializer.whitelist.types" })
	public void shouldIgnoreConfigurationWhosePrefixIsNotAnExactLoadedModuleId(String propertyName) {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries(propertyName, "org.openmrs.module.testmodule.Foo", withTestModule());

		assertThat(result.getIgnoreReason(), is("no loaded module has this id"));
	}

	@Test
	public void shouldHonourConfigurationForAModuleIdContainingDots() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl.filterSerializerTypeEntries(
		    "serialization.xstream.serializer.whitelist.types", "org.openmrs.module.serialization.xstream.Configuration",
		    withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(), contains("org.openmrs.module.serialization.xstream.Configuration"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "**", "*", "hierarchyOf:java.lang.Object", "hierarchyOf:org.openmrs.module.testmodule.Foo",
	        "hierarchyOf: java.lang.Object", "HIERARCHYOF:java.lang.Object", "hierarchyof:java.lang.Object",
	        "java.util.PriorityQueue", "java.**", "javax.**", "jdk.**", "sun.**", "com.sun.**", "org.springframework.**",
	        "org.hibernate.**", "org.apache.**", "com.fasterxml.**", "org.codehaus.groovy.**", "org.openmrs.**",
	        "org.openmrs.module.**", "org.openmrs.module.otheremodule.**", "org.openmrs.module.testmoduleX.Foo",
	        "org.openmrs.module.testmodule", "org.openmrs.module.testmodule..Foo", "org.openmrs.module.testmodule.",
	        "org.openmrs.module.**.Foo", "some class", "org.openmrs.module.testmodule.Foo Bar",
	        "org.openmrs.module.testmodule.Bar{*", "org.openmrs.module.testmodule.Bar}*" })
	public void shouldRejectEntryFormsOutsideTheOwningModulesPackage(String entry) {
		assertThat("rule table entry: " + entry,
		    AdministrationServiceImpl.serializerTypeEntryProblem(TEST_MODULE_PACKAGE, entry), is(notNullValue()));
	}

	@ParameterizedTest
	@ValueSource(strings = { "**", "*", "hierarchyOf:java.lang.Object", "org.hibernate.**", "org.openmrs.**" })
	public void shouldNotContributeRejectedEntryFormsAndShouldReportReasons(String entry) {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries("testmodule.serializer.whitelist.types", entry, withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(), is(emptyIterable()));
		assertThat(result.getRejectedEntries(), hasKey(entry));
		assertThat(result.getRejectedEntries().get(entry), is(notNullValue()));
	}

	@ParameterizedTest
	@ValueSource(strings = { "org.openmrs.module.testmodule.SomeClass", "org.openmrs.module.testmodule.**",
	        "org.openmrs.module.testmodule.sub.*", "org.openmrs.module.testmodule.sub.SomeClass",
	        "org.openmrs.module.testmodule.Nested$Inner", "org.openmrs.module.testmodule.**bar",
	        "org.openmrs.module.testmodule.Fo*" })
	public void shouldAcceptExactClassesAndWildcardsInsideTheModulePackage(String entry) {
		// A wildcard inside the module package can only ever match types of that package.
		assertThat("entry: " + entry, AdministrationServiceImpl.serializerTypeEntryProblem(TEST_MODULE_PACKAGE, entry),
		    is(nullValue()));
	}

	@Test
	public void shouldAcceptCommaSeparatedValidEntriesWithSpaces() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl.filterSerializerTypeEntries(
		    "testmodule.serializer.whitelist.types",
		    "org.openmrs.module.testmodule.SomeClass, org.openmrs.module.testmodule.**", withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(),
		    contains("org.openmrs.module.testmodule.SomeClass", "org.openmrs.module.testmodule.**"));
	}

	@ParameterizedTest
	@CsvSource({ "'  org.openmrs.module.testmodule.SomeClass  ', org.openmrs.module.testmodule.SomeClass",
	        "'org.openmrs.module.testmodule.SomeClass,', org.openmrs.module.testmodule.SomeClass",
	        "',,org.openmrs.module.testmodule.SomeClass,,', org.openmrs.module.testmodule.SomeClass",
	        "'org.openmrs.module.testmodule.SomeClass,org.openmrs.module.testmodule.**,', 'org.openmrs.module.testmodule.SomeClass|org.openmrs.module.testmodule.**'" })
	public void shouldNormalizeWhitespaceAndBlankEntries(String propertyValue, String expectedEntries) {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries("testmodule.serializer.whitelist.types", propertyValue, withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(), contains(expectedEntries.split("\\|")));
		// Blank entries are dropped, not reported as rejections.
		assertThat(result.getRejectedEntries().size(), is(0));
	}

	@Test
	public void shouldKeepTheValidEntryAndDropTheInvalidOneInTheSameProperty() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl.filterSerializerTypeEntries(
		    "testmodule.serializer.whitelist.types", "org.openmrs.module.testmodule.SomeClass, **", withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(), contains("org.openmrs.module.testmodule.SomeClass"));
		assertThat(result.getRejectedEntries(), hasKey("**"));
	}

	@Test
	public void shouldHandleLongValuesWithManyEntriesWithoutBlockingOtherEntries() {
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < 500; i++) {
			value.append("org.openmrs.module.testmodule.Class").append(i).append(", **,");
		}
		value.append("org.openmrs.module.testmodule.Last");

		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries("testmodule.serializer.whitelist.types", value.toString(), withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries().size(), is(501));
		// Rejected entries are keyed by the entry text, so the 500 repeats collapse to one.
		assertThat(result.getRejectedEntries().size(), is(1));
		assertThat(result.getRejectedEntries(), hasKey("**"));
	}

	@Test
	public void shouldContributeNoEntriesForANullValue() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries("testmodule.serializer.whitelist.types", null, withTestModule());

		assertThat(result.getIgnoreReason(), is(nullValue()));
		assertThat(result.getAcceptedEntries(), is(emptyIterable()));
	}

	@Test
	public void shouldIgnoreANullPropertyName() {
		AdministrationServiceImpl.SerializerTypeFilterResult result = AdministrationServiceImpl
		        .filterSerializerTypeEntries(null, "**", withTestModule());

		assertThat(result.getIgnoreReason(), is("the name does not carry the expected suffix"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "hierarchyOf:java.lang.Object", "HIERARCHYOF:java.lang.Object",
	        "hierarchyOf:org.openmrs.module.testmodule.Foo", "hierarchyOf:org.openmrs.module.testmodule.*",
	        "hierarchyof:java.lang.Object" })
	public void shouldRejectHierarchyEntriesBecauseHierarchiesAreNotAccepted(String entry) {
		// The exact reason is asserted, so accepting hierarchy entries for another reason still fails.
		assertThat("entry: " + entry, AdministrationServiceImpl.serializerTypeEntryProblem(TEST_MODULE_PACKAGE, entry),
		    is("hierarchy entries are not accepted"));
	}

	@Test
	public void shouldSanitizeLineBreaksAndControlCharactersForLogging() {
		assertThat(AdministrationServiceImpl.sanitizeForLog("line1\nline2"), is("line1\\nline2"));
		assertThat(AdministrationServiceImpl.sanitizeForLog("line1\r\nline2"), is("line1\\r\\nline2"));
		assertThat(AdministrationServiceImpl.sanitizeForLog("tab\there"), is("tab?here"));
		assertThat(AdministrationServiceImpl.sanitizeForLog("entry\u0000x"), is("entry?x"));
		assertThat(AdministrationServiceImpl.sanitizeForLog("line1\u2028line2"), is("line1\\u2028line2"));
		assertThat(AdministrationServiceImpl.sanitizeForLog("line1\u2029line2"), is("line1\\u2029line2"));
	}

	@Test
	public void shouldLeavePlainValuesUnchangedWhenSanitizing() {
		assertThat(AdministrationServiceImpl.sanitizeForLog("x.serializer.whitelist.types"),
		    is("x.serializer.whitelist.types"));
		assertThat(AdministrationServiceImpl.sanitizeForLog(null), is(nullValue()));
	}
}
