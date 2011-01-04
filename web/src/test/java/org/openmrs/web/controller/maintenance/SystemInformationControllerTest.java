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
	private void createController(){
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
	public void showPage_shouldReturnOpenmrsInformation(){
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo")).containsKey("SystemInfo.title.openmrsInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
    @Test
	@Verifies(value = "should add java runtime information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnUserInformation(){
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo")).containsKey("SystemInfo.title.javaRuntimeEnvironmentInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
    @Test
	@Verifies(value = "should add module information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnAllJavaRuntimeInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo")).containsKey("SystemInfo.title.moduleInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
    @Test
	@Verifies(value = "should add database information attribute to the model map", method = "showPage()")
	public void showPage_shouldReturnAllDatabaseInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo")).containsKey("SystemInfo.title.dataBaseInformation"));
	}
	
	/**
	 * @see {@link SystemInformationController#showPage(ModelMap)}
	 */
    @Test
	@Verifies(value = "should add memory information attribute to the model map", method = "getMemoryInformation()")
	public void getMemoryInformation_shouldReturnMemoryInformation() {
		Assert.assertTrue(((Map<String, Map<String, String>>) model.get("systemInfo")).containsKey("SystemInfo.title.memoryInformation"));
	}
	
}
