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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

/**
 * Tests CRUD operations for {@link ConceptReferenceTerm}s via web service calls
 */
public class ConceptReferenceTermMapController1_9Test extends MainResourceControllerTest {
	
	private RestHelperService service;
	
	@Override
	public String getURI() {
		return "conceptreferencetermmap";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CONCEPT_REFERENCE_TERM_MAP_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 0; //not supported
	}
	
	@Before
	public void before() {
		service = Context.getService(RestHelperService.class);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Test
	public void shouldCreateConceptReferenceTermMap() throws Exception {
		String termA = RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID;
		String termB = RestTestConstants1_9.CONCEPT_REFERENCE_TERM2_UUID;
		String json = "{\"termA\": \"" + termA + "\", " + "\"termB\": \"" + termB + "\", " + "\"conceptMapType\": \""
		        + RestTestConstants1_9.CONCEPT_MAP_TYPE_UUID + "\"}";
		
		SimpleObject result = deserialize(handle(newPostRequest(getURI(), json)));
		
		String uuid = (String) result.get("uuid");
		ConceptReferenceTermMap termMap = service.getObjectByUuid(ConceptReferenceTermMap.class, uuid);
		assertThat(termMap.getTermA().getUuid(), is(termA));
		assertThat(termMap.getTermB().getUuid(), is(termB));
		assertThat(termMap.getConceptMapType().getUuid(), is(RestTestConstants1_9.CONCEPT_MAP_TYPE_UUID));
	}
	
	@Test
	public void shouldEditingConceptReferenceTerm() throws Exception {
		String json = "{\"termB\": \"" + RestTestConstants1_9.CONCEPT_REFERENCE_TERM2_UUID + "\"}";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		ConceptReferenceTermMap termMap = service.getObjectByUuid(ConceptReferenceTermMap.class, getUuid());
		assertThat(termMap.getTermB().getUuid(), is(RestTestConstants1_9.CONCEPT_REFERENCE_TERM2_UUID));
	}
	
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldNotDeleteConceptReferenceTermMap() throws Exception {
		handle(newDeleteRequest(getURI() + "/" + getUuid()));
	}
	
	@Test
	public void shouldPurgeConceptReferenceTerm() throws Exception {
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "true")));
		assertNull(service.getObjectByUuid(ConceptReferenceTermMap.class, getUuid()));
	}
	
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldNotSearch() throws Exception {
		handle(newGetRequest(getURI(), new Parameter("q", "search query")));
	}
	
	@Test
	public void shouldFindByTermA() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("termA", "SSTRM-WGT234"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(2));
	}
	
	@Test
	public void shouldFindByTermAndMaptype() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("termA", "SSTRM-WGT234"),
		    new Parameter("maptype", "is-parent-to"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
		assertThat(BeanUtils.getProperty(results.get(0), "uuid"), is("dff198e4-562d-11e0-b169-18a905e044dc"));
	}
	
	@Test
	public void shouldNotFindByTermAndDifferentMaptype() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("termA", "SSTRM-WGT234"),
		    new Parameter("maptype", "is-a"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, empty());
	}
	
	@Test
	public void shouldFindByTermB() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("termB", "SSTRM-CD41003"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
		assertThat(BeanUtils.getProperty(results.get(0), "uuid"), is("dff198e4-562d-11e0-b169-18a905e044dc"));
	}
	
	@Test
	public void shouldFindByTermBAndMaptype() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("termB", "SSTRM-CD41003"),
		    new Parameter("maptype", "is-parent-to"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
		assertThat(BeanUtils.getProperty(results.get(0), "uuid"), is("dff198e4-562d-11e0-b169-18a905e044dc"));
	}
	
	@Test
	public void shouldFindByMapsAndTo() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("maps", "SSTRM-WGT234"),
		    new Parameter("to", "SSTRM-CD41003"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
		assertThat(BeanUtils.getProperty(results.get(0), "uuid"), is("dff198e4-562d-11e0-b169-18a905e044dc"));
	}
	
	@Test
	public void shouldNotFindByMapsAndDifferentTo() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("maps", "SSTRM-WGT234"),
		    new Parameter("to", "SNOMED CT-7345693"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
	}
	
	@Test
	public void shouldFindByMaps() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("maps", "SSTRM-WGT234"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(2));
	}
	
	@Test
	public void shouldFindByMapsAndMaptype() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter("maps", "SSTRM-CD41003"),
		    new Parameter("maptype", "is-parent-to"))));
		List<Object> results = Util.getResultsList(result);
		assertThat(results, hasSize(1));
		assertThat(BeanUtils.getProperty(results.get(0), "uuid"), is("dff198e4-562d-11e0-b169-18a905e044dc"));
	}
}
