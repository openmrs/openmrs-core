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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.StartModule;

@SkipBaseSetup
@StartModule( { "org/openmrs/module/include/newmoduleiddemo-1.0-SNAPSHOT.omod",
        "org/openmrs/module/include/newmoduleiddemo-1.1-SNAPSHOT.omod" })
public class ModuleIntegrationTest extends BaseContextSensitiveTest {
	
	/**
	 * Tests that 2 modules that have the same module id and different package names can be started
	 */
	@Test
	public void shouldAllowStartingTwoModulesWithTheSameModuleIdButDifferentPackageNames() throws Exception {
		Class<?> newModuleServiceClass = Context.loadClass("org.openmrs.module.newmoduleiddemo.api.NewModuleIdService");
		assertNotNull(newModuleServiceClass);
		
		ModuleClassLoader newmoduleiddemoClassLoader = (ModuleClassLoader) newModuleServiceClass.getClassLoader();
		assertEquals("newmoduleiddemo", newmoduleiddemoClassLoader.getModule().getModuleId());
		assertEquals("org.openmrs.module.newmoduleiddemo", newmoduleiddemoClassLoader.getModule().getPackageName());
		
		Class<?> duplicateModuleServiceClass = Context
		        .loadClass("org.openmrs.module.duplicate.newmoduleiddemo.api.DuplicateModuleService");
		assertNotNull(duplicateModuleServiceClass);
		
		ModuleClassLoader duplicateiddemoClassLoader = (ModuleClassLoader) duplicateModuleServiceClass.getClassLoader();
		assertEquals("newmoduleiddemo", duplicateiddemoClassLoader.getModule().getModuleId());
		assertEquals("org.openmrs.module.duplicate.newmoduleiddemo", duplicateiddemoClassLoader.getModule().getPackageName());
		
	}
	
}
