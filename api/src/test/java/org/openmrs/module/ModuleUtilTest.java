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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests methods on the {@link org.openmrs.module.ModuleUtil} class
 */
public class ModuleUtilTest extends BaseContextSensitiveTest {
	
	Properties initialRuntimeProperties;
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkMandatoryModulesStarted()
	 */
	@Test(expected = MandatoryModuleException.class)
	@Verifies(value = "should throw ModuleException if a mandatory module is not started", method = "checkMandatoryModulesStarted()")
	public void checkMandatoryModulesStarted_shouldThrowModuleExceptionIfAMandatoryModuleIsNotStarted() throws Exception {
		//given
		assertThat(ModuleFactory.getStartedModules(), empty());
		
		GlobalProperty gp1 = new GlobalProperty("module1.mandatory", "true");
		Context.getAdministrationService().saveGlobalProperty(gp1);
		
		//when
		ModuleUtil.checkMandatoryModulesStarted();
		//then exception
	}
	
	@After
	public void revertRuntimeProperties() {
		if (initialRuntimeProperties != null) {
			Context.setRuntimeProperties(initialRuntimeProperties);
			initialRuntimeProperties = null;
		}
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#getMandatoryModules()
	 */
	@Test
	@Verifies(value = "should return mandatory module ids", method = "getMandatoryModules()")
	public void getMandatoryModules_shouldReturnMandatoryModuleIds() throws Exception {
		//given
		GlobalProperty gp1 = new GlobalProperty("firstmodule.mandatory", "true");
		GlobalProperty gp2 = new GlobalProperty("secondmodule.mandatory", "false");
		
		Context.getAdministrationService().saveGlobalProperty(gp1);
		Context.getAdministrationService().saveGlobalProperty(gp2);
		
		//when
		//then
		assertThat(ModuleUtil.getMandatoryModules(), contains("firstmodule"));
	}


	/**
	 * @verifies return false when versions is null
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test public void isOpenmrsVersionInVersions_shouldReturnFalseWhenVersionsIsNull() throws Exception {

		Assert.assertFalse(ModuleUtil.isOpenmrsVersionInVersions(null));
	}
	/**
	 * @verifies return false when versions is empty
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test public void isOpenmrsVersionInVersions_shouldReturnFalseWhenVersionsIsEmpty() throws Exception {

		Assert.assertFalse(ModuleUtil.isOpenmrsVersionInVersions( new String[] {}));
	}

	/**
	 * @verifies return true if current openmrs version matches one element in versions
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test public void isOpenmrsVersionInVersions_shouldReturnTrueIfCurrentOpenmrsVersionMatchesOneElementInVersions()
			throws Exception {

		Field versionField = OpenmrsConstants.class.getDeclaredField("OPENMRS_VERSION_SHORT");
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);
		final String currentVersion = "1.9.8";
		versionField.set(null, currentVersion);

		Assert.assertTrue(ModuleUtil.isOpenmrsVersionInVersions( currentVersion, "1.10.*"));
	}

	/**
	 * @verifies return false if current openmrs version does not match any element in versions
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test public void isOpenmrsVersionInVersions_shouldReturnFalseIfCurrentOpenmrsVersionDoesNotMatchAnyElementInVersions()
			throws Exception {

		Field versionField = OpenmrsConstants.class.getDeclaredField("OPENMRS_VERSION_SHORT");
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);
		versionField.set(null, "1.9.8");

		Assert.assertFalse(ModuleUtil.isOpenmrsVersionInVersions( new String[] { "1.11.*", "2.1.0" }));
	}

	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow ranged required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.3 - 1.4.4";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow ranged required version with wild card", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedRequiredVersionWithWildCard() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.* - 1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
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
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow single entry for required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowSingleEntryForRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.2";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow required version with wild card", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRequiredVersionWithWildCard() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow non numeric character required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowNonNumericCharacterRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.3a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow ranged non numeric character required version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterRequiredVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.1a - 1.4.3a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should allow ranged non numeric character with wild card", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterWithWildCard() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*a - 1.4.*a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
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
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
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
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should return false when required version beyond openmrs version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionBeyondOpenmrsVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}

	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
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
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
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
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should return false when single entry required version beyond openmrs version", method = "matchRequiredVersions(String,String)")
	public void matchRequiredVersions_shouldReturnFalseWhenSingleEntryRequiredVersionBeyondOpenmrsVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.0";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
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
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should match when revision number is below maximum revision number", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldMatchWhenRevisionNumberIsBelowMaximumRevisionNumber() {
		String openmrsVersion = "1.4.1111";
		String requiredVersion = "1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should not match when revision number is above maximum revision number", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldNotMatchWhenRevisionNumberIsAboveMaximumRevisionNumber() {
		Long revisionNumber = (long) Integer.MAX_VALUE + 2;
		String openmrsVersion = "1.4." + revisionNumber;
		String requiredVersion = "1.4.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should not match when version has wild card and is outside boundary", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldNotMatchWhenVersionHasWildCardAndIsOutsideBoundary() {
		String openmrsVersion = "1.*.4";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should match when version has wild card and is within boundary", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldMatchWhenVersionHasWildCardAndIsWithinBoundary() {
		String openmrsVersion = "1.4.*";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should not match when version has wild card plus qualifier and is outside boundary", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldNotMatchWhenVersionHasWildPlusQualifierCardAndIsOutsideBoundary() {
		String openmrsVersion = "1.*.4-SNAPSHOT";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should match when version has wild card plus qualifier and is within boundary", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldMatchWhenVersionHasWildCardPlusQualifierAndIsWithinBoundary() {
		String openmrsVersion = "1.4.*-SNAPSHOT";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should return true when required version is empty", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldReturnTrueWhenRequiredVersionIsEmpty() {
		String openmrsVersion = "1.11.4";
		String requiredVersion = "";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	@Verifies(value = "should correctly set upper and lower limit for versionRange with qualifiers and wild card", method = "matchRequiredVersions(String version, String versionRange)")
	public void matchRequiredVersions_shouldCorrectlySetUpperAndLoweLimitForVersionRangeWithQualifiersAndWildCard() {
		String openmrsVersion = "1.4.11111";
		String requiredVersion = "1.4.200 - 1.4.*-SNAPSHOT";
		Long revisionNumber = (long) Integer.MAX_VALUE + 2;
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
		requiredVersion = "1.4.*-SNAPSHOT - 1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
		openmrsVersion = "1.4." + revisionNumber;
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#getPathForResource(org.openmrs.module.Module,String)
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
	 * @see org.openmrs.module.ModuleUtil#getModuleForPath(String)
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
		ModuleFactory.getStartedModulesMap().clear();
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#getModuleForPath(String)
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
		ModuleFactory.getStartedModulesMap().clear();
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#getModuleForPath(String)
	 */
	@Test
	@Verifies(value = "should return null for ui springmvc css ui dot css when no relevant module is running", method = "getModuleForPath(String)")
	public void getModuleForPath_shouldReturnNullForUiSpringmvcCssUiDotCssWhenNoRelevantModuleIsRunning() throws Exception {
		ModuleFactory.getStartedModulesMap().clear();
		String path = "/ui/springmvc/css/ui.css";
		Assert.assertNull(ModuleUtil.getModuleForPath(path));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if openmrs version beyond wild card range", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfOpenmrsVersionBeyondWildCardRange() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if required version beyond openmrs version", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionBeyondOpenmrsVersion() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
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
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
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
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
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
	 * @see org.openmrs.module.ModuleUtil#compareVersion(String,String)
	 */
	@Test
	@Verifies(value = "should correctly comparing two version numbers", method = "compareVersion(String,String)")
	public void compareVersion_shouldCorrectlyComparingTwoVersionNumbers() throws Exception {
		String olderVersion = "2.1.1";
		String newerVersion = "2.1.10";
		Assert.assertTrue(ModuleUtil.compareVersion(olderVersion, newerVersion) < 0);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#compareVersion(String,String)
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
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	@Verifies(value = "should throw ModuleException if SNAPSHOT not handled correctly", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldThrowModuleExceptionIfSNAPSHOTNotHandledCorrectly() throws Exception {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.5-SNAPSHOT";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test
	@Verifies(value = "Should handle SNAPSHOT versions ", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldHandleSnapshotVersion() throws Exception {
		String openMRSVersion = "1.9.2-SNAPSHOT";
		String requiredOpenmrsVersion = "1.9.2-SNAPSHOT";
		ModuleUtil.checkRequiredVersion(openMRSVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test
	@Verifies(value = "Should handle UUID suffix versions ", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldHandleUuidSuffixVersion() throws Exception {
		String openMRSVersion = "1.9.9-f4927f";
		String requiredOpenmrsVersion = "1.9.9-SNAPSHOT";
		ModuleUtil.checkRequiredVersion(openMRSVersion, requiredOpenmrsVersion);
	}
	
	@Test
	@Verifies(value = "Should handle ALPHA versions ", method = "checkRequiredVersion(String, String)")
	public void checkRequiredVersion_shouldHandleAlphaVersion() throws Exception {
		String openMRSVersion = "1.9.2-ALPHA";
		String requiredOpenmrsVersion = "1.9.2-ALPHA";
		ModuleUtil.checkRequiredVersion(openMRSVersion, requiredOpenmrsVersion);
	}
	
	private JarFile loadModuleJarFile(String moduleId, String version) throws IOException {
		InputStream moduleStream = null;
		File tempFile = null;
		OutputStream tempFileStream = null;
		JarFile jarFile = null;
		try {
			moduleStream = getClass().getClassLoader().getResourceAsStream(
			    "org/openmrs/module/include/" + moduleId + "-" + version + ".omod");
			Assert.assertNotNull(moduleStream);
			tempFile = File.createTempFile("moduleTest", "omod");
			tempFileStream = new FileOutputStream(tempFile);
			IOUtils.copy(moduleStream, tempFileStream);
			jarFile = new JarFile(tempFile);
		}
		finally {
			IOUtils.closeQuietly(moduleStream);
			IOUtils.closeQuietly(tempFileStream);
			if (tempFile != null) {
				tempFile.delete();
			}
		}
		return jarFile;
	}
	
	/**
	 * @see ModuleUtil#getResourceFromApi(JarFile,String,String,String)
	 */
	@Test
	@Verifies(value = "load file from api as input stream", method = "getResourceFromApi(JarFile,String,String,String)")
	public void getResourceFromApi_shouldLoadFileFromApiAsInputStream() throws Exception {
		String moduleId = "test1";
		String version = "1.0-SNAPSHOT";
		String resource = "messages.properties";
		JarFile moduleJarFile = loadModuleJarFile(moduleId, version);
		Assert.assertNotNull(moduleJarFile);
		InputStream resultStream = ModuleUtil.getResourceFromApi(moduleJarFile, moduleId, version, resource);
		Assert.assertNotNull(resultStream);
	}
	
	/**
	 * @see ModuleUtil#getResourceFromApi(JarFile,String,String,String)
	 */
	@Test
	@Verifies(value = "return null if api is not found", method = "getResourceFromApi(JarFile,String,String,String)")
	public void getResourceFromApi_shouldReturnNullIfApiIsNotFound() throws Exception {
		String moduleId = "logic";
		String version = "0.2";
		String resource = "messages.properties";
		JarFile moduleJarFile = loadModuleJarFile(moduleId, version);
		Assert.assertNotNull(moduleJarFile);
		InputStream resultStream = ModuleUtil.getResourceFromApi(moduleJarFile, moduleId, version, resource);
		Assert.assertNull(resultStream);
	}
	
	/**
	 * @see ModuleUtil#getResourceFromApi(JarFile,String,String,String)
	 */
	@Test
	@Verifies(value = "return null if file is not found in api", method = "getResourceFromApi(JarFile,String,String,String)")
	public void getResourceFromApi_shouldReturnNullIfFileIsNotFoundInApi() throws Exception {
		String moduleId = "test1";
		String version = "1.0-SNAPSHOT";
		String resource = "messages_doesnotexist.properties";
		JarFile moduleJarFile = loadModuleJarFile(moduleId, version);
		Assert.assertNotNull(moduleJarFile);
		InputStream resultStream = ModuleUtil.getResourceFromApi(moduleJarFile, moduleId, version, resource);
		Assert.assertNull(resultStream);
	}

	/**
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 * @verifies expand directory with parent tree if name is directory and keepFullPath is true
	 */
	@Test
	public void expandJar_shouldExpandDirectoryWithParentTreeIfNameIsDirectoryAndKeepFullPathIsTrue() throws Exception {
		final int numberOfFilesInSpecifiedJarDirectory = 2;
		String directoryPath = "META-INF/maven/org.openmrs.module/test1-api";
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, directoryPath, true);

		List<File> actualExpandedFiles = (List<File>)FileUtils.listFiles(destinationFolder, null, true);
		Assert.assertEquals(numberOfFilesInSpecifiedJarDirectory, actualExpandedFiles.size());
		File expectedPath = new File(destinationFolder, directoryPath);
		Assert.assertEquals(expectedPath.toString(), actualExpandedFiles.get(0).getParent());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 * @verifies expand directory without parent tree if name is directory and keepFullPath is false
	 */
	@Test
	public void expandJar_shouldExpandDirectoryWithoutParentTreeIfNameIsDirectoryAndKeepFullPathIsFalse()
			throws Exception {
		final int numberOfFilesInSpecifiedDirectory = 2;
		String directoryPath = "META-INF/maven/org.openmrs.module/test1-api";
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, directoryPath, false);

		List<File> actualExpandedFiles = (List<File>)FileUtils.listFiles(destinationFolder, null, true);
		Assert.assertEquals(numberOfFilesInSpecifiedDirectory, actualExpandedFiles.size());
		Assert.assertEquals(destinationFolder.toString(), actualExpandedFiles.get(0).getParent());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 * @verifies expand entire jar if name is empty string
	 */
	@Test
	public void expandJar_shouldExpandEntireJarIfNameIsEmptyString() throws Exception {
		final int numberOfFilesInJar = 6;
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, "", false);

		Assert.assertEquals(numberOfFilesInJar, FileUtils.listFiles(destinationFolder, null, true).size());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 * @verifies expand entire jar if name is null
	 */
	@Test
	public void expandJar_shouldExpandEntireJarIfNameIsNull() throws Exception {
		final int numberOfFilesInJar = 6;
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, null, false);

		Assert.assertEquals(numberOfFilesInJar, FileUtils.listFiles(destinationFolder, null, true).size());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 * @verifies expand file with parent tree if name is file and keepFullPath is true
	 */
	@Test
	public void expandJar_shouldExpandFileWithParentTreeIfNameIsFileAndKeepFullPathIsTrue() throws Exception {
		String fileName = "META-INF/maven/org.openmrs.module/test1-api/pom.properties";
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, fileName, true);

		List<File> actualExpandedFiles = (List<File>)FileUtils.listFiles(destinationFolder, null, true);
		Assert.assertEquals(1, actualExpandedFiles.size());
		File expectedPath = new File(destinationFolder, fileName);
		Assert.assertEquals(expectedPath.toString(), actualExpandedFiles.get(0).toString());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * Gets Jar file to be expanded.
	 * 
	 * @return <code>File</code> containing Jar file.
	 */
	protected File getJarFile() {
		return new File(this.getClass().getResource("/org/openmrs/module/include/testJarExpand.omod").getFile());
	}
	
	/**
	 * Gets folder to which Jar should be extracted. 
	 * 
	 * @return <code>File</code> containing folder for Jar tests.
	 */
	protected File getEmptyJarDestinationFolder() throws IOException {
		File destinationFolder = new File(System.getProperty("java.io.tmpdir"), "expandedJar");
		if (destinationFolder.exists()) {
			FileUtils.cleanDirectory(destinationFolder);
		}
		else {
			destinationFolder.mkdirs();
		}
		return destinationFolder;
	}
}
