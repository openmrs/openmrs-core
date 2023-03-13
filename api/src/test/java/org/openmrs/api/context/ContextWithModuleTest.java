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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleInteroperabilityTest;
import org.openmrs.module.ModuleUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * This test class is meant just for testing the {@link Context#loadClass(String)} method. This
 * method needs to have a module loaded for it to test correctly, so it is put into a separate class
 * The module is stolen/copied from the {@link ModuleInteroperabilityTest}
 * 
 * @see ContextTest
 */
public class ContextWithModuleTest extends BaseContextSensitiveTest {
	
	@BeforeEach
	public void startupBeforeEachTest() {
		ModuleUtil.startup(getRuntimeProperties());
	}
	
	@AfterEach
	public void cleanupAfterEachTest() {
		ModuleUtil.shutdown();
	}
	
	/**
	 * This class file uses the atd and dss modules to test the compatibility
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
	 */
	@Override
	public Properties getRuntimeProperties() {
		Properties props = super.getRuntimeProperties();
		
		// NOTE! This module is modified heavily from the original atd modules.
		// the "/lib" folder has been emptied to compact the size.
		// the "/metadata/sqldiff.xml" file has been deleted in order to load the modules into hsql.
		//    (the sql tables are built from hibernate mapping files automatically in unit tests)
		props.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD,
		    "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod org/openmrs/module/include/test2-1.0-SNAPSHOT.omod");
		
		return props;
	}
	
	/**
	 * @throws ClassNotFoundException
	 * @see Context#loadClass(String)
	 */
	@Test
	public void loadClass_shouldLoadClassWithOpenmrsClassLoader() throws ClassNotFoundException {
		Class<?> c = Context.loadClass("org.openmrs.module.test1.api.Test1Service");
		assertTrue(c.getClassLoader() instanceof ModuleClassLoader, "Should be loaded by OpenmrsClassLoader");
	}
	
}
