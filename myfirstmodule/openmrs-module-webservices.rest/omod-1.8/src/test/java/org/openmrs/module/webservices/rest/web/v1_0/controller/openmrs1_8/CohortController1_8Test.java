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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of {@link CohortController}.
 */
public class CohortController1_8Test extends MainResourceControllerTest {
	
	private static final String DATASET_FILENAME = "customTestDataset.xml";
	
	private CohortService service;
	
	@Before
	public void before() throws Exception {
		this.service = Context.getCohortService();
		executeDataSet(DATASET_FILENAME);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "cohort";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 2;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.COHORT_UUID;
	}
	
	@Test
	public void createCohort_shouldCreateANewCohort() throws Exception {
		
		SimpleObject cohort = new SimpleObject();
		cohort.add("name", "New cohort");
		cohort.add("description", "New cohort description");
		cohort.add("memberIds", new Integer[] { 2, 6 });
		
		String json = new ObjectMapper().writeValueAsString(cohort);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newCohort = deserialize(handle(req));
		Util.log("Created cohort", newCohort);
		
		// Check existence in database
		String uuid = (String) newCohort.get("uuid");
		Assert.assertNotNull(service.getCohortByUuid(uuid));
	}
	
	@Test
	public void getCohort_shouldGetADefaultRepresentationOfACohort() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Util.log("Cohort fetched (default)", result);
		Assert.assertEquals(getUuid(), result.get("uuid"));
	}
	
	@Test
	public void getCohort_shouldGetADefaultRepresentationInXML() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addHeader("Accept", "application/xml");
		MockHttpServletResponse result = handle(req);
		
		String xml = result.getContentAsString();
		
		Assert.assertEquals(getUuid(), evaluateXPath(xml, "//uuid"));
	}
	
	@Test
	public void getCohortByExactName_shouldGetADefaultRepresentationOfACohort() throws Exception {
		
		String cohortName = "B13 deficit";
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + cohortName);
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertNotNull(result);
		Util.log("Cohort fetched (default)", result);
		Assert.assertEquals(cohortName, result.get("name"));
	}
	
	@Test
	public void getCohorts_shouldSearchForCohortsByName() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "B13");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> results = (List<Object>) result.get("results");
		Assert.assertEquals(2, results.size());
		Util.log("Found " + results.size() + " cohort(s)", results);
	}
	
	@Test
	public void voidCohort_shouldVoidACohort() throws Exception {
		Cohort cohort = service.getCohort(1);
		Assert.assertFalse(cohort.isVoided());
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("reason", "unit test");
		handle(req);
		
		cohort = service.getCohort(1);
		Assert.assertTrue(cohort.isVoided());
		Assert.assertEquals("unit test", cohort.getVoidReason());
	}
	
	@Test
	public void updateCohort_shouldChangeAPropertyOnACohort() throws Exception {
		
		SimpleObject attributes = new SimpleObject();
		attributes.add("name", "Updated cohort name");
		
		String json = new ObjectMapper().writeValueAsString(attributes);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		Cohort editedCohort = service.getCohortByUuid(getUuid());
		Assert.assertEquals("Updated cohort name", editedCohort.getName());
	}
	
	@Test(expected = ConversionException.class)
	public void updateCohort_shouldFailToOverwriteMemberIdsOnACohort() throws Exception {
		Assert.assertEquals(3, service.getCohortByUuid(getUuid()).getMemberIds().size());
		
		SimpleObject attributes = new SimpleObject();
		attributes.add("memberIds", new Integer[] { 2, 6 });
		String json = new ObjectMapper().writeValueAsString(attributes);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
	}
	
	@Test
	public void purgeCohort_shouldPurgeCohort() throws Exception {
		
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("purge", "true");
		handle(req);
		
		Assert.assertNull(service.getCohortByUuid(getUuid()));
	}
	
}
