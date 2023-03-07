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

import static org.junit.Assert.assertEquals;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Contains tests for the {@link VisitTypeController}
 */
public class VisitTypeController1_9Test extends MainResourceControllerTest {
	
	private VisitService service;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "visittype";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.VISIT_TYPE_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		int count = 0;
		for (VisitType type : service.getAllVisitTypes()) {
			if (!type.isRetired()) {
				count++;
			}
		}
		
		return count;
	}
	
	@Before
	public void before() {
		this.service = Context.getVisitService();
	}
	
	@Test
	public void shouldGetAVisitTypeByName() throws Exception {
		Object result = deserialize(handle(newGetRequest(getURI() + "/Return TB Clinic Visit")));
		Assert.assertNotNull(result);
		Assert.assertEquals(RestTestConstants1_9.VISIT_TYPE_UUID, PropertyUtils.getProperty(result, "uuid"));
		Assert.assertEquals("Return TB Clinic Visit", PropertyUtils.getProperty(result, "name"));
	}
	
	@Test
	public void shouldCreateAVisitType() throws Exception {
		int originalCount = service.getAllVisitTypes().size();
		String json = "{ \"name\":\"test visitType\", \"description\":\"description\" }";
		Object newVisitType = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newVisitType, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getAllVisitTypes().size());
	}
	
	@Test
	public void shouldEditAVisitType() throws Exception {
		String json = "{ \"name\":\"new visit type\", \"description\":\"new description\" }";
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		VisitType updated = service.getVisitTypeByUuid(RestTestConstants1_9.VISIT_TYPE_UUID);
		Assert.assertNotNull(updated);
		Assert.assertEquals("new visit type", updated.getName());
		Assert.assertEquals("new description", updated.getDescription());
	}
	
	@Test
	public void shouldRetireAVisitType() throws Exception {
		VisitType visitType = service.getVisitTypeByUuid(RestTestConstants1_9.VISIT_TYPE_UUID);
		Assert.assertFalse(visitType.isRetired());
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "test reason")));
		visitType = service.getVisitTypeByUuid(RestTestConstants1_9.VISIT_TYPE_UUID);
		Assert.assertTrue(visitType.isRetired());
		Assert.assertEquals("test reason", visitType.getRetireReason());
	}
	
	@Test
	public void shouldPurgeAVisitType() throws Exception {
		String uuid = "759799ab-c9a5-435e-b671-77773ada74e6";
		Assert.assertNotNull(service.getVisitTypeByUuid(uuid));
		int originalCount = service.getAllVisitTypes().size();
		handle(newDeleteRequest(getURI() + "/" + uuid, new Parameter("purge", "true")));
		Assert.assertNull(service.getVisitTypeByUuid(uuid));
		Assert.assertEquals(originalCount - 1, service.getAllVisitTypes().size());
	}
	
	@Test
	public void shouldSearchAndReturnAListOfVisitTypesMatchingTheQueryString() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("q", "Ret"))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals(RestTestConstants1_9.VISIT_TYPE_UUID, PropertyUtils.getProperty(hits.get(0), "uuid"));
		
	}
	
	@Test
	public void shouldSearchAndReturnAListOfVisitTypesMatchingTheQueryStringExcludingRetiredOnes() throws Exception {
		final String searchString = "Hos";
		//sanity check
		Assert.assertEquals(1, Context.getVisitService().getVisitTypes(searchString).size());
		
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("q", searchString))));
		List<Object> hits = Util.getResultsList(result);
		Assert.assertEquals(0, hits.size());
		
	}
	
	@Test
	public void shouldGetAVisitTypeByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		VisitType visitType = service.getVisitTypeByUuid(getUuid());
		assertEquals(visitType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(visitType.getName(), PropertyUtils.getProperty(result, "name"));
	}
	
}
