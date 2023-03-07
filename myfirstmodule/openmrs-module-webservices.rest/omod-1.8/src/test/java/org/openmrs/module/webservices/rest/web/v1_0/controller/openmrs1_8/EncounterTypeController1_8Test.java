/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link EncounterTypeController}.
 */
public class EncounterTypeController1_8Test extends MainResourceControllerTest {
	
	private EncounterService service;
	
	@Override
	public String getURI() {
		return "encountertype";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_8.ENCOUNTER_TYPE_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getAllEncounterTypes(false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getEncounterService();
	}
	
	@Test
	public void shouldGetAnEncounterTypeByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		EncounterType encounterType = service.getEncounterTypeByUuid(getUuid());
		assertEquals(encounterType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(encounterType.getName(), PropertyUtils.getProperty(result, "name"));
		assertEquals(encounterType.getDescription(), PropertyUtils.getProperty(result, "description"));
	}
	
	@Test
	public void shouldGetAEncounterTypeByName() throws Exception {
		final String name = "Scheduled";
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + name);
		SimpleObject result = deserialize(handle(req));
		
		EncounterType encounterType = service.getEncounterType(name);
		assertEquals(encounterType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(encounterType.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldListAllEncounterTypes() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateAEncounterType() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject encounterType = new SimpleObject();
		encounterType.add("name", "test name");
		encounterType.add("description", "test description");
		
		String json = new ObjectMapper().writeValueAsString(encounterType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newEncounterType = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(newEncounterType, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldEditingAnEncounterType() throws Exception {
		final String newName = "updated name";
		SimpleObject encounterType = new SimpleObject();
		encounterType.add("name", newName);
		
		String json = new ObjectMapper().writeValueAsString(encounterType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		assertEquals(newName, service.getEncounterTypeByUuid(getUuid()).getName());
	}
	
	@Test
	public void shouldRetireAEncounterType() throws Exception {
		assertEquals(false, service.getEncounterTypeByUuid(getUuid()).isRetired());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		assertEquals(true, service.getEncounterTypeByUuid(getUuid()).isRetired());
		assertEquals(reason, service.getEncounterTypeByUuid(getUuid()).getRetireReason());
	}
	
	@Test
	public void shouldUnRetireAnEncounterType() throws Exception {
		EncounterType encounterType = service.getEncounterTypeByUuid(getUuid());
		encounterType.setRetired(true);
		encounterType.setRetireReason("random reason");
		service.saveEncounterType(encounterType);
		encounterType = service.getEncounterTypeByUuid(getUuid());
		assertTrue(encounterType.isRetired());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		encounterType = service.getEncounterTypeByUuid(getUuid());
		assertFalse(encounterType.isRetired());
		assertEquals("false", PropertyUtils.getProperty(response, "retired").toString());
		
	}
	
	@Test
	public void shouldPurgeAEncounterType() throws Exception {
		final String uuid = "02c533ab-b74b-4ee4-b6e5-ffb6d09a0ac8";
		assertNotNull(service.getEncounterTypeByUuid(uuid));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + uuid);
		req.addParameter("purge", "true");
		handle(req);
		assertNull(service.getEncounterTypeByUuid(uuid));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfEncounterTypesMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Sch");
		SimpleObject result = deserialize(handle(req));
		assertEquals(1, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
	}
}
