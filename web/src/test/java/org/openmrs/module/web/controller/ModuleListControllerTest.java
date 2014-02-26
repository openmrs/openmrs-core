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
package org.openmrs.module.web.controller;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.module.Module;

public class ModuleListControllerTest {
	
	/**
	 * @see ModuleListController#sortStartupOrder(List)
	 * @verifies sort modules correctly
	 */
	@Test
	public void sortStartupOrder_shouldSortModulesCorrectly() throws Exception {
		// simulate the particular case I'm seeing in TRUNK-3384
		Module appframework = mockModule("appframework", "org.openmrs.module.uiframework,org.openmrs.module.uilibrary");
		Module kenyaemr = mockModule(
		    "kenyaemr",
		    "org.openmrs.module.uiframework,org.openmrs.module.uilibrary,org.openmrs.module.appframework,org.openmrs.module.metadatasharing,org.openmrs.module.htmlformentry");
		Module uilibrary = mockModule("uilibrary", "org.openmrs.module.uiframework");
		
		List<Module> list = new ModuleListController().sortStartupOrder(Arrays.asList(appframework, kenyaemr, kenyaemr,
		    uilibrary));
		Assert.assertSame(uilibrary, list.get(0));
		Assert.assertSame(appframework, list.get(1));
		Assert.assertSame(kenyaemr, list.get(2));
		Assert.assertSame(kenyaemr, list.get(3));
	}
	
	/**
	 * @param moduleId
	 * @param requiredModules
	 * @return
	 */
	private Module mockModule(String moduleId, String requiredModules) {
		Module ret = new Module(moduleId);
		ret.setModuleId(moduleId);
		ret.setPackageName("org.openmrs.module." + moduleId);
		ret.setRequiredModules(Arrays.asList(requiredModules.split(",")));
		return ret;
	}
}
