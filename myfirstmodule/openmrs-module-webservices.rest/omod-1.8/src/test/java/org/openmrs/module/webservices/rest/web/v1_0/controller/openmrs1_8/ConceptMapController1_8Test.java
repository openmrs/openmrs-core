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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.OpenmrsProfileRule;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

/**
 * Tests CRUD operations for {@link ConceptMapType}s via web service calls
 */
public class ConceptMapController1_8Test extends MainResourceControllerTest {
	
	private ConceptService service;
	
	private RestHelperService restHelperService;
	
	private String conceptMapUuid;
	
	@Rule
	public OpenmrsProfileRule openmrsProfileRule = new OpenmrsProfileRule("1.8.*");
	
	@Override
	public String getURI() {
		return "concept/" + RestTestConstants1_8.CONCEPT2_UUID + "/mapping";
	}
	
	@Override
	public String getUuid() {
		return conceptMapUuid;
	}
	
	@Override
	public long getAllCount() {
		return service.getConceptByUuid(RestTestConstants1_8.CONCEPT2_UUID).getConceptMappings().size();
	}
	
	@Before
	public void before() {
		service = Context.getConceptService();
		restHelperService = Context.getService(RestHelperService.class);
		
		Concept concept = service.getConceptByUuid(RestTestConstants1_8.CONCEPT2_UUID);
		ConceptMap next = concept.getConceptMappings().iterator().next();
		//The UUID property is not set in standardTestDataset.xml.
		next.setUuid(UUID.randomUUID().toString());
		service.saveConcept(concept);
		conceptMapUuid = next.getUuid();
	}
	
	@Test
	public void shouldCreateConceptMap() throws Exception {
		String json = "{\"source\": \"" + RestTestConstants1_8.CONCEPT_SOURCE_UUID + "\", \"sourceCode\": \"test\"}";
		
		SimpleObject newConceptMap = deserialize(handle(newPostRequest(getURI(), json)));
		
		String uuid = (String) newConceptMap.get("uuid");
		
		ConceptMap conceptMap = restHelperService.getObjectByUuid(ConceptMap.class, uuid);
		assertThat(conceptMap.getConcept().getUuid(), is(RestTestConstants1_8.CONCEPT2_UUID));
		assertThat(conceptMap.getSource().getUuid(), is(RestTestConstants1_8.CONCEPT_SOURCE_UUID));
		assertThat(conceptMap.getSourceCode(), is("test"));
	}
	
	@Test
	public void shouldEditConceptMap() throws Exception {
		String json = "{\"sourceCode\": \"test\"}";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		ConceptMap conceptMap = restHelperService.getObjectByUuid(ConceptMap.class, getUuid());
		assertThat(conceptMap.getSourceCode(), is("test"));
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
