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
	public void checkMandatoryModulesStarted_shouldThrowModuleExceptionIfAMandatoryModuleIsNotStarted() {
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
	public void getMandatoryModules_shouldReturnMandatoryModuleIds() {
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
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test
	public void isOpenmrsVersionInVersions_shouldReturnFalseWhenVersionsIsNull() {
		Assert.assertFalse(ModuleUtil.isOpenmrsVersionInVersions((String[]) null));
	}
	
	/**
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test
	public void isOpenmrsVersionInVersions_shouldReturnFalseWhenVersionsIsEmpty() {

		Assert.assertFalse(ModuleUtil.isOpenmrsVersionInVersions());
	}

	/**
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test
	public void isOpenmrsVersionInVersions_shouldReturnTrueIfCurrentOpenmrsVersionMatchesOneElementInVersions()
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
	 * @see ModuleUtil#isOpenmrsVersionInVersions(String[])
	 */
	@Test
	public void isOpenmrsVersionInVersions_shouldReturnFalseIfCurrentOpenmrsVersionDoesNotMatchAnyElementInVersions()
	        throws Exception {

		Field versionField = OpenmrsConstants.class.getDeclaredField("OPENMRS_VERSION_SHORT");
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(versionField, versionField.getModifiers() & ~Modifier.FINAL);
		versionField.set(null, "1.9.8");

		Assert.assertFalse(ModuleUtil.isOpenmrsVersionInVersions("1.11.*", "2.1.0"));
	}

	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRangedRequiredVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.3 - 1.4.4";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRangedRequiredVersionWithWildCard() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.2.* - 1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRangedRequiredVersionWithWildCardOnOneEnd() {
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
	public void matchRequiredVersions_shouldAllowSingleEntryForRequiredVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.2";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRequiredVersionWithWildCard() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowNonNumericCharacterRequiredVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.3a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterRequiredVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.1a - 1.4.3a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterWithWildCard() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*a - 1.4.*a";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowRangedNonNumericCharacterWithWildCardOnOneEnd() {
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
	public void matchRequiredVersions_shouldReturnFalseWhenOpenmrsVersionBeyondWildCardRange() {
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
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionBeyondOpenmrsVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}

	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionWithWildCardBeyondOpenmrsVersion()
	{
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.* - 1.6.*";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldReturnFalseWhenRequiredVersionWithWildCardOnOneEndBeyondOpenmrsVersion()
	{
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
	public void matchRequiredVersions_shouldReturnFalseWhenSingleEntryRequiredVersionBeyondOpenmrsVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.0";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredOpenmrsVersion));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldAllowReleaseTypeInTheVersion() {
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
	public void matchRequiredVersions_shouldMatchWhenRevisionNumberIsBelowMaximumRevisionNumber() {
		String openmrsVersion = "1.4.1111";
		String requiredVersion = "1.4.*";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
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
	public void matchRequiredVersions_shouldNotMatchWhenVersionHasWildCardAndIsOutsideBoundary() {
		String openmrsVersion = "1.*.4";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldMatchWhenVersionHasWildCardAndIsWithinBoundary() {
		String openmrsVersion = "1.4.*";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldNotMatchWhenVersionHasWildPlusQualifierCardAndIsOutsideBoundary() {
		String openmrsVersion = "1.*.4-SNAPSHOT";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertFalse(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldMatchWhenVersionHasWildCardPlusQualifierAndIsWithinBoundary() {
		String openmrsVersion = "1.4.*-SNAPSHOT";
		String requiredVersion = "1.4.0 - 1.4.10";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
	public void matchRequiredVersions_shouldReturnTrueWhenRequiredVersionIsEmpty() {
		String openmrsVersion = "1.11.4";
		String requiredVersion = "";
		Assert.assertTrue(ModuleUtil.matchRequiredVersions(openmrsVersion, requiredVersion));
	}
	
	/**
	 * @see ModuleUtil#matchRequiredVersions(String,String)
	 */
	@Test
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
	public void getPathForResource_shouldHandleUiSpringmvcCssUiDotCssExample() {
		Module module = new Module("Unit test");
		module.setModuleId("ui.springmvc");
		String path = "/ui/springmvc/css/ui.css";
		Assert.assertEquals("/css/ui.css", ModuleUtil.getPathForResource(module, path));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#getModuleForPath(String)
	 */
	@Test
	public void getModuleForPath_shouldHandleUiSpringmvcCssUiDotCssWhenUiDotSpringmvcModuleIsRunning() {
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
	public void getModuleForPath_shouldHandleUiSpringmvcCssUiDotCssWhenUiModuleIsRunning() {
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
	public void getModuleForPath_shouldReturnNullForUiSpringmvcCssUiDotCssWhenNoRelevantModuleIsRunning() {
		ModuleFactory.getStartedModulesMap().clear();
		String path = "/ui/springmvc/css/ui.css";
		Assert.assertNull(ModuleUtil.getModuleForPath(path));
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	public void checkRequiredVersion_shouldThrowModuleExceptionIfOpenmrsVersionBeyondWildCardRange() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.3.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionBeyondOpenmrsVersion() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionWithWildCardBeyondOpenmrsVersion()
	{
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.* - 1.6.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	public void checkRequiredVersion_shouldThrowModuleExceptionIfRequiredVersionWithWildCardOnOneEndBeyondOpenmrsVersion()
	{
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.5 - 1.5.*";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test(expected = ModuleException.class)
	public void checkRequiredVersion_shouldThrowModuleExceptionIfSingleEntryRequiredVersionBeyondOpenmrsVersion()
	{
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.5.0";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#compareVersion(String,String)
	 */
	@Test
	public void compareVersion_shouldCorrectlyComparingTwoVersionNumbers() {
		String olderVersion = "2.1.1";
		String newerVersion = "2.1.10";
		Assert.assertTrue(ModuleUtil.compareVersion(olderVersion, newerVersion) < 0);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#compareVersion(String,String)
	 */
	@Test
	public void compareVersion_shouldTreatSNAPSHOTAsEarliestVersion() {
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
	public void checkRequiredVersion_shouldThrowModuleExceptionIfSNAPSHOTNotHandledCorrectly() {
		String openmrsVersion = "1.4.3";
		String requiredOpenmrsVersion = "1.4.5-SNAPSHOT";
		ModuleUtil.checkRequiredVersion(openmrsVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test
	public void checkRequiredVersion_shouldHandleSnapshotVersion() {
		String openMRSVersion = "1.9.2-SNAPSHOT";
		String requiredOpenmrsVersion = "1.9.2-SNAPSHOT";
		ModuleUtil.checkRequiredVersion(openMRSVersion, requiredOpenmrsVersion);
	}
	
	/**
	 * @see org.openmrs.module.ModuleUtil#checkRequiredVersion(String, String)
	 */
	@Test
	public void checkRequiredVersion_shouldHandleUuidSuffixVersion() {
		String openMRSVersion = "1.9.9-f4927f";
		String requiredOpenmrsVersion = "1.9.9-SNAPSHOT";
		ModuleUtil.checkRequiredVersion(openMRSVersion, requiredOpenmrsVersion);
	}
	
	@Test
	public void checkRequiredVersion_shouldHandleAlphaVersion() {
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
	 * @throws IOException
	 * @see ModuleUtil#getResourceFromApi(JarFile,String,String,String)
	 */
	@Test
	public void getResourceFromApi_shouldLoadFileFromApiAsInputStream() throws IOException {
		String moduleId = "test1";
		String version = "1.0-SNAPSHOT";
		String resource = "messages.properties";
		JarFile moduleJarFile = loadModuleJarFile(moduleId, version);
		Assert.assertNotNull(moduleJarFile);
		InputStream resultStream = ModuleUtil.getResourceFromApi(moduleJarFile, moduleId, version, resource);
		Assert.assertNotNull(resultStream);
	}
	
	/**
	 * @throws IOException
	 * @see ModuleUtil#getResourceFromApi(JarFile,String,String,String)
	 */
	@Test
	public void getResourceFromApi_shouldReturnNullIfApiIsNotFound() throws IOException {
		String moduleId = "logic";
		String version = "0.2";
		String resource = "messages.properties";
		JarFile moduleJarFile = loadModuleJarFile(moduleId, version);
		Assert.assertNotNull(moduleJarFile);
		InputStream resultStream = ModuleUtil.getResourceFromApi(moduleJarFile, moduleId, version, resource);
		Assert.assertNull(resultStream);
	}
	
	/**
	 * @throws IOException
	 * @see ModuleUtil#getResourceFromApi(JarFile,String,String,String)
	 */
	@Test
	public void getResourceFromApi_shouldReturnNullIfFileIsNotFoundInApi() throws IOException {
		String moduleId = "test1";
		String version = "1.0-SNAPSHOT";
		String resource = "messages_doesnotexist.properties";
		JarFile moduleJarFile = loadModuleJarFile(moduleId, version);
		Assert.assertNotNull(moduleJarFile);
		InputStream resultStream = ModuleUtil.getResourceFromApi(moduleJarFile, moduleId, version, resource);
		Assert.assertNull(resultStream);
	}

	/**
	 * @throws IOException
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 */
	@Test
	public void expandJar_shouldExpandDirectoryWithParentTreeIfNameIsDirectoryAndKeepFullPathIsTrue() throws IOException {
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
	 * @throws IOException
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 */
	@Test
	public void expandJar_shouldExpandDirectoryWithoutParentTreeIfNameIsDirectoryAndKeepFullPathIsFalse()
	        throws IOException {
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
	 * @throws IOException
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 */
	@Test
	public void expandJar_shouldExpandEntireJarIfNameIsEmptyString() throws IOException {
		final int numberOfFilesInJar = 6;
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, "", false);

		Assert.assertEquals(numberOfFilesInJar, FileUtils.listFiles(destinationFolder, null, true).size());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * @throws IOException
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 */
	@Test
	public void expandJar_shouldExpandEntireJarIfNameIsNull() throws IOException {
		final int numberOfFilesInJar = 6;
		File destinationFolder = this.getEmptyJarDestinationFolder();

		ModuleUtil.expandJar(getJarFile(), destinationFolder, null, false);

		Assert.assertEquals(numberOfFilesInJar, FileUtils.listFiles(destinationFolder, null, true).size());

		FileUtils.deleteDirectory(destinationFolder);
	}

	/**
	 * @throws IOException
	 * @see ModuleUtil#expandJar(File,File,String,boolean)
	 */
	@Test
	public void expandJar_shouldExpandFileWithParentTreeIfNameIsFileAndKeepFullPathIsTrue() throws IOException {
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
