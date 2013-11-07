package org.openmrs.module;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.StartModule;

@SkipBaseSetup
@StartModule({ "org/openmrs/module/include/test3-1.0-SNAPSHOT.omod", "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod",
        "org/openmrs/module/include/test2-1.0-SNAPSHOT.omod" })
public class ModuleActivatorTest extends BaseContextSensitiveTest {
	
	private static final String MODULE1_ID = "test1";
	
	private static final String MODULE2_ID = "test2";
	
	private static final String MODULE3_ID = "test3";
	
	ModuleTestData moduleTestData;
	
	@Before
	public void beforeEachTest() {
		moduleTestData = ModuleTestData.getInstance();
		
		init();
		
		ModuleFactory.startModule(ModuleFactory.getModuleById(MODULE1_ID));
		ModuleFactory.startModule(ModuleFactory.getModuleById(MODULE2_ID));
		ModuleFactory.startModule(ModuleFactory.getModuleById(MODULE3_ID));
	}
	
	@Test
	public void shouldCallWillStartOnStartup() throws Exception {
		assertTrue(moduleTestData.getWillStartCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	@Ignore("This is work in progress. So not yet finished this")
	public void shouldCallWillRefreshContextOnStartup() throws Exception {
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	@Ignore("This is work in progress. So not yet looked into why this fails")
	public void shouldCallStartedOnStartup() throws Exception {
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldStartModulesInOrder() throws Exception {
		//module test2 depends on test1
		//while test3 depends on test2
		assertTrue(moduleTestData.getWillStartCallTime(MODULE1_ID) <= moduleTestData.getWillStartCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getWillStartCallTime(MODULE2_ID) <= moduleTestData.getWillStartCallTime(MODULE3_ID));
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnlyForStoppedModule() throws Exception {
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//should have called willStop() for only module test3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		
		//should have called stopped() for only module test3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
	}
	
	@Test
	public void shouldStopDependantModulesOnStopModule() throws Exception {
		//since module test2 depends on test1 and test3 depends on test2
		//stopping test1 should also stop both modules test2 and test3
		ModuleFactory.stopModule(ModuleFactory.getModuleById(MODULE1_ID));
		
		//should have called willStop() for only modules test1, test2 and test3
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		//should have called stopped() for only modules test1, test2 and test3
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldCallWillStopAndStoppedOnShutdown() throws Exception {
		ModuleUtil.shutdown();
		
		//should have called willStop()
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		
		//should have called stopped()
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//willStop() should have been called before stopped()
		assertTrue(moduleTestData.getWillStopCallTime(MODULE1_ID) <= moduleTestData.getStoppedCallTime(MODULE1_ID));
		assertTrue(moduleTestData.getWillStopCallTime(MODULE2_ID) <= moduleTestData.getStoppedCallTime(MODULE2_ID));
		assertTrue(moduleTestData.getWillStopCallTime(MODULE3_ID) <= moduleTestData.getStoppedCallTime(MODULE3_ID));
	}
	
	private void init() {
		moduleTestData.init(MODULE1_ID);
		moduleTestData.init(MODULE2_ID);
		moduleTestData.init(MODULE3_ID);
	}
}
