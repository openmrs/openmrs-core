/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.openmrs.module.BaseModuleActivatorTest;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.controller.ModuleListController;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests all actions in ModuleListController. When i move this class to the
 * org.openmrs.module.web.controller package, it fails when run under maven, though passes in
 * eclipse. And i just do not know why. :)
 */
@ContextConfiguration(locations = { "classpath*:webModuleApplicationContext.xml" }, loader = TestContextLoader.class)
public class WebModuleListControllerTest extends BaseModuleActivatorTest {
	
	@Test
	@NotTransactional
	public void shouldStopModule() throws Exception {
		
		init();
		createWebInfFolderIfNotExist();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("action", "stop");
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue("Module.stopped".equals(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR)));
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
	}
	
	@Test
	@NotTransactional
	public void shouldStartModule() throws Exception {
		
		init();
		createWebInfFolderIfNotExist();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("action", "start");
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue("Module.started".equals(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR)));
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
	}
	
	@Test
	@NotTransactional
	public void shouldUnloadModule() throws Exception {
		
		init();
		createWebInfFolderIfNotExist();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("action", "unload");
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue("Module.unloaded".equals(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR)));
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
	}
	
	@Test
	@NotTransactional
	public void shouldUpdateModule() throws Exception {
		
		Module module = ModuleFactory.getModuleById(MODULE3_ID);
		
		assertTrue(module.getVersion().equals("1.0-SNAPSHOT"));
		
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-2.0-SNAPSHOT.omod");
		module.setDownloadURL("file:" + url.getFile());
		
		init();
		createWebInfFolderIfNotExist();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("action", "Module.installUpdate");
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR) == null);
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
		
		assertTrue(ModuleFactory.getModuleById(MODULE3_ID).getVersion().equals("2.0-SNAPSHOT"));
	}
	
	@Test
	@NotTransactional
	public void shouldUploadDownloadedModule() throws Exception {
		
		init();
		createWebInfFolderIfNotExist();
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-1.0-SNAPSHOT.omod");
		request.setParameter("action", "upload");
		request.setParameter("download", "true");
		request.setParameter("downloadURL", "file:" + url.getFile());
		
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue("Module.loadedAndStarted".equals(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR)));
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
	}
	
	@Test
	@NotTransactional
	public void shouldUploadUpdateModule() throws Exception {
		
		init();
		createWebInfFolderIfNotExist();
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-1.0-SNAPSHOT.omod");
		MockMultipartFile file = new MockMultipartFile("moduleFile", "test3-1.0-SNAPSHOT.omod", null, new FileInputStream(
		        url.getFile()));
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.addFile(file);
		
		request.setParameter("action", "upload");
		request.setParameter("update", "true");
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue("Module.loadedAndStarted".equals(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR)));
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
	}
	
	@Test
	@NotTransactional
	public void shouldUploadModule() throws Exception {
		
		init();
		createWebInfFolderIfNotExist();
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-1.0-SNAPSHOT.omod");
		MockMultipartFile file = new MockMultipartFile("moduleFile", "test3-1.0-SNAPSHOT.omod", null, new FileInputStream(
		        url.getFile()));
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.addFile(file);
		
		request.setParameter("action", "upload");
		request.setParameter("moduleId", MODULE3_ID);
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue("Module.loadedAndStarted".equals(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR)));
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
	}
	
	@Test
	@NotTransactional
	public void shouldCheckForModuleUpdates() throws Exception {
		
		Module module1 = ModuleFactory.getModuleById(MODULE1_ID);
		URL url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test1-update.rdf");
		module1.setUpdateURL("file:" + url.getFile());
		
		Module module2 = ModuleFactory.getModuleById(MODULE2_ID);
		url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test2-update.rdf");
		module2.setUpdateURL("file:" + url.getFile());
		
		Module module3 = ModuleFactory.getModuleById(MODULE3_ID);
		url = OpenmrsClassLoader.getInstance().getResource("org/openmrs/module/include/test3-update.rdf");
		module3.setUpdateURL("file:" + url.getFile());
		
		//no updates yet
		assertTrue(module1.getDownloadURL() == null);
		assertTrue(module1.getUpdateVersion() == null);
		
		assertTrue(module2.getDownloadURL() == null);
		assertTrue(module2.getUpdateVersion() == null);
		
		assertTrue(module3.getDownloadURL() == null);
		assertTrue(module3.getUpdateVersion() == null);
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		ModuleListController controller = (ModuleListController) applicationContext.getBean("moduleListController");
		controller.handleRequest(request, response);
		
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_MSG_ATTR) == null);
		assertTrue(request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR) == null);
		
		//we should have found updates of version 2.0-SNAPSHOT for each module 
		assertTrue(module1.getDownloadURL().equals(
		    "http://modules.openmrs.org/modules/download/test1/test1-2.0-SNAPSHOT.omod"));
		assertTrue(module1.getUpdateVersion().equals("2.0-SNAPSHOT"));
		
		assertTrue(module2.getDownloadURL().equals(
		    "http://modules.openmrs.org/modules/download/test2/test2-2.0-SNAPSHOT.omod"));
		assertTrue(module2.getUpdateVersion().equals("2.0-SNAPSHOT"));
		
		assertTrue(module3.getDownloadURL().equals(
		    "http://modules.openmrs.org/modules/download/test3/test3-2.0-SNAPSHOT.omod"));
		assertTrue(module3.getUpdateVersion().equals("2.0-SNAPSHOT"));
	}
	
	private void createWebInfFolderIfNotExist() throws Exception {
		//when run from the IDE and this folder does not exist, some tests fail with
		//org.openmrs.module.ModuleException: Unable to load module messages from file: 
		// /Projects/openmrs/core/web/target/test-classes/WEB-INF/module_messages_fr.properties
		File folder = new File("target" + File.separatorChar + "test-classes" + File.separatorChar + "WEB-INF");
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}
}
