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

import java.util.Map;

import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ConceptReferenceController1_9Test extends RestControllerTestUtils {
	
	private static final String CONCEPT_UUID = "c607c80f-1ea9-4da3-bb88-6276ce8868dd";
	
	private static final String CONCEPT_MAPPING = "Some Standardized Terminology:WGT234";
	
	public String getURI() {
		return "conceptreferences";
	}
	
	@Test
	public void shouldGetConceptByUuid() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(), new MainResourceControllerTest.Parameter("references", CONCEPT_UUID))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(CONCEPT_UUID), notNullValue());
		assertThat(((Map<String, String>) result.get(CONCEPT_UUID)).get("uuid"), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldGetConceptByMapping() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(),
						new MainResourceControllerTest.Parameter("references", CONCEPT_MAPPING))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(CONCEPT_MAPPING), notNullValue());
		assertThat(((Map<String, String>) result.get(CONCEPT_MAPPING)).get("uuid"), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldSupportMultipleConcepts() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(),
						new MainResourceControllerTest.Parameter("references", CONCEPT_UUID + "," + CONCEPT_MAPPING))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(2));
		assertThat(result.get(CONCEPT_UUID), notNullValue());
		assertThat(result.get(CONCEPT_MAPPING), notNullValue());
	}
	
	@Test
	public void shouldReturnEmptyResultWithInvalidConcept() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(),
						new MainResourceControllerTest.Parameter("references", "aef986bd-e993-4634-ac51-8efcc407e237"))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(0));
	}
	
	@Test
	public void shouldSupportRefRepresentation() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(),
						new MainResourceControllerTest.Parameter("references", CONCEPT_UUID),
						new MainResourceControllerTest.Parameter("v", "ref"))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(CONCEPT_UUID), notNullValue());
		assertThat(((Map<String, String>) result.get(CONCEPT_UUID)).get("uuid"), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldSupportFullRepresentation() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(),
						new MainResourceControllerTest.Parameter("references", CONCEPT_UUID),
						new MainResourceControllerTest.Parameter("v", "full"))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(CONCEPT_UUID), notNullValue());
		assertThat(((Map<String, String>) result.get(CONCEPT_UUID)).get("uuid"), equalTo(CONCEPT_UUID));
	}
	
	@Test
	public void shouldSupportCustomRepresentation() throws Exception {
		// Arrange
		
		// Act
		SimpleObject result = deserialize(
				handle(newGetRequest(getURI(),
						new MainResourceControllerTest.Parameter("references", CONCEPT_UUID),
						new MainResourceControllerTest.Parameter("v", "custom:(uuid)"))));
		
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(CONCEPT_UUID), notNullValue());
		assertThat(((Map<String, String>) result.get(CONCEPT_UUID)).get("uuid"), equalTo(CONCEPT_UUID));
	}
}
