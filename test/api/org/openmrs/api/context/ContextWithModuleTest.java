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
import org.openmrs.test.SkipBaseSetup;
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
		    "org/openmrs/api/context/include/dssmodule-1.44.omod");
		
		return props;
	}
	
	/**
	 * @see {@link Context#loadClass(String)}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should load class with the OpenmrsClassLoader", method = "loadClass(String)")
	public void loadClass_shouldLoadClassWithOpenmrsClassLoader() throws Exception {
		Class<?> c = Context.loadClass("org.openmrs.module.dssmodule.DssService");
		Assert.assertTrue("Should be loaded by OpenmrsClassLoader", c.getClassLoader() instanceof ModuleClassLoader);
	}
	
}
