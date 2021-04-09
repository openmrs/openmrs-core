/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.module.ModuleFactory.getLoadedModules;
import static org.openmrs.module.ModuleFactory.getStartedModules;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.BaseModuleActivatorTest;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.Listener;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * ModuleActivator tests that need refreshing the spring application context. The only reason why i
 * did not put these in the api projects's ModuleActivatorTest is because when the spring
 * application context is refreshed, classes that the module references which are not in the api but
 * web, will lead to ClassNotFoundException s, hence preventing the refresh. If you want to try this
 * out, just put these tests in ModuleActivatorTest NOTE: The way we start, stop, unload, etc,
 * modules is copied from ModuleListController
 */
@ContextConfiguration(locations = {
        "classpath*:webModuleApplicationContext.xml" }, inheritLocations = true, loader = TestContextLoader.class)
@SkipBaseSetup
public class WebModuleActivatorTest extends BaseModuleActivatorTest {
	
	public void createWebInfFolderIfNotExist() {
		//when run from the IDE and this folder does not exist, some tests fail with
		//org.openmrs.module.ModuleException: Unable to load module messages from file: 
		// /Projects/openmrs/core/web/target/test-classes/WEB-INF/module_messages_fr.properties
		
		File folder = Paths.get("target", "test-classes", "WEB-INF").toFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}
	
	@Test
	public void shouldCallWillRefreshContextAndContextRefreshedOnRefresh() {
		
		ModuleUtil.refreshApplicationContext((AbstractRefreshableApplicationContext) applicationContext, false, null);
		
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldRefreshOtherModulesOnStoppingModule() {
		
		//When OpenMRS is running and you stop a module:
		//    willRefreshContext() and contextRefreshed() methods get called for ONLY the started modules' activators EXCLUDING the stopped module
		//    willStop() and stopped() methods get called for ONLY the stopped module's activator
		
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		ModuleFactory.stopModule(module);
		WebModuleUtil.stopModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext());
		
		//module3 should have stopped
		assertTrue(moduleTestData.getWillStopCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE3_ID) == 1);
		
		//module1 and module2 should not stop
		assertTrue(moduleTestData.getWillStopCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStopCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStoppedCallCount(MODULE2_ID) == 0);
		
		//module3 should not refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 0);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 0);
		
