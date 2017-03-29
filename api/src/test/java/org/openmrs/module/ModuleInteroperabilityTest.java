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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.StartModule;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Tests how modules interact and call each other. Both when loaded by Spring during OpenMRS startup
 * and during normal file usage.
 */
@SkipBaseSetup
@StartModule( { "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod", "org/openmrs/module/include/test2-1.0-SNAPSHOT.omod" })
public class ModuleInteroperabilityTest extends BaseContextSensitiveTest {
	
	/**
	 * Test that module A that requires module B can call a service method on module B
	 * 
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	@Test
	public void shouldAllowModuleAToLoadModuleBIfARequiresB() throws ClassNotFoundException {
		OpenmrsClassLoader loader = OpenmrsClassLoader.getInstance();
		Class<?> module1ServiceClass = loader.loadClass("org.openmrs.module.test1.api.Test1Service");
		Class<?> module2ServiceClass = loader.loadClass("org.openmrs.module.test2.api.Test2Service");
		assertNotNull(module1ServiceClass);
		assertNotNull(module2ServiceClass);
		
		ModuleClassLoader module1ClassLoader = (ModuleClassLoader) module1ServiceClass.getClassLoader();
		assertEquals("test1", module1ClassLoader.getModule().getModuleId());
		
		ModuleClassLoader module2ClassLoader = (ModuleClassLoader) module2ServiceClass.getClassLoader();
		assertEquals("test2", module2ClassLoader.getModule().getModuleId());
		
		// load a module1 class from the module2 classloader.  This simulates a normal class (like a
		// controller) in one module loading another class that is located in a separate module 
		Class<?> module1TestClass = module2ClassLoader.loadClass("org.openmrs.module.test1.Test1");
		ModuleClassLoader module1TestClassLoader = (ModuleClassLoader) module1TestClass.getClassLoader();
		assertEquals("test1", module1TestClassLoader.getModule().getModuleId());
		
		// try the same as above except with an already loaded class (the Module1Service class)
		Class<?> module1ServiceClass2 = module2ClassLoader.loadClass("org.openmrs.module.test1.api.Test1Service");
		ModuleClassLoader module1ServiceClassLoader = (ModuleClassLoader) module1ServiceClass2.getClassLoader();
		assertEquals("test1", module1ServiceClassLoader.getModule().getModuleId());
	}
	
}
