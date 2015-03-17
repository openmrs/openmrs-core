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

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.StartModule;
import org.openmrs.util.OpenmrsClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests how modules interact and call each other. Both when loaded by Spring during OpenMRS startup
 * and during normal file usage.
 */
@SkipBaseSetup
@StartModule( { "org/openmrs/module/include/dssmodule-1.44.omod", "org/openmrs/module/include/atdproducer-0.51.omod" })
public class ModuleInteroperabilityTest extends BaseContextSensitiveTest {
	
	/**
	 * Test that module A that requires module B can call a service method on module B
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAllowModuleAToLoadModuleBIfARequiresB() throws Exception {
		OpenmrsClassLoader loader = OpenmrsClassLoader.getInstance();
		Class<?> atdServiceClass = loader.loadClass("org.openmrs.module.atdproducer.service.ATDService");
		Class<?> dssServiceClass = loader.loadClass("org.openmrs.module.dssmodule.DssService");
		assertNotNull(atdServiceClass);
		assertNotNull(dssServiceClass);
		
		ModuleClassLoader atdClassLoader = (ModuleClassLoader) atdServiceClass.getClassLoader();
		assertEquals("atdproducer", atdClassLoader.getModule().getModuleId());
		
		ModuleClassLoader dssClassLoader = (ModuleClassLoader) dssServiceClass.getClassLoader();
		assertEquals("dssmodule", dssClassLoader.getModule().getModuleId());
		
		// load a dss class from the atd classloader.  This simulates a normal class (like a
		// controller) in one module loading another class that is located in a separate module 
		Class<?> dssUtilClass = atdClassLoader.loadClass("org.openmrs.module.dssmodule.util.Util");
		ModuleClassLoader dssUtilClassLoader = (ModuleClassLoader) dssUtilClass.getClassLoader();
		assertEquals("dssmodule", dssUtilClassLoader.getModule().getModuleId());
		
		// try the same as above except with an already loaded class (the DssService class)
		Class<?> dssServiceClass2 = atdClassLoader.loadClass("org.openmrs.module.dssmodule.DssService");
		ModuleClassLoader dssServiceClassLoader = (ModuleClassLoader) dssServiceClass2.getClassLoader();
		assertEquals("dssmodule", dssServiceClassLoader.getModule().getModuleId());
	}
	
}
