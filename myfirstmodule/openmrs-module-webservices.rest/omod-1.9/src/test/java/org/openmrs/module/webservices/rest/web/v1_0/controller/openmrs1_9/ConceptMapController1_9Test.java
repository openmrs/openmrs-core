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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

/**
 * Tests CRUD operations for {@link ConceptMapType}s via web service calls
 */
public class ConceptMapController1_9Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	private RestHelperService restHelperService;
	
	@Override
	public String getURI() {
		return "concept/" + RestTestConstants1_9.CONCEPT_UUID + "/mapping";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CONCEPT_MAP_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getConceptByUuid(RestTestConstants1_9.CONCEPT_UUID).getConceptMappings().size();
	}
	
	@Before
	public void before() {
		service = Context.getConceptService();
		restHelperService = Context.getService(RestHelperService.class);
	}
	
	@Test
	public void shouldCreateConceptMap() throws Exception {
		String json = "{\"conceptReferenceTerm\": \"" + RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID
		        + "\", \"conceptMapType\": \"" + RestTestConstants1_9.CONCEPT_MAP_TYPE_UUID + "\"}";
		
		SimpleObject newConceptMap = deserialize(handle(newPostRequest(getURI(), json)));
		
		String uuid = (String) newConceptMap.get("uuid");
		
		ConceptMap conceptMap = restHelperService.getObjectByUuid(ConceptMap.class, uuid);
		assertThat(conceptMap.getConcept().getUuid(), is(RestTestConstants1_9.CONCEPT_UUID));
		assertThat(conceptMap.getConceptMapType().getUuid(), is(RestTestConstants1_9.CONCEPT_MAP_TYPE_UUID));
		assertThat(conceptMap.getConceptReferenceTerm().getUuid(), is(RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID));
	}
	
	@Test
	public void shouldEditConceptMap() throws Exception {
		String json = "{\"conceptReferenceTerm\": \"" + RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID + "\"}";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		ConceptMap conceptMap = restHelperService.getObjectByUuid(ConceptMap.class, getUuid());
		assertThat(conceptMap.getConceptReferenceTerm().getUuid(), is(RestTestConstants1_9.CONCEPT_REFERENCE_TERM_UUID));
	}
	
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldNotDeleteConceptMap() throws Exception {
		handle(newDeleteRequest(getURI() + "/" + getUuid()));
	}
	
	@Test
	public void shouldPurgeConceptMap() throws Exception {
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("purge", "")));
		assertNull(restHelperService.getObjectByUuid(ConceptMap.class, getUuid()));
	}
}
