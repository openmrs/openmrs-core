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

import java.util.Collection;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests functionality of {@link LocationController}.
 */
public class LocationController1_9Test extends MainResourceControllerTest {
	
	private static final String LOCATION_TAG_INITIAL_XML = "customLocationTagDataset.xml";
	
	private LocationService service;
	
	@Before
	public void init() {
		service = Context.getLocationService();
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
	
	@Test
	public void shouldGetALocationByUuid() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Location location = service.getLocationByUuid(getUuid());
		Assert.assertNotNull(result);
		Assert.assertEquals(location.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertEquals(location.getName(), PropertyUtils.getProperty(result, "name"));
		
	}
	
	@Test
	public void shouldGetALocationByName() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/Xanadu");
		SimpleObject result = deserialize(handle(req));
		
		Location location = service.getLocation("Xanadu");
		Assert.assertNotNull(result);
		Assert.assertEquals(location.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertEquals(location.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldListAllUnRetiredLocations() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Assert.assertEquals(getAllCount(), Util.getResultsSize(result));
		
	}
	
	@Test
	public void shouldCreateALocation() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject location = new SimpleObject();
		location.add("name", "Location name");
		location.add("description", "Location description");
		
		String json = new ObjectMapper().writeValueAsString(location);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newLocation = deserialize(handle(req));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newLocation, "uuid"));
		Assert.assertEquals(originalCount + 1, getAllCount());
		
	}
	
	@Test
	public void shouldEditALocation() throws Exception {
		
		final String editedName = "Xanadu edited";
		String json = "{ \"name\":\"" + editedName + "\" }";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		Location editedLocation = service.getLocationByUuid(getUuid());
		Assert.assertNotNull(editedLocation);
		Assert.assertEquals(editedName, editedLocation.getName());
		
	}
	
	/**
	 * See RESTWS-418 - Allow REST POST requests to accept un-updatable properties if they haven't
	 * been updated
	 */
	@Test
	public void shouldAllowYouToPostANonUpdatablePropertyWithAnUnchangedValue() throws Exception {
		MockHttpServletRequest get = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject location = deserialize(handle(get));
		location.put("name", "New York");
		
		MockHttpServletRequest post = newPostRequest(getURI() + "/" + getUuid(), location);
		handle(post);
		
		Location updatedLocation = service.getLocationByUuid(getUuid());
		assertThat(updatedLocation.getName(), is("New York"));
	}
	
	@Test
	public void shouldOverwriteAListOfChildLocations() throws Exception {
		
		Location location = service.getLocationByUuid(getUuid());
		location.addChildLocation(service.getLocation(2));
		service.saveLocation(location);
		
		String json = "{ \"childLocations\": [] }";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		Location updatedLocation = service.getLocationByUuid(getUuid());
		Assert.assertNotNull(updatedLocation);
		Assert.assertTrue(updatedLocation.getChildLocations().isEmpty());
		
	}
	
	@Test
	public void shouldRetireALocation() throws Exception {
		
		Location location = service.getLocation(2);
		Assert.assertFalse(location.isRetired());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + location.getUuid());
		req.addParameter("!purge", "");
		req.addParameter("reason", "random reason");
		handle(req);
		
		Location retiredLocation = service.getLocation(2);
		Assert.assertTrue(retiredLocation.isRetired());
		Assert.assertEquals("random reason", retiredLocation.getRetireReason());
		
	}
	
	@Test
	public void shouldUnretireALocation() throws Exception {
		
		Location location = service.getLocation(3);
		Assert.assertTrue(location.isRetired());
		
		String json = "{ \"retired\": false }";
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		Location updatedLocation = service.getLocationByUuid(getUuid());
		Assert.assertTrue(!updatedLocation.isRetired());
		
	}
	
	@Test
	public void shouldPurgeARetiredLocation() throws Exception {
		
		Location location = service.getLocation(3);
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + location.getUuid());
		req.addParameter("purge", "true");
		handle(req);
		
		Assert.assertNull(service.getLocation(3));
		
	}
	
	@Test
	public void shouldIncludeTheParentLocation() throws Exception {
		
		Location location = service.getLocationByUuid(getUuid());
		location.setParentLocation(service.getLocation(2));
		service.saveLocation(location);
		
		MockHttpServletRequest httpReq = request(RequestMethod.GET, getURI() + "/" + getUuid());
		httpReq.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(httpReq));
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(PropertyUtils.getProperty(result, "parentLocation"));
		
	}
	
	@Test
	public void shouldIncludeTheListOfChildLocations() throws Exception {
		
		Location location = service.getLocationByUuid(getUuid());
		Assert.assertEquals(0, location.getChildLocations().size());
		location.addChildLocation(service.getLocation(2));
		service.saveLocation(location);
		
		MockHttpServletRequest httpReq = request(RequestMethod.GET, getURI() + "/" + getUuid());
		httpReq.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(httpReq));
		
		Assert.assertEquals(1, ((Collection) PropertyUtils.getProperty(result, "childLocations")).size());
		
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		
		MockHttpServletRequest httpReq = request(RequestMethod.GET, getURI() + "/" + getUuid());
		httpReq.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(httpReq));
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
		
	}
	
	@Test
	public void shouldSearchAndReturnAListOfLocationsMatchingTheQueryString() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "xan");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals(service.getLocation(2).getUuid(), PropertyUtils.getProperty(hits.get(0), "uuid"));
		
	}
	
	@Test
	public void shouldSearchAndReturnListOfLocationsWithSpecifiedTag() throws Exception {
		
		executeDataSet(LOCATION_TAG_INITIAL_XML);
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("tag", "001e503a-47ed-11df-bc8b-001e378eb67e");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size()); // should ignore retired location?
		Assert.assertEquals(service.getLocation(1).getUuid(), PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchAndReturnListOfLocationsWithSpecifiedTagAndQueryString() throws Exception {
		
		executeDataSet(LOCATION_TAG_INITIAL_XML);
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("tag", "0940c6d4-47ed-11df-bc8b-001e378eb67e");
		req.addParameter("q", "Xan");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size()); // should ignore retired location?
		Assert.assertEquals(service.getLocation(2).getUuid(), PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void shouldSearchAndReturnNothingIfTagDoesNotMatchEvenIfQueryDoes() throws Exception {
		
		executeDataSet(LOCATION_TAG_INITIAL_XML);
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("tag", "invalid-uuid");
		req.addParameter("q", "Xan");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(0, hits.size()); // should ignore retired location?
	}
	
}
