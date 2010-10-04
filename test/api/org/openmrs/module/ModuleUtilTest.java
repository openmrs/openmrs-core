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

import java.net.URL;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods on the {@link ModuleUtil} class
 */
public class ModuleUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURLStream(URL)}
	 */
	@Test
	@Verifies(value = "should return a valid input stream for old module urls", method = "getURLStream(URL)")
	public void getURLStream_shouldReturnAValidInputStreamForOldModuleUrls() throws Exception {
		ModuleUtil.getURLStream(new URL("https://dev.openmrs.org/modules/download/formentry/update.rdf"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should return an update rdf page for old https dev urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsDevUrls() throws Exception {
		String updateRdf = ModuleUtil.getURL(new URL("https://dev.openmrs.org/modules/download/formentry/update.rdf"));
		Assert.assertTrue(updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should return an update rdf page for old https module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsModuleUrls() throws Exception {
		String updateRdf = ModuleUtil.getURL(new URL("https://modules.openmrs.org/modules/download/formentry/update.rdf"));
		Assert.assertTrue(updateRdf.contains("<updates"));
	}
	
	/**
	 * @see {@link ModuleUtil#checkMandatoryModulesStarted()}
	 */
	@Test(expected = MandatoryModuleException.class)
	@Verifies(value = "should throw ModuleException if a mandatory module is not started", method = "checkMandatoryModulesStarted()")
	public void checkMandatoryModulesStarted_shouldThrowModuleExceptionIfAMandatoryModuleIsNotStarted() throws Exception {
		executeDataSet("org/openmrs/module/include/mandatoryModulesGlobalProperties.xml");
		ModuleUtil.checkMandatoryModulesStarted();
	}
	
	/**
	 * @see {@link ModuleUtil#getMandatoryModules()}
	 */
	@Test
	@Verifies(value = "should return mandatory module ids", method = "getMandatoryModules()")
	public void getMandatoryModules_shouldReturnMandatoryModuleIds() throws Exception {
		executeDataSet("org/openmrs/module/include/mandatoryModulesGlobalProperties.xml");
		Assert.assertEquals(1, ModuleUtil.getMandatoryModules().size());
		Assert.assertEquals("firstmodule", ModuleUtil.getMandatoryModules().get(0));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should return an update rdf page for module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForModuleUrls() throws Exception {
		String updateRdf = ModuleUtil.getURL(new URL("http://modules.openmrs.org/modules/download/formentry/update.rdf"));
		Assert.assertTrue(updateRdf.contains("<updates"));
	}

	/**
     * @see {@link ModuleUtil#getPathForResource(Module,String)}
     */
    @Test
    @Verifies(value = "should handle ui springmvc css ui dot css example", method = "getPathForResource(Module,String)")
    public void getPathForResource_shouldHandleUiSpringmvcCssUiDotCssExample() throws Exception {
    	Module module = new Module("Unit test");
    	module.setModuleId("ui.springmvc");
    	String path = "/ui/springmvc/css/ui.css";
    	Assert.assertEquals("/css/ui.css", ModuleUtil.getPathForResource(module, path));
    }

	/**
     * @see {@link ModuleUtil#getModuleForPath(String)}
     */
    @Test
    @Verifies(value = "should handle ui springmvc css ui dot css when ui dot springmvc module is running", method = "getModuleForPath(String)")
    public void getModuleForPath_shouldHandleUiSpringmvcCssUiDotCssWhenUiDotSpringmvcModuleIsRunning() throws Exception {
    	ModuleFactory.getStartedModulesMap().clear();
	    Module module = new Module("For Unit Test");
	    module.setModuleId("ui.springmvc");
	    ModuleFactory.getStartedModulesMap().put(module.getModuleId(), module);
	    
	    String path = "/ui/springmvc/css/ui.css";
	    Assert.assertEquals(module, ModuleUtil.getModuleForPath(path));
    }

	/**
     * @see {@link ModuleUtil#getModuleForPath(String)}
     */
    @Test
    @Verifies(value = "should handle ui springmvc css ui dot css when ui module is running", method = "getModuleForPath(String)")
    public void getModuleForPath_shouldHandleUiSpringmvcCssUiDotCssWhenUiModuleIsRunning() throws Exception {
    	ModuleFactory.getStartedModulesMap().clear();
	    Module module = new Module("For Unit Test");
	    module.setModuleId("ui");
	    ModuleFactory.getStartedModulesMap().put(module.getModuleId(), module);
	    
	    String path = "/ui/springmvc/css/ui.css";
	    Assert.assertEquals(module, ModuleUtil.getModuleForPath(path));
    }

	/**
     * @see {@link ModuleUtil#getModuleForPath(String)}
     */
    @Test
    @Verifies(value = "should return null for ui springmvc css ui dot css when no relevant module is running", method = "getModuleForPath(String)")
    public void getModuleForPath_shouldReturnNullForUiSpringmvcCssUiDotCssWhenNoRelevantModuleIsRunning() throws Exception {
    	ModuleFactory.getStartedModulesMap().clear();
	    String path = "/ui/springmvc/css/ui.css";
	    Assert.assertNull(ModuleUtil.getModuleForPath(path));
    }
	
}
