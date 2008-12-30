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
package org.openmrs.web.concept;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.web.controller.ConceptFormController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/**
 * Test the methods on the {@link ConceptFormController}
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
		Concept commandConcept = (Concept) modelAndView.getModel().get("command");
		Assert.assertNotNull(commandConcept.getConceptId());
		
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
	
}
