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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.LocationAttribute;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

/**
 * Tests functionality of {@link LocationAttributeController}.
 */
public class LocationAttributeController1_9Test extends MainResourceControllerTest {
	
	private LocationService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "location/" + RestTestConstants1_9.LOCATION_UUID + "/attribute";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.LOCATION_ATTRIBUTE_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getLocationByUuid(RestTestConstants1_9.LOCATION_UUID).getActiveAttributes().size();
	}
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
		this.service = Context.getLocationService();
	}
	
	@Test
	public void shouldAddAttributeToLocation() throws Exception {
		int before = service.getLocationByUuid(RestTestConstants1_9.LOCATION_UUID).getAttributes().size();
		String json = "{\"attributeType\":\"9516cc50-6f9f-132r-5433-001e378eb67f\", \"value\":\"2012-05-05\"}";
		handle(newPostRequest(getURI(), json));
		int after = service.getLocationByUuid(RestTestConstants1_9.LOCATION_UUID).getAttributes().size();
		Assert.assertEquals(before + 1, after);
	}
	
	@Test
	public void shouldEditLocationAttribute() throws Exception {
		String json = "{ \"attributeType\":\"9516cc50-6f9f-132r-5433-001e378eb67f\" }";
		
		LocationAttribute locationAttribute = service.getLocationAttributeByUuid(getUuid());
		Assert.assertEquals("Audit Date", locationAttribute.getAttributeType().getName());
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		locationAttribute = service.getLocationAttributeByUuid(getUuid());
		Assert.assertEquals("Care Date", locationAttribute.getAttributeType().getName());
	}
	
	@Test
	public void shouldVoidAttribute() throws Exception {
		LocationAttribute locationAttribute = service.getLocationAttributeByUuid(getUuid());
		Assert.assertFalse(locationAttribute.isVoided());
		
		MockHttpServletRequest request = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		request.addParameter("reason", "unit test");
		handle(request);
		
		locationAttribute = service.getLocationAttributeByUuid(getUuid());
		Assert.assertTrue(locationAttribute.isVoided());
		Assert.assertEquals("unit test", locationAttribute.getVoidReason());
	}
	
	@Test
	public void shouldReturnOnlyAttributesOfGivenTypeWhenSearch() throws Exception {
		String attributeTypeUuid = "9516cc50-6f9f-11e0-8414-001e378eb67e";
		searchForAttributeOfGivenType_AndCheckIfTypeMatches(attributeTypeUuid);
		
		String anotherAttributeTypUuid = "9516cc50-6f9f-132r-6556-001e378eb67f";
		searchForAttributeOfGivenType_AndCheckIfTypeMatches(anotherAttributeTypUuid);
	}
	
	/**
	 * sends search request for attributes with given type, checks if all result attributes are of
	 * this type
	 * 
	 * @param attributeTypeUuid
	 * @throws Exception
	 */
	private void searchForAttributeOfGivenType_AndCheckIfTypeMatches(String attributeTypeUuid) throws Exception {
		SimpleObject response2 = deserialize(handle(newGetRequest(getURI(),
		    new Parameter("attributeType", attributeTypeUuid))));
		assertThat(Util.getResultsList(response2), is(not(empty())));
		for (Object result : Util.getResultsList(response2)) {
			Object resultAttributeTypeUuid = Util.getByPath(Util.getByPath(result, "attributeType"), "uuid");
			Assert.assertThat((String) resultAttributeTypeUuid, is(attributeTypeUuid));
		}
	}
}
