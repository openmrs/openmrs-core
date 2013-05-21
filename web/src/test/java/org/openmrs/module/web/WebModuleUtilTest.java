package org.openmrs.module.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleException;
import org.openmrs.web.DispatcherServlet;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WebModuleUtil.class)
public class WebModuleUtilTest {
	
	private Properties propertiesWritten;
	
	private static final String REAL_PATH = "/usr/local/apache-tomcat-7.0.27/webapps/openmrs";
	
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
	
	private Module buildModuleForMessageTest() {
		Properties englishMessages = new Properties();
		englishMessages.put("mymodule.title", "My Module");
		englishMessages.put("withoutPrefix", "Without prefix");
		
		Module mod = new Module("My Module");
		mod.setModuleId("mymodule");
		mod.setMessages(new HashMap<String, Properties>());
		mod.getMessages().put("en", englishMessages);
		
		return mod;
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
