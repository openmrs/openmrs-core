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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests CRUD operations for {@link Person}s via web service calls
 */
public class PersonController1_9Test extends MainResourceControllerTest {
	
	private PersonService service;
	
	@Override
	public String getURI() {
		return "person";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PERSON_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Before
	public void before() {
		this.service = Context.getPersonService();
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Test
	public void shouldGetAPersonByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Person person = service.getPersonByUuid(getUuid());
		assertEquals(person.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertNotNull(PropertyUtils.getProperty(result, "preferredName"));
		assertEquals(person.getGender(), PropertyUtils.getProperty(result, "gender"));
		assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldGetPersonDateCreated() throws Exception {
		Person person = service.getPersonByUuid(getUuid());
		Date personDateCreated = new Date();
		person.setPersonDateCreated(personDateCreated);
		service.savePerson(person);
		
		MockHttpServletRequest req = newGetRequest(getURI() + "/" + getUuid(), new Parameter("v",
		        RestConstants.REPRESENTATION_FULL));
		SimpleObject result = deserialize(handle(req));
		
		Map<String, String> auditInfo = (Map<String, String>) PropertyUtils.getProperty(result, "auditInfo");
		
		assertEquals(ConversionUtil.convertToRepresentation(personDateCreated, Representation.FULL),
		    auditInfo.get("dateCreated"));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldCreateAPerson() throws Exception {
		long originalCount = service.getPeople("", false).size();
		String json = "{ \"names\": [{ \"givenName\":\"Helen\", \"familyName\":\"of Troy\" }, "
		        + "{\"givenName\":\"Leda\", \"familyName\":\"Nemesis\"} ], "
		        + "\"birthdate\":\"2003-01-01\", \"gender\":\"F\" }";
		
		SimpleObject newPerson = deserialize(handle(newPostRequest(getURI(), json)));
		
		String uuid = PropertyUtils.getProperty(newPerson, "uuid").toString();
		Person person = Context.getPersonService().getPersonByUuid(uuid);
		assertEquals(2, person.getNames().size());
		assertEquals("Helen of Troy", person.getPersonName().getFullName());
		assertEquals(++originalCount, service.getPeople("", false).size());
	}
	
	@Test
	public void shouldCreateAPersonWithAttributes() throws Exception {
		long originalCount = service.getPeople("", false).size();
		final String birthPlace = "Nsambya";
		String json = "{ \"names\": [{ \"givenName\":\"Helen\", \"familyName\":\"of Troy\" }], "
		        + "\"birthdate\":\"2003-01-01\", \"gender\":\"F\", \"attributes\":"
		        + "[{\"attributeType\":\"54fc8400-1683-4d71-a1ac-98d40836ff7c\",\"value\": \"" + birthPlace + "\"}] }";
		
		SimpleObject newPerson = deserialize(handle(newPostRequest(getURI(), json)));
		
		String uuid = PropertyUtils.getProperty(newPerson, "uuid").toString();
		Person person = Context.getPersonService().getPersonByUuid(uuid);
		assertEquals(++originalCount, service.getPeople("", false).size());
		assertEquals(birthPlace, person.getAttribute("Birthplace").getValue());
	}
	
	@Test
	public void shouldEditAPerson() throws Exception {
		Person person = service.getPersonByUuid(getUuid());
		assertFalse("F".equals(person.getGender()));
		assertFalse(person.isDead());
		assertNull(person.getCauseOfDeath());
		String json = "{\"gender\":\"F\",\"dead\":true, \"causeOfDeath\":\"15f83cd6-64e9-4e06-a5f9-364d3b14a43d\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		assertNotNull(response);
		Object responsePersonContents = PropertyUtils.getProperty(response, "person");
		assertNotNull(responsePersonContents);
		assertTrue("F".equals(PropertyUtils.getProperty(responsePersonContents, "gender").toString()));
		assertEquals("F", person.getGender());
		assertTrue(person.isDead());
		assertNotNull(person.getCauseOfDeath());
	}
	
	@Test(expected = ConversionException.class)
	public void shouldNotAllowUpdatingNamesProperty() throws Exception {
		handle(newPostRequest(getURI() + "/" + getUuid(), "{\"names\":\"[]\"}"));
	}
	
	@Test(expected = ConversionException.class)
	public void shouldNotAllowUpdatingAddressesProperty() throws Exception {
		handle(newPostRequest(getURI() + "/" + getUuid(), "{\"addresses\":\"[]\"}"));
	}
	
	@Test
	public void shouldSetThePreferredAddressAndUnmarkTheOldOne() throws Exception {
		executeDataSet("PersonControllerTest-otherPersonData.xml");
		Person person = service.getPersonByUuid(getUuid());
		PersonAddress preferredAddress = service.getPersonAddressByUuid("8a806d8c-822d-11e0-872f-18a905e044dc");
		PersonAddress notPreferredAddress = service.getPersonAddressByUuid("3350d0b5-821c-4e5e-ad1d-a9bce331e118");
		assertTrue(preferredAddress.isPreferred());
		assertFalse(notPreferredAddress.isPreferred());
		assertFalse(notPreferredAddress.isVoided());
		//sanity check that the addresses belong to the person
		assertEquals(person, preferredAddress.getPerson());
		assertEquals(person, notPreferredAddress.getPerson());
		
		handle(newPostRequest(getURI() + "/" + getUuid(), "{ \"preferredAddress\":\"" + notPreferredAddress.getUuid()
		        + "\" }"));
		
		assertEquals(notPreferredAddress, person.getPersonAddress());
		assertTrue(notPreferredAddress.isPreferred());
		assertFalse(preferredAddress.isPreferred());
	}
	
	@Test
	public void shouldSetThePreferredNameAndUnmarkTheOldOne() throws Exception {
		executeDataSet("PersonControllerTest-otherPersonData.xml");
		Person person = service.getPersonByUuid(getUuid());
		PersonName preferredName = service.getPersonNameByUuid("399e3a7b-6482-487d-94ce-c07bb3ca3cc7");
		PersonName notPreferredName = service.getPersonNameByUuid("499e3a7b-6482-487d-94ce-c07bb3ca3cc8");
		assertTrue(preferredName.isPreferred());
		assertFalse(notPreferredName.isPreferred());
		assertFalse(notPreferredName.isVoided());
		//sanity check that the names belong to the person
		assertEquals(person, preferredName.getPerson());
		assertEquals(person, notPreferredName.getPerson());
		
		handle(newPostRequest(getURI() + "/" + getUuid(), "{ \"preferredName\":\"" + notPreferredName.getUuid() + "\" }"));
		
		assertEquals(notPreferredName, person.getPersonName());
		assertTrue(notPreferredName.isPreferred());
		assertFalse(preferredName.isPreferred());
	}
	
	@Test
	public void shouldVoidAPerson() throws Exception {
		Person person = service.getPersonByUuid(getUuid());
		final String reason = "some random reason";
		assertEquals(false, person.isVoided());
		MockHttpServletRequest req = newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", ""),
		    new Parameter("reason", reason));
		handle(req);
		person = service.getPersonByUuid(getUuid());
		assertTrue(person.isVoided());
		assertEquals(reason, person.getVoidReason());
	}
	
	@Test
	public void shouldUnVoidAPerson() throws Exception {
		Person person = service.getPersonByUuid(getUuid());
		service.voidPerson(person, "some random reason");
		person = service.getPersonByUuid(getUuid());
		assertTrue(person.isVoided());
		
		String json = "{\"deleted\": \"false\"}";
		SimpleObject response = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		
		person = service.getPersonByUuid(getUuid());
		assertFalse(person.isVoided());
		assertEquals("false", PropertyUtils.getProperty(response, "voided").toString());
		
	}
	
	@Test
	public void shouldPurgeAPerson() throws Exception {
		final String uuid = "86526ed6-3c11-11de-a0ba-001e378eb67e";
		assertNotNull(service.getPersonByUuid(uuid));
		MockHttpServletRequest req = newDeleteRequest(getURI() + "/" + uuid, new Parameter("purge", "true"));
		handle(req);
		assertNull(service.getPersonByUuid(uuid));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfPersonsMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Horatio");
		SimpleObject result = deserialize(handle(req));
		assertEquals(1, Util.getResultsSize(result));
		assertEquals(getUuid(), PropertyUtils.getProperty(Util.getResultsList(result).get(0), "uuid"));
	}
	
	@Test(expected = ConversionException.class)
	public void shouldFailIfThePreferreNameBeingSetIsNew() throws Exception {
		String json = "{\"preferredName\":{ \"givenName\":\"Joe\", \"familyName\":\"Smith\" }}";
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
	}
	
	@Test(expected = ConversionException.class)
	public void shouldFailIfThePreferreAddressBeingSetIsNew() throws Exception {
		String json = "{\"preferredAddress\":{ \"address1\":\"test address\", \"country\":\"USA\" }}";
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotShowVoidedNamesInFullRepresentation() throws Exception {
		executeDataSet("PersonControllerTest-otherPersonData.xml");
		Person person = service.getPersonByUuid(getUuid());
		assertEquals(4, person.getNames().size());
		PersonName nameToVoid = person.getNames().iterator().next();
		String nameToVoidUuid = nameToVoid.getUuid();
		if (!nameToVoid.isVoided()) {
			//void the Name
			handle(newDeleteRequest("person/" + getUuid() + "/name/" + nameToVoidUuid, new Parameter("!purge", ""),
			    new Parameter("reason", "none")));
		}
		assertTrue(nameToVoid.isVoided());
		
		MockHttpServletRequest req = newGetRequest(getURI() + "/" + getUuid(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL));
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		
		SimpleObject result = deserialize(handle(req));
		
		List<SimpleObject> names = (List<SimpleObject>) PropertyUtils.getProperty(result, "names");
		assertEquals(3, names.size());
		assertFalse(nameToVoidUuid.equals(PropertyUtils.getProperty(names.get(0), "uuid")));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotShowVoidedAddressesInFullRepresentation() throws Exception {
		executeDataSet("PersonControllerTest-otherPersonData.xml");
		Person person = service.getPersonByUuid(getUuid());
		assertEquals(2, person.getAddresses().size());
		PersonAddress voidedAddress = service.getPersonAddressByUuid("8a806d8c-822d-11e0-872f-18a905e044dc");
		String voidedAddressUuid = voidedAddress.getUuid();
		assertTrue(voidedAddress.isVoided());
		
		MockHttpServletRequest req = newGetRequest(getURI() + "/" + getUuid(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL));
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		
		SimpleObject result = deserialize(handle(req));
		
		List<SimpleObject> addresses = (List<SimpleObject>) PropertyUtils.getProperty(result, "addresses");
		assertEquals(1, addresses.size());
		assertFalse(voidedAddressUuid.equals(PropertyUtils.getProperty(addresses.get(0), "uuid")));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotShowVoidedAttributesInFullRepresentation() throws Exception {
		Person person = service.getPersonByUuid(getUuid());
		PersonAttribute attributeToVoid = person.getActiveAttributes().get(0);
		String attributeToVoidUuid = attributeToVoid.getUuid();
		assertEquals(3, person.getActiveAttributes().size());
		if (!attributeToVoid.isVoided()) {
			//void the attribute
			handle(newDeleteRequest("person/" + getUuid() + "/attribute/" + attributeToVoidUuid,
			    new Parameter("!purge", ""), new Parameter("reason", "none")));
		}
		assertTrue(attributeToVoid.isVoided());
		
		MockHttpServletRequest req = newGetRequest(getURI() + "/" + getUuid(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL));
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		
		SimpleObject result = deserialize(handle(req));
		
		List<SimpleObject> attributes = (List<SimpleObject>) PropertyUtils.getProperty(result, "attributes");
		assertEquals(2, attributes.size());
		List<Object> uuids = Arrays.asList(PropertyUtils.getProperty(attributes.get(0), "uuid"),
		    PropertyUtils.getProperty(attributes.get(1), "uuid"));
		assertFalse(uuids.contains(attributeToVoidUuid));
	}
	
	@Test
	public void shouldRespectStartIndexAndLimit() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI());
		req.setParameter("q", "Test");
		SimpleObject results = deserialize(handle(req));
		int fullCount = Util.getResultsSize(results);
		assertTrue("This test assumes > 2 matching persons", fullCount > 2);
		
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_LIMIT, "2");
		results = deserialize(handle(req));
		int firstCount = Util.getResultsSize(results);
		assertEquals(2, firstCount);
		
		req.removeParameter(RestConstants.REQUEST_PROPERTY_FOR_LIMIT);
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_START_INDEX, "2");
		results = deserialize(handle(req));
		int restCount = Util.getResultsSize(results);
		assertEquals(fullCount, firstCount + restCount);
	}
	
	@Test
	public void shouldCreateAPersonWithBooleanAttributeWithoutQuotes() throws Exception {
		executeDataSet("personAttributeTypeWithConcept.xml");
		long originalCount = service.getPeople("", false).size();
		String givenName = "TestName1";
		String attributeUuid = "55e6ce9e-25bf-11e3-a013-3c0754156a5f";
		int attributeId = 10;
		String json = "{\"gender\": \"M\", \"attributes\":[{\"value\":true,\"attributeType\":" + "\"" + attributeUuid
		        + "\"}]," + "\"names\": [{\"givenName\":\"" + givenName + "\", \"familyName\":\"TestFamily\"}]}";
		SimpleObject newPerson = deserialize(handle(newPostRequest(getURI(), json)));
		String uuid = PropertyUtils.getProperty(newPerson, "uuid").toString();
		Person person = Context.getPersonService().getPersonByUuid(uuid);
		assertEquals(++originalCount, service.getPeople("", false).size());
		assertEquals(givenName, person.getGivenName());
		assertEquals("true", person.getAttribute(attributeId).getValue());
	}
	
	@Test
	public void shouldCreateAPersonWithBooleanAttributeWithQuotes() throws Exception {
		executeDataSet("personAttributeTypeWithConcept.xml");
		long originalCount = service.getPeople("", false).size();
		String givenName = "TestName2";
		int attributeId = 10;
		String attributeUuid = "55e6ce9e-25bf-11e3-a013-3c0754156a5f";
		String json = "{\"gender\": \"M\", \"attributes\":[{\"value\":\"true\",\"attributeType\":" + "\"" + attributeUuid
		        + "\"}]," + "\"names\": [{\"givenName\":\"" + givenName + "\", \"familyName\":\"TestFamily\"}]}";
		SimpleObject newPerson = deserialize(handle(newPostRequest(getURI(), json)));
		String uuid = PropertyUtils.getProperty(newPerson, "uuid").toString();
		Person person = Context.getPersonService().getPersonByUuid(uuid);
		assertEquals(++originalCount, service.getPeople("", false).size());
		assertEquals(givenName, person.getGivenName());
		assertEquals("true", person.getAttribute(attributeId).getValue());
	}
}
