/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.Module;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.MockModuleFactoryWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ModuleResource1_8;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 */
public class ModuleController1_8Test extends MainResourceControllerTest {
	
	@Autowired
	RestService restService;
	
	private Module atlasModule = new Module("Atlas Module", "atlas", "name", "author", "description", "version");
	
	private Module conceptLabModule = new Module("Open Concept Lab Module", "openconceptlab", "name", "author",
	        "description", "version");
	
	private Module mockModuleToLoad = new Module("MockModule", "mockModule", "name", "author", "description", "version");
	
	MockModuleFactoryWrapper mockModuleFactory = new MockModuleFactoryWrapper();
	
	@Before
	public void setUp() throws Exception {
		mockModuleFactory.loadedModules.add(atlasModule);
		mockModuleFactory.loadedModules.add(conceptLabModule);
		
		ModuleResource1_8 resource = (ModuleResource1_8) restService.getResourceBySupportedClass(Module.class);
		resource.setModuleFactoryWrapper(mockModuleFactory);
	}
	
	@Test
	public void shouldGetAllModules() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		List<Object> results = Util.getResultsList(result);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getAllCount(), results.size());
	}
	
	@Test
	public void shouldGetModuleByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result.get("description"));
	}
	
	@Test
	public void shouldIncludeIfModuleIsStarted() throws Exception {
		SimpleObject result = deserialize(handle(request(RequestMethod.GET, getURI() + "/" + getUuid())));
		Assert.assertThat((Boolean) PropertyUtils.getProperty(result, "started"), is(false));
	}
	
	@Test
	public void shouldIncludeAuthorToFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(result, "author"));
	}
	
	@Test
	public void shouldIncludeVersionToDefaultRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(result, "version"));
	}
	
	@Test
	public void shouldIncludeAllPropertiesForFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "description"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "packageName"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "author"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "version"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "started"));
	}
	
	@Test
	public void shouldIncludeAllPropertiesForDefaultRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "description"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "version"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "started"));
	}
	
	@Test
	public void shouldIncludeAllPropertiesForRefRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "ref");
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "display"));
	}
	
	@Test
	public void shouldUploadModule() throws Exception {
		final String moduleFile = "org/openmrs/module/webservices/rest/include/mockModule.omod";
		byte[] fileData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(moduleFile));
		MockMultipartFile toUpload = new MockMultipartFile("file", "mockModule.omod", "archive/zip", fileData);
		
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.setRequestURI(getBaseRestURI() + getURI());
		request.setMethod(RequestMethod.POST.name());
		request.addHeader("Content-Type", "multipart/form-data");
		
		request.addFile(toUpload);
		
		mockModuleFactory.loadModuleMock = mockModuleToLoad;
		
		MockHttpServletResponse response = handle(request);
		assertThat(mockModuleFactory.loadedModules, hasItem(mockModuleToLoad));
		assertThat(mockModuleFactory.startedModules, hasItem(mockModuleToLoad));
	}
	
	@Override
	public String getURI() {
		return "module";
	}
	
	@Override
	public String getUuid() {
		return "atlas";
	}
	
	@Override
	public long getAllCount() {
		return mockModuleFactory.loadedModules.size();
	}
	
}
