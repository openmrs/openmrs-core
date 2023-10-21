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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleClassLoader;
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
	
	@AfterEach
	public void tearDown() {
		ModuleFactory.getLoadedModules().clear();
		ModuleFactory.getStartedModulesMap().clear();
		ModuleFactory.getModuleClassLoaderMap().clear();
	}
	
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
		Module mod = buildModuleForMessageTest(buildModuleConfig());
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
	 * @throws ParserConfigurationException
	 * @throws FileNotFoundException
	 * @see WebModuleUtil#startModule(Module, ServletContext, boolean)
	 */
	@Test
	public void startModule_dwrModuleXmlshouldContainModuleInfo()
	        throws ParserConfigurationException, FileNotFoundException {
		// create dummy module and start it
		Module mod = buildModuleForMessageTest(buildModuleConfig());
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

	@Test
	public void loadServlets_AddsServletsWithoutInitParams()
		throws ParserConfigurationException, ClassNotFoundException {
		String servletClassName1 = "servletClass1";
		String servletClassName2 = "servletClass2";

		Map<String, String> initParams1 = new HashMap<>();
		ServletInfo servletInfo1 = new ServletInfo("servletName1", servletClassName1, initParams1);

		Map<String, String> initParams2 = new HashMap<>();
		ServletInfo servletInfo2 = new ServletInfo("servletName2", servletClassName2, initParams2);

		List<ServletInfo> servletInfos = Arrays.asList(servletInfo1, servletInfo2);

		Module mod = buildModuleForMessageTest(buildModuleConfigWithServlets(servletInfos));
		ServletContext servletContext = mock(ServletContext.class);
		ModuleClassLoader moduleClassLoader = mock(ModuleClassLoader.class);

		when(moduleClassLoader.loadClass(eq(servletClassName1))).thenAnswer((Answer<Class<?>>) invocation -> ServletClass1.class);
		when(moduleClassLoader.loadClass(eq(servletClassName2))).thenAnswer((Answer<Class<?>>) invocation -> ServletClass2.class);

		ModuleFactory.getModuleClassLoaderMap().put(mod, moduleClassLoader);

		WebModuleUtil.loadServlets(mod, servletContext);

		HttpServlet servlet1 = WebModuleUtil.getServlet("servletName1");
		assertNotNull(servlet1);
		ServletConfig servletConfig1 = servlet1.getServletConfig();
		assertFalse(servletConfig1.getInitParameterNames().hasMoreElements());
		assertNull(servletConfig1.getInitParameter("param1"));

		HttpServlet servlet2 = WebModuleUtil.getServlet("servletName2");
		assertNotNull(servlet2);
		ServletConfig servletConfig2 = servlet2.getServletConfig();
		assertFalse(servletConfig2.getInitParameterNames().hasMoreElements());
		assertNull(servletConfig2.getInitParameter("paramA"));

		WebModuleUtil.unloadServlets(mod);
	}

	@Test
	public void loadServlets_AddsCorrectInitParamsToConfig()
		throws ParserConfigurationException, ClassNotFoundException {
		String servletClassName1 = "servletClass1";
		String servletClassName2 = "servletClass2";
		
		Map<String, String> initParams1 = new HashMap<>();
		initParams1.put("param1", "value1");
		initParams1.put("param2", "value2");
		ServletInfo servletInfo1 = new ServletInfo("servletName1", servletClassName1, initParams1);

		Map<String, String> initParams2 = new HashMap<>();
		initParams2.put("paramA", "valueA");
		initParams2.put("paramB", "valueB");
		ServletInfo servletInfo2 = new ServletInfo("servletName2", servletClassName2, initParams2);

		List<ServletInfo> servletInfos = Arrays.asList(servletInfo1, servletInfo2);

		Module mod = buildModuleForMessageTest(buildModuleConfigWithServlets(servletInfos));
		ServletContext servletContext = mock(ServletContext.class);
		ModuleClassLoader moduleClassLoader = mock(ModuleClassLoader.class);

		when(moduleClassLoader.loadClass(eq(servletClassName1))).thenAnswer((Answer<Class<?>>) invocation -> ServletClass1.class);
		when(moduleClassLoader.loadClass(eq(servletClassName2))).thenAnswer((Answer<Class<?>>) invocation -> ServletClass2.class);

		ModuleFactory.getModuleClassLoaderMap().put(mod, moduleClassLoader);

		WebModuleUtil.loadServlets(mod, servletContext);

		HttpServlet servlet1 = WebModuleUtil.getServlet("servletName1");
		assertNotNull(servlet1);
		ServletConfig servletConfig1 = servlet1.getServletConfig();
		assertTrue(servletConfig1.getInitParameterNames().hasMoreElements());
		assertEquals("value1", servletConfig1.getInitParameter("param1"));
		assertEquals("value2", servletConfig1.getInitParameter("param2"));

		HttpServlet servlet2 = WebModuleUtil.getServlet("servletName2");
		assertNotNull(servlet2);
		ServletConfig servletConfig2 = servlet2.getServletConfig();
		assertTrue(servletConfig2.getInitParameterNames().hasMoreElements());
		assertEquals("valueA", servletConfig2.getInitParameter("paramA"));
		assertEquals("valueB", servletConfig2.getInitParameter("paramB"));

		WebModuleUtil.unloadServlets(mod);
	}
	
	private Module buildModuleForMessageTest(Document moduleConfig) throws ParserConfigurationException {
		Properties englishMessages = new Properties();
		englishMessages.put("withoutPrefix", "Without prefix");
		
		Module mod = new Module("My Module");
		mod.setModuleId("mymodule");
		mod.setMessages(new HashMap<>());
		mod.getMessages().put("en", englishMessages);
		mod.setFile(new File("sampleFile.jar"));
		mod.setConfig(moduleConfig);
		
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

	private Document buildModuleConfigWithServlets(List<ServletInfo> servlets) throws ParserConfigurationException {
		Document doc = buildModuleConfig();
		Element rootElement = doc.getDocumentElement();

		for (ServletInfo servletInfo : servlets) {
			Element servletElement = doc.createElement("servlet");

			Element servletNameElement = doc.createElement("servlet-name");
			servletNameElement.setTextContent(servletInfo.getServletName());
			servletElement.appendChild(servletNameElement);

			Element servletClassElement = doc.createElement("servlet-class");
			servletClassElement.setTextContent(servletInfo.getServletClass());
			servletElement.appendChild(servletClassElement);

			for (Map.Entry<String, String> initParam : servletInfo.getInitParams().entrySet()) {
				Element initParamElement = doc.createElement("init-param");

				Element paramNameElement = doc.createElement("param-name");
				paramNameElement.setTextContent(initParam.getKey());
				initParamElement.appendChild(paramNameElement);

				Element paramValueElement = doc.createElement("param-value");
				paramValueElement.setTextContent(initParam.getValue());
				initParamElement.appendChild(paramValueElement);

				servletElement.appendChild(initParamElement);
			}
			rootElement.appendChild(servletElement);
		}
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

	public class ServletInfo {
		private String servletName;
		private String servletClass;
		private Map<String, String> initParams;

		public ServletInfo(String servletName, String servletClass, Map<String, String> initParams) {
			this.servletName = servletName;
			this.servletClass = servletClass;
			this.initParams = initParams;
		}

		public String getServletName() {
			return servletName;
		}

		public void setServletName(String servletName) {
			this.servletName = servletName;
		}

		public String getServletClass() {
			return servletClass;
		}

		public void setServletClass(String servletClass) {
			this.servletClass = servletClass;
		}

		public Map<String, String> getInitParams() {
			return initParams;
		}

		public void setInitParams(Map<String, String> initParams) {
			this.initParams = initParams;
		}
	}

	static class ServletClass1 extends HttpServlet {}
	static class ServletClass2 extends HttpServlet {}
}
