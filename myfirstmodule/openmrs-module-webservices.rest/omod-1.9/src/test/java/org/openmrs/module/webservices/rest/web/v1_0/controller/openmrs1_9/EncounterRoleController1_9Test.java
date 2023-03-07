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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterRole;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests functionality of {@link EncounterRoleController}.
 */
public class EncounterRoleController1_9Test extends MainResourceControllerTest {
	
	private EncounterService service;
	
	@Override
	public String getURI() {
		return "encounterrole";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.ENCOUNTER_ROLE_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getAllEncounterRoles(false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getEncounterService();
	}
	
	@Test
	public void shouldGetAnEncounterRoleByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		EncounterRole encounterRole = service.getEncounterRoleByUuid(getUuid());
		assertEquals(encounterRole.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(encounterRole.getName(), PropertyUtils.getProperty(result, "name"));
		assertEquals(encounterRole.getDescription(), PropertyUtils.getProperty(result, "description"));
	}
	
	@Test
	public void shouldListAllEncounterRoles() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateAEncounterRole() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject encounterRole = new SimpleObject();
		encounterRole.add("name", "test name");
		encounterRole.add("description", "test description");
		
		String json = new ObjectMapper().writeValueAsString(encounterRole);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newEncounterRole = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(newEncounterRole, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldEditingAnEncounterRole() throws Exception {
		final String newName = "updated name";
		SimpleObject encounterRole = new SimpleObject();
		encounterRole.add("name", newName);
		
		String json = new ObjectMapper().writeValueAsString(encounterRole);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		assertEquals(newName, service.getEncounterRoleByUuid(getUuid()).getName());
	}
	
	@Test
	public void shouldRetireAEncounterRole() throws Exception {
		assertEquals(false, service.getEncounterRoleByUuid(getUuid()).isRetired());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		assertEquals(true, service.getEncounterRoleByUuid(getUuid()).isRetired());
		assertEquals(reason, service.getEncounterRoleByUuid(getUuid()).getRetireReason());
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
}
