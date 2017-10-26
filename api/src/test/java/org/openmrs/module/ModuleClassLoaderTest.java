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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ModuleClassLoaderTest extends BaseContextSensitiveTest {
	
	Module mockModule;
	
	Map<String, String> mockModules;
	
	@Before
	public void before() {
		mockModule = new Module("mockmodule", "mockmodule", "org.openmrs.module.mockmodule", "author", "description", "1.0");
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
		
		mockModule.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		mockModules.put("module", "1.1");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		ModuleFactory.getStartedModulesMap().put("module", new Module("", "module", "", "", "", "3.0"));
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		mockModules.put("differentModule", "1.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
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
		
		mockModule.getConditionalResources().add(resource);
		
		mockModules.put("module", "3.0");
		
		boolean result = ModuleClassLoader.shouldResourceBeIncluded(mockModule, URI.create(
		    "file://module/mockmodule/lib/mockmodule-api-1.10.jar").toURL(), "1.10.0-SNAPSHOT", mockModules);
		
		assertThat(result, is(true));
	}
}
