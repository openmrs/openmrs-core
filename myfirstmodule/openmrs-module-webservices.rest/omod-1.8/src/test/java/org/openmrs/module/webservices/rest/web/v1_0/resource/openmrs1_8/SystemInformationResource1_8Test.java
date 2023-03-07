/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.openmrs.api.AdministrationService;
import java.util.Map;
import org.springframework.mock.web.MockHttpServletRequest;

public class SystemInformationResource1_8Test extends BaseModuleWebContextSensitiveTest {
	
	private AdministrationService administrationService;
	
	@Autowired
	private MainResourceController mainResourceController;
	
	public String getURI() {
		return "systeminformation";
	}
	
	@Test
	public void testGetAll() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleObject result = mainResourceController.get(getURI(), request, response);
		
		Map<String, Map<String, String>> systemInfo = result.get("systemInfo");
		
		// Check openmrsInformation
		Map<String, String> openmrsInformation = systemInfo.get("SystemInfo.title.openmrsInformation");
		Assert.assertTrue(systemInfo.containsKey("SystemInfo.title.openmrsInformation"));
		// Check openmrsInformation Property
		Assert.assertTrue(openmrsInformation.containsKey("SystemInfo.OpenMRSInstallation.systemDate"));
		Assert.assertTrue(openmrsInformation.containsKey("SystemInfo.OpenMRSInstallation.systemTime"));
		Assert.assertTrue(openmrsInformation.containsKey("SystemInfo.OpenMRSInstallation.openmrsVersion"));
		
		// Check javaRuntimeEnvironmentInformation
		Map<String, String> javRuntime = systemInfo.get("SystemInfo.title.javaRuntimeEnvironmentInformation");
		Assert.assertTrue(systemInfo.containsKey("SystemInfo.title.javaRuntimeEnvironmentInformation"));
		// Check javaRuntimeEnvironmentInformation Property
		Assert.assertTrue(javRuntime.containsKey("SystemInfo.JavaRuntimeEnv.operatingSystem"));
		Assert.assertTrue(javRuntime.containsKey("SystemInfo.JavaRuntimeEnv.operatingSystemArch"));
		Assert.assertTrue(javRuntime.containsKey("SystemInfo.JavaRuntimeEnv.operatingSystemVersion"));
		Assert.assertTrue(javRuntime.containsKey("SystemInfo.JavaRuntimeEnv.javaVersion"));
		Assert.assertTrue(javRuntime.containsKey("SystemInfo.JavaRuntimeEnv.javaVendor"));
		
		// Check memoryInformation
		Map<String, String> memoryInformation = systemInfo.get("SystemInfo.title.memoryInformation");
		Assert.assertTrue(systemInfo.containsKey("SystemInfo.title.memoryInformation"));
		// Check memoryInformation Property
		Assert.assertTrue(memoryInformation.containsKey("SystemInfo.Memory.totalMemory"));
		Assert.assertTrue(memoryInformation.containsKey("SystemInfo.Memory.freeMemory"));
		Assert.assertTrue(memoryInformation.containsKey("SystemInfo.Memory.maximumHeapSize"));
		
		// Check dataBaseInformation
		Map<String, String> dataBaseInformation = systemInfo.get("SystemInfo.title.dataBaseInformation");
		Assert.assertTrue(systemInfo.containsKey("SystemInfo.title.dataBaseInformation"));
		// Check dataBaseInformation Property
		Assert.assertTrue(dataBaseInformation.containsKey("SystemInfo.Database.name"));
		Assert.assertTrue(dataBaseInformation.containsKey("SystemInfo.Database.connectionURL"));
		Assert.assertTrue(dataBaseInformation.containsKey("SystemInfo.Database.userName"));
		Assert.assertTrue(dataBaseInformation.containsKey("SystemInfo.Database.driver"));
		Assert.assertTrue(dataBaseInformation.containsKey("SystemInfo.Database.dialect"));
		
		// Check moduleInformation
		Map<String, String> moduleInformation = systemInfo.get("SystemInfo.title.moduleInformation");
		Assert.assertTrue(systemInfo.containsKey("SystemInfo.title.moduleInformation"));
		// Check moduleInformation Property
		Assert.assertTrue(moduleInformation.containsKey("SystemInfo.Module.repositoryPath"));
	}
}
