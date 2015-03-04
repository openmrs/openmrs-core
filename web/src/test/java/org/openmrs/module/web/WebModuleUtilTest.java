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
