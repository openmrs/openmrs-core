/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LocaleAndThemeConfigurationController2_0Test extends RestControllerTestUtils {

	private AdministrationService administrationService;

	@Before
	public void before() throws SchedulerException {
		administrationService = Context.getAdministrationService();
	}

	@Test
	public void shouldGetCurrentConfiguration() throws Exception {
		// set initial configuration
		administrationService.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "en_GB");
		administrationService.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME, "green");

		// make GET call
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));

		// assert response
		assertNotNull(result);
		assertEquals("en_GB", PropertyUtils.getProperty(result, "defaultLocale"));
		assertEquals("green", PropertyUtils.getProperty(result, "defaultTheme"));
	}

	@Test
	public void shouldUpdateCurrentConfiguration() throws Exception {
		// assert initial configuration
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		assertNotNull(result);
		assertNull(PropertyUtils.getProperty(result, "defaultLocale"));
		assertNull(PropertyUtils.getProperty(result, "defaultTheme"));

		// update configuration
		String json = "{\"defaultLocale\": \"en_GB\",\"defaultTheme\": \"purple\"}";
		handle(newPostRequest(getURI(), json));

		// make POST call
		result = deserialize(handle(req));

		// assert response
		assertNotNull(result);
		assertEquals("en_GB", PropertyUtils.getProperty(result, "defaultLocale"));
		assertEquals("purple", PropertyUtils.getProperty(result, "defaultTheme"));
	}

	private String getURI() {
		return "localeandthemeconfiguration";
	}
}
