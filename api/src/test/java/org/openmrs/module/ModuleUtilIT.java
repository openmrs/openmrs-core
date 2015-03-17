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
import java.net.URL;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests methods on the {@link ModuleUtil} class
 */
public class ModuleUtilIT extends BaseContextSensitiveTest {
	
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
	@Ignore
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
	@Ignore
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
	
	/**
	 * @see {@link ModuleUtil#checkRequiredVersion(String, String)}
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if openmrs version beyond wild card range", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfOpenmrsVersionBeyondWildCardRange() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see {@link ModuleUtil#checkRequiredVersion(String, String)}
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if required version beyond openmrs version", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionBeyondOpenmrsVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see {@link ModuleUtil#checkRequiredVersion(String, String)}
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if required version with wild card beyond openmrs version", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionWithWildCardBeyondOpenmrsVersion()
	        throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.* - 1.6.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see {@link ModuleUtil#checkRequiredVersion(String, String)}
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if required version with wild card on one end beyond openmrs version", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionWithWildCardOnOneEndBeyondOpenmrsVersion()
	        throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.5 - 1.5.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see {@link ModuleUtil#checkRequiredVersion(String, String)}
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if single entry required version beyond openmrs version", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfSingleEntryRequiredVersionBeyondOpenmrsVersion()
	        throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.0";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see {@link ModuleUtil#compareVersion(String,String)}
	 */
	@Test
	@Verifies(value = "should correctly comparing two version numbers", method = "compareVersion(String,String)")
	public void compareVersion_shouldCorrectlyComparingTwoVersionNumbers() throws Exception {
		String olderVersion = "2.1.1";
		String newerVersion = "2.1.10";
		Assert.assertTrue(ModuleUtil.compareVersion(olderVersion, newerVersion) < 0);
	}
	
	/**
	 * @see {@link ModuleUtil#compareVersion(String,String)}
	 */
	@Test
	@Verifies(value = "treat SNAPSHOT as earliest version", method = "compareVersion(String,String)")
	public void compareVersion_shouldTreatSNAPSHOTAsEarliestVersion() throws Exception {
		String olderVersion = "1.8.3";
		String newerVersion = "1.8.4-SNAPSHOT";
		Assert.assertTrue(ModuleUtil.compareVersion(newerVersion, olderVersion) > 0);
		//should still return the correct value if the arguments are switched
		Assert.assertTrue(ModuleUtil.compareVersion(olderVersion, newerVersion) < 0);
	}
	
	/**
	 * @see {@link ModuleUtil#getModuleRepository()}
	 */
	@Test
	@Verifies(value = "should use the runtime property as the first choice if specified", method = "getModuleRepository()")
	public void getModuleRepository_shouldUseTheRuntimePropertyAsTheFirstChoiceIfSpecified() throws Exception {
		final String folderName = "test_folder";
		File testFolder = null;
		runtimeProperties.setProperty(ModuleConstants.REPOSITORY_FOLDER_RUNTIME_PROPERTY, folderName);
		try {
			testFolder = ModuleUtil.getModuleRepository();
			Assert.assertNotNull(testFolder);
			Assert.assertEquals(new File(OpenmrsUtil.getApplicationDataDirectory(), folderName), ModuleUtil
			        .getModuleRepository());
		}
		finally {
			if (testFolder != null)
				testFolder.deleteOnExit();
			runtimeProperties.setProperty(ModuleConstants.REPOSITORY_FOLDER_RUNTIME_PROPERTY, "");
		}
	}
	
	/**
	 * @see {@link ModuleUtil#getModuleRepository()}
	 */
	@Test
	@Verifies(value = "should return the correct file if the runtime property is an absolute path", method = "getModuleRepository()")
	public void getModuleRepository_shouldReturnTheCorrectFileIfTheRuntimePropertyIsAnAbsolutePath() throws Exception {
		final File expectedModuleRepo = new File(System.getProperty("java.io.tmpdir"), "test_folder");
		expectedModuleRepo.mkdirs();
		
		runtimeProperties.setProperty(ModuleConstants.REPOSITORY_FOLDER_RUNTIME_PROPERTY, expectedModuleRepo
		        .getAbsolutePath());
		try {
			File testFolder = ModuleUtil.getModuleRepository();
			Assert.assertNotNull(testFolder);
			Assert.assertEquals(expectedModuleRepo, testFolder);
		}
		finally {
			runtimeProperties.setProperty(ModuleConstants.REPOSITORY_FOLDER_RUNTIME_PROPERTY, "");
			expectedModuleRepo.deleteOnExit();
		}
	}
}
