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
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Module}.
 */
public class ModuleExtensionsTest {

	private Module module;

	@Before
	public void before() {
		module = new Module("mockmodule");
	}

	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfExtensionNamesIsNull() {
		
		Extension extension = new MockExtension();
		List<Extension> extensions = new ArrayList<>();
		extensions.add(extension);
		module.setExtensions(extensions);
		module.setExtensionNames(null);
		
		List<Extension> result = module.getExtensions();
		
		assertThat(result, is(extensions));
	}
	
	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfExtensionNamesIsEmpty() {
		
		Extension extension = new MockExtension();
		List<Extension> extensions = new ArrayList<>();
		extensions.add(extension);
		module.setExtensions(extensions);
		module.setExtensionNames(new IdentityHashMap<>());

		List<Extension> result = module.getExtensions();
		
		assertThat(result, is(extensions));
	}

	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfExtensionsMatchesExtensionNames() {

		Extension extension = new MockExtension();
		extension.setPointId("1");
		List<Extension> extensions = new ArrayList<>();
		extensions.add(extension);
		
		IdentityHashMap<String, String> extensionNames = new IdentityHashMap<>();
		extensionNames.put("1", extension.getClass().getName());

		module.setExtensions(extensions);
		module.setExtensionNames(extensionNames);

		List<Extension> result = module.getExtensions();
		
		assertThat(result, is(extensions));
	}
	
	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfNoModuleClassloaderIsFound() {

		Extension extension = new MockExtension();
		extension.setPointId("1");
		ArrayList<Extension> extensions = new ArrayList<>();
		extensions.add(extension);
		
		IdentityHashMap<String, String> extensionNames = new IdentityHashMap<>();
		extensionNames.put("2", extension.getClass().getName());

		module.setExtensions(extensions);
		module.setExtensionNames(extensionNames);

		List<Extension> result = module.getExtensions();
		
		assertThat(result, is(extensions));
	}
	
	private class MockExtension extends Extension {
		@Override
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
	}
}
