/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_10;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class OrderableController1_10Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	private boolean isIndexUpToDate = false;
	
	@Before
	public void before() throws Exception {
		service = Context.getConceptService();
		if (!isIndexUpToDate) {
			service.updateConceptIndex(service.getConceptByUuid(RestTestConstants1_10.COUGH_SYRUP_UUID));
			isIndexUpToDate = true;
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "orderable";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return "";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Test
	public void shouldReturnOrderableConceptClasses() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "cough");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> results = (List<Object>) result.get("results");
		
		Assert.assertTrue(results instanceof List);
		assertThat(results, containsInAnyOrder(isConceptWithUuid(RestTestConstants1_10.COUGH_SYRUP_UUID)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnOrdersWithDrugConceptOnly() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "");
		req.addParameter("conceptClasses", "3d065ed4-b0b9-4710-9a17-6d8c4fd259b7");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertTrue(hits.size() > 0);
		
		// test with test concept uuid
		req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "");
		req.addParameter("conceptClasses", "97097dd9-b092-4b68-a2dc-e5e5be961d42");
		result = deserialize(handle(req));
		hits = (List<Object>) result.get("results");
		Assert.assertTrue(hits.size() == 0);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnDrugOrderTypeOnly() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "");
		req.addParameter("orderTypes", "131168f4-15f5-102d-96e4-000c29c2a5d7");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertTrue(hits.size() > 0);
		
		// test with lab test order type uuid
		req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "");
		req.addParameter("orderTypes", "52a447d3-a64a-11e3-9aeb-50e549534c5e");
		result = deserialize(handle(req));
		hits = (List<Object>) result.get("results");
		Assert.assertTrue(hits.size() == 0);
	}
	
	private Matcher<? super Object> isConceptWithUuid(final String uuid) {
		return new TypeSafeMatcher<Object>(
		                                   Object.class) {
			
			@Override
			public void describeTo(Description description) {
			}
			
			@Override
			protected boolean matchesSafely(Object item) {
				@SuppressWarnings("unchecked")
				Map<String, Object> safeItem = (Map<String, Object>) item;
				@SuppressWarnings("unchecked")
				Map<String, Object> concept = (Map<String, Object>) safeItem.get("concept");
				
				return uuid.equals(concept.get("uuid"));
			}
		};
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Override
	@Ignore
	public void shouldGetDefaultByUuid() throws Exception {
	}
	
	@Override
	@Ignore
	public void shouldGetFullByUuid() throws Exception {
	}
	
	@Override
	@Ignore
	public void shouldGetRefByUuid() throws Exception {
	}
	
}