		//module1 and module2 should refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
	}
	
	@Test
	public void shouldRefreshOtherModulesOnStartingStoppedModule() {
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		ModuleFactory.stopModule(module);
		
		init(); //to initialize for the condition below:
		
		createWebInfFolderIfNotExist();
		
		//When OpenMRS is running and you start a stopped module:
		//	willRefreshContext() and contextRefreshed() methods get called for all started modules' activators (including the newly started module)
		//  willStart() and started() methods get called for ONLY the newly started module's activator
		
		//start module3 which was previously stopped
		ModuleFactory.startModule(module);
		WebModuleUtil.startModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext(), false);
		
		assertTrue(module.isStarted());
		assertTrue(ModuleFactory.isModuleStarted(module));
		
		//module1, module2 and module3 should refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 1);
		
		//willStart() and started() methods get called for ONLY the newly started module's activator
		assertTrue(moduleTestData.getWillStartCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldRefreshContextForAllStartedModulesOnWebStartup() throws Throwable {
		
		//At OpenMRS start up:
		//  willRefreshContext(), contextRefreshed(), willStart() and started() methods get called for all started modules' activators
		Listener.performWebStartOfModules(((XmlWebApplicationContext) applicationContext).getServletContext());
		
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getWillStartCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE3_ID) == 1);
		
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldRefreshOtherModulesOnInstallingNewModule() {
		//first completely remove module3
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		ModuleFactory.stopModule(module);
		WebModuleUtil.stopModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext());
		ModuleFactory.unloadModule(module);
		
		init(); //to initialize for the condition below:
		
		createWebInfFolderIfNotExist();
		
		//When OpenMRS is running and you install a new module:
		//	willRefreshContext() and contextRefreshed() methods get called for all started modules' activators (including the newly installed module)
		//  willStart() and started() methods get called for ONLY the newly installed module's activator
		
		//install a new module3
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-1.0-SNAPSHOT.omod");
		File file = new File(url.getFile());
		module = ModuleFactory.loadModule(file);
		ModuleFactory.startModule(module);
		WebModuleUtil.startModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext(), false);
		
		assertTrue(module.isStarted());
		assertTrue(ModuleFactory.isModuleStarted(module));
		
		//module1, module2 and module3 should refresh
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getWillRefreshContextCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE1_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE2_ID) == 1);
		assertTrue(moduleTestData.getContextRefreshedCallCount(MODULE3_ID) == 1);
		
		//willStart() and started() methods get called for ONLY the newly installed module's activator
		assertTrue(moduleTestData.getWillStartCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getWillStartCallCount(MODULE3_ID) == 1);
		assertTrue(moduleTestData.getStartedCallCount(MODULE1_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE2_ID) == 0);
		assertTrue(moduleTestData.getStartedCallCount(MODULE3_ID) == 1);
	}
	
	@Test
	public void shouldUpgradeModule() {
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		
		assertTrue(module.getVersion().equals("1.0-SNAPSHOT"));
		
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-2.0-SNAPSHOT.omod");
		module.setDownloadURL("file:" + url.getFile());
		
		createWebInfFolderIfNotExist();
		
		ModuleFactory.stopModule(module, false, true); // stop the module with these parameters so that mandatory modules can be upgraded
		WebModuleUtil.stopModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext());
		Module newModule = ModuleFactory.updateModule(module);
		WebModuleUtil.startModule(newModule, ((XmlWebApplicationContext) applicationContext).getServletContext(), false);
		
		//module3 should have upgraded from version 1.0 to 2.0
		module = ModuleFactory.getModuleById(MODULE3_ID);
		assertTrue(module.getVersion().equals("2.0-SNAPSHOT"));
	}
	
	@Test
	public void shouldUpgradeModuleWithDependents() {
		Module module = ModuleFactory.getModuleById(MODULE1_ID);
		assertTrue(module.getVersion().equals("1.0-SNAPSHOT"));
		
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test1-2.0-SNAPSHOT.omod");
		module.setDownloadURL("file:" + url.getFile());
		
		createWebInfFolderIfNotExist();
		
		//all the modules should be started
		assertTrue(ModuleFactory.getModuleById(MODULE1_ID).isStarted());
		assertTrue(ModuleFactory.getModuleById(MODULE2_ID).isStarted());
		assertTrue(ModuleFactory.getModuleById(MODULE3_ID).isStarted());
		
		//and they should be only 3
		assertThat(getLoadedModules(), hasSize(5));
		assertThat(getStartedModules(), hasSize(5));
		
		//now stop module1
		ModuleFactory.stopModule(module, false, true); // stop the module with these parameters so that mandatory modules can be upgraded
		WebModuleUtil.stopModule(module, ((XmlWebApplicationContext) applicationContext).getServletContext());
		
		//module2 and module3 should have stopped since they depend on module1
		assertTrue(!ModuleFactory.getModuleById(MODULE1_ID).isStarted());
		assertTrue(!ModuleFactory.getModuleById(MODULE2_ID).isStarted());
		assertTrue(!ModuleFactory.getModuleById(MODULE3_ID).isStarted());
		
		//upgrade module1
		Module newModule = ModuleFactory.updateModule(module);
		
		//web start the upgraded module1
		WebModuleUtil.startModule(newModule, ((XmlWebApplicationContext) applicationContext).getServletContext(), false);
		
		//module1 should have upgraded from version 1.0 to 2.0
		module = ModuleFactory.getModuleById(MODULE1_ID);
		assertTrue(module.isStarted());
		assertTrue(module.getVersion().equals("2.0-SNAPSHOT"));
		
		//now try start module2 and module3
		ModuleFactory.startModule(ModuleFactory.getModuleById(MODULE2_ID));
		ModuleFactory.startModule(ModuleFactory.getModuleById(MODULE3_ID));
		
		//module2 and module3 should have started
		assertTrue(ModuleFactory.getModuleById(MODULE2_ID).isStarted());
		assertTrue(ModuleFactory.getModuleById(MODULE3_ID).isStarted());
		
		//we should have 5 modules instead of 6
		assertThat(getLoadedModules(), hasSize(5));
		assertThat(getStartedModules(), hasSize(5));
	}
	
	@BeforeEach
	public void initializeInDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
	}
	
	@AfterEach
	public void removeCurrentTransactionContext() throws Exception {
		Class<?> clazz = OpenmrsClassLoader.getInstance()
		        .loadClass("org.springframework.test.context.transaction.TransactionContextHolder");
		Method method = clazz.getDeclaredMethod("removeCurrentTransactionContext");
		ReflectionUtils.makeAccessible(method);
		ReflectionUtils.invokeMethod(method, null);
	}
}
