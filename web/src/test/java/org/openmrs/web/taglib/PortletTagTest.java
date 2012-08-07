/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.taglib;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.Verifies;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Tests the {@link PortletTag}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ModuleFactory.class)
public class PortletTagTest {
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "return the correct url for a core portlet", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldReturnTheCorrectUrlForACorePortlet() throws Exception {
		String portletUrl = "test.portlet";
		String moduleId = null;
		
		// Instantiate the portlet and generate the url
		PortletTag portlet = new PortletTag();
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		
		// Verify the portlet url
		assertEquals("/portlets/" + portletUrl, result);
	}
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "return the correct url for a module portlet", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldReturnTheCorrectUrlForAModulePortlet() throws Exception {
		String portletUrl = "test.portlet";
		String moduleId = "moduleId";
		
		// Setup the mocking for the ModuleFactory
		mockStatic(ModuleFactory.class);
		when(ModuleFactory.getModuleById(moduleId)).thenReturn(new Module(moduleId));
		
		// Instantiate the portlet and get the module url
		PortletTag portlet = new PortletTag();
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		
		// Verify the portlet url
		assertEquals("/module/" + moduleId + "/portlets/" + portletUrl, result);
	}
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "replace period in a module id with a forward slash when building a module portlet url", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldReplacePeriodInAModuleIdWithAForwardSlashWhenBuildingAModulePortletUrl() {
		String portletUrl = "test.portlet";
		String moduleId = "module.id";
		
		mockStatic(ModuleFactory.class);
		when(ModuleFactory.getModuleById(moduleId)).thenReturn(new Module(moduleId));
		
		PortletTag portlet = new PortletTag();
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		
		assertEquals("/module/" + moduleId.replace('.', '/') + "/portlets/" + portletUrl, result);
	}
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "not update the moduleId field for a module portlet", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldNotUpdateTheModuleIdFieldForAModulePortlet() {
		String portletUrl = "test.portlet";
		String moduleId = "module.id";
		
		mockStatic(ModuleFactory.class);
		when(ModuleFactory.getModuleById(moduleId)).thenReturn(new Module(moduleId));
		
		PortletTag portlet = new PortletTag();
		portlet.setModuleId(moduleId);
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		
		assertEquals("/module/" + moduleId.replace('.', '/') + "/portlets/" + portletUrl, result);
		assertEquals(moduleId, portlet.getModuleId());
	}
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "return a core portlet url when the specified module cannot be found", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldReturnACorePortletUrlWhenTheSpecifiedModuleCannotBeFound() {
		String portletUrl = "test.portlet";
		String moduleId = "moduleId";
		
		// Setup the mocking for ModuleFactory to return null to test when the module is not found
		mockStatic(ModuleFactory.class);
		when(ModuleFactory.getModuleById(moduleId)).thenReturn(null);
		
		PortletTag portlet = new PortletTag();
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		
		assertEquals("/portlets/" + portletUrl, result);
	}
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "append .portlet to the url if not specified", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldAppendDotPortletToTheUrlIfNotSpecified() {
		String portletUrl = "test";
		String moduleId = null;
		
		PortletTag portlet = new PortletTag();
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		
		assertEquals("/portlets/" + portletUrl + ".portlet", result);
	}
	
	/**
	 * @see org.openmrs.web.taglib.PortletTag#generatePortletUrl(String, String)
	 */
	@Test
	@Verifies(value = "treat both an empty and null module id as core portlets", method = "generatePortletUrl(String, String)")
	public void getModulePortletUrl_shouldTreatBothAnEmptyAndNullModuleIdAsCorePortlets() {
		String portletUrl = "test.portlet";
		String moduleId = null;
		
		PortletTag portlet = new PortletTag();
		
		// Test with a null module id
		String result = portlet.generatePortletUrl(portletUrl, moduleId);
		assertEquals("/portlets/" + portletUrl, result);
		
		// Test with an empty module id
		moduleId = "";
		result = portlet.generatePortletUrl(portletUrl, moduleId);
		assertEquals("/portlets/" + portletUrl, result);
	}
}
