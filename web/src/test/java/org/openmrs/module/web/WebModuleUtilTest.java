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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.web.DispatcherServlet;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WebModuleUtil.class)
public class WebModuleUtilTest {
	
	private Properties propertiesWritten;
	
	private static final String REAL_PATH = "/usr/local/apache-tomcat-7.0.27/webapps/openmrs";
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String) 
	 * @verifies return false for different package names
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldReturnFalseForDifferentPackageName() throws Exception {
		String modulePackageName = "org.openmrs.logic.task";
		String taskClass = "org.openmrs.logic.TaskInitializeLogicRuleProvidersTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String) 
	 * @verifies return false if module has longer package name
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldReturnFalseIfModuleHasLongerPackageName() throws Exception {
		String modulePackageName = "org.openmrs.logic.task";
		String taskClass = "org.openmrs.logic.TaskInitializeLogicRuleProvidersTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String) 
	 * @verifies properly match subpackages
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldProperlyMatchSubpackages() throws Exception {
		String modulePackageName = "org.openmrs.module.xforms";
		String taskClass = "org.openmrs.module.xforms.ProcessXformsQueueTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertTrue(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskClass(String, String)
	 * @verifies return false for empty package names
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskClass_shouldReturnFalseForEmptyPackageNames() throws Exception {
		String modulePackageName = "";
		String taskClass = "";
		boolean result = WebModuleUtil.isModulePackageNameInTaskClass(modulePackageName, taskClass);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#startModule(Module, ServletContext, boolean)
	 * @verifies creates dwr-modules.xml if not found
	 */
	@Test
	public void startModule_shouldCreateDwrModulesXmlIfNotExists() throws Exception {
		partialMockWebModuleUtilForMessagesTests();
		
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
	}
	
	/**
	 * @see WebModuleUtil#startModule(Module, ServletContext, boolean)
	 * @verifies dwr-modules.xml has dwr tag of module started
	 */
	@Test
	public void startModule_dwrModuleXmlshouldContainModuleInfo() throws Exception {
		partialMockWebModuleUtilForMessagesTests();
		
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
	}
	
	/**
	 * @see WebModuleUtil#copyModuleMessagesIntoWebapp(org.openmrs.module.Module, String)
	 * @verifies prefix messages with module id
	 */
	@Test
	public void copyModuleMessagesIntoWebapp_shouldPrefixMessagesWithModuleId() throws Exception {
		Module mod = buildModuleForMessageTest();
		partialMockWebModuleUtilForMessagesTests();
		WebModuleUtil.copyModuleMessagesIntoWebapp(mod, "unused/real/path");
		
		assertThat(propertiesWritten.getProperty("mymodule.title"), is("My Module"));
		assertThat(propertiesWritten.getProperty("mymodule.withoutPrefix"), is("Without prefix"));
		assertNull(propertiesWritten.getProperty("withoutPrefix"));
	}
	
	/**
	 * @see WebModuleUtil#copyModuleMessagesIntoWebapp(org.openmrs.module.Module, String)
	 * @verifies not prefix messages with module id if override setting is specified
	 */
	@Test
	public void copyModuleMessagesIntoWebapp_shouldNotPrefixMessagesWithModuleIdIfOverrideSettingIsSpecified()
	        throws Exception {
		Module mod = buildModuleForMessageTest();
		mod.getMessages().get("en").setProperty(ModuleConstants.MESSAGE_PROPERTY_ALLOW_KEYS_OUTSIDE_OF_MODULE_NAMESPACE,
		    "true");
		
		partialMockWebModuleUtilForMessagesTests();
		WebModuleUtil.copyModuleMessagesIntoWebapp(mod, "unused/real/path");
		
		assertThat(propertiesWritten.getProperty("mymodule.title"), is("My Module"));
		assertThat(propertiesWritten.getProperty("withoutPrefix"), is("Without prefix"));
		assertNull(propertiesWritten.getProperty("mymodule.withoutPrefix"));
	}
	
	private void partialMockWebModuleUtilForMessagesTests() throws Exception {
		PowerMockito.spy(WebModuleUtil.class);
		
		// cannot use the traditional when(WMU.insertInto(...)).thenAnswer(...) because calling the method throws an exception
		PowerMockito.doAnswer(new Answer<Boolean>() {
			
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				propertiesWritten = (Properties) invocation.getArguments()[1];
				return true;
			}
		}).when(WebModuleUtil.class, "insertIntoModuleMessagePropertiesFile", anyString(), any(Properties.class),
		    anyString());
	}
	
	private Module buildModuleForMessageTest() throws ParserConfigurationException {
		Properties englishMessages = new Properties();
		englishMessages.put("mymodule.title", "My Module");
		englishMessages.put("withoutPrefix", "Without prefix");
		
		Module mod = new Module("My Module");
		mod.setModuleId("mymodule");
		mod.setMessages(new HashMap<String, Properties>());
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
	 * @verifies return null if the dispatcher servlet is not yet set
	 */
	@Test(expected = ModuleException.class)
	public void getModuleWebFolder_shouldReturnNullIfTheDispatcherServletIsNotYetSet() throws Exception {
		//We need to do this in case it is run after getModuleWebFolder_shouldReturnTheCorrectModuleFolder 
		WebModuleUtil.setDispatcherServlet(null);
		WebModuleUtil.getModuleWebFolder("");
	}
	
	/**
	 * @see WebModuleUtil#getModuleWebFolder(String)
	 * @verifies return the correct module folder
	 */
	@Test
	public void getModuleWebFolder_shouldReturnTheCorrectModuleFolder() throws Exception {
		setupMocks(false);
		String moduleId = "basicmodule";
		String expectedPath = (REAL_PATH + "/WEB-INF/view/module/" + moduleId).replace("/", File.separator);
		String actualPath = WebModuleUtil.getModuleWebFolder(moduleId);
		
		assertEquals(expectedPath, actualPath);
	}
	
	/**
	 * @see WebModuleUtil#getModuleWebFolder(String)
	 * @verifies return the correct module folder if real path has a trailing slash
	 */
	@Test
	public void getModuleWebFolder_shouldReturnTheCorrectModuleFolderIfRealPathHasATrailingSlash() throws Exception {
		setupMocks(true);
		String moduleId = "basicmodule";
		String expectedPath = (REAL_PATH + "/WEB-INF/view/module/" + moduleId).replace("/", File.separator);
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
