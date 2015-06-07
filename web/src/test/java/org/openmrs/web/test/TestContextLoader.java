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
