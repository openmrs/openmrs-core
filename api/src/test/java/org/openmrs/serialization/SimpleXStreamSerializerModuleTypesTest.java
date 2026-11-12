/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.serialization.ProbeTarget;
import org.openmrs.test.serialization.StaticInitRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the module scoping rules of serializer type configuration, going through
 * {@link AdministrationService#getSerializerWhitelistTypes()} and {@link SimpleXStreamSerializer}.
 * The stand-in classes ({@code ProbeTarget}, {@code StaticInitProbe}) are harmless; the probe's
 * static initialiser flag is read through {@link StaticInitRecorder}.
 */
public class SimpleXStreamSerializerModuleTypesTest extends BaseContextSensitiveTest {

	private static final String OUTSIDE_XML = "<org.openmrs.test.serialization.ProbeTarget><name>probe</name>"
	        + "</org.openmrs.test.serialization.ProbeTarget>";

	private static final String MODULE_XML = "<org.openmrs.module.testmodule.ProbeTarget><name>probe</name>"
	        + "</org.openmrs.module.testmodule.ProbeTarget>";

	private static final String TEST_MODULE_ID = "testmodule";

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	public void loadTestModule() {
		Module testModule = new Module("Test Module", TEST_MODULE_ID, "org.openmrs.module." + TEST_MODULE_ID, "OpenMRS",
		        "module used by tests", "1.0", "1");
		ModuleFactory.loadModule(testModule, true);
		clearSerializerCaches();
	}

	@AfterEach
	public void unloadTestModule() {
		ModuleFactory.getLoadedModulesMap().remove(TEST_MODULE_ID);
	}

	private void clearSerializerCaches() {
		clearCacheIfPresent("serializerWhiteListTypes");
		clearCacheIfPresent("globalProperties");
	}

	/**
	 * Clears the named cache when this runtime defines it; a cache that is not defined is skipped.
	 */
	private void clearCacheIfPresent(String name) {
		if (cacheManager.getCacheNames().contains(name)) {
			cacheManager.getCache(name).clear();
		}
	}

	private AdministrationDAO administrationDao() {
		return Context.getRegisteredComponent("adminDAO", AdministrationDAO.class);
	}

	/**
	 * Returns the serializer configuration service as a plain object with only the data access wiring
	 * set, so reads of the type list do not go through the cache. This keeps the tests independent of
	 * how the runtime cache is configured and of cache expiry timing.
	 */
	private AdministrationService rawAdministrationService() {
		org.openmrs.api.impl.AdministrationServiceImpl impl = new org.openmrs.api.impl.AdministrationServiceImpl();
		impl.setAdministrationDAO(administrationDao());
		return impl;
	}

	private SimpleXStreamSerializer freshSerializer() {
		return new SimpleXStreamSerializer(rawAdministrationService());
	}

	@Test
	public void shouldRefuseOutsideTypesAfterAFreshSerializerBuildForNonModuleAndModulePrefixedBroadConfiguration() {
		// Saved through the DAO directly, as older data could contain such values.
		administrationDao().saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));
		administrationDao().saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types", "**"));

		SimpleXStreamSerializer serializer = freshSerializer();
		assertThrows(SerializationException.class, () -> serializer.deserialize(OUTSIDE_XML, ProbeTarget.class));
	}

	@Test
	public void shouldAcceptALegitimateModuleEntryForTheModulesOwnClassAfterAFreshSerializerBuild()
	        throws SerializationException {
		administrationDao().saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types",
		        "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));

		SimpleXStreamSerializer serializer = freshSerializer();
		org.openmrs.module.testmodule.ProbeTarget target = serializer.deserialize(MODULE_XML,
		    org.openmrs.module.testmodule.ProbeTarget.class);
		assertEquals("probe", target.getName());
	}

	@Test
	public void shouldIgnorePropertiesWithoutAMatchingLoadedModuleInGetSerializerWhitelistTypes() {
		administrationDao().saveGlobalProperty(
		    new GlobalProperty("x.serializer.whitelist.types", "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));
		administrationDao().saveGlobalProperty(new GlobalProperty("testmodul.serializer.whitelist.types",
		        "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));

		List<String> types = rawAdministrationService().getSerializerWhitelistTypes();
		assertThat(types, not(hasItem("org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget")));
	}

	@Test
	public void shouldHonourAMatchingLoadedModuleInGetSerializerWhitelistTypes() {
		administrationDao().saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types",
		        "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));

		List<String> types = rawAdministrationService().getSerializerWhitelistTypes();
		assertThat(types, hasItem("org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));
	}

	@Test
	public void shouldRefuseTypesBehindRejectedEntryFormsAfterAFreshSerializerBuild() {
		administrationDao().saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types",
		        "hierarchyOf:org.openmrs.test.serialization.ProbeTarget"));
		administrationDao().saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));

		SimpleXStreamSerializer serializer = freshSerializer();
		assertThrows(SerializationException.class, () -> serializer.deserialize(OUTSIDE_XML, ProbeTarget.class));
	}

	@Test
	public void shouldNotResolveTheClassesNamedByRejectedEntries() {
		administrationDao().saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types",
		        "hierarchyOf:org.openmrs.test.serialization.StaticInitProbe"));
		administrationDao().saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types",
		        "hierarchyOf:org.openmrs.test.serialization.StaticInitProbe"));

		SimpleXStreamSerializer serializer = freshSerializer();
		assertThrows(SerializationException.class, () -> serializer.deserialize(OUTSIDE_XML, ProbeTarget.class));

		assertThat("the static initialiser of a class named by a rejected entry must not have run",
		    StaticInitRecorder.probeInitialised, is(false));
	}

	@Test
	public void shouldReturnBuiltInDefaultsWhenNoPropertyQualifies() {
		List<String> types = Context.getAdministrationService().getSerializerWhitelistTypes();

		assertThat(types,
		    containsInAnyOrder("hierarchyOf:org.openmrs.OpenmrsObject", "hierarchyOf:org.openmrs.OpenmrsMetadata",
		        "hierarchyOf:org.openmrs.OpenmrsData", "hierarchyOf:org.openmrs.customdatatype.CustomDatatype",
		        "hierarchyOf:org.openmrs.customdatatype.SingleCustomValue",
		        "hierarchyOf:org.openmrs.customdatatype.CustomValueDescriptor",
		        "hierarchyOf:org.openmrs.customdatatype.Customizable", "hierarchyOf:org.openmrs.layout.LayoutTemplate",
		        "hierarchyOf:org.openmrs.layout.LayoutSupport", "hierarchyOf:org.openmrs.obs.ComplexData",
		        "hierarchyOf:org.openmrs.messagesource.PresentationMessage",
		        "hierarchyOf:org.openmrs.person.PersonMergeLogData"));
	}

	@Test
	public void shouldReturnBuiltInDefaultsWhenOnlyRejectedPropertiesExist() {
		administrationDao().saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));

		List<String> types = Context.getAdministrationService().getSerializerWhitelistTypes();
		assertEquals(12, types.size());
	}

	@Test
	public void shouldApplyTheFilteredConfigurationToAModuleBuiltInstance() throws Exception {
		administrationDao().saveGlobalProperty(new GlobalProperty("x.serializer.whitelist.types", "**"));
		administrationDao().saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types",
		        "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));

		clearSerializerCaches();

		// Add-on modules build their instances the same way: a new XStream through the shared setup.
		XStream moduleXStream = new XStream();
		Method initXStream = SimpleXStreamSerializer.class.getMethod("initXStream", XStream.class);
		initXStream.invoke(new SimpleXStreamSerializer(rawAdministrationService()), moduleXStream);

		assertEquals("probe", ((org.openmrs.module.testmodule.ProbeTarget) moduleXStream.fromXML(MODULE_XML)).getName());
		assertThrows(XStreamException.class, () -> moduleXStream.fromXML(OUTSIDE_XML));
	}

	@Test
	public void shouldAllowOnlyBuiltInDefaultsForAnInstanceWithoutTheAdministrationService() throws SerializationException {
		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer();
		assertThrows(SerializationException.class, () -> serializer.deserialize(OUTSIDE_XML, ProbeTarget.class));
	}

	@Test
	public void shouldFailNamingTheEntryWhenSavingABroadEntryForAModulePrefixedProperty() {
		AdministrationService adminService = Context.getAdministrationService();
		APIException exception = assertThrows(APIException.class,
		    () -> adminService.saveGlobalProperty(new GlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types", "**")));
		assertThat("the failure names the offending property", exception.getMessage(),
		    notNullValueContains(TEST_MODULE_ID + ".serializer.whitelist.types"));
	}

	@Test
	public void shouldSucceedSavingAValidModuleOwnedEntry() {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(
		        TEST_MODULE_ID + ".serializer.whitelist.types", "org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget"));

		assertEquals("org.openmrs.module." + TEST_MODULE_ID + ".ProbeTarget",
		    Context.getAdministrationService().getGlobalProperty(TEST_MODULE_ID + ".serializer.whitelist.types"));
	}

	@Test
	public void shouldLeaveUnrelatedGlobalPropertiesUnaffected() {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty("unrelated.property", "value"));

		assertEquals("value", Context.getAdministrationService().getGlobalProperty("unrelated.property"));
	}

	@Test
	public void shouldSaveAValidEntryForAModuleThatIsNotLoadedYet() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty("futuremodule.serializer.whitelist.types", "org.openmrs.module.futuremodule.SomeClass"));

		assertEquals("org.openmrs.module.futuremodule.SomeClass",
		    Context.getAdministrationService().getGlobalProperty("futuremodule.serializer.whitelist.types"));
	}

	@Test
	public void shouldRejectTheBadPropertyAndKeepEarlierOnesInABatchSave() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty("beforebatch.serializer.whitelist.types", "org.openmrs.module.beforebatch.SomeClass"));

		List<GlobalProperty> batch = Arrays.asList(
		    new GlobalProperty("testmodule.serializer.whitelist.types", "org.openmrs.module.testmodule.ProbeTarget"),
		    new GlobalProperty("x.serializer.whitelist.types", "**"),
		    new GlobalProperty("afterbatch.serializer.whitelist.types", "org.openmrs.module.afterbatch.SomeClass"));

		assertThrows(APIException.class, () -> Context.getAdministrationService().saveGlobalProperties(batch));
		// Properties saved before the failing one persist; later ones are not reached.
		AdministrationDAO administrationDao = administrationDao();
		assertNotNull(administrationDao.getGlobalPropertyObject("beforebatch.serializer.whitelist.types"));
		assertNull(administrationDao.getGlobalPropertyObject("x.serializer.whitelist.types"));
		assertNull(administrationDao.getGlobalPropertyObject("afterbatch.serializer.whitelist.types"));
	}

	@Test
	public void shouldSaveABatchOfValidProperties() {
		List<GlobalProperty> batch = Arrays.asList(
		    new GlobalProperty("testmodule.serializer.whitelist.types", "org.openmrs.module.testmodule.ProbeTarget"),
		    new GlobalProperty("futuremodule.serializer.whitelist.types", "org.openmrs.module.futuremodule.SomeClass"));

		Context.getAdministrationService().saveGlobalProperties(batch);

		AdministrationDAO administrationDao = administrationDao();
		assertNotNull(administrationDao.getGlobalProperty("testmodule.serializer.whitelist.types"));
		assertNotNull(administrationDao.getGlobalProperty("futuremodule.serializer.whitelist.types"));
	}

	@Test
	public void shouldHonourEntriesForAModuleIdThatIsAPrefixOfAnotherModulesPackage() {
		// A shorter module id's wildcard also matches a longer dotted id's package; installing
		// such a pair requires module management, so the overlap is accepted and pinned.
		try {
			Module shorter = new Module("Serialization", "serialization", "org.openmrs.module.serialization", "OpenMRS",
			        "module used by tests", "1.0", "1");
			Module longer = new Module("Serialization XStream", "serialization.xstream",
			        "org.openmrs.module.serialization.xstream", "OpenMRS", "module used by tests", "1.0", "1");
			ModuleFactory.loadModule(shorter, true);
			ModuleFactory.loadModule(longer, true);
			administrationDao().saveGlobalProperty(
			    new GlobalProperty("serialization.serializer.whitelist.types", "org.openmrs.module.serialization.**"));

			List<String> types = rawAdministrationService().getSerializerWhitelistTypes();
			assertThat(types, hasItem("org.openmrs.module.serialization.**"));
		} finally {
			ModuleFactory.getLoadedModulesMap().remove("serialization");
			ModuleFactory.getLoadedModulesMap().remove("serialization.xstream");
		}
	}

	@Test
	public void shouldAcceptShippedModulesDefaultValuesForTheirModuleIds() {
		try {
			Module reporting = new Module("Reporting", "reporting", "org.openmrs.module.reporting", "OpenMRS",
			        "module used by tests", "1.0", "1");
			Module metadatasharing = new Module("Metadata Sharing", "metadatasharing", "org.openmrs.module.metadatasharing",
			        "OpenMRS", "module used by tests", "1.0", "1");
			Module htmlformentry = new Module("HTML Form Entry", "htmlformentry", "org.openmrs.module.htmlformentry",
			        "OpenMRS", "module used by tests", "1.0", "1");
			ModuleFactory.loadModule(reporting, true);
			ModuleFactory.loadModule(metadatasharing, true);
			ModuleFactory.loadModule(htmlformentry, true);

			AdministrationDAO administrationDao = administrationDao();
			administrationDao.saveGlobalProperty(
			    new GlobalProperty("reporting.serializer.whitelist.types", "org.openmrs.module.reporting.**"));
			administrationDao.saveGlobalProperty(new GlobalProperty("metadatasharing.serializer.whitelist.types",
			        "org.openmrs.module.metadatasharing.ImportConfig"));
			administrationDao.saveGlobalProperty(new GlobalProperty("htmlformentry.serializer.whitelist.types",
			        "org.openmrs.module.htmlformentry.SerializableFormObject"));

			List<String> types = Context.getAdministrationService().getSerializerWhitelistTypes();
			assertThat(types, hasItem("org.openmrs.module.reporting.**"));
			assertThat(types, hasItem("org.openmrs.module.metadatasharing.ImportConfig"));
			assertThat(types, hasItem("org.openmrs.module.htmlformentry.SerializableFormObject"));
		} finally {
			ModuleFactory.getLoadedModulesMap().remove("reporting");
			ModuleFactory.getLoadedModulesMap().remove("metadatasharing");
			ModuleFactory.getLoadedModulesMap().remove("htmlformentry");
		}
	}

	private static org.hamcrest.Matcher<String> notNullValueContains(String expectedPart) {
		return new org.hamcrest.BaseMatcher<String>() {

			@Override
			public boolean matches(Object item) {
				return item instanceof String && ((String) item).contains(expectedPart);
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText("a message containing ").appendValue(expectedPart);
			}
		};
	}
}
