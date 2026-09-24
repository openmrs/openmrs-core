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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ModuleResourcesServlet}.
 * <p>
 * The {@code getFile} method is security-sensitive — it resolves untrusted request paths against
 * the module's resource directory. These tests cover every branch in that resolution: rejection of
 * null and null-byte paths, rejection of malformed paths, missing-module / missing-file paths,
 * directory-traversal rejection, and the dev-mode vs production-mode resolution split.
 */
public class ModuleResourcesServletTest {

	private static final String MODULE_ID = "fooModule";

	private static final String MODULE_ID_AS_PATH = "fooModule";

	@TempDir
	Path webappRoot;

	@TempDir
	Path devDir;

	private ServletContext servletContext;

	private ModuleResourcesServlet servlet;

	private Module module;

	private MockedStatic<ModuleUtil> moduleUtilMock;

	@BeforeEach
	public void setUp() throws Exception {
		servletContext = mock(ServletContext.class);
		when(servletContext.getRealPath("")).thenReturn(webappRoot.toString());

		ServletConfig servletConfig = mock(ServletConfig.class);
		when(servletConfig.getServletContext()).thenReturn(servletContext);
		when(servletConfig.getServletName()).thenReturn("moduleResources");

		servlet = new ModuleResourcesServlet();
		servlet.init(servletConfig);

		module = new Module("Foo");
		module.setModuleId(MODULE_ID);

		moduleUtilMock = mockStatic(ModuleUtil.class);
		// Default: no module matches, dev-dir not set. Individual tests override as needed.
		moduleUtilMock.when(() -> ModuleUtil.getDevelopmentDirectory(anyString())).thenReturn(null);
	}

	@AfterEach
	public void tearDown() {
		moduleUtilMock.close();
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldReturnNullWhenRequestPathIsNull() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn(null);

		assertNull(servlet.getFile(request));
		// Module lookup must not be invoked when the path is rejected up-front.
		moduleUtilMock.verify(() -> ModuleUtil.getModuleForPath(any()), never());
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldReturnNullWhenPathContainsANullByte() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		// A null byte injected into the request path is a classic poison-NUL attempt: most file
		// system APIs treat the byte as a terminator, allowing bypass of suffix-based checks.
		String pathWithNullByte = "/" + MODULE_ID + "/style.css" + ((char) 0) + ".png";
		when(request.getPathInfo()).thenReturn(pathWithNullByte);

		assertNull(servlet.getFile(request));
		moduleUtilMock.verify(() -> ModuleUtil.getModuleForPath(any()), never());
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldReturnNullForMalformedPathWithoutLeadingModuleSegment() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/");
		// Real ModuleUtil throws IllegalArgumentException for "/" — the servlet must translate this
		// into a 404-equivalent (null), not propagate a 500.
		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath("/")).thenThrow(new IllegalArgumentException("malformed"));

		assertNull(servlet.getFile(request));
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldReturnNullWhenNoModuleHandlesThePath() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/unknownModule/some.css");
		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath("/unknownModule/some.css")).thenReturn(null);

		assertNull(servlet.getFile(request));
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldRejectPathTraversalAttempts() throws Exception {
		// Stage a real file outside the module's resource directory so a traversal attempt would
		// otherwise succeed in returning a non-null File. The guard must catch this regardless.
		Path secretOutsideBase = Files.createFile(webappRoot.resolve("secret.txt"));
		assertNotNull(secretOutsideBase);

		HttpServletRequest request = mock(HttpServletRequest.class);
		String traversalPath = "/" + MODULE_ID + "/../../secret.txt";
		when(request.getPathInfo()).thenReturn(traversalPath);

		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath(traversalPath)).thenReturn(module);
		moduleUtilMock.when(() -> ModuleUtil.getPathForResource(module, traversalPath)).thenReturn("/../../secret.txt");

		assertNull(servlet.getFile(request));
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldReturnNullWhenResolvedFileDoesNotExist() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		String path = "/" + MODULE_ID + "/missing.css";
		when(request.getPathInfo()).thenReturn(path);

		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath(path)).thenReturn(module);
		moduleUtilMock.when(() -> ModuleUtil.getPathForResource(module, path)).thenReturn("/missing.css");

		assertNull(servlet.getFile(request));
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldResolveFromWebappRootInProductionMode() throws Exception {
		// Production layout: <webappRoot>/WEB-INF/view/module/<moduleIdAsPath>/resources/<resource>
		Path resourceDir = Files
		        .createDirectories(webappRoot.resolve("WEB-INF/view/module/" + MODULE_ID_AS_PATH + "/resources"));
		Path target = Files.createFile(resourceDir.resolve("style.css"));

		HttpServletRequest request = mock(HttpServletRequest.class);
		String path = "/" + MODULE_ID + "/style.css";
		when(request.getPathInfo()).thenReturn(path);

		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath(path)).thenReturn(module);
		moduleUtilMock.when(() -> ModuleUtil.getPathForResource(module, path)).thenReturn("/style.css");
		moduleUtilMock.when(() -> ModuleUtil.getDevelopmentDirectory(MODULE_ID)).thenReturn(null);

		File resolved = servlet.getFile(request);

		assertNotNull(resolved);
		assertEquals(target.toFile().getCanonicalFile(), resolved.getCanonicalFile());
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldResolveFromDevelopmentDirectoryWhenDevModeIsActive() throws Exception {
		// Dev-mode layout: <devDir>/omod/target/classes/web/module/resources/<resource>
		Path resourceDir = Files.createDirectories(devDir.resolve("omod/target/classes/web/module/resources"));
		Path target = Files.createFile(resourceDir.resolve("dev-only.js"));

		HttpServletRequest request = mock(HttpServletRequest.class);
		String path = "/" + MODULE_ID + "/dev-only.js";
		when(request.getPathInfo()).thenReturn(path);

		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath(path)).thenReturn(module);
		moduleUtilMock.when(() -> ModuleUtil.getPathForResource(module, path)).thenReturn("/dev-only.js");
		moduleUtilMock.when(() -> ModuleUtil.getDevelopmentDirectory(MODULE_ID)).thenReturn(devDir.toFile());

		File resolved = servlet.getFile(request);

		assertNotNull(resolved);
		// In dev mode the file must come from the dev directory, NOT the (unused) webapp root.
		assertEquals(target.toFile().getCanonicalFile(), resolved.getCanonicalFile());
	}

	/**
	 * @see ModuleResourcesServlet#getFile(HttpServletRequest)
	 */
	@Test
	public void getFile_shouldRejectTraversalEvenInDevelopmentMode() throws Exception {
		// Stage a file outside the dev resource directory.
		Files.createDirectories(devDir.resolve("omod/target/classes/web/module/resources"));
		Path secret = Files.createFile(devDir.resolve("secret.txt"));
		assertNotNull(secret);

		HttpServletRequest request = mock(HttpServletRequest.class);
		String path = "/" + MODULE_ID + "/../../secret.txt";
		when(request.getPathInfo()).thenReturn(path);

		moduleUtilMock.when(() -> ModuleUtil.getModuleForPath(path)).thenReturn(module);
		moduleUtilMock.when(() -> ModuleUtil.getPathForResource(module, path)).thenReturn("/../../secret.txt");
		moduleUtilMock.when(() -> ModuleUtil.getDevelopmentDirectory(MODULE_ID)).thenReturn(devDir.toFile());

		assertNull(servlet.getFile(request));
	}
}
