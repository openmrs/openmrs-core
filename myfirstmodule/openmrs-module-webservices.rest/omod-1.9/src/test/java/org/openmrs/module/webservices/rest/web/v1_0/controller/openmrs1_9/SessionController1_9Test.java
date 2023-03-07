/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SessionController1_9Test extends BaseModuleWebContextSensitiveTest {
	
	private static final String SESSION_ID = "test-session-id";
	
	private static final String UNKNOWN_LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0010c6dffd0f"; // Unknown Location
	
	private static final String XANADU_UUID = "9356400c-a5a2-4532-8f2b-2361b3446eb8"; // Xanadu
	
	private SessionController1_9 controller;
	
	private HttpServletRequest hsr;
	
	@Before
	public void before() {
		controller = Context.getRegisteredComponents(SessionController1_9.class).iterator().next(); // should only be 1
		MockHttpServletRequest mockHsr = new MockHttpServletRequest();
		mockHsr.setSession(new MockHttpSession(new MockServletContext(), SESSION_ID));
		hsr = mockHsr;
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, sp, fr"));
		Context.getUserContext().setLocation(Context.getLocationService().getLocationByUuid(UNKNOWN_LOCATION_UUID));
	}
	
	/**
	 * @see SessionController1_9#delete(HttpServletRequest) 
	 * @verifies log the client out
	 */
	@Test
	public void delete_shouldLogTheClientOut() throws Exception {
		Assert.assertTrue(Context.isAuthenticated());
		controller.delete(hsr);
		Assert.assertFalse(Context.isAuthenticated());
		Assert.assertNull(hsr.getSession(false));
	}
	
	/**
	 * @see SessionController1_9#get()
	 * @verifies return the session id if the user is authenticated
	 */
	@Test
	public void get_shouldReturnTheUserIfTheUserIsAuthenticated() throws Exception {
		Assert.assertTrue(Context.isAuthenticated());
		Object ret = controller.get();
		Object userProp = PropertyUtils.getProperty(ret, "user");
		List<HashMap<String, String>> userRoles = (List<HashMap<String, String>>) PropertyUtils.getProperty(userProp,
		    "roles");
		Assert.assertEquals("System Developer", userRoles.get(0).get("name"));
		Assert.assertEquals(true, PropertyUtils.getProperty(ret, "authenticated"));
		Assert.assertEquals(Context.getAuthenticatedUser().getUuid(), PropertyUtils.getProperty(userProp, "uuid"));
		Object personProp = PropertyUtils.getProperty(userProp, "person");
		Assert.assertEquals(Context.getAuthenticatedUser().getPerson().getUuid(),
		    PropertyUtils.getProperty(personProp, "uuid"));
	}
	
	@Test
	public void get_shouldReturnLocaleInfoIfTheUserIsAuthenticated() throws Exception {
		Assert.assertTrue(Context.isAuthenticated());
		Object ret = controller.get();
		Assert.assertEquals(Context.getLocale(), PropertyUtils.getProperty(ret, "locale"));
		Assert.assertArrayEquals(Context.getAdministrationService().getAllowedLocales().toArray(),
		    ((List<Locale>) PropertyUtils.getProperty(ret, "allowedLocales")).toArray());
	}
	
	@Test
	public void get_shouldReturnLocationIfTheUserIsAuthenticated() throws Exception {
		Assert.assertTrue(Context.isAuthenticated());
		Object ret = controller.get();
		Object loc = PropertyUtils.getProperty(ret, "sessionLocation");
		Assert.assertTrue(loc.toString() + " should contain 'display=Unknown Location'",
		    loc.toString().contains("display=Unknown Location"));
	}

	/**
	 * @see SessionController1_9#get()
	 * @verifies return the session with current provider if the user is authenticated
	 */
	@Test
	public void get_shouldReturnCurrentProviderIfTheUserIsAuthenticated() throws Exception {
		Assert.assertTrue(Context.isAuthenticated());
		Object ret = controller.get();
		Object currentProvider = PropertyUtils.getProperty(ret, "currentProvider");
		Assert.assertNotNull(currentProvider);
		Assert.assertTrue(currentProvider.toString().contains("Super User"));
	}
	
	@Test
	public void post_shouldSetTheUserLocale() throws Exception {
		Locale newLocale = new Locale("sp");
		String content = "{\"locale\":\"" + newLocale.toString() + "\"}";
		Assert.assertNotEquals(newLocale, Context.getLocale());
		controller.post(hsr, new ObjectMapper().readValue(content, HashMap.class));
		Assert.assertEquals(newLocale, Context.getLocale());
	}
	
	@Test(expected = APIException.class)
	public void post_shouldFailWhenSettingIllegalLocale() throws Exception {
		String newLocale = "fOOb@r:";
		String content = "{\"locale\":\"" + newLocale + "\"}";
		controller.post(hsr, new ObjectMapper().readValue(content, HashMap.class));
	}
	
	@Test(expected = APIException.class)
	public void post_shouldFailWhenSettingDisallowedLocale() throws Exception {
		String newLocale = "km_KH";
		String content = "{\"locale\":\"" + newLocale + "\"}";
		controller.post(hsr, new ObjectMapper().readValue(content, HashMap.class));
	}
	
	@Test
	public void post_shouldSetTheSessionLocation() throws Exception {
		String content = "{\"sessionLocation\":\"" + XANADU_UUID + "\"}";
		Location loc = Context.getLocationService().getLocationByUuid(XANADU_UUID);
		Assert.assertNotEquals(loc, Context.getUserContext().getLocation());
		controller.post(hsr, new ObjectMapper().readValue(content, HashMap.class));
		Assert.assertEquals(loc, Context.getUserContext().getLocation());
	}
	
	@Test(expected = APIException.class)
	public void post_shouldFailWhenSettingNonexistantLocation() throws Exception {
		String content = "{\"sessionLocation\":\"fake-nonexistant-uuid\"}";
		controller.post(hsr, new ObjectMapper().readValue(content, HashMap.class));
	}

}
