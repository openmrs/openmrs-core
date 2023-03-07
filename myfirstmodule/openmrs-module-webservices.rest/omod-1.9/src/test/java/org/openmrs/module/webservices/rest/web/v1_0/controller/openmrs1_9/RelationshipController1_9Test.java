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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Relationship;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Contains tests for {@link RelationshipController} CRUD operations
 */
public class RelationshipController1_9Test extends MainResourceControllerTest {
	
	private PersonService service;
	
	public static final String RELATIONSHIP_DATA_SET = "customRelationshipTypes1_8.xml";
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "relationship";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.RELATIONSHIP_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllRelationships().size(); //not supported
	}
	
	@Before
	public void before() throws Exception {
		executeDataSet(RELATIONSHIP_DATA_SET);
		this.service = Context.getPersonService();
	}
	
	@Test
	public void shouldCreateARelationship() throws Exception {
		int originalCount = service.getAllRelationships().size();
		String json = "{ \"personA\":\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\", \"relationshipType\":\""
		        + RestTestConstants1_8.RELATIONSHIP_TYPE_UUID + "\", \"personB\":"
		        + "\"5946f880-b197-400b-9caa-a3c661d23041\"" + "}";
		
		Object newRelationship = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newRelationship, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllRelationships().size());
	}
	
	@Test
	public void shouldEditARelationship() throws Exception {
		final String newRelationshipTypeUuid = "d47f056e-f147-49a3-88e1-0c91d199510d";
		Relationship relationship = service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID);
		Assert.assertNotNull(relationship);
		//sanity checks
		Assert.assertFalse(newRelationshipTypeUuid.equalsIgnoreCase(relationship.getRelationshipType().getUuid()));
		String json = "{\"relationshipType\":\"" + newRelationshipTypeUuid + "\"" + "}";
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		Relationship updated = service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID);
		Assert.assertNotNull(updated);
		Assert.assertEquals(newRelationshipTypeUuid, updated.getRelationshipType().getUuid());
	}
	
	@Test
	public void shouldVoidARelationship() throws Exception {
		Relationship relationship = service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID);
		Assert.assertFalse(relationship.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "test reason")));
		
		relationship = service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID);
		Assert.assertTrue(relationship.isVoided());
		Assert.assertEquals("test reason", relationship.getVoidReason());
	}
	
	@Test
	public void shouldUnVoidARelationship() throws Exception {
		Relationship relationship = service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID);
		service.voidRelationship(relationship, "some random reason");
		relationship = service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID);
		Assert.assertTrue(relationship.isVoided());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		relationship = service.getRelationshipByUuid(getUuid());
		Assert.assertFalse(relationship.isVoided());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());
		
	}
	
	@Test
	public void shouldPurgeARelatonship() throws Exception {
		Assert.assertNotNull(service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID));
		int originalCount = service.getAllRelationships().size();
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "true")));
		
		Assert.assertNull(service.getRelationshipByUuid(RestTestConstants1_8.RELATIONSHIP_UUID));
		Assert.assertEquals(originalCount - 1, service.getAllRelationships().size());
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonUuid() throws Exception {
		String firstRelationshipUuidBelongsToPerson = "4ce634c8-d744-40b3-9d5f-577a5f025b01";
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("person",
		        "86526ed6-3c11-11de-a0ba-001e378eb67e"))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(4, hits.size());
		Assert.assertEquals(firstRelationshipUuidBelongsToPerson, PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonUuidAndRelatedPersonUuid() throws Exception {
		String firstRelationshipUuid = "83d17902-2c7e-41e6-9d11-2d405c897da3";
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("person",
		        "86526ed6-3c11-11de-a0ba-001e378eb67e"), new Parameter("relatedPerson",
		        "5946f880-b197-400b-9caa-a3c661d23041"))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(2, hits.size());
		Assert.assertEquals(firstRelationshipUuid, PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonUuidAndRelationshipType() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("person",
		        "341b4e41-790c-484f-b6ed-71dc8da222de"), new Parameter("relation",
		        RestTestConstants1_8.RELATIONSHIP_TYPE_UUID))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(2, hits.size());
		Assert.assertEquals(RestTestConstants1_8.RELATIONSHIP_UUID, PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonAUuidAndPersonBUuidAndRelationshipType() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("personA",
		        "341b4e41-790c-484f-b6ed-71dc8da222de"), new Parameter("personB", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"),
		    new Parameter("relation", RestTestConstants1_8.RELATIONSHIP_TYPE_UUID))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals(RestTestConstants1_8.RELATIONSHIP_UUID, PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonAUuidAndPersonBUuid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("personA",
		        "341b4e41-790c-484f-b6ed-71dc8da222de"), new Parameter("personB", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5"))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals(RestTestConstants1_8.RELATIONSHIP_UUID, PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonAUuidAndRelationshipType() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("personA",
		        "86526ed6-3c11-11de-a0ba-001e378eb67e"), new Parameter("relation",
		        RestTestConstants1_8.RELATIONSHIP_TYPE_UUID))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(2, hits.size());
		Assert.assertEquals("4ce634c8-d744-40b3-9d5f-577a5f025b01", PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchRelationshipsByPersonBUuidAndRelationshipType() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("personB",
		        "86526ed6-3c11-11de-a0ba-001e378eb67e"), new Parameter("relation", "d38a8159-3e37-410d-83ae-245a205cb83c"))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals("70c34cf1-770b-49ec-9cc1-f79190834143", PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
}
