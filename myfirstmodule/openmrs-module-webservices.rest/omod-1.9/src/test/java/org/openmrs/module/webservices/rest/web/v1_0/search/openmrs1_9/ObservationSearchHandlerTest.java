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
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class ObservationSearchHandlerTest extends RestControllerTestUtils {
	
	protected String getURI() {
		return "obs";
	}
	
	@Test
	public void shouldReturnObsForPatientAndQuestionConcept() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("concept", "96408258-000b-424e-af1a-403919332938");
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(1, obs.size());
		Assert.assertEquals("e26cea2c-1b9f-4afe-b211-f3ef6c88af6f", PropertyUtils.getProperty(obs.get(0), "uuid"));
	}
	
	@Test
	public void shouldReturnObsForPatientAndQuestionConcepts() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("concepts", "95312123-e0c2-466d-b6b1-cb6e990d0d65,c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(4, obs.size());
		// test should sort by order descending
		Assert.assertEquals("2ed1e57d-9f18-41d3-b067-2eeaf4b30fb0", PropertyUtils.getProperty(obs.get(0), "uuid"));
		Assert.assertEquals("1ce473c8-3fac-440d-9f92-e10facab194f", PropertyUtils.getProperty(obs.get(1), "uuid"));
		Assert.assertEquals("2f616900-5e7c-4667-9a7f-dcb260abf1de", PropertyUtils.getProperty(obs.get(2), "uuid"));
		Assert.assertEquals("39fb7f47-e80a-4056-9285-bd798be13c63", PropertyUtils.getProperty(obs.get(3), "uuid"));
	}
	
	@Test
	public void shouldReturnObsForPatientAndAnswerConcepts() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("answers", "b055abd8-a420-4a11-8b98-02ee170a7b54,c607c80f-1ea9-4da3-bb88-6276ce8868dd"); // only a match for the first concept
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(1, obs.size());
		// test should sort by order descending
		Assert.assertEquals("b6521c32-47b6-47da-9c6f-3673ddfb74f9", PropertyUtils.getProperty(obs.get(0), "uuid"));
	}
	
	@Test
	public void shouldReturnObsForPatientAndQuestionAndAnswerConcepts() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("concepts", "0dde1358-7fcf-4341-a330-f119241a46e8");
		req.addParameter("answers", "b055abd8-a420-4a11-8b98-02ee170a7b54");
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(1, obs.size());
		// test should sort by order descending
		Assert.assertEquals("b6521c32-47b6-47da-9c6f-3673ddfb74f9", PropertyUtils.getProperty(obs.get(0), "uuid"));
	}
	
	@Test
	public void shouldNotReturnObsForPatientIfQuestionAndAnswerConceptsDontMatch() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("concepts", "95312123-e0c2-466d-b6b1-cb6e990d0d65");
		req.addParameter("answers", "b055abd8-a420-4a11-8b98-02ee170a7b54");
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(0, obs.size());
	}
	
	@Test
	public void shouldReturnAllObsInObsgroup() throws Exception {
		executeDataSet("encounterWithObsGroup1_9.xml");
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("groupingConcepts", "0f97e14e-cdc2-49ac-9255-b5126f8a5147");
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(5, obs.size());
		// these two obs have the same obs time, so we really shouldn't be depending on sort order here
		Assert.assertEquals("5117f5d4-96cc-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(0), "uuid"));
		Assert.assertEquals("565f39c6-96cc-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(1), "uuid"));
		Assert.assertEquals("05ba548c-96cd-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(2), "uuid"));
		Assert.assertEquals("0d37552a-96cd-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(3), "uuid"));
		Assert.assertEquals("11de743c-96cd-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(4), "uuid"));
	}
	
	@Test
	public void shouldReturnObsInObsgroupLimitedByQuestion() throws Exception {
		executeDataSet("encounterWithObsGroup1_9.xml");
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		req.addParameter("concepts", "96408258-000b-424e-af1a-403919332938");
		req.addParameter("groupingConcepts", "0f97e14e-cdc2-49ac-9255-b5126f8a5147");
		SimpleObject result = deserialize(handle(req));
		List<Object> obs = result.get("results");
		Assert.assertEquals(2, obs.size());
		// these two obs have the same obs time, so we really shouldn't be depending on sort order here
		Assert.assertEquals("5117f5d4-96cc-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(0), "uuid"));
		Assert.assertEquals("0d37552a-96cd-11e0-8d6b-9b9415a91465", PropertyUtils.getProperty(obs.get(1), "uuid"));
	}
}
