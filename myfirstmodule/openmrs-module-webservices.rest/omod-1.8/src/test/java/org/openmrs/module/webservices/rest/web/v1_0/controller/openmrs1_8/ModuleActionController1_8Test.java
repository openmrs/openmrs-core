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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.Module;
import org.openmrs.module.webservices.helper.ModuleAction;
import org.openmrs.module.webservices.rest.web.MockModuleFactoryWrapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ModuleActionResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ModuleResource1_8;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.core.IsNot.not;

public class ModuleActionController1_8Test extends MainResourceControllerTest {
	
	@Autowired
	RestService restService;
	
	private Module atlasModule = new Module("Atlas Module", "atlas", "name", "author", "description", "version");
	
	private Module conceptLabModule = new Module("Open Concept Lab Module", "openconceptlab", "name", "author",
	        "description", "version");
	
	private Module webservicesRestModule = new Module("Rest module", RestConstants.MODULE_ID,
	        "org.openmrs.module.webservices.rest", "openrms", "rest", "2.17");
	
	private Module mockModuleToLoad = new Module("MockModule", "mockModule", "name", "author", "description", "version");
	
	MockModuleFactoryWrapper mockModuleFactory = new MockModuleFactoryWrapper();
	
	@Before
	public void setUp() throws Exception {
		mockModuleFactory.loadedModules.addAll(Arrays.asList(atlasModule, conceptLabModule, webservicesRestModule));
		
		ModuleActionResource1_8 moduleActionResource = (ModuleActionResource1_8) restService
		        .getResourceBySupportedClass(ModuleAction.class);
		moduleActionResource.setModuleFactoryWrapper(mockModuleFactory);
		
		ModuleResource1_8 moduleResource = (ModuleResource1_8) restService.getResourceBySupportedClass(Module.class);
		moduleResource.setModuleFactoryWrapper(mockModuleFactory);
	}
	
	@Test
	public void shouldInstallModule() throws Exception {
		mockModuleFactory.loadModuleMock = mockModuleToLoad;
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"install\", \"modules\":[\""
		        + getUuid() + "\"], \"installUri\":\"" + getInstallUri() + "\"}")));
		assertThat(mockModuleFactory.loadedModules, hasItem(mockModuleToLoad));
		assertThat(mockModuleFactory.startedModules, hasItem(mockModuleToLoad));
	}
	
	@Test(expected = IllegalRequestException.class)
	public void shouldThrowErrorOnPoorUri() throws Exception {
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"install\", \"modules\":[\""
		        + getUuid() + "\"], \"installUri\":\"anystring\"}")));
	}
	
	@Test
	public void shouldStartAtlasModule() throws Exception {
		//sanity check
		assertThat(mockModuleFactory.startedModules, not(hasItem(atlasModule)));
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"start\", \"modules\":[\"" + getUuid() + "\"]}")));
		assertThat(mockModuleFactory.startedModules, hasItem(atlasModule));
	}
	
	@Test
	public void shouldStopAtlasModule() throws Exception {
		mockModuleFactory.startedModules.add(atlasModule);
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"stop\", \"modules\":[\"" + getUuid() + "\"]}")));
		assertThat(mockModuleFactory.startedModules, not(hasItem(atlasModule)));
	}
	
	@Test
	public void shouldDoNothingIfAtlasModuleAlreadyStarted() throws Exception {
		mockModuleFactory.startedModules.add(atlasModule);
		//sanity check
		assertThat(mockModuleFactory.startedModules, hasItem(atlasModule));
		assertThat(mockModuleFactory.loadedModules, hasItem(atlasModule));
		
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"start\", \"modules\":[\"" + getUuid() + "\"]}")));
		
		//check if state preserved
		assertThat(mockModuleFactory.startedModules, hasItem(atlasModule));
		assertThat(mockModuleFactory.loadedModules, hasItem(atlasModule));
	}
	
	@Test
	public void shouldDoNothingIfAtlasModuleAlreadyStopped() throws Exception {
		//sanity check
		assertThat(mockModuleFactory.startedModules, not(hasItem(atlasModule)));
		assertThat(mockModuleFactory.loadedModules, hasItem(atlasModule));
		
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"stop\", \"modules\":[\"" + getUuid() + "\"]}")));
		
		//check if state preserved
		assertThat(mockModuleFactory.startedModules, not(hasItem(atlasModule)));
		assertThat(mockModuleFactory.loadedModules, hasItem(atlasModule));
	}
	
	@Test
	public void shouldUnloadAtlasModule() throws Exception {
		//sanity check
		assertThat(mockModuleFactory.loadedModules, hasItem(atlasModule));
		
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"unload\", \"modules\":[\"" + getUuid() + "\"]}")));
		assertThat(mockModuleFactory.loadedModules, not(hasItem(atlasModule)));
	}
	
	@Test
	public void shouldStartAllModules() throws Exception {
		assertThat(mockModuleFactory.startedModules, emptyCollectionOf(Module.class));
		
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"start\", \"allModules\":\"true\"}")));
		
		for (Module loadedModule : mockModuleFactory.loadedModules) {
			assertThat(mockModuleFactory.startedModules, hasItem(loadedModule));
		}
	}
	
	@Test
	public void shouldRestartAllModules() throws Exception {
		//'start' all modules
		mockModuleFactory.startedModules.addAll(mockModuleFactory.getLoadedModules());
		
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"restart\", \"allModules\":\"true\"}")));
		
		for (Module loadedModule : mockModuleFactory.loadedModules) {
			assertThat(mockModuleFactory.startedModules, hasItem(loadedModule));
		}
	}
	
	@Test
	public void shouldNotStopRestModule() throws Exception {
		//'start' all modules
		mockModuleFactory.startedModules.addAll(mockModuleFactory.getLoadedModules());
		
		deserialize(handle(newPostRequest(getURI(), "{\"action\":\"stop\", \"allModules\":\"true\"}")));
		
		assertThat(mockModuleFactory.startedModules, hasSize(1));
		assertThat(mockModuleFactory.startedModules, hasItem(webservicesRestModule));
	}
	
	@Test
	public void shouldFailIfTryingToStopNonExistentModule() throws Exception {
		mockModuleFactory.startedModules.add(atlasModule);
		Exception exception = null;
		try {
			handle(newPostRequest(getURI(), "{\"action\":\"stop\", \"modules\":[\"atlas\", \"does.not.exist\"]}"));
		}
		catch (Exception ex) {
			exception = ex;
		}
		assertThat(exception, notNullValue());
		assertThat(exception, instanceOf(IllegalRequestException.class));
		assertThat(mockModuleFactory.startedModules, hasItem(atlasModule));
	}
	
	//ModuleAction resource does not support these operations
	@Override
	@Test(expected = Exception.class)
	public void shouldGetDefaultByUuid() throws Exception {
		super.shouldGetDefaultByUuid();
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetRefByUuid() throws Exception {
		super.shouldGetRefByUuid();
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetFullByUuid() throws Exception {
		super.shouldGetFullByUuid();
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Override
	public String getURI() {
		return "moduleaction";
	}
	
	@Override
	public String getUuid() {
		return "atlas";
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	public String getInstallUri() {
		return "https://dl.bintray.com/openmrs/omod/xforms-4.3.11.omod";
	}
}
