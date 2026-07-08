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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

public class ModuleClassLoaderTest extends BaseContextSensitiveTest {
	
	Module mockModuleV1_0;
	Module mockModuleV2_0;
	
	Map<String, String> mockModules;
	
	@BeforeEach
	public void before() {
		mockModuleV1_0 = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule", "author", "description", "1.0", "1.0");
		mockModuleV2_0 = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule", "author", "description", "2.0", "2.0");
		mockModules = new HashMap<>();
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnTrueIfFileMatchesAndOpenmrsVersionMatches()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		resource.setOpenmrsPlatformVersion("1.7-1.8,1.10-1.11");
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(true));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnFalseIfFileMatchesButOpenmrsVersionDoesNot()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		resource.setOpenmrsPlatformVersion("1.7-1.8, 1.10-1.11");
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.12.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(false));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnTrueIfFileDoesNotMatchAndOpenmrsVersionDoesNotMatch()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api.jar");
		resource.setOpenmrsPlatformVersion("1.10-1.11");
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.9.8-SNAPSHOT", mockModules);
		
		assertThat(result, is(true));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnTrueIfFileMatchesAndModuleVersionMatches()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("3.0-4.0,1.0-2.0");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		mockModules.put("module", "1.1");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(true));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnTrueIfFileMatchesAndModuleIsMissing()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("!");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(true));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnFalseIfFileMatchesAndModuleIsNotMissing()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("!");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		ModuleFactory.getStartedModulesMap().put("module", new Module("", "module", "", "", "", "3.0", "1.0"));
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(false));
		
		ModuleFactory.getStartedModulesMap().clear();
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnFalseIfFileMatchesAndModuleVersionDoesNotMatch()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("1.0-2.0");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(false));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnFalseIfFileMatchesAndOpenmrsVersionMatchesButModuleVersionDoesNotMatch()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		resource.setOpenmrsPlatformVersion("1.10");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("1.0-2.0,4.0");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(false));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnFalseIfFileMatchesAndModuleNotFound() throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("1.0-2.0");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		mockModules.put("differentModule", "1.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(false));
	}
	
	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnTrueIfFileDoesNotMatchAndModuleVersionDoesNotMatch()
	        throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api.jar");
		
		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("1.0-2.0");
		resource.getModules().add(module);
		
		mockModuleV1_0.getConditionalResources().add(resource);
		
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(true));
	}

	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_ShouldNotIncludeWhenVersionWildcardMatchUsingGlobs() throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "!";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();
		final String relatedModuleVersion = "1.2.3";
		
		Map<String, String> startedRelatedModules = new HashMap<>();
		startedRelatedModules.put(conditionalResourceModuleId, relatedModuleVersion);

		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath(conditionalResourcePath);
		resource.setOpenmrsPlatformVersion("1.7-1.8,1.10-1.11");
		resource.setModules(Collections.singletonList(moduleIdAndVersion));
		mockModuleV2_0.getConditionalResources().add(resource);

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId, new Module("", conditionalResourceModuleId, "", "", "", "3.0", "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, "1.10.0-SNAPSHOT", startedRelatedModules);

		assertThat(result, is(false));
		
		ModuleFactory.getStartedModulesMap().clear();
	}

	@Test
	public void shouldResourceBeIncluded_ShouldIncludeMatchingPlatformVersionsUsingGlobs()
		throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String commonModuleVersion = "1.2.3";
		final String commonPlatformVersion = "1.6.0";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

		Map<String, String> startedRelatedModules = new HashMap<>();
		startedRelatedModules.put(conditionalResourceModuleId, commonModuleVersion);

		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(commonModuleVersion);

		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath(conditionalResourcePath);
		resource.setOpenmrsPlatformVersion(commonPlatformVersion);
		resource.setModules(Collections.singletonList(moduleIdAndVersion));
		mockModuleV2_0.getConditionalResources().add(resource);

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId, new Module("", conditionalResourceModuleId, "", "", "", commonModuleVersion, "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, commonPlatformVersion, startedRelatedModules);

		assertThat(result, is(true));

		ModuleFactory.getStartedModulesMap().clear();
	}

	@Test
	public void shouldResourceBeIncluded_ShouldNotIncludeModuleWhenVersionsMatchUsingGlobs()
		throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String commonPlatformVersion = "1.2.3";
		final String conditionalResourceModuleVersion = "1.2.*";
		final String otherModuleVersion = "2.2.2";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

		Map<String, String> startedRelatedModules = new HashMap<>();
		startedRelatedModules.put(conditionalResourceModuleId, otherModuleVersion);

		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceModuleVersion);

		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath(conditionalResourcePath);
		resource.setOpenmrsPlatformVersion(commonPlatformVersion);
		resource.setModules(Collections.singletonList(moduleIdAndVersion));
		mockModuleV2_0.getConditionalResources().add(resource);

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId, new Module("", conditionalResourceModuleId, "", "", "", otherModuleVersion, "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, commonPlatformVersion, startedRelatedModules);

		assertThat(result, is(false));

		ModuleFactory.getStartedModulesMap().clear();
	}
	
	@Test
	public void shouldResourceBeIncluded_ShouldNotIncludeWhenPlatformVersionsMisnmatchUsingGlobs()
		throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

		final String conditionalResourcePlatformVersion = "1.0.*";
		final String openmrsPlatformVersion = "2.0.0";

		Map<String, String> startedRelatedModules = new HashMap<>();
		startedRelatedModules.put(conditionalResourceModuleId, conditionalResourceVersion);

		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath(conditionalResourcePath);
		resource.setOpenmrsPlatformVersion(conditionalResourcePlatformVersion);
		resource.setModules(Collections.singletonList(moduleIdAndVersion));
		mockModuleV2_0.getConditionalResources().add(resource);

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId, new Module("", conditionalResourceModuleId, "", "", "", "3.0", "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, openmrsPlatformVersion, startedRelatedModules);

		assertThat(result, is(false));

		ModuleFactory.getStartedModulesMap().clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"lib/jackson-mapper-asl-1.9.13.jar",
		"/lib/jackson-mapper-asl-1.9.13.jar",
		"\\lib/jackson-mapper-asl-1.9.13.jar",
		"atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
		"/atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
		"\\atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
		"/lib/jackson-mapper-asl-**.jar",
		"/lib/jackson-mapper-asl-**",
		"/lib/jackson-**-1.9.13.jar",
		"atomfeed/*/jackson-mapper-asl-1.9.13.jar",
		"C:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
		"D:\\atomfeed\\lib\\jackson-mapper-asl-1.9.13.jar"
	})
	void testGlobPatternMatches(String filePath) throws MalformedURLException {
		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource conditionalResource = new ModuleConditionalResource();
		conditionalResource.setPath(filePath);

		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();
		assertTrue(ModuleClassLoader.isMatchingConditionalResource(mockModuleV2_0, fileUrl, conditionalResource),
			"Path should match glob pattern: " + filePath);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"/libs/jackson-mapper-asl.jar",
		"/library/jackson-mapper-asl-1.9.13.jar",
		"/otherpath/lib/jackson-mapping-asl-1.9.13.jar",
		"/lib/jackson-mapper-xyz.jar",
		"/lib/jackson-mapper-asl.txt",
		"/lib/jackson-mapper-asl-1.9.13.json",
		"/lib/otherfolder/jackson-mapper-asl.jar",
		"/lib/jackson-mapper-asl/subdir/jackson-mapper-asl-1.9.13.jar",
		"C:/atomfeeds/lib/jackson-mapper-asl.jar",
		"D:\\lib\\jackson-mapper-asl-1.9.13.doc",
		"/a/b/c/atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
		"/a/b/c/atomfeed/lib/jackson-mapper-asl-**.jar",
	})
	void testGlobPatternDoesNotMatch(String filePath) throws MalformedURLException {
		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource conditionalResource = new ModuleConditionalResource();
		conditionalResource.setPath(filePath);

		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

		assertFalse(ModuleClassLoader.isMatchingConditionalResource(mockModuleV2_0, fileUrl, conditionalResource),
			"Path should not match glob pattern: " + filePath);
	}

	@Test
	public void getLibCacheFolderForModule_shouldRecreateNonEmptyDirWhenModuleChanged() throws Exception {
		File tempLibCache = Files.createTempDirectory("libcache-test").toFile();
		Field libCacheFolderField = OpenmrsClassLoader.class.getDeclaredField("libCacheFolder");
		libCacheFolderField.setAccessible(true);
		File savedLibCacheFolder = (File) libCacheFolderField.get(null);
		libCacheFolderField.set(null, tempLibCache);

		Field libCacheFoldersField = ModuleClassLoader.class.getDeclaredField("libCacheFolders");
		libCacheFoldersField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, File> libCacheFolders = (Map<String, File>) libCacheFoldersField.get(null);

		File omodFile = File.createTempFile("testmodule", ".omod");
		try {
			Context.getRuntimeProperties().setProperty("optimized.startup", "true");
			long currentTimestamp = System.currentTimeMillis();
			omodFile.setLastModified(currentTimestamp);

			Module module = new Module("testmodule", "testmodule", "org.openmrs.module.testmodule",
				"author", "description", "1.0", "1.0");
			module.setFile(omodFile);

			File moduleDir = new File(tempLibCache, "testmodule");
			moduleDir.mkdirs();
			FileUtils.writeStringToFile(new File(moduleDir, ".moduleLastModified"),
				String.valueOf(currentTimestamp - 1000), Charset.defaultCharset());
			File staleFile = new File(moduleDir, "old-resource.txt");
			FileUtils.writeStringToFile(staleFile, "stale content", Charset.defaultCharset());

			ModuleClassLoader.getLibCacheFolderForModule(module);

			assertFalse(staleFile.exists(), "Stale file should be deleted when module changes");
			assertTrue(moduleDir.exists(), "Module dir should be recreated");
		} finally {
			Context.getRuntimeProperties().remove("optimized.startup");
			libCacheFolderField.set(null, savedLibCacheFolder);
			libCacheFolders.remove("testmodule");
			omodFile.delete();
			FileUtils.deleteDirectory(tempLibCache);
		}
	}

	@Test
	public void getLibCacheFolderForModule_shouldRecreateNonEmptyDirWhenOptimizedStartupDisabled() throws Exception {
		File tempLibCache = Files.createTempDirectory("libcache-test").toFile();
		Field libCacheFolderField = OpenmrsClassLoader.class.getDeclaredField("libCacheFolder");
		libCacheFolderField.setAccessible(true);
		File savedLibCacheFolder = (File) libCacheFolderField.get(null);
		libCacheFolderField.set(null, tempLibCache);

		Field libCacheFoldersField = ModuleClassLoader.class.getDeclaredField("libCacheFolders");
		libCacheFoldersField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, File> libCacheFolders = (Map<String, File>) libCacheFoldersField.get(null);

		File omodFile = File.createTempFile("testmodule2", ".omod");
		try {
			Module module = new Module("testmodule2", "testmodule2", "org.openmrs.module.testmodule2",
				"author", "description", "1.0", "1.0");
			module.setFile(omodFile);

			File moduleDir = new File(tempLibCache, "testmodule2");
			moduleDir.mkdirs();
			File staleFile = new File(moduleDir, "old-resource.txt");
			FileUtils.writeStringToFile(staleFile, "stale content", Charset.defaultCharset());

			Context.getRuntimeProperties().setProperty("optimized.startup", "false");

			ModuleClassLoader.getLibCacheFolderForModule(module);

			assertFalse(staleFile.exists(), "Stale file should be deleted when optimized startup is disabled");
			assertTrue(moduleDir.exists(), "Module dir should be recreated");
		} finally {
			Context.getRuntimeProperties().remove("optimized.startup");
			libCacheFolderField.set(null, savedLibCacheFolder);
			libCacheFolders.remove("testmodule2");
			omodFile.delete();
			FileUtils.deleteDirectory(tempLibCache);
		}
	}

	@Test
	void testIsMatchingConditionalResourceWithNullConfigVersionDoesNotThrow() throws MalformedURLException {
		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource conditionalResource = new ModuleConditionalResource();
		conditionalResource.setPath("/libs/jackson-mapper-asl.jar");

		final Module moduleWithNullConfigVersions = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule", "author", "description", "2.0", null);

		final URL fileUrl = URI.create(
			"file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();
		assertFalse(ModuleClassLoader.isMatchingConditionalResource(moduleWithNullConfigVersions, fileUrl, conditionalResource));
	}
}
