package org.openmrs.module;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;


public class AwareOfModuleTest extends BaseContextSensitiveTest {
	
	protected static final String MODULE6_ID = "test6";
	
	protected static final String MODULE7_ID = "test7";
	
	protected ModuleTestData moduleTestData;
	
	@Before
	public void beforeEachTest() throws Exception {
		moduleTestData = ModuleTestData.getInstance();
		
		ModuleUtil.shutdown();
		
		init();
		
		String modulesToLoad = "org/openmrs/module/include/test7-1.0-SNAPSHOT.omod org/openmrs/module/include/test6-1.0-SNAPSHOT.omod";
		runtimeProperties.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
		ModuleUtil.startup(runtimeProperties);
	}
	
	protected void init() {
		moduleTestData.init(MODULE6_ID);
		moduleTestData.init(MODULE7_ID);
	}
	
	@Test
	public void shouldCallWillStartOnStartup() throws Exception {
		assertTrue(moduleTestData.getWillStartCallCount(MODULE6_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE7_ID) == 1);
	}
}
