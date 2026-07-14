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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ModuleClassLoaderTest extends BaseContextSensitiveTest {

	Module mockModuleV1_0;

	Module mockModuleV2_0;

	Map<String, String> mockModules;

	@BeforeEach
	public void before() {
		mockModuleV1_0 = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule", "author", "description",
		        "1.0", "1.0");
		mockModuleV2_0 = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule", "author", "description",
		        "2.0", "2.0");
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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.12.0-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.9.8-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

		assertThat(result, is(true));
	}

	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnTrueIfFileMatchesAndModuleIsMissing() throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");

		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("!");
		resource.getModules().add(module);

		mockModuleV1_0.getConditionalResources().add(resource);

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

		assertThat(result, is(true));
	}

	/**
	 * @throws MalformedURLException
	 * @see ModuleClassLoader#shouldResourceBeIncluded(Module, java.net.URL, String, java.util.Map)
	 */
	@Test
	public void shouldResourceBeIncluded_shouldReturnFalseIfFileMatchesAndModuleIsNotMissing() throws MalformedURLException {
		ModuleConditionalResource resource = new ModuleConditionalResource();
		resource.setPath("lib/mockmodule-api-1.10.jar");

		ModuleConditionalResource.ModuleAndVersion module = new ModuleConditionalResource.ModuleAndVersion();
		module.setModuleId("module");
		module.setVersion("!");
		resource.getModules().add(module);

		mockModuleV1_0.getConditionalResources().add(resource);

		ModuleFactory.getStartedModulesMap().put("module", new Module("", "module", "", "", "", "3.0", "1.0"));
		mockModules.put("module", "3.0");

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

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

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV1_0,
		    URI.create("file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);

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
		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();
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

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId,
		    new Module("", conditionalResourceModuleId, "", "", "", "3.0", "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, "1.10.0-SNAPSHOT",
		    startedRelatedModules);

		assertThat(result, is(false));

		ModuleFactory.getStartedModulesMap().clear();
	}

	@Test
	public void shouldResourceBeIncluded_ShouldIncludeMatchingPlatformVersionsUsingGlobs() throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String commonModuleVersion = "1.2.3";
		final String commonPlatformVersion = "1.6.0";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

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

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId,
		    new Module("", conditionalResourceModuleId, "", "", "", commonModuleVersion, "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, commonPlatformVersion,
		    startedRelatedModules);

		assertThat(result, is(true));

		ModuleFactory.getStartedModulesMap().clear();
	}

	@Test
	public void shouldResourceBeIncluded_ShouldNotIncludeModuleWhenVersionsMatchUsingGlobs() throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String commonPlatformVersion = "1.2.3";
		final String conditionalResourceModuleVersion = "1.2.*";
		final String otherModuleVersion = "2.2.2";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

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

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId,
		    new Module("", conditionalResourceModuleId, "", "", "", otherModuleVersion, "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, commonPlatformVersion,
		    startedRelatedModules);

		assertThat(result, is(false));

		ModuleFactory.getStartedModulesMap().clear();
	}

	@Test
	public void shouldResourceBeIncluded_ShouldNotIncludeWhenPlatformVersionsMisnmatchUsingGlobs()
	        throws MalformedURLException {

		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		final String conditionalResourcePath = "/lib/jackson-mapper-asl*";
		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

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

		ModuleFactory.getStartedModulesMap().put(conditionalResourceModuleId,
		    new Module("", conditionalResourceModuleId, "", "", "", "3.0", "1.0"));

		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModuleV2_0, fileUrl, openmrsPlatformVersion,
		    startedRelatedModules);

		assertThat(result, is(false));

		ModuleFactory.getStartedModulesMap().clear();
	}

	@ParameterizedTest
	@ValueSource(strings = { "lib/jackson-mapper-asl-1.9.13.jar", "/lib/jackson-mapper-asl-1.9.13.jar",
	        "\\lib/jackson-mapper-asl-1.9.13.jar", "atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
	        "/atomfeed/lib/jackson-mapper-asl-1.9.13.jar", "\\atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
	        "/lib/jackson-mapper-asl-**.jar", "/lib/jackson-mapper-asl-**", "/lib/jackson-**-1.9.13.jar",
	        "atomfeed/*/jackson-mapper-asl-1.9.13.jar", "C:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
	        "D:\\atomfeed\\lib\\jackson-mapper-asl-1.9.13.jar" })
	void testGlobPatternMatches(String filePath) throws MalformedURLException {
		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource conditionalResource = new ModuleConditionalResource();
		conditionalResource.setPath(filePath);

		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();
		assertTrue(ModuleClassLoader.isMatchingConditionalResource(mockModuleV2_0, fileUrl, conditionalResource),
		    "Path should match glob pattern: " + filePath);
	}

	@ParameterizedTest
	@ValueSource(strings = { "/libs/jackson-mapper-asl.jar", "/library/jackson-mapper-asl-1.9.13.jar",
	        "/otherpath/lib/jackson-mapping-asl-1.9.13.jar", "/lib/jackson-mapper-xyz.jar", "/lib/jackson-mapper-asl.txt",
	        "/lib/jackson-mapper-asl-1.9.13.json", "/lib/otherfolder/jackson-mapper-asl.jar",
	        "/lib/jackson-mapper-asl/subdir/jackson-mapper-asl-1.9.13.jar", "C:/atomfeeds/lib/jackson-mapper-asl.jar",
	        "D:\\lib\\jackson-mapper-asl-1.9.13.doc", "/a/b/c/atomfeed/lib/jackson-mapper-asl-1.9.13.jar",
	        "/a/b/c/atomfeed/lib/jackson-mapper-asl-**.jar", })
	void testGlobPatternDoesNotMatch(String filePath) throws MalformedURLException {
		final String conditionalResourceModuleId = "module123";
		final String conditionalResourceVersion = "1.0.0";
		ModuleConditionalResource.ModuleAndVersion moduleIdAndVersion = new ModuleConditionalResource.ModuleAndVersion();
		moduleIdAndVersion.setModuleId(conditionalResourceModuleId);
		moduleIdAndVersion.setVersion(conditionalResourceVersion);

		ModuleConditionalResource conditionalResource = new ModuleConditionalResource();
		conditionalResource.setPath(filePath);

		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();

		assertFalse(ModuleClassLoader.isMatchingConditionalResource(mockModuleV2_0, fileUrl, conditionalResource),
		    "Path should not match glob pattern: " + filePath);
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

		final Module moduleWithNullConfigVersions = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule",
		        "author", "description", "2.0", null);

		final URL fileUrl = URI.create("file:/atomfeed/lib/jackson-mapper-asl-1.9.13.jar").toURL();
		assertFalse(
		    ModuleClassLoader.isMatchingConditionalResource(moduleWithNullConfigVersions, fileUrl, conditionalResource));
	}

	/**
	 * Builds a module whose backing omod is a fresh temp file, with a module id unique to the calling
	 * test so each test works in its own folder under the shared lib cache root.
	 */
	private Module moduleWithTempOmod(String moduleId) throws IOException {
		Module module = new Module("mockmodule", moduleId, "org.openmrs.module." + moduleId, "author", "description", "2.0",
		        "2.0");
		File omod = File.createTempFile(moduleId, ".omod");
		omod.deleteOnExit();
		module.setFile(omod);
		return module;
	}

	/**
	 * @see ModuleClassLoader#getLibCacheFolderForModule(Module)
	 */
	@Test
	public void getLibCacheFolderForModule_shouldPruneJarsDroppedByAModifiedModule() throws IOException {
		Module module = moduleWithTempOmod("libcacheprunetest");
		File omod = module.getFile();

		// Prime the cache folder to look like the previous version's expansion: a lib jar the new
		// version no longer ships, plus a .moduleLastModified marker predating this omod so the
		// module counts as modified.
		File cacheFolder = new File(OpenmrsClassLoader.getLibCacheFolder(), module.getModuleId());
		File staleJar = new File(new File(cacheFolder, "lib"), "dropped-by-upgrade.jar");
		FileUtils.writeStringToFile(staleJar, "stale", Charset.defaultCharset());
		File marker = new File(cacheFolder, ".moduleLastModified");
		FileUtils.writeStringToFile(marker, Long.toString(omod.lastModified() - 1000L), Charset.defaultCharset());
		assertTrue(staleJar.exists());

		try {
			File result = ModuleClassLoader.getLibCacheFolderForModule(module);

			assertThat(result, is(cacheFolder));
			assertFalse(staleJar.exists(), "a jar dropped by the upgraded module must not linger in the lib cache");
			assertTrue(cacheFolder.isDirectory(), "the lib cache folder should be recreated for re-expansion");
			// The successful clear must also refresh the marker, or every later optimized startup
			// would re-delete and re-expand the cache, silently defeating optimized startup.
			assertThat(FileUtils.readFileToString(marker, Charset.defaultCharset()), is(Long.toString(omod.lastModified())));
		} finally {
			FileUtils.deleteQuietly(cacheFolder);
		}
	}

	/**
	 * @see ModuleClassLoader#getLibCacheFolderForModule(Module)
	 */
	@Test
	public void getLibCacheFolderForModule_shouldClearTheCacheWhenTheLastModifiedMarkerIsUnparseable() throws IOException {
		Module module = moduleWithTempOmod("libcachemarkertest");

		// A corrupt marker we cannot parse: we can no longer tell whether the module changed, so the
		// cache must be cleared rather than reused with possibly stale jars.
		File cacheFolder = new File(OpenmrsClassLoader.getLibCacheFolder(), module.getModuleId());
		File staleJar = new File(new File(cacheFolder, "lib"), "dropped-by-upgrade.jar");
		FileUtils.writeStringToFile(staleJar, "stale", Charset.defaultCharset());
		FileUtils.writeStringToFile(new File(cacheFolder, ".moduleLastModified"), "not-a-timestamp",
		    Charset.defaultCharset());
		assertTrue(staleJar.exists());

		try {
			ModuleClassLoader.getLibCacheFolderForModule(module);

			assertFalse(staleJar.exists(), "an unparseable last-modified marker must clear the cache rather than reuse it");
		} finally {
			FileUtils.deleteQuietly(cacheFolder);
		}
	}

	/**
	 * @see ModuleClassLoader#deleteLibCacheFolder(File)
	 */
	@Test
	public void deleteLibCacheFolder_shouldRefuseToDeleteAFolderOutsideTheLibCacheRoot() throws IOException {
		// A folder that is a sibling of the lib cache root, i.e. not a direct child, as a malformed
		// module id such as "../something" would resolve to.
		File outside = new File(OpenmrsClassLoader.getLibCacheFolder().getParentFile(), "libcache-guard-test");
		File keep = new File(outside, "keep.txt");
		FileUtils.writeStringToFile(keep, "keep", Charset.defaultCharset());
		assertTrue(keep.exists());

		try {
			assertFalse(ModuleClassLoader.deleteLibCacheFolder(outside),
			    "a refused delete must not report the folder as cleared");

			assertTrue(keep.exists(),
			    "must refuse to recursively delete a folder that is not directly inside the lib cache root");
		} finally {
			FileUtils.deleteQuietly(outside);
		}
	}

	/**
	 * @see ModuleClassLoader#getLibCacheFolderForModule(Module)
	 */
	@Test
	@DisabledOnOs(OS.WINDOWS) // java.io.File cannot remove a directory's read permission on Windows
	public void getLibCacheFolderForModule_shouldNotRefreshTheMarkerWhenTheCacheCannotBeCleared() throws IOException {
		Module module = moduleWithTempOmod("libcachefailedcleartest");
		File omod = module.getFile();

		// A folder the recursive delete cannot fully remove, standing in for jars still locked by the
		// previous classloader during a hot upgrade: commons-io restores write permissions while
		// deleting (OVERRIDE_READ_ONLY), but it cannot walk a directory it is not allowed to read.
		File cacheFolder = new File(OpenmrsClassLoader.getLibCacheFolder(), module.getModuleId());
		File undeletable = new File(new File(cacheFolder, "lib"), "undeletable");
		FileUtils.writeStringToFile(new File(undeletable, "child.txt"), "x", Charset.defaultCharset());
		File marker = new File(cacheFolder, ".moduleLastModified");
		FileUtils.writeStringToFile(marker, Long.toString(omod.lastModified() - 1000L), Charset.defaultCharset());
		assumeTrue(undeletable.setReadable(false, false));

		try {
			ModuleClassLoader.getLibCacheFolderForModule(module);

			// If the folder was cleared anyway (e.g. running as root), the failure path was not exercised.
			assumeTrue(undeletable.exists());
			String stamped = marker.exists() ? FileUtils.readFileToString(marker, Charset.defaultCharset()) : null;
			assertNotEquals(Long.toString(omod.lastModified()), stamped,
			    "the last modified marker must not be refreshed over a cache that could not be cleared, otherwise "
			            + "the next optimized startup trusts the stale jars instead of retrying the delete");
		} finally {
			undeletable.setReadable(true, false);
			FileUtils.deleteQuietly(cacheFolder);
		}
	}

	/**
	 * @see ModuleClassLoader#getLibCacheFolderForModule(Module)
	 */
	@Test
	public void getLibCacheFolderForModule_shouldReplaceAStrayFileAtTheCacheFolderPath() throws IOException {
		Module module = moduleWithTempOmod("libcachestrayfiletest");

		// A regular file sitting where the module's cache folder should be; FileUtils.deleteDirectory
		// would throw an unchecked IllegalArgumentException for it and fail the module's startup.
		File cacheFolder = new File(OpenmrsClassLoader.getLibCacheFolder(), module.getModuleId());
		FileUtils.writeStringToFile(cacheFolder, "stray", Charset.defaultCharset());
		assertTrue(cacheFolder.isFile());

		try {
			File result = ModuleClassLoader.getLibCacheFolderForModule(module);

			assertThat(result, is(cacheFolder));
			assertTrue(cacheFolder.isDirectory(),
			    "a stray regular file at the cache folder path must be replaced with the folder");
		} finally {
			FileUtils.deleteQuietly(cacheFolder);
		}
	}
}
