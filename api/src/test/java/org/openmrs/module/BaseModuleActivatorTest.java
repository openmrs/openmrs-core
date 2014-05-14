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

import org.junit.Before;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Has functionality commonly used by unit tests for module activator
 */
public abstract class BaseModuleActivatorTest extends BaseContextSensitiveTest {
	
	protected static final String MODULE1_ID = "test1";
	
	protected static final String MODULE2_ID = "test2";
	
	protected static final String MODULE3_ID = "test3";
	
	protected static final String MODULE4_ID = "test4";
	
	protected static final String MODULE5_ID = "test5";
	
	protected ModuleTestData moduleTestData;
	
	@Before
	public void beforeEachTest() throws Exception {
		moduleTestData = ModuleTestData.getInstance();
		
		ModuleUtil.shutdown();
		
		init();
		
		String modulesToLoad = "org/openmrs/module/include/test3-1.0-SNAPSHOT.omod "
		        + "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod org/openmrs/module/include/test2-1.0-SNAPSHOT.omod "
		        + "org/openmrs/module/include/test4-1.0-SNAPSHOT.omod org/openmrs/module/include/test5-1.0-SNAPSHOT.omod";
		runtimeProperties.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
		ModuleUtil.startup(runtimeProperties);
	}
	
	protected void init() {
		moduleTestData.init(MODULE1_ID);
		moduleTestData.init(MODULE2_ID);
		moduleTestData.init(MODULE3_ID);
		moduleTestData.init(MODULE4_ID);
		moduleTestData.init(MODULE5_ID);
	}
}
