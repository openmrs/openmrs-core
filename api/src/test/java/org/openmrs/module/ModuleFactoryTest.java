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

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.Test;

import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class ModuleFactoryTest extends BaseContextSensitiveTest {
	
	protected static final String MODULE1 = "test1";
	
	protected static final String MODULE1_PATH = "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod";
	protected static final String MODULE1_UPDATE_PATH = "org/openmrs/module/include/test1-2.0-SNAPSHOT.omod";
	
	protected static final String MODULE2 = "test2";
	protected static final String MODULE2_PATH = "org/openmrs/module/include/test2-1.0-SNAPSHOT.omod";
	
	protected static final String MODULE3 = "test3";
	protected static final String MODULE3_PATH = "org/openmrs/module/include/test3-1.0-SNAPSHOT.omod";
	
	@Before
	public void before() throws Exception {
		ModuleUtil.shutdown();
		
		String modulesToLoad = MODULE1_PATH + " ";
		       
		runtimeProperties.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
		ModuleUtil.startup(runtimeProperties);
	}
	
	@AfterClass
	public static void cleanUp() {
		//ensure that we do not have any left overs to interfere with other tests
		ModuleUtil.shutdown();
	}
	
	@Test
	@Verifies(value = "should load module if it is currently not loaded", method = "loadModule")
	public void loadModule_shouldLoadModuleIfItIsCurrentlyNotLoaded() {
		Module test2 = loadModule(MODULE2_PATH, MODULE2, false);
		
		//verify that module test2 is started
		ModuleFactory.startModule(test2);
		Assert.assertTrue(ModuleFactory.getLoadedModules().contains(test2));
	}
	
	@Test(expected = ModuleException.class)
	@Verifies(value = "should not load module if already loaded", method = "loadModule")
	public void loadModule_shouldNotLoadModuleIfAlreadyLoaded() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		
		//verify that module test1 is started
		Assert.assertNotNull(ModuleFactory.getStartedModuleById(MODULE1));
		Assert.assertTrue(test1.isStarted());
		
		//this should throw an exception for trying to load this module again
		ModuleFactory.loadModule(test1, false);
	}

	@Test
	@Verifies(value = "should always load module if replacement is wanted", method = "loadModule")
	public void loadModule_shouldAlwaysLoadModuleIfReplacementIsWanted() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		
		Module newModule = loadModule(MODULE1_PATH, MODULE1, true);
		
	    //verify that module test1 is stopped and newModule loaded
		Assert.assertNull(ModuleFactory.getStartedModuleById(MODULE1));
		Assert.assertFalse(test1.isStarted());
		
		Assert.assertTrue(ModuleFactory.getLoadedModules().contains(newModule));
	}
		
	@Test
	@Verifies(value = "should load a newer version of the same module", method = "loadModule") 
	public void loadModule_shouldLoadANewerVersionOfTheSameModule() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);

		Module newModule = loadModule(MODULE1_UPDATE_PATH, MODULE1, true);
		
		//verify updated module is loaded and old shut down.
		Assert.assertNull(ModuleFactory.getStartedModuleById(MODULE1));
		Assert.assertFalse(test1.isStarted());
		
		Assert.assertTrue(ModuleFactory.getLoadedModules().contains(newModule));
	}
	
	@Test
	@Verifies(value = "should not load an older version of the same module", method = "loadModule") 
	public void loadModule_shouldNotLoadAnOlderVersionOfTheSameModule() {
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		Module newModule = loadModule(MODULE1_UPDATE_PATH, MODULE1, true);
		
		//first upgrade to a newer version so a revert can be tried
		Assert.assertNotNull(ModuleFactory.getLoadedModules().contains(newModule));
		
		//now verify that a rollback simply returns the newer version's module.
		Module oldModule = loadModule(MODULE1_PATH, MODULE1, true);
		
		Assert.assertEquals(newModule, oldModule);
		Assert.assertNotNull(ModuleFactory.getLoadedModules().contains(oldModule));
	}
	
	@Test
	public void startModule_shouldStartAllDependencies() {
		Module test1 = loadModule(MODULE1_PATH, MODULE1, true);
		Module test2 = loadModule(MODULE2_PATH, MODULE2, true);
		
		ModuleFactory.startModule(test2);
		
		Assert.assertNotNull(ModuleFactory.getStartedModuleById("test1")); // test1 should have been started, just by starting test2
		Assert.assertNotNull(ModuleFactory.getStartedModuleById("test2")); // should be started after starting all dependencies
		Assert.assertTrue(test1.isStarted());
		Assert.assertTrue(test2.isStarted());
	}
	
	@Test
	@Verifies(value = "should not crash when file is not found or broken", method = "loadModules")
	public void loadModules_shouldNotCrashWhenFileIsNotFoundOrBroken() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath();
		moduleLocation += "/i/broke/this/path/module.omod";
		File moduleToLoad = new File(moduleLocation);
		
		List<File> modulesToLoad = new ArrayList<File>();
		modulesToLoad.add(moduleToLoad);
		ModuleFactory.loadModules(modulesToLoad);
		
		Assert.assertEquals(0, ModuleFactory.getLoadedModules().size());
	}
	
	@Test
	@Verifies(value = "should setup requirement mappings for every module", method = "loadModules") 
	public void loadModules_shouldSetupRequirementMappingsForEveryModule() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		
		List<File> modulesToLoad = getModuleFiles();
		
		ModuleFactory.loadModules(modulesToLoad);
		Assert.assertEquals(3, ModuleFactory.getLoadedModules().size());
		
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		Module test2 = ModuleFactory.getModuleById(MODULE2);
		Module test3 = ModuleFactory.getModuleById(MODULE3);
		
		Assert.assertEquals(0, test1.getRequiredModules().size());
		Assert.assertEquals(1, test2.getRequiredModules().size());
		Assert.assertEquals(1, test3.getRequiredModules().size());
	}
	
	@Test
	@Verifies(value = "should not start the loaded modules", method = "loadModules") 
	public void loadModules_shouldNotStartTheLoadedModules() {
		ModuleFactory.unloadModule(ModuleFactory.getModuleById(MODULE1));
		
		List<File> modulesToLoad = getModuleFiles();
		
		ModuleFactory.loadModules(modulesToLoad);
		Assert.assertEquals(3, ModuleFactory.getLoadedModules().size());
		
		Module test1 = ModuleFactory.getModuleById(MODULE1);
		Module test2 = ModuleFactory.getModuleById(MODULE2);
		Module test3 = ModuleFactory.getModuleById(MODULE3);
		
		Assert.assertFalse(test1.isStarted());
		Assert.assertFalse(test2.isStarted());
		Assert.assertFalse(test3.isStarted());
	}
	
	private Module loadModule(String location, String moduleName, boolean replace) {
		String moduleLocation = ModuleUtil.class.getClassLoader().getResource(location).getPath();
		Module newModule = ModuleFactory.loadModule(new File(moduleLocation), replace);

		return newModule;
	}
	
	private List<File> getModuleFiles() {
		List<File> modulesToLoad = new ArrayList<File>();
		modulesToLoad.add(new File(ModuleUtil.class.getClassLoader().getResource(MODULE1_PATH).getPath()));
		modulesToLoad.add(new File(ModuleUtil.class.getClassLoader().getResource(MODULE2_PATH).getPath()));
		modulesToLoad.add(new File(ModuleUtil.class.getClassLoader().getResource(MODULE3_PATH).getPath()));
		
		return modulesToLoad;
	}
}
