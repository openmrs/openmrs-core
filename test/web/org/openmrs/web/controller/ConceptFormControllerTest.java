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

import java.util.Collection;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit testing for the ConceptFormController.
 * 
 */
public class ConceptFormControllerTest extends BaseContextSensitiveTest {
	
	@Test
	public void shouldAddConceptWithOnlyNameSpecified() throws Exception {
		final String EXPECTED_PREFERRED_NAME = "no such concept";

		ConceptService cs = Context.getConceptService();
		
		// make sure the concept doesn't already exist
		Concept conceptToAdd = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNull(conceptToAdd);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("newSynonyms_en", ""); 
		mockRequest.setParameter("shortName_en", ""); 
		mockRequest.setParameter("description_en", ""); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
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
		MockHttpServletResponse response = new MockHttpServletResponse();

		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("newSynonyms_en", ""); 
		mockRequest.setParameter("shortName_en", EXPECTED_SHORT_NAME); 
		mockRequest.setParameter("description_en", ""); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
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
	public void shouldAddConceptWithNameAndShortNameAndDescriptionSpecified() throws Exception {
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
		mockRequest.setParameter("newSynonyms_en", ""); 
		mockRequest.setParameter("shortName_en", EXPECTED_SHORT_NAME); 
		mockRequest.setParameter("description_en", EXPECTED_DESCRIPTION); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
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
		mockRequest.setParameter("newSynonyms_en", 
		                         EXPECTED_SYNONYM_A + "," + 
		                         EXPECTED_SYNONYM_B + "," + 
		                         EXPECTED_SYNONYM_C 
		                         ); 
		mockRequest.setParameter("shortName_en", EXPECTED_SHORT_NAME); 
		mockRequest.setParameter("description_en", EXPECTED_DESCRIPTION); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
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
		mockRequest.setParameter("newSynonyms_en", 
		                         EXPECTED_SYNONYM_A + "," + 
		                         EXPECTED_SYNONYM_B + "," + 
		                         EXPECTED_SYNONYM_C 
		                         ); 
		mockRequest.setParameter("shortName_en", EXPECTED_SHORT_NAME); 
		mockRequest.setParameter("description_en", EXPECTED_DESCRIPTION); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
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
		mockRequest.setParameter("newSynonyms_en", 
		                         EXPECTED_SYNONYM_A + "," + 
		                         EXPECTED_SYNONYM_B + "," + 
		                         EXPECTED_SYNONYM_C 
		                         ); 
		mockRequest.setParameter("shortName_en", EXPECTED_SHORT_NAME); 
		mockRequest.setParameter("description_en", EXPECTED_DESCRIPTION); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
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
		mockRequest.setParameter("newSynonyms_en", 
		                         EXPECTED_SYNONYM_A + "," + 
		                         EXPECTED_SYNONYM_B + "," + 
		                         EXPECTED_SYNONYM_C 
		                         ); 
		mockRequest.setParameter("shortName_en", EXPECTED_SHORT_NAME); 
		mockRequest.setParameter("description_en", EXPECTED_DESCRIPTION); 
		mockRequest.setParameter("name_en", EXPECTED_PREFERRED_NAME ); 
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);

		assertNotNull(actualConcept.getDescription(Locale.ENGLISH));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(Locale.ENGLISH).getDescription());
	}
}
