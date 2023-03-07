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

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import org.apache.commons.beanutils.PropertyUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link PersonAddressController}.
 */
public class PersonAddressController1_8Test extends MainResourceControllerTest {
	
	private static final String ADDRESS_TEST_UUID = "address-test-uuid";
	
	private PersonService service;
	
	@Before
	public void before() throws Exception {
		executeDataSet("PersonControllerTest-otherPersonData.xml");
		this.service = Context.getPersonService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "person" + "/" + RestTestConstants1_8.PERSON_UUID + "/address";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		int count = 0;
		
		Person person = service.getPersonByUuid(RestTestConstants1_8.PERSON_UUID);
		
		for (PersonAddress addr : person.getAddresses()) {
			if (!addr.isVoided()) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PERSON_ADDRESS_UUID;
	}
	
	@Test
	public void shouldGetAnAddressOfAPerson() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Assert.assertEquals("3350d0b5-821c-4e5e-ad1d-a9bce331e118", PropertyUtils.getProperty(result, "uuid"));
		Assert.assertEquals("1050 Wishard Blvd.", PropertyUtils.getProperty(result, "address1"));
		Assert.assertNull(PropertyUtils.getProperty(result, "auditInfo"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "latitude"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "longitude"));
	}
	
	@Test
	public void shouldIncludeLatitudeLongituteAndAuditInfoForFullRepresentation() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "latitude"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "longitude"));
	}
	
	@Test
	public void shouldGetAllNonVoidedAddressesOfAPerson() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(1, Util.getResultsSize(result));
	}
	
	@Test
	public void shouldExcludeVoidedAddressesForAPerson() throws Exception {
		// For test purposes, we need to void all addresses
		Person person = service.getPersonByUuid(RestTestConstants1_8.PERSON_UUID);
		for (PersonAddress addr : person.getAddresses()) {
			addr.setVoided(true);
		}
		service.savePerson(person);
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(0, Util.getResultsSize(result));
	}
	
	@Test
	public void shouldAddAnAddressToAPerson() throws Exception {
		int before = service.getPersonByUuid(RestTestConstants1_8.PERSON_UUID).getAddresses().size();
		
		SimpleObject address = new SimpleObject();
		address.add("address1", "test address");
		address.add("country", "USA");
		address.add("preferred", "true");
		
		MockHttpServletRequest req = newPostRequest(getURI(), address);
		handle(req);
		
		int after = service.getPersonByUuid(RestTestConstants1_8.PERSON_UUID).getAddresses().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldAddAnAddressWithSpecificUuidToAPerson() throws Exception {
		SimpleObject address = new SimpleObject();
		address.add("address1", "test address");
		address.add("country", "USA");
		address.add("preferred", "true");
		address.add("uuid", ADDRESS_TEST_UUID);
		
		MockHttpServletRequest req = newPostRequest(getURI(), address);
		handle(req);
		
		PersonAddress personAddress = service.getPersonByUuid(RestTestConstants1_8.PERSON_UUID).getPersonAddress();
		Assert.assertNotNull(personAddress);
		Assert.assertEquals(ADDRESS_TEST_UUID, personAddress.getUuid());
	}
	
	@Test
	public void shouldEditAnAddress() throws Exception {
		
		SimpleObject address = new SimpleObject();
		address.add("address1", "new address1");
		address.add("address2", "new address2");
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), address);
		handle(req);
		
		PersonAddress updated = service.getPersonAddressByUuid(getUuid());
		Assert.assertEquals(address.get("address1"), updated.getAddress1());
		Assert.assertEquals(address.get("address2"), updated.getAddress2());
	}
	
	@Test
	public void shouldVoidADeletedPersonAddress() throws Exception {
		
		PersonAddress address = service.getPersonAddressByUuid(getUuid());
		Assert.assertFalse(address.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("!purge", ""), new Parameter("reason",
		        "random reason")));
		
		address = service.getPersonAddressByUuid(getUuid());
		Assert.assertTrue(address.isVoided());
		Assert.assertEquals("random reason", address.getVoidReason());
	}
	
	@Test
	public void shouldPurgeAPersonAddress() throws Exception {
		
		PersonAddress address = service.getPersonAddressByUuid(getUuid());
		Assert.assertNotNull(address);
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		
		address = service.getPersonAddressByUuid(getUuid());
		Assert.assertNull(address);
	}
	
	@Test
	public void shouldSetPreferred() throws Exception {
		PersonAddress nonPreferred = service.getPersonAddressByUuid(getUuid());
		Person person = nonPreferred.getPerson();
		
		PersonAddress preferred = new PersonAddress();
		preferred.setCityVillage("Seattle");
		preferred.setPreferred(true);
		person.addAddress(preferred);
		
		service.savePerson(person);
		
		// sanity check
		assertThat(nonPreferred.isPreferred(), is(false));
		assertThat(preferred.isPreferred(), is(true));
		
		SimpleObject address = new SimpleObject();
		address.add("preferred", "true");
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), address);
		handle(req);
		
		PersonAddress updated = service.getPersonAddressByUuid(getUuid());
		assertThat(updated.isPreferred(), is(true));
		// the one that was originally preferred should now not be preferred
		assertThat(updated.getPerson().getAddresses(), (Matcher) hasItem(allOf(
		    hasProperty("preferred", is(false)),
		    hasProperty("cityVillage", is("Seattle"))
		    )));
	}
}
