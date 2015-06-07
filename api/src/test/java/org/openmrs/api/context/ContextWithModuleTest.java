/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleInteroperabilityTest;
import org.openmrs.module.ModuleUtil;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * This test class is meant just for testing the {@link Context#loadClass(String)} method. This
 * method needs to have a module loaded for it to test correctly, so it is put into a separate class
 * The module is stolen/copied from the {@link ModuleInteroperabilityTest}
 * 
 * @see ContextTest
 */
public class ContextWithModuleTest extends BaseContextSensitiveTest {
	
	@Before
	public void startupBeforeEachTest() throws Exception {
		ModuleUtil.startup(getRuntimeProperties());
	}
	
	@After
	public void cleanupAfterEachTest() throws Exception {
		ModuleUtil.shutdown();
	}
	
	/**
	 * This class file uses the atd and dss modules to test the compatibility
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
	 */
	public Properties getRuntimeProperties() {
		Properties props = super.getRuntimeProperties();
		
		// NOTE! This module is modified heavily from the original atd modules.
		// the "/lib" folder has been emptied to compact the size.
		// the "/metadata/sqldiff.xml" file has been deleted in order to load the modules into hsql.
		//    (the sql tables are built from hibernate mapping files automatically in unit tests)
		props.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD,
		    "org/openmrs/module/include/logic-0.2.omod org/openmrs/module/include/dssmodule-1.44.omod");
		
		return props;
	}
	
	/**
	 * @see {@link Context#loadClass(String)}
	 */
	@Test
	@Verifies(value = "should load class with the OpenmrsClassLoader", method = "loadClass(String)")
	public void loadClass_shouldLoadClassWithOpenmrsClassLoader() throws Exception {
		Class<?> c = Context.loadClass("org.openmrs.module.dssmodule.DssService");
		Assert.assertTrue("Should be loaded by OpenmrsClassLoader", c.getClassLoader() instanceof ModuleClassLoader);
	}
	
}
