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
package org.openmrs.module;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.StartModule;
import org.openmrs.test.Verifies;

/**
 * Contains tests for the {@link ModuleFactory} class
 * 
 */
@SkipBaseSetup
@StartModule( { "org/openmrs/module/include/newmoduleiddemo-1.0-SNAPSHOT.omod",
        "org/openmrs/module/include/newmoduleiddemo-1.1-SNAPSHOT.omod", "org/openmrs/module/include/dssmodule-1.44.omod" })
public class ModuleFactoryTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ModuleFactory#getModuleById(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should get a loaded module with a matching module id", method = "getModuleById(String)")
	public void getModuleById_shouldGetALoadedModuleWithAMatchingModuleId() throws Exception {
		final String moduleId = "dssmodule";
		Module mod = ModuleFactory.getModuleById(moduleId);
		Assert.assertNotNull(mod);
		Assert.assertEquals(moduleId, mod.getModuleId());
	}
	
	/**
	 * @see {@link ModuleFactory#getStartedModuleById(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should get a started module with a matching module id", method = "getStartedModuleById(String)")
	public void getStartedModuleById_shouldGetAStartedModuleWithAMatchingModuleId() throws Exception {
		final String moduleId = "dssmodule";
		Module mod = ModuleFactory.getStartedModuleById(moduleId);
		Assert.assertNotNull(mod);
		Assert.assertEquals(moduleId, mod.getModuleId());
	}
	
	/**
	 * @see {@link ModuleFactory#getModuleById(String)}
	 * 
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should fail if there are multiple loaded modules matching the module id", method = "getModuleById(String)")
	public void getModuleById_shouldFailIfThereAreMultipleLoadedModulesMatchingTheModuleId() throws Exception {
		ModuleFactory.getModuleById("newmoduleiddemo");
	}
	
	/**
	 * @see {@link ModuleFactory#getStartedModuleById(String)}
	 *      (expected=ModuleException.class)
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should fail if there are multiple started modules matching the module id", method = "getStartedModuleById(String)")
	public void getStartedModuleById_shouldFailIfThereAreMultipleStartedModulesMatchingTheModuleId() throws Exception {
		ModuleFactory.getStartedModuleById("newmoduleiddemo");
	}
}
