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
package org.openmrs.module.web;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.Verifies;

public class WebModuleUtilTest {
	
	/**
	 * @see {@link WebModuleUtil#transformModuleViewName(String)}
	 */
	@Test
	@Verifies(value = "should return the correct view name for a module url using a module id", method = "transformModuleViewName(String)")
	public void transformModuleViewName_shouldReturnTheCorrectViewNameForAModuleUrlUsingAModuleId() throws Exception {
		Module module = addTestModule("useless module", "useless.demo", "org.openmrs.module.");
		Assert.assertEquals("module/" + module.getModulePackageAsPath() + "/manage", WebModuleUtil
		        .transformModuleViewName("module/" + module.getModuleId() + "/manage"));
	}
	
	/**
	 * @see {@link WebModuleUtil#transformModuleViewName(String)}
	 */
	@Test
	@Verifies(value = "should return the correct view name for a module url using a package name", method = "transformModuleViewName(String)")
	public void transformModuleViewName_shouldReturnTheCorrectViewNameForAModuleUrlUsingAPackageName() throws Exception {
		Module module = addTestModule("useless module", "useless.demo", "org.openmrs.module.");
		Assert.assertEquals("module/" + module.getModulePackageAsPath() + "/manage", WebModuleUtil
		        .transformModuleViewName("module/" + module.getPackageName() + "/manage"));
	}
	
	/**
	 * @see {@link WebModuleUtil#transformModuleViewName(String)}
	 */
	@Test
	@Verifies(value = "should ignore if there are multiple modules with same moduleId and the url has moduleId", method = "transformModuleViewName(String)")
	public void transformModuleViewName_shouldIgnoreIfThereAreMultipleModulesWithSameModuleIdAndTheUrlHasModuleId()
	        throws Exception {
		final String moduleId = "useless.demo";
		Module module = addTestModule("useless module", moduleId, "org.openmrs.module.");
		module.setModuleId(moduleId);
		module.setPackageName("org.openmrs.module." + module.getModuleId());
		ModuleFactory.getStartedModulesMap().put(module.getPackageName(), module);
		
		addTestModule("duplicate useless module", moduleId, "org.openmrs.module.duplicate.");
		String viewName = "module/" + moduleId + "/manage";
		Assert.assertEquals(viewName, WebModuleUtil.transformModuleViewName(viewName));
	}
	
	public static Module addTestModule(String name, String moduleId, String packagePrefix) {
		Module module = new Module(name);
		module.setModuleId(moduleId);
		module.setPackageName(packagePrefix + module.getModuleId());
		ModuleFactory.getStartedModulesMap().put(module.getPackageName(), module);
		
		return module;
	}
}
