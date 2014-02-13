/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * The only reason why i created this class is to be able to create an application context which can
 * be refreshed multiple times, unlike the default GenericApplicationContext which can be refreshed
 * only once. This class is used for @ContextConfiguration in WebModuleActivatorTest
 */
public class TestContextLoader extends AbstractContextLoader {
	
	public TestContextLoader() {
	}
	
	@Override
	public final ConfigurableApplicationContext loadContext(String... locations) throws Exception {
		XmlWebApplicationContext context = new XmlWebApplicationContext();
		context.setConfigLocations(locations);
		MockServletContext sc = new MockServletContext();
		sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
		context.setServletContext(sc);
		context.refresh();
		return context;
	}
	
	@Override
	protected String getResourceSuffix() {
		return "-context.xml";
	}
	
	/**
	 * @see org.springframework.test.context.SmartContextLoader#loadContext(org.springframework.test.context.MergedContextConfiguration)
	 */
	@Override
	public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
		return null;
	}
}
