/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
