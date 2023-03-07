/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_9;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_8.LocationSearchHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class LocationSearchHandlerTest extends MainResourceControllerTest {
	
	private LocationService service;
	
	private static final String LOCATION_TAG_INITIAL_XML = "customLocationTagDataset.xml";
	
	@Before
	public void init() throws Exception {
		service = Context.getLocationService();
		executeDataSet(LOCATION_TAG_INITIAL_XML);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "location";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return service.getAllLocations(false).size();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.LOCATION_UUID;
	}
	
	/**
	 * @verifies return location by tag uuid
	 * @see LocationSearchHandler#getSearchConfig()
	 */
	@Test
	public void getSearchConfig_shouldReturnLocationByTagUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("tag", "0940c6d4-47ed-11df-bc8b-001e378eb67e");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(2, hits.size());
		List<Location> locations = service.getAllLocations();
		Assert.assertEquals(service.getLocation(1).getUuid(), PropertyUtils.getProperty(hits.get(0), "uuid"));
		Assert.assertEquals(service.getLocation(2).getUuid(), PropertyUtils.getProperty(hits.get(1), "uuid"));
	}
	
	/**
	 * @verifies return location by tag name
	 * @see LocationSearchHandler#getSearchConfig()
	 */
	@Test
	public void getSearchConfig_shouldReturnLocationByTagName() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("tag", "General Hospital");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals(service.getLocation(1).getUuid(), PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
}
