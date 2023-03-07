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
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests CRUD operations for {@link PatientIdentifierType}s via web service calls
 */
public class PatientIdentifierTypeController1_8Test extends MainResourceControllerTest {
	
	private PatientService service;
	
	@Override
	public String getURI() {
		return "patientidentifiertype";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PATIENT_IDENTIFIER_TYPE_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getAllPatientIdentifierTypes(false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getPatientService();
	}
	
	@Test
	public void shouldGetAPatientIdentifierTypeByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		PatientIdentifierType patientIdentifierType = service.getPatientIdentifierTypeByUuid(getUuid());
		assertEquals(patientIdentifierType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(patientIdentifierType.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldGetAPatientIdentifierTypeByName() throws Exception {
		final String name = "OpenMRS Identification Number";
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + name);
		SimpleObject result = deserialize(handle(req));
		
		PatientIdentifierType patientIdentifierType = service.getPatientIdentifierTypeByName(name);
		assertEquals(patientIdentifierType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(patientIdentifierType.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldListAllPatientIdentifierTypes() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateAPatientIdentifierType() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject patientIdentifierType = new SimpleObject();
		patientIdentifierType.add("name", "test name");
		patientIdentifierType.add("description", "test description");
		
		String json = new ObjectMapper().writeValueAsString(patientIdentifierType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newPatientIdentifierType = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(newPatientIdentifierType, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldEditingAPatientIdentifierType() throws Exception {
		final String newName = "updated name";
		SimpleObject patientIdentifierType = new SimpleObject();
		patientIdentifierType.add("name", newName);
		
		String json = new ObjectMapper().writeValueAsString(patientIdentifierType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		assertEquals(newName, service.getPatientIdentifierTypeByUuid(getUuid()).getName());
	}
	
	@Test
	public void shouldRetireAPatientIdentifierType() throws Exception {
		assertEquals(false, service.getPatientIdentifierTypeByUuid(getUuid()).isRetired());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		assertEquals(true, service.getPatientIdentifierTypeByUuid(getUuid()).isRetired());
		assertEquals(reason, service.getPatientIdentifierTypeByUuid(getUuid()).getRetireReason());
	}
	
	@Test
	public void shouldUnRetireAPatientIdentifierType() throws Exception {
		PatientIdentifierType patientIdentifierType = service.getPatientIdentifierTypeByUuid(getUuid());
		patientIdentifierType.setRetired(true);
		patientIdentifierType.setRetireReason("random reason");
		service.savePatientIdentifierType(patientIdentifierType);
		patientIdentifierType = service.getPatientIdentifierTypeByUuid(getUuid());
		assertTrue(patientIdentifierType.isRetired());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		patientIdentifierType = service.getPatientIdentifierTypeByUuid(getUuid());
		assertFalse(patientIdentifierType.isRetired());
		assertEquals("false", PropertyUtils.getProperty(response, "retired").toString());
		
	}
	
	@Test
	public void shouldPurgeAPatientIdentifierType() throws Exception {
		final String uuid = "158d6b17-a8ab-435b-8fe3-952a04bda757";
		assertNotNull(service.getPatientIdentifierTypeByUuid(uuid));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + uuid);
		req.addParameter("purge", "true");
		handle(req);
		assertNull(service.getPatientIdentifierTypeByUuid(uuid));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
}
