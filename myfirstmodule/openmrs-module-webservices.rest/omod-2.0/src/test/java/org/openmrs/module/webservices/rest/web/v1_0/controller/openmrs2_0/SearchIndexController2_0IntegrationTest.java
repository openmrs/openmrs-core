/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;

public class SearchIndexController2_0IntegrationTest extends RestControllerTestUtils {
	
	private static final String SEARCH_INDEX_URI = "searchindexupdate";
	
	@Autowired
	private ConceptService conceptService;
	
	@Test
	public void updateSearchIndexForType_shouldUpdateTheSearchIndexForAllInstancesOfTheSpecifiedType() throws Exception {
		executeDataSet("UpdateSearchIndexForType_testData.xml");
		assertEquals(0, conceptService.getConcepts("Measles", Context.getLocale(), false).size());
		assertEquals(0, conceptService.getConcepts("Rubeola", Context.getLocale(), false).size());
		assertEquals(0, conceptService.getDrugs("Panadol").size());
		final String data = "{\"resource\": \"concept\", \"subResource\": \"name\"}";
		
		MockHttpServletResponse response = handle(newPostRequest(SEARCH_INDEX_URI, data));
		
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
		assertEquals(1, conceptService.getConcepts("Measles", Context.getLocale(), false).size());
		assertEquals(1, conceptService.getConcepts("Rubeola", Context.getLocale(), false).size());
		//The indices for other types should not have been updated
		assertEquals(0, conceptService.getDrugs("Panadol").size());
	}
	
	@Test
	public void updateSearchIndexForObject_shouldUpdateTheSearchIndexForTheSpecifiedObjectOnly() throws Exception {
		executeDataSet("UpdateSearchIndexForObject_testData.xml");
		assertEquals(0, conceptService.getConcepts("Headache", Context.getLocale(), false).size());
		assertEquals(0, conceptService.getConcepts("Pain", Context.getLocale(), false).size());
		assertEquals(0, conceptService.getConcepts("Typhoid", Context.getLocale(), false).size());
		
		final String data = "{\"resource\": \"concept\", \"subResource\": \"name\", " +
		        "\"uuid\": \"1bd5693b-f558-30c9-8177-145a4b119ca7\"}";
		MockHttpServletResponse response = handle(newPostRequest(SEARCH_INDEX_URI, data));
		
		assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
		assertEquals(1, conceptService.getConcepts("Headache", Context.getLocale(), false).size());
		//The indices for other concept names should not have been updated
		assertEquals(0, conceptService.getConcepts("Pain", Context.getLocale(), false).size());
		//The indices for other concepts should not have been updated
		assertEquals(0, conceptService.getConcepts("Typhoid", Context.getLocale(), false).size());
	}
	
}
