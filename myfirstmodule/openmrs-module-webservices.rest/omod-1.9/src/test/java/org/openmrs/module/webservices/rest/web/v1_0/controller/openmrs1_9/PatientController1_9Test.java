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
import static org.junit.Assert.assertFalse;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests CRUD operations for {@link Patient}s via web service calls
 */
public class PatientController1_9Test extends MainResourceControllerTest {
	
	private PatientService service;
	
	@Override
	public String getURI() {
		return "patient";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PATIENT_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Before
	public void before() {
		this.service = Context.getPatientService();
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Test
	public void shouldGetAPatientByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Patient patient = service.getPatientByUuid(getUuid());
		assertEquals(patient.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertNotNull(PropertyUtils.getProperty(result, "identifiers"));
		assertNotNull(PropertyUtils.getProperty(result, "person"));
		assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldCreateAPatient() throws Exception {
		long originalCount = service.getAllPatients().size();
		String json = "{ \"person\": \"ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562\", "
		        + "\"identifiers\": [{ \"identifier\":\"abc123ez\", "
		        + "\"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", "
		        + "\"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\", " + "\"preferred\": true }] }";
		
		SimpleObject newPatient = deserialize(handle(newPostRequest(getURI(), json)));
		
		assertNotNull(PropertyUtils.getProperty(newPatient, "uuid"));
		assertEquals(originalCount + 1, service.getAllPatients().size());
	}
	
	@Test
	public void shouldCreatePersonAndPatient() throws Exception {
		long originalCount = service.getAllPatients().size();
		String json = "{ \"identifiers\": [{ \"identifier\":\"abc123ez\", "
		        + "\"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", "
		        + "\"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\", " + "\"preferred\": true }], " + "\"person\": { "
		        + "\"gender\": \"M\", " + "\"age\": 47, " + "\"birthdate\": \"1970-01-01T00:00:00.000+0100\", "
		        + "\"birthdateEstimated\": false, " + "\"dead\": false, " + "\"deathDate\": null, "
		        + "\"causeOfDeath\": null, " + "\"names\": [{\"givenName\": \"Thomas\", \"familyName\": \"Smith\"}] " + "}}";
		
		SimpleObject newPatient = deserialize(handle(newPostRequest(getURI(), json)));
		
		assertNotNull(PropertyUtils.getProperty(newPatient, "uuid"));
		assertEquals(originalCount + 1, service.getAllPatients().size());
	}
	
	@Test
	public void shouldVoidAPatient() throws Exception {
		Patient patient = service.getPatientByUuid(getUuid());
		final String reason = "some random reason";
		assertEquals(false, patient.isVoided());
		MockHttpServletRequest req = newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", ""),
		    new Parameter("reason", reason));
		handle(req);
		patient = service.getPatientByUuid(getUuid());
		assertTrue(patient.isVoided());
		assertEquals(reason, patient.getVoidReason());
	}
	
	@Test
	public void shouldUnVoidAPatient() throws Exception {
		service = Context.getPatientService();
		Patient patient = service.getPatientByUuid(getUuid());
		service.voidPatient(patient, "some random reason");
		patient = service.getPatientByUuid(getUuid());
		assertTrue(patient.isVoided());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		patient = service.getPatientByUuid(getUuid());
		assertFalse(patient.isVoided());
		assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());
		
	}
	
	@Test
	public void shouldPurgeAPatient() throws Exception {
		final String uuid = "86526ed6-3c11-11de-a0ba-001e378eb67e";
		assertNotNull(service.getPatientByUuid(uuid));
		MockHttpServletRequest req = newDeleteRequest(getURI() + "/" + uuid, new Parameter("purge", "true"));
		handle(req);
		assertNull(service.getPatientByUuid(uuid));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfPatientsMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Horatio");
		SimpleObject result = deserialize(handle(req));
		assertEquals(1, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchPatientsWithCustomRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Horatio");
		String customRep = "custom:(uuid,identifiers:(identifierType:(name),identifier),person:(preferredName,age,gender))";
		req.addParameter("v", customRep);
		SimpleObject result = deserialize(handle(req));
		assertEquals(1, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
		assertEquals("Horatio",
		    PropertyUtils.getProperty(Util.getResultsList(result).get(0), "person.preferredName.givenName"));
		assertEquals("Hornblower",
		    PropertyUtils.getProperty(Util.getResultsList(result).get(0), "person.preferredName.familyName"));
	}
	
	@Test
	public void shouldRespectStartIndexAndLimit() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI());
		req.setParameter("q", "Test");
		SimpleObject results = deserialize(handle(req));
		int fullCount = Util.getResultsSize(results);
		assertTrue("This test assumes > 2 matching patients", fullCount > 2);
		
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_LIMIT, "2");
		results = deserialize(handle(req));
		int firstCount = Util.getResultsSize(results);
		assertEquals(1, firstCount);
		
		req.removeParameter(RestConstants.REQUEST_PROPERTY_FOR_LIMIT);
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX, "2");
		results = deserialize(handle(req));
		int restCount = Util.getResultsSize(results);
		assertEquals(fullCount, firstCount + restCount);
	}
	
	@Test
	public void shouldMarkTheFirstIdentifierAsPreferredIfNoneMarked() throws Exception {
		String uuid = "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562";
		String preferredIdentifier = "1234";
		String json = "{ \"person\": \""
		        + uuid
		        + "\", \"identifiers\": ["
		        + "{ \"identifier\":\""
		        + preferredIdentifier
		        + "\", \"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", \"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\" }, "
		        + "{\"identifier\":\"12345678\", \"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", \"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\"} ] }";
		
		deserialize(handle(newPostRequest(getURI(), json)));
		PatientIdentifier prefIdentifier = service.getPatientByUuid(uuid).getPatientIdentifier();
		assertTrue(prefIdentifier.isPreferred());
		assertEquals(preferredIdentifier, prefIdentifier.getIdentifier());
	}
	
	@Test
	public void shouldRespectPreferredIdentifier() throws Exception {
		String uuid = "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562";
		String preferredIdentifier = "12345678";
		String json = "{ \"person\": \""
		        + uuid
		        + "\", \"identifiers\": ["
		        + "{ \"identifier\":\"1234\", \"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", \"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\"}, "
		        + "{\"identifier\":\""
		        + preferredIdentifier
		        + "\", \"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", \"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\", \"preferred\": true} ] }";
		
		deserialize(handle(newPostRequest(getURI(), json)));
		PatientIdentifier prefIdentifier = service.getPatientByUuid(uuid).getPatientIdentifier();
		assertTrue(prefIdentifier.isPreferred());
		assertEquals(preferredIdentifier, prefIdentifier.getIdentifier());
	}
	
	@Test(expected = ConversionException.class)
	public void shouldFailIfThereAreMultiplePreferredIdentifiers() throws Exception {
		String uuid = "ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562";
		String json = "{ \"person\": \""
		        + uuid
		        + "\", \"identifiers\": ["
		        + "{ \"identifier\":\"1234\", \"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", \"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\", \"preferred\": true}, "
		        + "{\"identifier\":\"12345678\", \"identifierType\":\"2f470aa8-1d73-43b7-81b5-01f0c0dfa53c\", \"location\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\", \"preferred\": true} ] }";
		
		deserialize(handle(newPostRequest(getURI(), json)));
	}
	
	/**
	 * @verifies return delegating resource description
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCustomRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.CustomRepresentation)
	 */
	@Test
	public void getCustomRepresentationDescription_shouldReturnDelegatingResourceDescription() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Horatio");
		req.addParameter("v",
		    "custom:(uuid,identifiers:(uuid,identifierType:(uuid)),attributes:(uuid,attributeType:(uuid)))");
		SimpleObject result = deserialize(handle(req));
		
		assertEquals(1, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
		assertNotNull(PropertyUtils.getProperty(Util.getResultsList(result).get(0), "identifiers"));
		assertNotNull(PropertyUtils.getProperty(Util.getResultsList(result).get(0), "attributes"));
	}
}
