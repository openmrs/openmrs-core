/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.controller.ConceptFormController.ConceptFormBackingObject;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.openmrs.web.test.WebTestHelper.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit testing for the ConceptFormController.
 */
public class ConceptFormControllerTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	WebTestHelper webTestHelper;
	
	@Autowired
	ConceptService conceptService;
	
	@Before
	public void updateSearchIndex() {
		super.updateSearchIndex();
	}
	
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
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(1, actualNames.size());
		assertNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertNull(actualConcept.getDescription(new Locale("en", "GB")));
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
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(2, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		assertNotNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(new Locale("en", "GB")).getName());
		assertNull(actualConcept.getDescription(new Locale("en", "GB")));
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
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "4");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(2, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		assertNotNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(new Locale("en", "GB")).getName());
		
		assertNotNull(actualConcept.getDescription(new Locale("en", "GB")));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(new Locale("en", "GB")).getDescription());
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
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(2, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		assertNotNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(new Locale("en", "GB")).getName());
		
		assertNotNull(actualConcept.getDescription(new Locale("en", "GB")));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(new Locale("en", "GB")).getDescription());
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
		mockRequest.setParameter("synonymsByLocale[en_GB][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en_GB][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en_GB][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(5, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		assertNotNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(new Locale("en", "GB")).getName());
		
		assertNotNull(actualConcept.getDescription(new Locale("en", "GB")));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(new Locale("en", "GB")).getDescription());
		
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
		mockRequest.setParameter("synonymsByLocale[en_GB][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en_GB][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en_GB][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(4, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		assertNotNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(new Locale("en", "GB")).getName());
		
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
		mockRequest.setParameter("synonymsByLocale[en_GB][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en_GB][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en_GB][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		Collection<ConceptName> actualNames = actualConcept.getNames();
		assertEquals(4, actualNames.size());
		assertEquals(EXPECTED_PREFERRED_NAME, actualConcept.getFullySpecifiedName(new Locale("en", "GB")).getName());
		assertNotNull(actualConcept.getShortNameInLocale(new Locale("en", "GB")));
		assertEquals(EXPECTED_SHORT_NAME, actualConcept.getShortNameInLocale(new Locale("en", "GB")).getName());
		
	}
	
	/**
	 * Test updating a concept by adding a name
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateConceptByAddingName() throws Exception {
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept already exists
		Concept concept = cs.getConcept(3);
		assertNotNull(concept);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		mockRequest.setParameter("namesByLocale[en_GB].name", "new name");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		updateSearchIndex();
		
		Concept actualConcept = cs.getConceptByName("new name");
		assertNotNull(actualConcept);
		assertEquals(concept.getConceptId(), actualConcept.getConceptId());
	}
	
	/**
	 * Test removing short name by adding a blank short name
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldVoidShortName() throws Exception {
		final String CONCEPT_NAME = "default concept name";
		
		ConceptService cs = Context.getConceptService();
		
		final Concept concept = new Concept();
		concept.addName(new ConceptName(CONCEPT_NAME, new Locale("en", "GB")));
		concept.setShortName(new ConceptName("shortname", new Locale("en", "GB")));
		cs.saveConcept(concept);
		
		Concept actualConcept = cs.getConceptByName(CONCEPT_NAME);
		assertThat(actualConcept.getShortNameInLocale(new Locale("en", "GB")), is(notNullValue()));
		assertThat(actualConcept.getShortNames().size(), greaterThan(0));
		assertThat(actualConcept.getNames().size(), is(2));
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", " ");
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		
		actualConcept = cs.getConceptByName(CONCEPT_NAME);
		assertThat(actualConcept.getShortNameInLocale(new Locale("en", "GB")), is(nullValue()));
		assertThat(actualConcept.getShortNames().size(), is(0));
		assertThat(actualConcept.getNames().size(), is(1));
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
		originalConceptDescription.setLocale(new Locale("en", "GB"));
		originalConceptDescription.setDescription(ORIGINAL_DESCRIPTION);
		conceptToUpdate.addDescription(originalConceptDescription);
		cs.saveConcept(conceptToUpdate);
		
		// then submit changes through the controller
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("synonymsByLocale[en_GB][0].name", EXPECTED_SYNONYM_A);
		mockRequest.setParameter("synonymsByLocale[en_GB][1].name", EXPECTED_SYNONYM_B);
		mockRequest.setParameter("synonymsByLocale[en_GB][2].name", EXPECTED_SYNONYM_C);
		mockRequest.setParameter("shortNamesByLocale[en_GB].name", EXPECTED_SHORT_NAME);
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", EXPECTED_DESCRIPTION);
		mockRequest.setParameter("namesByLocale[en_GB].name", EXPECTED_PREFERRED_NAME);
		mockRequest.setParameter("concept.datatype", "1");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConceptByName(EXPECTED_PREFERRED_NAME);
		assertNotNull(actualConcept);
		
		assertNotNull(actualConcept.getDescription(new Locale("en", "GB")));
		assertEquals(EXPECTED_DESCRIPTION, actualConcept.getDescription(new Locale("en", "GB")).getDescription());
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
		mockRequest.setParameter("namesByLocale[en_GB].name", "WEIGHT (KG)");
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
		mockRequest.setParameter("namesByLocale[en_GB].name", "FOOD CONSTRUCT");
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
		mockRequest.setParameter("namesByLocale[en_GB].name", "FOOD ASSISTANCE FOR ENTIRE FAMILY");
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
		mockRequest.setParameter("namesByLocale[en_GB].name", "CIVIL STATUS");
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
		mockRequest.setParameter("namesByLocale[en_GB].name", "A complex concept");
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
		Assert.assertNotSame("CD3+CD4+ABS CNT", concept.getPreferredName(new Locale("en", "GB")).getName());
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "5497");
		mockRequest.setParameter("preferredNamesByLocale[en_GB]", "CD3+CD4+ABS CNT");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Assert.assertEquals("CD3+CD4+ABS CNT", concept.getPreferredName(new Locale("en", "GB")).getName());
		//preferred name should be the new one that has been set from the form
		Assert.assertEquals(true, concept.getPreferredName(new Locale("en", "GB")).isLocalePreferred());
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
		ConceptName preferredName = new ConceptName("pref name", new Locale("en", "GB"));
		preferredName.setLocalePreferred(true);
		concept.addName(preferredName);
		cs.saveConcept(concept);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", "5497");
		//remove the synonym that is marked as preferred
		mockRequest.setParameter("synonymsByLocale[en_GB][0].voided", "true");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, new MockHttpServletResponse());
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Assert.assertEquals(true, preferredName.isVoided());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should add a new Concept map to an existing concept", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldAddANewConceptMapToAnExistingConcept() throws Exception {
		ConceptService cs = Context.getConceptService();
		int conceptId = 3;
		
		// make sure the concept already exists
		Concept concept = cs.getConcept(conceptId);
		assertNotNull(concept);
		int initialConceptMappingCount = concept.getConceptMappings().size();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		mockRequest.setParameter("conceptMappings[0].conceptReferenceTerm", "1");
		mockRequest.setParameter("conceptMappings[0].conceptMapType", "3");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		assertEquals(initialConceptMappingCount + 1, cs.getConcept(conceptId).getConceptMappings().size());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should add a new Concept map when creating a concept", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldAddANewConceptMapWhenCreatingAConcept() throws Exception {
		ConceptService cs = Context.getConceptService();
		final String conceptName = "new concept";
		// make sure the concept doesn't already exist
		Concept newConcept = cs.getConceptByName(conceptName);
		assertNull(newConcept);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("namesByLocale[en_GB].name", conceptName);
		mockRequest.setParameter("concept.datatype", "1");
		mockRequest.setParameter("conceptMappings[0].conceptReferenceTerm", "1");
		mockRequest.setParameter("conceptMappings[0].conceptMapType", "3");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept createdConcept = cs.getConceptByName(conceptName);
		assertNotNull(createdConcept);
		Assert.assertEquals(1, createdConcept.getConceptMappings().size());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should ignore new concept map row if the user did not select a term", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldIgnoreNewConceptMapRowIfTheUserDidNotSelectATerm() throws Exception {
		ConceptService cs = Context.getConceptService();
		int conceptId = 3;
		
		// make sure the concept already exists
		Concept concept = cs.getConcept(conceptId);
		assertNotNull(concept);
		int initialConceptMappingCount = concept.getConceptMappings().size();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		mockRequest.setParameter("conceptMappings[0].conceptReferenceTerm", "");
		mockRequest.setParameter("conceptMappings[0].conceptMapType", "");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		assertEquals(initialConceptMappingCount, cs.getConcept(conceptId).getConceptMappings().size());
	}
	
	/**
	 * @see {@link ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should remove a concept map from an existing concept", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRemoveAConceptMapFromAnExistingConcept() throws Exception {
		ConceptService cs = Context.getConceptService();
		int conceptId = 5089;
		
		// make sure the concept already exists and has some concept mappings
		Concept concept = cs.getConcept(conceptId);
		assertNotNull(concept);
		Collection<ConceptMap> maps = concept.getConceptMappings();
		int initialConceptMappingCount = maps.size();
		assertTrue(initialConceptMappingCount > 0);
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		//remove the first row
		mockRequest.setParameter("conceptMappings[0].conceptReferenceTerm", "");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		assertEquals(initialConceptMappingCount - 1, cs.getConcept(conceptId).getConceptMappings().size());
	}
	
	/**
	 * @see ConceptFormController#validateConceptUsesPersistedObjects(Concept,BindException)
	 * @verifies add error if map type is not saved
	 */
	@Test
	public void validateConceptReferenceTermUsesPersistedObjects_shouldAddErrorIfMapTypeIsNotSaved() throws Exception {
		Concept concept = new Concept();
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(new ConceptSource(1));
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), new ConceptMapType()));
		concept.addConceptMapping(new ConceptMap(term, new ConceptMapType(1)));
		BindException errors = new BindException(concept, "concept");
		new ConceptFormController().validateConceptUsesPersistedObjects(concept, errors);
		Assert.assertEquals(1, errors.getErrorCount());
		Assert.assertEquals(true, errors
		        .hasFieldErrors("conceptMappings[0].conceptReferenceTerm.conceptReferenceTermMaps[0].conceptMapType"));
	}
	
	/**
	 * @see ConceptFormController#validateConceptUsesPersistedObjects(Concept,BindException)
	 * @verifies add error if source is not saved
	 */
	@Test
	public void validateConceptReferenceTermUsesPersistedObjects_shouldAddErrorIfSourceIsNotSaved() throws Exception {
		Concept concept = new Concept();
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(new ConceptSource());
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), new ConceptMapType(1)));
		concept.addConceptMapping(new ConceptMap(term, new ConceptMapType(1)));
		BindException errors = new BindException(concept, "concept");
		new ConceptFormController().validateConceptUsesPersistedObjects(concept, errors);
		Assert.assertEquals(1, errors.getErrorCount());
		Assert.assertEquals(true, errors.hasFieldErrors("conceptMappings[0].conceptReferenceTerm.conceptSource"));
	}
	
	/**
	 * @see ConceptFormController#validateConceptUsesPersistedObjects(Concept,BindException)
	 * @verifies add error if term b is not saved
	 */
	@Test
	public void validateConceptReferenceTermUsesPersistedObjects_shouldAddErrorIfTermBIsNotSaved() throws Exception {
		Concept concept = new Concept();
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(new ConceptSource(1));
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(), new ConceptMapType(1)));
		concept.addConceptMapping(new ConceptMap(term, new ConceptMapType(1)));
		BindException errors = new BindException(concept, "concept");
		new ConceptFormController().validateConceptUsesPersistedObjects(concept, errors);
		Assert.assertEquals(1, errors.getErrorCount());
		Assert.assertEquals(true, errors
		        .hasFieldErrors("conceptMappings[0].conceptReferenceTerm.conceptReferenceTermMaps[0].termB"));
	}
	
	/**
	 * @see ConceptFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies not save changes if there are validation errors
	 */
	@Test
	public void onSubmit_shouldNotSaveChangesIfThereAreValidationErrors() throws Exception {
		Integer conceptId = 792;
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/dictionary/concept.form");
		request.setParameter("conceptId", conceptId.toString());
		request.setParameter("namesByLocale[en_GB].name", "should not change");
		request.setParameter("preferredNamesByLocale[en_GB]", "should not change");
		request.setParameter("synonymsByLocale[en_GB][1].name", ""); //empty name is invalid
		request.setParameter("synonymsByLocale[en_GB][1].voided", "false");
		
		Response response = webTestHelper.handle(request);
		assertThat(response.getErrors().hasFieldErrors("synonymsByLocale[en_GB][1].name"), is(true));
		
		Context.clearSession();
		
		Concept concept = conceptService.getConcept(conceptId);
		assertThat(concept.getPreferredName(new Locale("en", "GB")).getName(), is("STAVUDINE LAMIVUDINE AND NEVIRAPINE"));
	}
	
	@Test
	public void shouldRemoveConceptDescriptionIfRemovedFromUI() throws Exception {
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept already exists
		Concept concept = cs.getConcept(3);
		assertNotNull(concept);
		assertNotNull(concept.getDescription());
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", "");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConcept(3);
		assertNotNull(actualConcept);
		assertNull(concept.getDescription());
	}
	
	@Test
	public void shouldRemoveConceptDescriptionIfEmptyStringFromUI() throws Exception {
		ConceptService cs = Context.getConceptService();
		
		// make sure the concept already exists
		Concept concept = cs.getConcept(3);
		assertNotNull(concept);
		assertNotNull(concept.getDescription());
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "");
		mockRequest.setParameter("conceptId", concept.getConceptId().toString());
		mockRequest.setParameter("descriptionsByLocale[en_GB].description", "    ");
		
		ModelAndView mav = conceptFormController.handleRequest(mockRequest, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Concept actualConcept = cs.getConcept(3);
		assertNotNull(actualConcept);
		assertNull(concept.getDescription());
	}
	
	/**
	 * @see ConceptFormBackingObject#getConceptFromFormData()
	 */
	@Test
	@Verifies(value = "should set concept on concept answers", method = "getConceptFromFormData()")
	public void getConceptFromFormData_shouldSetConceptOnConceptAnswers() throws Exception {
		ConceptService cs = Context.getConceptService();
		int conceptId = 21;
		
		Concept concept = cs.getConcept(conceptId);
		assertNotNull(concept);
		
		int initialCount = concept.getAnswers().size();
		
		ConceptFormController conceptFormController = (ConceptFormController) applicationContext.getBean("conceptForm");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		mockRequest.setMethod("POST");
		mockRequest.setParameter("action", "Save Concept");
		mockRequest.setParameter("conceptId", "21");
		mockRequest.setParameter("namesByLocale[en].name", concept.getName().getName());
		mockRequest.setParameter("concept.datatype", "2");
		mockRequest.setParameter("concept.answers", "7 8 22 5089");
		
		ConceptFormBackingObject cb = conceptFormController.formBackingObject(mockRequest);
		
		// Bind the request parameters
		ServletRequestDataBinder srdb = new ServletRequestDataBinder(cb);
		conceptFormController.initBinder(mockRequest, srdb);
		srdb.bind(mockRequest);
		
		Concept parsedConcept = cb.getConceptFromFormData();
		
		assertEquals(initialCount + 1, parsedConcept.getAnswers().size());
		for (ConceptAnswer ca : parsedConcept.getAnswers()) {
			assertNotNull(ca.getConcept());
		}
	}
}
