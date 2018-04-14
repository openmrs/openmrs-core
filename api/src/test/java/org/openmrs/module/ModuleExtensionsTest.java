/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.test.BaseContextMockTest;

/**
 * Tests for {@link Module#getExtensions()}.
 * 
 * Look at {@link ModuleFileParser#parse()} for how a Module is constructed and initialized.
 * At first the extension tags found in config.xml are parsed and set in {@link Module#setExtensionNames(Map)}.
 */
public class ModuleExtensionsTest extends BaseContextMockTest {

	private static final String EXTENSION_POINT_ID_PATIENT_DASHBOARD = "org.openmrs.patientDashboard";
	private static final String LOGIC_MODULE_PATH = "org/openmrs/module/include/logic-0.2.omod";
	
	@Mock
	MessageSourceService messageSourceService;

	private Module module;

	@Before
	public void before() {
		module = new Module("Extension Test", "extensiontest", "org.openmrs.module.extensiontest", "", "", "0.0.1");
	}
	
	@After
	public void after() {
		// needed so other tests which rely on no ModuleClassLoaderFound
		// are not affected by tests registering one
		ModuleFactory.moduleClassLoaders = null;
	}

	@Test
	public void getExtensions_shouldNotExpandIfExtensionNamesAreNull() {
		
		module.setExtensionNames(null);

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}
	
	@Test
	public void getExtensions_shouldNotExpandIfExtensionNamesAreEmpty() {

		module.setExtensionNames(new HashMap<>());

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void getExtensions_shouldNotExpandIfNoModuleClassloaderIsFound() {

		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, AccessibleExtension.class.getName());
		module.setExtensionNames(extensionNames);
		
		ModuleFactory.moduleClassLoaders = null;

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}
	
	@Test
	public void getExtensions_shouldNotFailExpandingAnExtensionNameCausingANoClassDefinitionFoundError() {
		// Tests that when an extension is defined in config.xml and its class is found inside the module but
		// the Extension extends another Extension for which no definition can be found for.
		// In this particular case the logic module has an Extension based on one that was moved to the
		// legacyui module, which since thats not loaded cannot be found, more specifically leads to
		// java.lang.NoClassDefFoundError: org/openmrs/module/web/extension/AdministrationSectionExt

		module = new ModuleFileParser(messageSourceService).parse(
			new File(getClass().getClassLoader().getResource(LOGIC_MODULE_PATH).getPath())
		);
		ModuleClassLoader moduleClassLoader = new ModuleClassLoader(module, getClass().getClassLoader());
		ModuleFactory.getModuleClassLoaderMap().put(module, moduleClassLoader);

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}

	@Test
	public void getExtensions_shouldNotFailExpandingAClassWhichCannotBeFound() {

		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, "org.openmrs.unknown.Nonexisting.class");
		module.setExtensionNames(extensionNames);
		registerModuleClassLoader();

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}
	
	@Test
	public void getExtensions_shouldNotFailExpandingAClassWhichCannotBeInstantiated() {

		// pass in the abstract base class Extension itself which cannot be instantiated
		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, Extension.class.getName());
		module.setExtensionNames(extensionNames);
		registerModuleClassLoader();

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}
	
	@Test
	public void getExtensions_shouldNotFailExpandingAClassWhichCannotAccessed() {

		// pass in the abstract base class Extension itself which cannot be instantiated
		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, ExtensionCausingIllegalAccessException.class.getName());
		module.setExtensionNames(extensionNames);
		registerModuleClassLoader();

		assertThat(module.getExtensions(), is(equalTo(Collections.EMPTY_LIST)));
	}
	
	@Test
	public void getExtensions_shouldExpandClassNamesIntoClassInstances() {

		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, AccessibleExtension.class.getName());
		module.setExtensionNames(extensionNames);
		registerModuleClassLoader();

		List<Extension> result = module.getExtensions();
		assertThat(result.size(), is(1));
		Extension extension = result.get(0);
		assertThat(extension, is(instanceOf(AccessibleExtension.class)));
		assertThat(extension.getPointId(), is(EXTENSION_POINT_ID_PATIENT_DASHBOARD));
		assertThat(extension.getModuleId(), is(module.getModuleId()));
	}
	
	@Test
	public void getExtensions_shouldNotExpandAgainIfClassNamesMatch() {

		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, AccessibleExtension.class.getName());
		module.setExtensionNames(extensionNames);
		registerModuleClassLoader();

		List<Extension> result = module.getExtensions();
		assertThat(result.size(), is(1));
		Extension extension = result.get(0);

		result = module.getExtensions();
		assertThat(result.size(), is(1));
		assertThat(result.get(0), is(sameInstance(extension)));
	}
	
	@Test
	public void getExtensions_shouldExpandAgainIfExtensionNamesNowHaveADifferentClassOnSameExtensionPoint() {

		HashMap<String, String> extensionNames = new HashMap<>();
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, AccessibleExtension.class.getName());
		module.setExtensionNames(extensionNames);
		registerModuleClassLoader();

		List<Extension> result = module.getExtensions();
		assertThat(result.size(), is(1));
		
		extensionNames.put(EXTENSION_POINT_ID_PATIENT_DASHBOARD, AnotherAccessibleExtension.class.getName());

		result = module.getExtensions();
		assertThat(result.size(), is(1));
		Extension extension = result.get(0);
		assertThat(extension, is(instanceOf(AnotherAccessibleExtension.class)));
		assertThat(extension.getPointId(), is(EXTENSION_POINT_ID_PATIENT_DASHBOARD));
		assertThat(extension.getModuleId(), is(module.getModuleId()));
	}

	private void registerModuleClassLoader() {
		// needed to prevent NullPointerException's in the ModuleClassLoader constructor
		// TODO: we should aim to properly initialize Module after construction so a module without
		// required modules can be safely used as module.
		module.setRequiredModulesMap(new HashMap<>());
		module.setAwareOfModulesMap(new HashMap<>());
		ModuleClassLoader moduleClassLoader = new ModuleClassLoader(module, new ArrayList<>(), getClass().getClassLoader());
		ModuleFactory.getModuleClassLoaderMap().put(module, moduleClassLoader);
	}

	static class AccessibleExtension extends Extension {
		@Override
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
	}
	
	static class AnotherAccessibleExtension extends Extension {
		@Override
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
	}
	
	static class ExtensionCausingIllegalAccessException extends Extension {
		@Override
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
		private ExtensionCausingIllegalAccessException() {
		}
	}
}
