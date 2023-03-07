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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.not;

/**
 * Tests functionality of {@link PersonAttributeController}.
 */
public class PersonAttributeController1_9Test extends MainResourceControllerTest {
	
	String personUuid = RestTestConstants1_8.PERSON_UUID;
	
	private PersonService service;
	
	@Before
	public void before() throws Exception {
		this.service = Context.getPersonService();
	}
	
	@Test
	public void shouldAddAttributeToPerson() throws Exception {
		int before = service.getPersonByUuid(personUuid).getAttributes().size();
		String json = "{ \"attributeType\":\"b3b6d540-a32e-44c7-91b3-292d97667518\", \"value\":\"testing\"}";
		handle(newPostRequest(getURI(), json));
		int after = service.getPersonByUuid(personUuid).getAttributes().size();
		assertThat(after, is(before + 1));
	}
	
	@Test
	public void shouldAddConceptAttributeToPerson() throws Exception {
		executeDataSet("personAttributeTypeWithConcept.xml");
		
		int before = service.getPersonByUuid(personUuid).getAttributes().size();
		
		String json = "{ \"hydratedObject\":\"f102c80f-1yz9-4da3-bb88-8122ce8868dd\", \"attributeType\":\"55e6ce9e-25bf-11e3-a013-3c0754156a5d\"}";
		handle(newPostRequest(getURI(), json));
		
		Set<PersonAttribute> attributes = service.getPersonByUuid(personUuid).getAttributes();
		int after = attributes.size();
		assertThat(after, is(before + 1));
		
		assertThat(getLastPersonAttribute(attributes).getValue(), is("102"));
	}
	
	@Test
	public void shouldRenderHydratedAttributable() throws Exception {
		executeDataSet("personAttributeTypeWithConcept.xml");
		
		String json = "{ \"hydratedObject\":\"f102c80f-1yz9-4da3-bb88-8122ce8868dd\", \"attributeType\":\"55e6ce9e-25bf-11e3-a013-3c0754156a5d\"}";
		String postResponse = handle(newPostRequest(getURI(), json)).getContentAsString();
		SimpleObject postResponseObject = new ObjectMapper().readValue(postResponse, SimpleObject.class);
		
		String getResponse = handle(
		    newGetRequest(getURI() + "/" + postResponseObject.get("uuid"), new Parameter("v", "full"))).getContentAsString();
		SimpleObject getResponseObject = new ObjectMapper().readValue(getResponse, SimpleObject.class);
		
		assertThat(getResponseObject.get("hydratedObject"), not(is(nullValue())));
	}
	
	private PersonAttribute getLastPersonAttribute(Set<PersonAttribute> attributes) {
		PersonAttribute personAttribute = null;
		Iterator<PersonAttribute> iterator = attributes.iterator();
		while (iterator.hasNext()) {
			personAttribute = iterator.next();
		}
		return personAttribute;
	}
	
	@Test
	public void shouldEditAttribute() throws Exception {
		String json = "{ \"attributeType\":\"54fc8400-1683-4d71-a1ac-98d40836ff7c\" }";
		
		PersonAttribute personAttribute = service.getPersonAttributeByUuid(getUuid());
		assertThat(personAttribute.getAttributeType().getName(), is("Race"));
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		personAttribute = service.getPersonAttributeByUuid(getUuid());
		assertThat(personAttribute.getAttributeType().getName(), is("Birthplace"));
	}
	
	@Test
	public void shouldVoidAttribute() throws Exception {
		PersonAttribute personAttribute = service.getPersonAttributeByUuid(getUuid());
		assertThat(personAttribute.isVoided(), is(false));
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "unit test")));
		
		personAttribute = service.getPersonAttributeByUuid(getUuid());
		assertThat(personAttribute.isVoided(), is(true));
		assertThat(personAttribute.getVoidReason(), is("unit test"));
	}
	
	@Test
	public void shouldPurgeAttribute() throws Exception {
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		assertThat(service.getPersonAttributeByUuid(getUuid()), nullValue());
	}
	
	@Test
	public void shouldSupportLocationPersonAttribute() throws Exception {
		String personAttributeTypeJson = "{\"name\": \"location\", \"description\": \"Points to a location\", \"format\": \"org.openmrs.Location\"}";
		SimpleObject personAttributeType = deserialize(handle(newPostRequest("personattributetype", personAttributeTypeJson)));
		String personAttributeTypeUuid = (String) personAttributeType.get("uuid");
		assertThat(personAttributeTypeUuid, is(notNullValue()));
		
		String personAttributeJson = "{ \"attributeType\":\"" + personAttributeTypeUuid + "\", \"value\":\"1\"}"; //We should be able to pass UUID, see RESTWS-398
		SimpleObject personAttribute = deserialize(handle(newPostRequest(getURI(), personAttributeJson)));
		
		Map<String, Object> value = (Map<String, Object>) personAttribute.get("value");
		
		assertThat(value.get("uuid"), is(notNullValue()));
		assertThat((String) value.get("display"), is("Unknown Location"));
		assertThat(value.get("links"), is(notNullValue()));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "person/" + RestTestConstants1_8.PERSON_UUID + "/attribute";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PERSON_ATTRIBUTE_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getPersonByUuid(RestTestConstants1_8.PERSON_UUID).getAttributes().size();
	}
}
