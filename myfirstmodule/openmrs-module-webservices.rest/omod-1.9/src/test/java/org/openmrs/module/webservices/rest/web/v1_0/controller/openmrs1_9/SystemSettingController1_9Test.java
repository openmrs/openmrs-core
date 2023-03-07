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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests CRUD operations for {@link GlobalProperty}s via web service calls
 */
public class SystemSettingController1_9Test extends MainResourceControllerTest {
	
	private AdministrationService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "systemsetting";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.GLOBAL_PROPERTY_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 4;
	}
	
	@Before
	public void before() throws Exception {
		this.service = Context.getAdministrationService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Test
	public void shouldSaveSystemSettingWithCustomDatatype() throws Exception {
		SimpleObject property = new SimpleObject();
		property.add("property", "a.property.test");
		property.add("description", "Testing post operation of global property");
		property.add("datatypeClassname", "org.openmrs.customdatatype.datatype.BooleanDatatype");
		property.add("value", "true");
		String json = new ObjectMapper().writeValueAsString(property);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newlyCreatedSetting = deserialize(handle(req));
		String uuid = (String) PropertyUtils.getProperty(newlyCreatedSetting, "uuid");
		
		MockHttpServletRequest getReq = request(RequestMethod.GET, getURI() + "/" + uuid);
		getReq.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(getReq));
		assertEquals("a.property.test", PropertyUtils.getProperty(result, "property"));
		assertEquals("Testing post operation of global property",
		    PropertyUtils.getProperty(newlyCreatedSetting, "description"));
		assertEquals("true", PropertyUtils.getProperty(result, "value"));
		assertEquals("org.openmrs.customdatatype.datatype.BooleanDatatype",
		    PropertyUtils.getProperty(result, "datatypeClassname"));
		assertNull(PropertyUtils.getProperty(result, "datatypeConfig"));
	}
	
	@Test
	public void shouldSaveSystemSettingWithoutCustomDatatype() throws Exception {
		SimpleObject property = new SimpleObject();
		property.add("property", "a.property.test");
		property.add("description", "Testing post operation of global property");
		property.add("value", "Saving property value without custome datatype");
		String json = new ObjectMapper().writeValueAsString(property);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject result = deserialize(handle(req));
		assertEquals("a.property.test", PropertyUtils.getProperty(result, "property"));
		assertEquals("Testing post operation of global property", PropertyUtils.getProperty(result, "description"));
		assertEquals("Saving property value without custome datatype", PropertyUtils.getProperty(result, "value"));
	}
	
	@Test
	public void shouldFindASystemSettingWithUUID() throws Exception {
		SimpleObject property = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		
		GlobalProperty expectedProperty = service.getGlobalPropertyByUuid(getUuid());
		assertNotNull(property);
		assertEquals(expectedProperty.getUuid(), PropertyUtils.getProperty(property, "uuid"));
		assertEquals(expectedProperty.getProperty(), PropertyUtils.getProperty(property, "property"));
		assertEquals(expectedProperty.getValue(), PropertyUtils.getProperty(property, "value"));
	}
	
	@Test
	public void shouldGetASystemSettingByName() throws Exception {
		final String name = service.getAllGlobalProperties().get(0).getProperty();
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + name);
		SimpleObject result = deserialize(handle(req));
		
		GlobalProperty gp = service.getGlobalPropertyObject(name);
		assertEquals(gp.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(gp.getProperty(), PropertyUtils.getProperty(result, "property"));
		assertEquals(gp.getDescription(), PropertyUtils.getProperty(result, "description"));
		assertEquals(gp.getValue(), PropertyUtils.getProperty(result, "value"));
	}
	
	@Test
	public void shouldEditASystemSetting() throws Exception {
		final String newValue = "Adding description by editing property";
		GlobalProperty expectedProperty = service.getGlobalPropertyByUuid(getUuid());
		assertNull(expectedProperty.getDescription());
		String json = "{ \"description\":\"" + newValue + "\" }";
		
		SimpleObject updatedProperty = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		assertTrue(newValue.equals(PropertyUtils.getProperty(updatedProperty, "description")));
	}
	
	@Test
	public void shouldPurgeASystemSetting() throws Exception {
		assertNotNull(service.getGlobalPropertyByUuid(getUuid()));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("purge", "true");
		handle(req);
		assertNull(service.getGlobalPropertyByUuid(getUuid()));
	}
	
	@Test
	public void shouldDeleteASystemSetting() throws Exception {
		assertNotNull(service.getGlobalPropertyByUuid(getUuid()));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		handle(req);
		assertNull(service.getGlobalPropertyByUuid(getUuid()));
	}
}
