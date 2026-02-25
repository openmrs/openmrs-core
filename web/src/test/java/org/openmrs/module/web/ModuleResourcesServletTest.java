/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleUtil;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;



/**
 * Unit tests for {@link ModuleResourcesServlet} focusing on path traversal security.
 *
 * <p>These tests verify the fix for the path traversal vulnerability where
 * user-supplied pathInfo was used to construct file paths without verifying
 * the resolved path stays within the expected module resource directory.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModuleResourcesServletTest {

	private ModuleResourcesServlet servlet;
	private HttpServletRequest request;
	private ServletContext servletContext;
	private Module module;
	private Path tempWebRoot;
	private Path resourcesDir;
	private File legitimateFile;


	@BeforeEach
	public void setUp() throws IOException {
		tempWebRoot = Files.createTempDirectory("openmrs-servlet-test");
		resourcesDir = tempWebRoot
			.resolve("WEB-INF")
			.resolve("view")
			.resolve("module")
			.resolve("testmodule")
			.resolve("resources");
		Files.createDirectories(resourcesDir);
		
		legitimateFile = resourcesDir.resolve("style.css").toFile();
		Files.writeString(legitimateFile.toPath(), "body { color: red; }");
		
		servletContext = mock(ServletContext.class);
		when(servletContext.getRealPath("")).thenReturn(tempWebRoot.toAbsolutePath().toString());
		
		module = mock(Module.class);
		when(module.getModuleId()).thenReturn("testmodule");
		when(module.getModuleIdAsPath()).thenReturn("testmodule");
		
		servlet = new ModuleResourcesServlet() {
			@Override
			public ServletContext getServletContext() {
				return servletContext;
			}
		};

		request = mock(HttpServletRequest.class);
	}

	@AfterEach
	public void tearDown() throws IOException {
		// Clean up temp files
		deleteRecursively(tempWebRoot.toFile());
	}


	@Test
	@DisplayName("Should return null when pathInfo is null")
	public void getFile_shouldReturnNullWhenPathIsNull() {
		when(request.getPathInfo()).thenReturn(null);

		File result = servlet.getFile(request);

		assertNull(result, "Expected null when pathInfo is null");
	}

	@Test
	@DisplayName("Should block null-byte injection in path")
	public void getFile_shouldBlockNullByteInjection() {
		when(request.getPathInfo()).thenReturn("/testmodule/style.css\0.jpg");

		File result = servlet.getFile(request);

		assertNull(result, "Expected null when path contains a null byte");
	}

	@Test
	@DisplayName("Should block null-byte injection mid-path")
	public void getFile_shouldBlockNullByteInjectionMidPath() {
		when(request.getPathInfo()).thenReturn("/testmodule/\0../../etc/passwd");

		File result = servlet.getFile(request);

		assertNull(result, "Expected null when path contains a null byte mid-path");
	}
	
	
	// Path Traversal Attack Tests
	@Test
	@DisplayName("Should block classic ../ path traversal")
	public void getFile_shouldBlockClassicPathTraversal() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath("/testmodule/../../WEB-INF/web.xml"))
				.thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, "/testmodule/../../WEB-INF/web.xml"))
				.thenReturn("/../../WEB-INF/web.xml");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn("/testmodule/../../WEB-INF/web.xml");

			File result = servlet.getFile(request);

			assertNull(result, "Path traversal using ../ should be blocked");
		}
	}

	@Test
	@DisplayName("Should block encoded %2e%2e path traversal")
	public void getFile_shouldBlockEncodedPathTraversal() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath("/testmodule/%2e%2e/etc/passwd"))
				.thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, "/testmodule/%2e%2e/etc/passwd"))
				.thenReturn("/%2e%2e/etc/passwd");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn("/testmodule/%2e%2e/etc/passwd");

			File result = servlet.getFile(request);

			assertNull(result, "URL-encoded path traversal should be blocked");
		}
	}

	@Test
	@DisplayName("Should block deep nested ../ traversal targeting /etc/passwd")
	public void getFile_shouldBlockDeepNestedTraversal() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String maliciousPath = "/testmodule/../../../../../etc/passwd";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(maliciousPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, maliciousPath))
				.thenReturn("/../../../../../etc/passwd");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn(maliciousPath);

			File result = servlet.getFile(request);

			assertNull(result, "Deep nested path traversal should be blocked");
		}
	}

	@Test
	@DisplayName("Should block traversal targeting WEB-INF/web.xml")
	public void getFile_shouldBlockTraversalToWebInf() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String maliciousPath = "/testmodule/../../../WEB-INF/web.xml";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(maliciousPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, maliciousPath))
				.thenReturn("/../../../WEB-INF/web.xml");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn(maliciousPath);

			File result = servlet.getFile(request);

			assertNull(result, "Traversal to WEB-INF should be blocked");
		}
	}

	@Test
	@DisplayName("Should block path traversal in dev mode")
	public void getFile_shouldBlockPathTraversalInDevMode() throws IOException {
		Path devDir = Files.createTempDirectory("openmrs-dev-module");
		Path devResources = devDir.resolve("omod/target/classes/web/module/resources");
		Files.createDirectories(devResources);

		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String maliciousPath = "/testmodule/../../secret.txt";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(maliciousPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, maliciousPath))
				.thenReturn("/../../secret.txt");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(devDir.toFile());

			when(request.getPathInfo()).thenReturn(maliciousPath);

			File result = servlet.getFile(request);

			assertNull(result, "Path traversal in dev mode should also be blocked");
		} finally {
			deleteRecursively(devDir.toFile());
		}
	}

	/**
	 * A "happy path" test to ensure that legitimate resource requests 
	 * within the allowed directory still work correctly after the security fix.
	 */
	@Test
	@DisplayName("Should serve a valid legitimate resource file")
	public void getFile_shouldServeValidResource() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String validPath = "/testmodule/style.css";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(validPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, validPath))
				.thenReturn("/style.css");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn(validPath);

			File result = servlet.getFile(request);

			assertNotNull(result, "A valid resource file should be returned");
			assertTrue(result.exists(), "The returned file should exist");
			assertTrue(result.isFile(), "The returned path should be a file, not a directory");
		}
	}

	@Test
	@DisplayName("Should return null when the requested file does not exist")
	public void getFile_shouldReturnNullForNonExistentFile() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String missingPath = "/testmodule/missing.js";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(missingPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, missingPath))
				.thenReturn("/missing.js");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn(missingPath);

			File result = servlet.getFile(request);

			assertNull(result, "Should return null for a non-existent file");
		}
	}

	@Test
	@DisplayName("Should return null when no module handles the path")
	public void getFile_shouldReturnNullWhenNoModuleFound() {
		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String unknownPath = "/unknownmodule/style.css";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(unknownPath)).thenReturn(null);

			when(request.getPathInfo()).thenReturn(unknownPath);

			File result = servlet.getFile(request);

			assertNull(result, "Should return null when no module handles the path");
		}
	}

	@Test
	@DisplayName("Should not serve a directory path even if it is within base dir")
	public void getFile_shouldNotServeDirectory() throws IOException {
		Path subDir = resourcesDir.resolve("subdir");
		Files.createDirectories(subDir);

		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String dirPath = "/testmodule/subdir";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(dirPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, dirPath))
				.thenReturn("/subdir");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(null);

			when(request.getPathInfo()).thenReturn(dirPath);

			File result = servlet.getFile(request);

			assertNull(result, "Directories should not be served even if within base dir");
		}
	}

	@Test
	@DisplayName("Should serve a valid resource in dev mode")
	public void getFile_shouldServeValidResourceInDevMode() throws IOException {
		Path devDir = Files.createTempDirectory("openmrs-dev-module");
		Path devResources = devDir.resolve("omod/target/classes/web/module/resources");
		Files.createDirectories(devResources);
		File devFile = devResources.resolve("app.js").toFile();
		Files.writeString(devFile.toPath(), "console.log('hello');");

		try (MockedStatic<ModuleUtil> mockedModuleUtil = Mockito.mockStatic(ModuleUtil.class)) {
			String validPath = "/testmodule/app.js";
			mockedModuleUtil.when(() -> ModuleUtil.getModuleForPath(validPath)).thenReturn(module);
			mockedModuleUtil.when(() -> ModuleUtil.getPathForResource(module, validPath))
				.thenReturn("/app.js");
			mockedModuleUtil.when(() -> ModuleUtil.getDevelopmentDirectory("testmodule"))
				.thenReturn(devDir.toFile());

			when(request.getPathInfo()).thenReturn(validPath);

			File result = servlet.getFile(request);

			assertNotNull(result, "Should serve valid resource in dev mode");
			assertTrue(result.exists(), "File should exist");
			assertTrue(result.isFile(), "Result should be a file");
		} finally {
			deleteRecursively(devDir.toFile());
		}
	}
	
	private void deleteRecursively(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					deleteRecursively(child);
				}
			}
		}
		file.delete();
	}
}
