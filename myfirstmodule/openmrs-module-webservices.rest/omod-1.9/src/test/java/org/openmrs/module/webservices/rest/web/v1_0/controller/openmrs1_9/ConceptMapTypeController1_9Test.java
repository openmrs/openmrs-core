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
import org.openmrs.ConceptMapType;
import org.openmrs.api.ConceptService;
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
import static org.junit.Assert.assertNull;

/**
 * Tests CRUD operations for {@link ConceptMapType}s via web service calls
 */
public class ConceptMapTypeController1_9Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	@Override
	public String getURI() {
		return "conceptmaptype";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CONCEPT_MAP_TYPE_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getConceptMapTypes(false, false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getConceptService();
	}
	
	@Test
	public void shouldGetAnConceptMapTypeByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		ConceptMapType conceptMapTypeType = service.getConceptMapTypeByUuid(getUuid());
		assertEquals(conceptMapTypeType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(conceptMapTypeType.getName(), PropertyUtils.getProperty(result, "name"));
		assertEquals(conceptMapTypeType.isHidden(), PropertyUtils.getProperty(result, "isHidden"));
		assertEquals(conceptMapTypeType.getDescription(), PropertyUtils.getProperty(result, "description"));
	}
	
	@Test
	public void shouldGetAConceptMapTypeByName() throws Exception {
		final String name = "related-to";
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + name);
		SimpleObject result = deserialize(handle(req));
		
		ConceptMapType conceptMapTypeType = service.getConceptMapTypeByName(name);
		assertEquals(conceptMapTypeType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(conceptMapTypeType.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldListAllConceptMapTypes() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateAConceptMapType() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject conceptMapTypeType = new SimpleObject();
		conceptMapTypeType.add("name", "test name");
		
		String json = new ObjectMapper().writeValueAsString(conceptMapTypeType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newConceptMapType = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(newConceptMapType, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldEditingAConceptMapType() throws Exception {
		final String newName = "updated name";
		SimpleObject conceptMapTypeType = new SimpleObject();
		conceptMapTypeType.add("name", newName);
		
		String json = new ObjectMapper().writeValueAsString(conceptMapTypeType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		assertEquals(newName, service.getConceptMapTypeByUuid(getUuid()).getName());
	}
	
	@Test
	public void shouldRetireAConceptMapType() throws Exception {
		assertEquals(false, service.getConceptMapTypeByUuid(getUuid()).isRetired());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		assertEquals(true, service.getConceptMapTypeByUuid(getUuid()).isRetired());
		assertEquals(reason, service.getConceptMapTypeByUuid(getUuid()).getRetireReason());
	}
	
	@Test
	public void shouldPurgeAConceptMapType() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("purge", "true");
		handle(req);
		assertNull(service.getConceptMapTypeByUuid(getUuid()));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfConceptMapTypesMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "is");
		SimpleObject result = deserialize(handle(req));
		assertEquals(2, Util.getResultsSize(result));
	}
}
