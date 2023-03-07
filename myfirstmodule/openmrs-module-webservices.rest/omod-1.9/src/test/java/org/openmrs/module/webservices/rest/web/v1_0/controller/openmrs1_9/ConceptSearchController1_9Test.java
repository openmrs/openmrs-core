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
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests functionality of searching for concepts and return concept search results
 * 
 * @see org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.ConceptSearchResource1_9
 */
public class ConceptSearchController1_9Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	private boolean isIndexUpToDate = false;
	
	private static final String MARRIED_CONCEPT_UUID = "92afda7c-78c9-47bd-a841-0de0817027d4";
	
	private static final String MALARIA_PROGRAM_CONCEPT_UUID = "f923524a-b90c-4870-a948-4125638606fd";
	
	@Before
	public void before() {
		service = Context.getConceptService();
		if (!isIndexUpToDate) {
			service.updateConceptIndex(service.getConceptByUuid(MARRIED_CONCEPT_UUID));
			service.updateConceptIndex(service.getConceptByUuid(MALARIA_PROGRAM_CONCEPT_UUID));
			isIndexUpToDate = true;
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "conceptsearch";
	}
	
	@Override
	public String getUuid() {
		return "";
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Test
	public void shouldSearchAndReturnAListOfConceptsMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "ma");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		assertThat(
		    hits,
		    containsInAnyOrder(isConceptWithUuid("92afda7c-78c9-47bd-a841-0de0817027d4"),
		        isConceptWithUuid("f923524a-b90c-4870-a948-4125638606fd")));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfConceptsMatchingTheQueryStringAndConceptClass() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "ma");
		req.addParameter("conceptClasses", "2a3738f5-26f0-4f97-ae7a-f99e42fa6d44");
		SimpleObject result = deserialize(handle(req));
		
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size());
		Assert.assertEquals("MALARIA PROGRAM", Util.getByPath(hits.get(0), "display"));
		Assert.assertEquals("f923524a-b90c-4870-a948-4125638606fd", Util.getByPath(hits.get(0), "concept/uuid"));
		
		//Try multiple concept classes
		req.addParameter("conceptClasses", "ecdee8a7-d741-4fe7-8e01-f79cacbe97bc");
		result = deserialize(handle(req));
		
		hits = (List<Object>) result.get("results");
		assertThat(
		    hits,
		    containsInAnyOrder(isConceptWithUuid("92afda7c-78c9-47bd-a841-0de0817027d4"),
		        isConceptWithUuid("f923524a-b90c-4870-a948-4125638606fd")));
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
		//The super test class does some unnecessary crazy stuff not supported by the resource
	}
	
	@Override
	@Ignore
	public void shouldGetFullByUuid() throws Exception {
		//The super test class does some unnecessary crazy stuff not supported by the resource
	}
	
	@Override
	@Ignore
	public void shouldGetRefByUuid() throws Exception {
		//The super test class does some unnecessary crazy stuff not supported by the resource
	}
	
}
