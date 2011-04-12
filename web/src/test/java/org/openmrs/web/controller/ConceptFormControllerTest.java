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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.controller.ConceptFormController.ConceptFormBackingObject;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit testing for the ConceptFormController.
 */
public class ConceptFormControllerTest extends BaseWebContextSensitiveTest {
	
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
		
		ConceptFormController controller = (ConceptFormController) applicationContext.getBean("conceptForm");
		
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
		ConceptFormController controller = (ConceptFormController) applicationContext.getBean("conceptForm");
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
	
	/**
	 * This test concept form being submitted with only one name supplied
	 * 
	 * @throws Exception
	 */
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(1, actualNames.size());
		assertNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertNull(actualConcept.getDescription(Locale.ENGLISH));
	}
	
	/**
	 * This tests a concept form being submitted with also a short name supplied
	 * 
	 * @throws Exception
	 */
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		assertNull(actualConcept.getDescription(Locale.ENGLISH));
	}
	
	/**
	 * Tests a concept form being submitted with name/shortname/description all filled in
	 * 
	 * @throws Exception
	 */
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
		assertNotNull(actualConcept.getShortNameInLocale(Locale.ENGLISH));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(Locale.ENGLISH).getName());
		
		assertNotNull(actualConcept.getDescription(Locale.ENGLISH));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(Locale.ENGLISH).getDescription());
	}
	
	/**
	 * Tests a concept form being submitted with a name and description for numeric type of concepts
	 * 
	 * @throws Exception
	 */
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
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
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
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
		conceptToUpdate.addName(new ConceptName("demo name", Context.getLocale()));
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
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should copy numeric values into numeric concepts", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldCopyNumericValuesIntoNumericConcepts() throws Exception {
		final Double EXPECTED_LOW_ABSOLUTE = 100.0;
		final Double EXPECTED_LOW_CRITICAL = 103.0;
		final Double EXPECTED_LOW_NORMAL = 105.0;
		final Double EXPECTED_HI_NORMAL = 110.0;
		final Double EXPECTED_HI_CRITICAL = 117.0;
		final Double EXPECTED_HI_ABSOLUTE = 120.0;
		
		ConceptService cs = Context.getConceptService();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("namesByLocale[en].name", "WEIGHT (KG)");
		mockRequest.setParameter("conceptId", "5089");
		mockRequest.setParameter("concept.datatype", "1");
		mockRequest.setParameter("lowAbsolute", EXPECTED_LOW_ABSOLUTE.toString());
		mockRequest.setParameter("lowCritical", EXPECTED_LOW_CRITICAL.toString());
		mockRequest.setParameter("lowNormal", EXPECTED_LOW_NORMAL.toString());
		mockRequest.setParameter("hiNormal", EXPECTED_HI_NORMAL.toString());
		mockRequest.setParameter("hiCritical", EXPECTED_HI_CRITICAL.toString());
		mockRequest.setParameter("hiAbsolute", EXPECTED_HI_ABSOLUTE.toString());
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		ConceptNumeric concept = (ConceptNumeric) cs.getConcept(5089);
		Assert.assertEquals(EXPECTED_LOW_NORMAL, concept.getLowNormal());
		Assert.assertEquals(EXPECTED_HI_NORMAL, concept.getHiNormal());
		Assert.assertEquals(EXPECTED_LOW_ABSOLUTE, concept.getLowAbsolute());
		Assert.assertEquals(EXPECTED_HI_ABSOLUTE, concept.getHiAbsolute());
		Assert.assertEquals(EXPECTED_LOW_CRITICAL, concept.getLowCritical());
		Assert.assertEquals(EXPECTED_HI_CRITICAL, concept.getHiCritical());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should display numeric values from table", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldDisplayNumericValuesFromTable() throws Exception {
		final Double EXPECTED_LOW_ABSOLUTE = 0.0;
		final Double EXPECTED_LOW_CRITICAL = 99.0;
		final Double EXPECTED_LOW_NORMAL = 445.0;
		final Double EXPECTED_HI_NORMAL = 1497.0;
		final Double EXPECTED_HI_CRITICAL = 1800.0;
		final Double EXPECTED_HI_ABSOLUTE = 2500.0;
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("GET");
		mockRequest.setParameter("conceptId", "5497");
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		
		assertNotNull(mav);
		ConceptFormBackingObject formBackingObject = (ConceptFormBackingObject) mav.getModel().get("command");
		
		Assert.assertEquals(EXPECTED_LOW_NORMAL, formBackingObject.getLowNormal());
		Assert.assertEquals(EXPECTED_HI_NORMAL, formBackingObject.getHiNormal());
		Assert.assertEquals(EXPECTED_LOW_ABSOLUTE, formBackingObject.getLowAbsolute());
		Assert.assertEquals(EXPECTED_HI_ABSOLUTE, formBackingObject.getHiAbsolute());
		Assert.assertEquals(EXPECTED_LOW_CRITICAL, formBackingObject.getLowCritical());
		Assert.assertEquals(EXPECTED_HI_CRITICAL, formBackingObject.getHiCritical());
	}
	
	/**
	 * This tests removing a concept set
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRemoveConceptSet() throws Exception {
		ConceptService cs = Context.getConceptService();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "23");
		mockRequest.setParameter("namesByLocale[en].name", "FOOD CONSTRUCT");
		mockRequest.setParameter("concept.datatype", "4");
		mockRequest.setParameter("concept.class", "10");
		mockRequest.setParameter("concept.conceptSets", "18 19");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept concept = cs.getConcept(23);
		assertNotNull(concept);
		assertEquals(2, concept.getConceptSets().size());
	}
	
	/**
	 * This tests removing an answer
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRemoveConceptAnswer() throws Exception {
		ConceptService cs = Context.getConceptService();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "21");
		mockRequest.setParameter("namesByLocale[en].name", "FOOD ASSISTANCE FOR ENTIRE FAMILY");
		mockRequest.setParameter("concept.datatype", "2");
		mockRequest.setParameter("concept.class", "7");
		mockRequest.setParameter("concept.answers", "7 8");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept concept = cs.getConcept(21);
		assertNotNull(concept);
		assertEquals(2, concept.getAnswers(false).size());
	}
	
	/**
	 * This test makes sure that all answers are deleted if the user changes this concept's datatype
	 * to something other than "Coded"
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRemoveConceptAnswersIfDatatypeChangedFromCoded() throws Exception {
		ConceptService cs = Context.getConceptService();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "4"); // this must be a concept id that is not used in an observation in order to be changed
		mockRequest.setParameter("namesByLocale[en].name", "CIVIL STATUS");
		mockRequest.setParameter("concept.datatype", "1"); // set it to something other than "Coded"
		mockRequest.setParameter("concept.class", "10");
		mockRequest.setParameter("concept.answers", "5 6");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept concept = cs.getConcept(4);
		assertNotNull(concept);
		assertEquals(0, concept.getAnswers(false).size());
	}
	
	/**
	 * This test makes sure that ConceptComplex objects can be edited
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldEditConceptComplex() throws Exception {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-complex.xml");
		
		ConceptService cs = Context.getConceptService();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "8473");
		mockRequest.setParameter("namesByLocale[en].name", "A complex concept");
		mockRequest.setParameter("concept.datatype", "13");
		mockRequest.setParameter("concept.class", "5");
		mockRequest.setParameter("handlerKey", "TextHandler"); // switching it from an ImageHandler to a TextHandler
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept concept = cs.getConcept(8473);
		assertEquals(ConceptComplex.class, concept.getClass());
		ConceptComplex complex = (ConceptComplex) concept;
		assertEquals("TextHandler", complex.getHandler());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should return a concept with a null id if no match is found", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldReturnAConceptWithANullIdIfNoMatchIsFound() throws Exception {
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("GET");
		mockRequest.setParameter("conceptId", "57432223");
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		ConceptFormBackingObject formBackingObject = (ConceptFormBackingObject) mav.getModel().get("command");
		assertNotNull(formBackingObject.getConcept());
		assertNull(formBackingObject.getConcept().getConceptId());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should set the local preferred name", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldSetTheLocalPreferredName() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = cs.getConcept(5497);
		//sanity check, the current preferred Name should be different from what will get set in the form
		Assert.assertNotSame("CD3+CD4+ABS CNT", concept.getPreferredName(Locale.ENGLISH).getName());
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "5497");
		mockRequest.setParameter("preferredNamesByLocale[en]", "CD3+CD4+ABS CNT");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Assert.assertEquals("CD3+CD4+ABS CNT", concept.getPreferredName(Locale.ENGLISH).getName());
		//preferred name should be the new one that has been set from the form
		Assert.assertEquals(true, concept.getPreferredName(Locale.ENGLISH).isLocalePreferred());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should void a synonym marked as preferred when it is removed", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldVoidASynonymMarkedAsPreferredWhenItIsRemoved() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = cs.getConcept(5497);
		//mark one of the synonyms as preferred
		ConceptName preferredName = new ConceptName("pref name", Locale.ENGLISH);
		preferredName.setLocalePreferred(true);
		concept.addName(preferredName);
		cs.saveConcept(concept);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "5497");
		//remove the synonym that is marked as preferred
		mockRequest.setParameter("synonymsByLocale[en][0].voided", "true");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Assert.assertEquals(true, preferredName.isVoided());
	}
	
}
