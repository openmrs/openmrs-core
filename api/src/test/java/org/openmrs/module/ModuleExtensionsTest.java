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

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.mockito.Mockito.never;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * Tests Module Methods
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(Module.class)
public class ModuleExtensionsTest {

	private Module mockModule;

	@Before
	public void before() throws Exception {
		mockModule = spy(new Module("mockmodule"));
	}

	/*
	 * @verifies not expand extensionNames if extensionNames is null
	 * @see Module#getExtensions()
	 */
	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfExtensionNamesIsNull() throws Exception {
		ArrayList<Extension> extensions = new ArrayList<Extension>();

		Extension mockExtension = new MockExtension();
		extensions.add(mockExtension);

		mockModule.setExtensions(extensions);
		mockModule.setExtensionNames(null);
		ArrayList<Extension> ret = new ArrayList<Extension>(mockModule.getExtensions());

		verifyPrivate(mockModule, never()).invoke("expandExtensionNames");
	}

	/*
         * @verifies not expand extensionNames if extensionNames is empty
	 * @see Module#getExtensions()
	 */
	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfExtensionNamesIsEmpty() throws Exception {
		ArrayList<Extension> extensions = new ArrayList<Extension>();

		Extension mockExtension = new MockExtension();
		extensions.add(mockExtension);

		mockModule.setExtensions(extensions);
		mockModule.setExtensionNames(new IdentityHashMap<String, String>());
		ArrayList<Extension> ret = new ArrayList<Extension>(mockModule.getExtensions());

		verifyPrivate(mockModule, never()).invoke("expandExtensionNames");
	}

	/*
         * @verifies not expand extensionNames if extensions matches extensionNames
	 * @see Module#getExtensions()
	 */
	@Test
	public void getExtensions_shouldNotExpandExtensionNamesIfExtensionsMatchesExtensionNames() throws Exception {
		ArrayList<Extension> extensions = new ArrayList<Extension>();
		IdentityHashMap<String, String> extensionNames = new IdentityHashMap<String, String>();

		Extension mockExtension = new MockExtension();
		mockExtension.setPointId("1");
		extensions.add(mockExtension);
		extensionNames.put("1", mockExtension.getClass().getName());

		mockModule.setExtensions(extensions);
		mockModule.setExtensionNames(extensionNames);
		ArrayList<Extension> ret = new ArrayList<Extension>(mockModule.getExtensions());

		verifyPrivate(mockModule, never()).invoke("expandExtensionNames");
	}

	/*
         * @verifies expand extensionNames if extensions does not match extensionNames
	 * @see Module#getExtensions()
	 */
	@Test
	public void getExtensions_shouldExpandExtensionNamesIfExtensionsDoesNotMatchExtensionNames() throws Exception {
		ArrayList<Extension> extensions = new ArrayList<Extension>();
		IdentityHashMap<String, String> extensionNames = new IdentityHashMap<String, String>();

		Extension mockExtension = new MockExtension();
		mockExtension.setPointId("1");
		extensions.add(mockExtension);
		extensionNames.put("2", mockExtension.getClass().getName());

		mockModule.setExtensions(extensions);
		mockModule.setExtensionNames(extensionNames);
		ArrayList<Extension> ret = new ArrayList<Extension>(mockModule.getExtensions());

		verifyPrivate(mockModule).invoke("expandExtensionNames");
	}

	private class MockExtension extends Extension {
		public Extension.MEDIA_TYPE getMediaType() {
			return null;
		}
	}
}
