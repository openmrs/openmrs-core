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
	@Verifies(value = "should return an update rdf page for old https dev urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsDevUrls() throws Exception {
		String url = "https://dev.openmrs.org/modules/download/formentry/update.rdf";
		String updateRdf = ModuleUtil.getURL(new URL(url));
		Assert.assertTrue("Unable to fetch module update url: " + url, updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for old https module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForOldHttpsModuleUrls() throws Exception {
		String url = "https://modules.openmrs.org/modules/download/formentry/update.rdf";
		String updateRdf = ModuleUtil.getURL(new URL(url));
		Assert.assertTrue("Unable to fetch module update url: " + url, updateRdf.contains("<updates"));
	}
	
	/**
	 * This test requires a connection to the internet to pass
	 * 
	 * @see {@link ModuleUtil#getURL(URL)}
	 */
	@Test
	@Verifies(value = "should return an update rdf page for module urls", method = "getURL(URL)")
	public void getURL_shouldReturnAnUpdateRdfPageForModuleUrls() throws Exception {
		String url = "http://modules.openmrs.org/modules/download/formentry/update.rdf";
		String updateRdf = ModuleUtil.getURL(new URL(url));
		Assert.assertTrue("Unable to fetch module update url: " + url, updateRdf.contains("<updates"));
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
	 * @see {@link ModuleUtil#checkOpenmrsCoreModulesStarted()}
	 */
	@Test(expected = OpenmrsCoreModuleException.class)
	@Verifies(value = "should throw ModuleException if a core module is not started", method = "checkOpenmrsCoreModulesStarted()")
	public void checkMandatoryModulesStarted_shouldThrowModuleExceptionIfACoreModuleIsNotStarted() throws Exception {
		
		runtimeProperties.setProperty(ModuleConstants.IGNORE_CORE_MODULES_PROPERTY, "false");
		try {
			ModuleUtil.checkOpenmrsCoreModulesStarted();
		}
		finally {
			runtimeProperties.setProperty(ModuleConstants.IGNORE_CORE_MODULES_PROPERTY, "true");
		}
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
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow ranged required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.3 - 1.4.4";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow ranged required version with wild card", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedRequiredVersionWithWildCard() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.* - 1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow ranged required version with wild card on one end", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedRequiredVersionWithWildCardOnOneEnd() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.3 - 1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
		requiredOpenmrsVersion = "1.4.* - 1.4.5";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow single entry for required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowSingleEntryForRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.2";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow required version with wild card", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRequiredVersionWithWildCard() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow non numeric character required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowNonNumericCharacterRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.3a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow ranged non numeric character required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.1a - 1.4.3a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow ranged non numeric character with wild card", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterWithWildCard() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*a - 1.4.*a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should allow ranged non numeric character with wild card on one end", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterWithWildCardOnOneEnd() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.3 - 1.4.*a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
		requiredOpenmrsVersion = "1.4.*a - 1.4.5a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should return false when openmrs version beyond wild card range", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenOpenmrsVersionBeyondWildCardRange() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
		requiredOpenmrsVersion = "1.5.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should return false when required version beyond openmrs version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionBeyondOpenmrsVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should return false when required version with wild card beyond openmrs version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionWithWildCardBeyondOpenmrsVersion()
	                                                                                                        throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.* - 1.6.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should return false when required version with wild card on one end beyond openmrs version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionWithWildCardOnOneEndBeyondOpenmrsVersion()
	                                                                                                                throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.5 - 1.5.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
		requiredOpenmrsVersion = "1.5.* - 1.6.0";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
	 */
	@Test
	@Verifies(value = "should return false when single entry required version beyond openmrs version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenSingleEntryRequiredVersionBeyondOpenmrsVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.0";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}

	/**
     * @see {@link ModuleUtil#matchRequiredVersions(String,String)}
     * 
     */
    @Test
    @Verifies(value = "should allow release type in the version", method = "matchRequiredVersions(String,String)")
    public void matchRequiredVersions_shouldAllowReleaseTypeInTheVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.1-dev - 1.5.*-alpha";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
		requiredOpenmrsVersion = "1.5.*-dev - 1.6.0-dev";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
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
