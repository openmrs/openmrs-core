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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.web.DispatcherServlet;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class WebModuleUtilTest {
	
	private static final String REAL_PATH = "/usr/local/apache-tomcat-7.0.27/webapps/openmrs";
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String)
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldReturnFalseForDifferentPackageName() {
		String modulePackageName = "org.openmrs.logic.task";
		String taskClass = "org.openmrs.logic.TaskInitializeLogicRuleProvidersTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String)
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldReturnFalseIfModuleHasLongerPackageName() {
		String modulePackageName = "org.openmrs.logic.task";
		String taskClass = "org.openmrs.logic.TaskInitializeLogicRuleProvidersTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String)
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldProperlyMatchSubpackages() {
		String modulePackageName = "org.openmrs.module.xforms";
		String taskClass = "org.openmrs.module.xforms.ProcessXformsQueueTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertTrue(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String)
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldReturnFalseForEmptyPackageNames() {
		String modulePackageName = "";
		String taskClass = "";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertFalse(result);
	}
	
	/**
	 * @throws ParserConfigurationException
	 * @see WebModuleUtil#startModule(Module, ServletContext, boolean)
	 */
	@Test
	public void startModule_shouldCreateDwrModulesXmlIfNotExists() throws ParserConfigurationException {
		// create dummy module and start it
		Module mod = buildModuleForMessageTest();
		ModuleFactory.getStartedModulesMap().put(mod.getModuleId(), mod);
		
		ServletContext servletContext = mock(ServletContext.class);
		String realPath = servletContext.getRealPath("");
		if (realPath == null)
			realPath = System.getProperty("user.dir");
		
		// manually delete dwr-modules.xml 
		File f = new File(realPath + "/WEB-INF/dwr-modules.xml");
		f.delete();
		
		// start the dummy module
		WebModuleUtil.startModule(mod, servletContext, true);
		
		// test if dwr-modules.xml is created
		assertTrue(f.exists());
		
		ModuleFactory.getStartedModulesMap().clear();
	}
	
	/**
	 * @throws ParserConfigurationException
	 * @throws FileNotFoundException
	 * @see WebModuleUtil#startModule(Module, ServletContext, boolean)
	 */
	@Test
	public void startModule_dwrModuleXmlshouldContainModuleInfo()
	        throws ParserConfigurationException, FileNotFoundException {
		// create dummy module and start it
		Module mod = buildModuleForMessageTest();
		ModuleFactory.getStartedModulesMap().put(mod.getModuleId(), mod);
		
		ServletContext servletContext = mock(ServletContext.class);
		String realPath = servletContext.getRealPath("");
		if (realPath == null)
			realPath = System.getProperty("user.dir");
		
		WebModuleUtil.startModule(mod, servletContext, true);
		
		// test if dwr-modules.xml contains id of started dummy module
		File f = new File(realPath + "/WEB-INF/dwr-modules.xml");
		Scanner scanner = new Scanner(f);
		boolean found = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains(mod.getModuleId())) {
				found = true;
				break;
			}
		}
		if (scanner != null)
			scanner.close();
		
		assertTrue(found);
		
		ModuleFactory.getStartedModulesMap().clear();
	}
	
	private Module buildModuleForMessageTest() throws ParserConfigurationException {
		Properties englishMessages = new Properties();
		englishMessages.put("withoutPrefix", "Without prefix");
		
		Module mod = new Module("My Module");
		mod.setModuleId("mymodule");
		mod.setMessages(new HashMap<>());
		mod.getMessages().put("en", englishMessages);
		mod.setFile(new File("sampleFile.jar"));
		mod.setConfig(buildModuleConfig());
		
		return mod;
	}
	
	private Document buildModuleConfig() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("module");
		doc.appendChild(rootElement);
		
		Element dwr = doc.createElement("dwr");
		dwr.appendChild(doc.createTextNode(""));
		rootElement.appendChild(dwr);
		
		Element allow = doc.createElement("allow");
		allow.appendChild(doc.createTextNode(""));
		dwr.appendChild(allow);
		
		Attr attr = doc.createAttribute("moduleId");
		attr.setValue("mymodule");
		allow.setAttributeNode(attr);
		
		Element create = doc.createElement("create");
		allow.appendChild(create);
		
		return doc;
	}
	
	/**
	 * @see WebModuleUtil#getModuleWebFolder(String)
	 */
	@Test
	public void getModuleWebFolder_shouldReturnNullIfTheDispatcherServletIsNotYetSet() {
		//We need to do this in case it is run after getModuleWebFolder_shouldReturnTheCorrectModuleFolder 
		WebModuleUtil.setDispatcherServlet(null);
		assertThrows(ModuleException.class, () -> WebModuleUtil.getModuleWebFolder(""));
	}
	
	/**
	 * @see WebModuleUtil#getModuleWebFolder(String)
	 */
	@Test
	public void getModuleWebFolder_shouldReturnTheCorrectModuleFolder() {
		setupMocks(false);
		String moduleId = "basicmodule";
		String expectedPath = Paths.get(REAL_PATH, "WEB-INF", "view", "module", moduleId).toString();
		
		String actualPath = WebModuleUtil.getModuleWebFolder(moduleId);
		
		assertEquals(expectedPath, actualPath);
	}
	
	/**
	 * @see WebModuleUtil#getModuleWebFolder(String)
	 */
	@Test
	public void getModuleWebFolder_shouldReturnTheCorrectModuleFolderIfRealPathHasATrailingSlash() {
		setupMocks(true);
		String moduleId = "basicmodule";
		String expectedPath = Paths.get(REAL_PATH, "WEB-INF", "view", "module", moduleId).toString();
		String actualPath = WebModuleUtil.getModuleWebFolder(moduleId);
		
		assertEquals(expectedPath, actualPath);
	}
	
	private static void setupMocks(boolean includeTrailingSlash) {
		ServletConfig servletConfig = mock(ServletConfig.class);
		
		ServletContext servletContext = mock(ServletContext.class);
		String realPath = (includeTrailingSlash) ? REAL_PATH + "/" : REAL_PATH;
		when(servletContext.getRealPath("")).thenReturn(realPath);
		
		DispatcherServlet dispatcherServlet = mock(DispatcherServlet.class);
		when(dispatcherServlet.getServletConfig()).thenReturn(servletConfig);
		when(dispatcherServlet.getServletContext()).thenReturn(servletContext);
		
		WebModuleUtil.setDispatcherServlet(dispatcherServlet);
	}
	
}
