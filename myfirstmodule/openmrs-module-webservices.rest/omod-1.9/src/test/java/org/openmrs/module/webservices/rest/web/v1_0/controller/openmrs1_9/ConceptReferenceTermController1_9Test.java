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

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Tests CRUD operations for {@link ConceptReferenceTerm}s via web service calls
 */
public class ConceptReferenceTermController1_9Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	@Override
	public String getURI() {
		return "conceptreferenceterm";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getConceptReferenceTerms(false).size();
	}
	
	@Before
	public void before() {
		this.service = Context.getConceptService();
	}
	
	@Test
	public void shouldGetAnConceptReferenceTermByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		SimpleObject result = deserialize(handle(req));
		
		ConceptReferenceTerm conceptReferenceTermType = service.getConceptReferenceTermByUuid(getUuid());
		assertEquals(conceptReferenceTermType.getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertEquals(conceptReferenceTermType.getCode(), PropertyUtils.getProperty(result, "code"));
	}
	
	@Test
	public void shouldListAllConceptReferenceTerms() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateAConceptReferenceTerm() throws Exception {
		long originalCount = getAllCount();
		
		SimpleObject conceptReferenceTermType = new SimpleObject();
		conceptReferenceTermType.add("code", "test code");
		conceptReferenceTermType.add("conceptSource", "00001827-639f-4cb4-961f-1e025bf80000");
		
		String json = new ObjectMapper().writeValueAsString(conceptReferenceTermType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newConceptReferenceTerm = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(newConceptReferenceTerm, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
	}
	
	@Test
	public void shouldEditingAConceptReferenceTerm() throws Exception {
		final String newCode = "updated code";
		SimpleObject conceptReferenceTermType = new SimpleObject();
		conceptReferenceTermType.add("code", newCode);
		
		String json = new ObjectMapper().writeValueAsString(conceptReferenceTermType);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
		req.setContent(json.getBytes());
		handle(req);
		assertEquals(newCode, service.getConceptReferenceTermByUuid(getUuid()).getCode());
	}
	
	@Test
	public void shouldRetireAConceptReferenceTerm() throws Exception {
		assertEquals(false, service.getConceptReferenceTermByUuid(getUuid()).isRetired());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		assertEquals(true, service.getConceptReferenceTermByUuid(getUuid()).isRetired());
		assertEquals(reason, service.getConceptReferenceTermByUuid(getUuid()).getRetireReason());
	}
	
	@Test
	public void shouldPurgeAConceptReferenceTerm() throws Exception {
		final String uuid = "SSTRM-retired code";
		assertNotNull(service.getConceptReferenceTermByUuid(uuid));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + uuid);
		req.addParameter("purge", "true");
		handle(req);
		assertNull(service.getConceptReferenceTermByUuid(uuid));
	}
	
	@Test
	public void shouldReturnTheAuditInfoForTheFullRepresentation() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldSearchAndReturnAListOfConceptReferenceTermsMatchingTheQueryString() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "cd4");
		SimpleObject result = deserialize(handle(req));
		assertEquals(3, Util.getResultsSize(result));
	}
	
	@Test
	public void shouldFindBySourceName() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"))));
		Integer resultsSize = Util.getResultsSize(result);
		assertThat(resultsSize, is(8));
	}
	
	@Test
	public void shouldFindBySourceUuid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "00001827-639f-4cb4-961f-1e025bf80000"))));
		Integer resultsSize = Util.getResultsSize(result);
		assertThat(resultsSize, is(8));
	}
	
	@Test
	public void shouldFindBySourceAndCodeOrName() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"), new Parameter("codeOrName", "WGT234"), new Parameter("v", "full"))));
		
		List<Object> results = Util.getResultsList(result);
		assertThat(results, contains((Matcher) hasEntry("code", "WGT234")));
	}
	
	@Test
	public void shouldFindBySourceAndCodeOrNameAlike() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"), new Parameter("codeOrName", "WGT"), new Parameter("searchType", "alike"),
		    new Parameter("v", "full"))));
		
		List<Object> results = Util.getResultsList(result);
		assertThat(results, contains((Matcher) hasEntry("code", "WGT234")));
	}
	
	@Test
	public void shouldFindByCodeOrNameAlikeName() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("codeOrName", "no term name"),
		    new Parameter("searchType", "alike"), new Parameter("v", "full"))));
		
		List<Object> results = Util.getResultsList(result);
		assertThat(
		    results,
		    containsInAnyOrder((Matcher) hasEntry("name", "no term name"), hasEntry("name", "no term name2"),
		        hasEntry("name", "no term name3")));
	}
	
	@Test
	public void shouldFindBySourceAndCodeOrNameAlikeName() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"), new Parameter("codeOrName", "no term name"), new Parameter("searchType",
		        "alike"), new Parameter("v", "full"))));
		assertThat(Util.getResultsSize(result), is(3));
		List<Object> results = Util.getResultsList(result);
		assertThat(
		    results,
		    containsInAnyOrder((Matcher) hasEntry("name", "no term name"), hasEntry("name", "no term name2"),
		        hasEntry("name", "no term name3")));
	}
	
	@Test
	public void shouldFindBySourceAndCodeOrNameAlikeNameWithPaging() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"), new Parameter("codeOrName", "no term name"), new Parameter("searchType",
		        "alike"), new Parameter("v", "full"), new Parameter("limit", "2"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(2));
		
		result = deserialize(handle(newGetRequest(getURI(), new Parameter("source", "Some Standardized Terminology"),
		    new Parameter("codeOrName", "no term name"), new Parameter("searchType", "alike"), new Parameter("v", "full"),
		    new Parameter("limit", "10"), new Parameter("startIndex", "2"))));
		List<Object> resultsSecondPage = Util.getResultsList(result);
		assertThat(resultsSecondPage, hasSize(1));
		
		results.addAll(resultsSecondPage);
		assertThat(
		    results,
		    hasItems((Matcher) hasEntry("name", "no term name"), hasEntry("name", "no term name2"),
		        hasEntry("name", "no term name3")));
	}
	
	@Test
	public void shouldFindBySourceAndCodeOrNameEqualName() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"), new Parameter("codeOrName", "no term name"), new Parameter("searchType",
		        "equal"), new Parameter("v", "full"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, contains((Matcher) hasEntry("name", "no term name")));
	}
	
	@Test
	public void shouldFindByCodeOrNameEqualCode() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("codeOrName", "127689"),
		    new Parameter("searchType", "equal"), new Parameter("v", "full"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, containsInAnyOrder((Matcher) hasEntry("name", "died term"), hasEntry("name", "married term")));
	}
	
	@Test
	public void shouldFindByCodeOrNameEqualCodeWithLimit() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("codeOrName", "127689"),
		    new Parameter("searchType", "equal"), new Parameter("limit", "1"), new Parameter("v", "full"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
		
		result = deserialize(handle(newGetRequest(getURI(), new Parameter("codeOrName", "127689"), new Parameter(
		        "searchType", "equal"), new Parameter("limit", "5"), new Parameter("startIndex", "1"), new Parameter("v",
		        "full"))));
		List<Object> resultsSecondPage = Util.getResultsList(result);
		assertThat(resultsSecondPage, hasSize(1));
		
		results.addAll(resultsSecondPage);
		assertThat(results, containsInAnyOrder((Matcher) hasEntry("name", "died term"), hasEntry("name", "married term")));
	}
	
	@Test(expected = InvalidSearchException.class)
	public void shouldThrowExceptionWhenSearchTypeIsInvalid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("source",
		        "Some Standardized Terminology"), new Parameter("codeOrName", "WGT"), new Parameter("searchType", "invalid"))));
	}
}
