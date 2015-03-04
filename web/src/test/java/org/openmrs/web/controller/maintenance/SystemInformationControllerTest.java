/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.maintenance;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.ui.ModelMap;

/**
 * Tests the {@link SystemInformationController} controller
 */
public class SystemInformationControllerTest extends BaseWebContextSensitiveTest {
	
	private ModelMap model = null;
	
	@Before
	public void before() throws Exception {
		createController();
	}
	
	/**
	 * Creates the controller with necessary parameters
	 */
	private void createController() {
		model = new ModelMap();
		SystemInformationController controller = new SystemInformationController();
		controller.showPage(model);
		//System.out.println("SystemInformationControllerTest.createController() "+model.toString());
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
	@Test
	@Verifies(value = "should add openmrs information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnOpenmrsInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo"))
		        .containsKey("SystemInfo.title.openmrsInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
	@Test
	@Verifies(value = "should add java runtime information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnUserInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo"))
		        .containsKey("SystemInfo.title.javaRuntimeEnvironmentInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
	@Test
	@Verifies(value = "should add module information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnAllJavaRuntimeInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo"))
		        .containsKey("SystemInfo.title.moduleInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
	@Test
	@Verifies(value = "should add database information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnAllDatabaseInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo"))
		        .containsKey("SystemInfo.title.dataBaseInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
	@Test
	@Verifies(value = "should add memory information attribute to the model map", method = "getMemoryInformation()")
	public void getMemoryInformation_shouldReturnMemoryInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo"))
		        .containsKey("SystemInfo.title.memoryInformation"));
	}
	
}
