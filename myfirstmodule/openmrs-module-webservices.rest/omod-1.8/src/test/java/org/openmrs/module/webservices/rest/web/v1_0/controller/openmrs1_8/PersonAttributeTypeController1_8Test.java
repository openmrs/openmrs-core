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

import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

public class PersonAttributeTypeController1_8Test extends MainResourceControllerTest {
	
	private PersonService service;
	
	@Before
	public void init() {
		service = Context.getPersonService();
	}
	
	/**
	 * @see PersonAttributeTypeController#createPersonAttributeType(SimpleObject,WebRequest)
	 * @throws Exception
	 * @verifies create a new PersonAttributeType
	 */
	@Test
	public void createPersonAttributeType_shouldCreateANewPersonAttributeType() throws Exception {
		
		long originalCount = getAllCount();
		
		SimpleObject obj = new SimpleObject();
		obj.add("name", "Some attributeType");
		obj.add("description", "Attribute Type for test");
		obj.add("format", "java.lang.String");
		obj.add("searchable", "false");
		
		String json = new ObjectMapper().writeValueAsString(obj);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newPersonAttributeType = deserialize(handle(req));
		
		Util.log("Created person attribute type", newPersonAttributeType);
		Assert.assertEquals(originalCount + 1, getAllCount());
	}
	
	/**
	 * @see PersonAttributeTypeController#getPersonAttributeType(PersonAttributeType,WebRequest)
	 * @throws Exception
	 * @verifies get a default representation of a person attribute type
	 */
	@Test
	public void getPersonAttributeType_shouldGetADefaultRepresentationOfAPersonAttributeType() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Util.log("Person fetched (default)", result);
		
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	/**
	 * @see PersonAttributeTypeController#getPersonAttributeType(String,WebRequest)
	 * @throws Exception
	 * @verifies get a full representation of a person attribute type
	 */
	@Test
	public void getPersonAttributeType_shouldGetAFullRepresentationOfAPersonAttributeType() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Util.log("Person fetched (full)", result);
		
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "name"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	/**
	 * @see PersonAttributeTypeController#updatePersonAttributeType(PersonAttributeType,SimpleObject,WebRequest)
	 * @throws Exception
	 * @verifies change a property on a person
	 */
	@Test
	public void updatePersonAttributeType_shouldChangeAPropertyOnAPersonAttributeType() throws Exception {
		
		final String newDescription = "Updated description";
		
		PersonAttributeType obj = service.getPersonAttributeTypeByUuid(getUuid());
		Assert.assertNotNull(obj);
		Assert.assertFalse(newDescription.equals(obj.getDescription()));
		Util.log("Old PersonAttributeType Description: ", obj.getDescription());
		
		String json = "{\"description\":\"Updated description\"}";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		PersonAttributeType editedAttr = service.getPersonAttributeTypeByUuid(getUuid());
		Assert.assertNotNull(editedAttr);
		Assert.assertEquals(newDescription, editedAttr.getDescription());
		Util.log("Edited PersonAttributeType Description: ", editedAttr.getDescription());
	}
	
	/**
	 * @see PersonAttributeTypeController#retirePersonAttributeType(PersonAttributeType,String,WebRequest)
	 * @throws Exception
	 * @verifies void a person attribute type
	 */
	@Test
	public void retirePersonAttributeType_shouldRetireAPersonAttributeType() throws Exception {
		
		final String nonRetiredAttribute = "a0f5521c-dbbd-4c10-81b2-1b7ab18330df";
		
		PersonAttributeType obj = service.getPersonAttributeTypeByUuid(nonRetiredAttribute);
		Assert.assertNotNull(obj);
		Assert.assertFalse(obj.isRetired());
		
		MockHttpServletRequest delRequest = request(RequestMethod.DELETE, getURI() + "/" + nonRetiredAttribute);
		delRequest.addParameter("!purge", "");
		delRequest.addParameter("reason", "unit test");
		handle(delRequest);
		
		obj = service.getPersonAttributeTypeByUuid(nonRetiredAttribute);
		Assert.assertNotNull(obj);
		Assert.assertTrue(obj.isRetired());
		Assert.assertTrue("unit test".equals(obj.getRetireReason()));
	}
	
	@Test
	public void shouldUnRetireAPersonAttributeType() throws Exception {
		
		final String nonRetiredAttribute = "a0f5521c-dbbd-4c10-81b2-1b7ab18330df";
		PersonAttributeType personAttrType = service.getPersonAttributeTypeByUuid(nonRetiredAttribute);
		personAttrType.setRetired(true);
		personAttrType.setRetireReason("random reason");
		service.savePersonAttributeType(personAttrType);
		personAttrType = service.getPersonAttributeTypeByUuid(nonRetiredAttribute);
		Assert.assertTrue(personAttrType.isRetired());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + nonRetiredAttribute, json)));
		
		personAttrType = service.getPersonAttributeTypeByUuid(nonRetiredAttribute);
		Assert.assertFalse(personAttrType.isRetired());
		Assert.assertEquals("false", PropertyUtils.getProperty(response, "retired").toString());
		
	}
	
	/**
	 * @see PersonAttributeTypeController#findPersonAttributeTypes(String,WebRequest,HttpServletResponse)
	 * @throws Exception
	 * @verifies return no results if there are no matching person(s)
	 */
	@Test
	public void findPersonAttributeTypes_shouldReturnNoResultsIfThereAreNoMatchingPersons() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "foo-bar-baz");
		
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result);
		
		List<PersonAttributeType> hits = (List<PersonAttributeType>) result.get("results");
		Assert.assertEquals(0, hits.size());
	}
	
	/**
	 * @see PersonAttributeTypeController#findPersonAttributeTypes(String,WebRequest,HttpServletResponse)
	 * @throws Exception
	 * @verifies find matching person attribute types
	 */
	@Test
	public void findPersonAttributeTypes_shouldFindMatchingPersonAttributeTypes() throws Exception {
		
		final String uuidFound = "54fc8400-1683-4d71-a1ac-98d40836ff7c";
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		req.addParameter("q", "Birthplace");
		
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result);
		Util.log("findPersonAttributeTypes", result);
		
		List<PersonAttributeType> results = (List<PersonAttributeType>) result.get("results");
		Util.log("Found " + results.size() + " personAttributeType(s)", results);
		Assert.assertEquals(1, results.size());
		
		Object obj = results.get(0);
		Assert.assertEquals(uuidFound, PropertyUtils.getProperty(obj, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(obj, "links"));
		Assert.assertNotNull(PropertyUtils.getProperty(obj, "display"));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "personattributetype";
	}
	
	/**
	 * Return UUID for Race PersonAttributeType
	 * 
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PERSON_ATTRIBUTE_TYPE_UUID;
	}
	
	/**
	 * Return all PersonAttributeTypes regardless of retired status
	 * 
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllPersonAttributeTypes(false).size();
	}
}
