package org.openmrs.module.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WebModuleUtil.class)
public class WebModuleUtilTest {
	
	private Properties propertiesWritten;
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskPackageName(String, String) 
	 * @verifies Module package name different from task package name
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskPackageName_shouldReturnFalseForDifferentPackageNames() throws Exception {
		String modulePackageName = "org.openmrs.logic.task";
		String taskPackageName = "org.openmrs.logic.taskInitializeLogicRuleProvidersTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskPackageName(modulePackageName, taskPackageName);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskPackageName(String, String) 
	 * @verifies Module package name longer than task package name
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskPackageName_shouldReturnFalseForLongerModulePackageName() throws Exception {
		String modulePackageName = "org.openmrs.logic.task";
		String taskPackageName = "org.openmrs.logic";
		boolean result = WebModuleUtil.isModulePackageNameInTaskPackageName(modulePackageName, taskPackageName);
		assertFalse(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskPackageName(String, String) 
	 * @verifies Module package name shorter task package name
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskPackageName_shouldReturnTrueForLongerTaskPackageName() throws Exception {
		String modulePackageName = "org.openmrs.module.xforms";
		String taskPackageName = "org.openmrs.module.xforms.ProcessXformsQueueTask";
		boolean result = WebModuleUtil.isModulePackageNameInTaskPackageName(modulePackageName, taskPackageName);
		assertTrue(result);
	}
	
	/**
	 * @see WebModuleUtil#isModulePackageNameInTaskPackageName(String, String)
	 * @verifies Module package name shorter task package name
	 * @throws Exception
	 */
	@Test
	public void isModulePackageNameInTaskPackageName_shouldReturnFalseForEmptyPackageNames() throws Exception {
		String modulePackageName = "";
		String taskPackageName = "";
		boolean result = WebModuleUtil.isModulePackageNameInTaskPackageName(modulePackageName, taskPackageName);
		assertFalse(result);
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
	
}
