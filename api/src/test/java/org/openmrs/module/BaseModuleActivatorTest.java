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

import org.junit.jupiter.api.BeforeEach;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

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
	
	@BeforeEach
	public void beforeEachTest() {
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
