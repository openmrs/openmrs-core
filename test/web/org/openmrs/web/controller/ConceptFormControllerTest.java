/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.web.controller.ConceptFormController.ConceptFormBackingObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit testing for the ConceptFormController.
 */
public class ConceptFormControllerTest extends BaseContextSensitiveTest {
	
	/**
	 * Checks that the conceptId query param gets a concept from the database
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetConcept() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("conceptId", "3");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		ConceptFormController controller = new ConceptFormController();
		
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure there is an "conceptId" filled in on the concept
		ConceptFormBackingObject command = (ConceptFormBackingObject) modelAndView.getModel().get("command");
		Assert.assertNotNull(command.getConcept().getConceptId());
		
	}
	
	/**
	 * Test to make sure a new patient form can save a person relationship
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotDeleteConceptsWhenConceptsAreLocked() throws Exception {
		// this dataset should lock the concepts
		executeDataSet("org/openmrs/web/include/ConceptFormControllerTest.xml");
		
		ConceptService cs = Context.getConceptService();
		
		// set up the controller
		ConceptFormController controller = new ConceptFormController();
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("concept.form");
		
		// set up the request and do an initial "get" as if the user loaded the
		// page for the first time
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dictionary/concept.form?conceptId=3");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		// set this to be a page submission
		request.setMethod("POST");
		
		request.addParameter("action", "Delete Concept"); // so that the form is processed
		
		// send the parameters to the controller
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertNotSame("The purge attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotNull(cs.getConcept(3));
		
	}
	
	@Test
	public void shouldAddConceptWithOnlyNameSpecified() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(1, actualNames.size());
		assertNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertNull(actualConcept.getDescription(Locale.ENGLISH));
	}
	
	@Test
	public void shouldAddConceptWithNameAndShortNameSpecified() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(2, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		assertNull(actualConcept.getDescription(Locale.ENGLISH));
	}
	
	@Test
	public void shouldAddConceptWithNameAndShortNameAndDescriptionSpecifiedToCodeConcepts() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		final String EXPECTED_DESCRIPTION = "this is not really a concept";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "4");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(2, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		
		assertNotNull(actualConcept.getDescription(Locale.ENGLISH));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(Locale.ENGLISH).getDescription());
	}
	
	@Test
	public void shouldAddConceptWithNameAndShortNameAndDescriptionSpecifiedToNumericConcepts() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		final String EXPECTED_DESCRIPTION = "this is not really a concept";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(2, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		
		assertNotNull(actualConcept.getDescription(Locale.ENGLISH));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(Locale.ENGLISH).getDescription());
	}
	
	/**
	 * Test adding a concept with a preferred name, short name, description and synonyms.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAddConceptWithAllNamingSpecified() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		final String EXPECTED_DESCRIPTION = "this is not really a concept";
		final String EXPECTED_SYNONYM_A = "phantom";
		final String EXPECTED_SYNONYM_B = "imaginary";
		final String EXPECTED_SYNONYM_C = "mock";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("synonymsByLocale[en][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(5, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		
		assertNotNull(actualConcept.getDescription(Locale.ENGLISH));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(Locale.ENGLISH).getDescription());
		
	}
	
	/**
	 * Test adding a concept with a preferred name, short name, description and synonyms.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateConceptWithNameAlreadyInSynonymList() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		final String EXPECTED_DESCRIPTION = "this is not really a concept";
		final String EXPECTED_SYNONYM_A = "phantom";
		final String EXPECTED_SYNONYM_B = EXPECTED_PREFERRED_NAME;
		final String EXPECTED_SYNONYM_C = "mock";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("synonymsByLocale[en][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(4, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		
	}
	
	/**
	 * Test adding a concept with a preferred name, short name, description and synonyms.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateConceptWithShortNameAlreadyInSynonymList() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		final String EXPECTED_DESCRIPTION = "this is not really a concept";
		final String EXPECTED_SYNONYM_A = "phantom";
		final String EXPECTED_SYNONYM_B = EXPECTED_SHORT_NAME;
		final String EXPECTED_SYNONYM_C = "mock";
		
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("synonymsByLocale[en][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(4, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getPreferredName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		
	}
	
	/**
	 * Test adding a concept with a preferred name, short name, description and synonyms.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldReplacePreviousDescription() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";
		final String EXPECTED_SHORT_NAME = "nonesuch";
		final String ORIGINAL_DESCRIPTION = "this is indescribable";
		final String EXPECTED_DESCRIPTION = "this is not really a concept";
		final String EXPECTED_SYNONYM_A = "phantom";
		final String EXPECTED_SYNONYM_B = EXPECTED_SHORT_NAME;
		final String EXPECTED_SYNONYM_C = "mock";
		
		ConceptService cs = Context.getConceptService();
		
		// first, add the concept with an original description
		Concept conceptToUpdate = new Concept();
		ConceptDescription originalConceptDescription = new ConceptDescription();
		originalConceptDescription.setLocale(Locale.ENGLISH);
		originalConceptDescription.setDescription(ORIGINAL_DESCRIPTION);
		conceptToUpdate.addDescription(originalConceptDescription);
		cs.saveConcept(conceptToUpdate);
		
		// then submit changes through the controller
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("synonymsByLocale[en][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		
		assertNotNull(actualConcept.getDescription(Locale.ENGLISH));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(Locale.ENGLISH).getDescription());
	}
}
