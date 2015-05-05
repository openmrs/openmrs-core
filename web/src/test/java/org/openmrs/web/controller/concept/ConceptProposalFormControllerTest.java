/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class ConceptProposalFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ConceptProposalFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should create a single unique synonym and obs for all similar proposals", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldCreateASingleUniqueSynonymAndObsForAllSimilarProposals() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-proposals.xml");
		
		ConceptService cs = Context.getConceptService();
		ObsService os = Context.getObsService();
		final Integer conceptproposalId = 5;
		ConceptProposal cp = cs.getConceptProposal(conceptproposalId);
		Concept obsConcept = cp.getObsConcept();
		Concept conceptToMap = cs.getConcept(5);
		Locale locale = Locale.ENGLISH;
		//sanity checks
		Assert.assertFalse(conceptToMap.hasName(cp.getOriginalText(), locale));
		Assert.assertEquals(0, os.getObservationsByPersonAndConcept(cp.getEncounter().getPatient(), obsConcept).size());
		List<ConceptProposal> proposals = cs.getConceptProposals(cp.getOriginalText());
		Assert.assertEquals(5, proposals.size());
		for (ConceptProposal conceptProposal : proposals) {
			Assert.assertNull(conceptProposal.getObs());
		}
		
		// set up the controller
		ConceptProposalFormController controller = (ConceptProposalFormController) applicationContext
		        .getBean("conceptProposalForm");
		controller.setApplicationContext(applicationContext);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession(null));
		request.setMethod("POST");
		request.addParameter("conceptProposalId", conceptproposalId.toString());
		request.addParameter("finalText", cp.getOriginalText());
		request.addParameter("conceptId", conceptToMap.getConceptId().toString());
		request.addParameter("conceptNamelocale", locale.toString());
		request.addParameter("action", "");
		request.addParameter("actionToTake", "saveAsSynonym");
		
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mav = controller.handleRequest(request, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Assert.assertEquals(cp.getOriginalText(), cp.getFinalText());
		Assert.assertTrue(conceptToMap.hasName(cp.getOriginalText(), locale));
		Assert.assertNotNull(cp.getObs());
		//Obs should have been created for the 2 proposals with same text, obsConcept but different encounters
		Assert.assertEquals(2, os.getObservationsByPersonAndConcept(cp.getEncounter().getPatient(), obsConcept).size());
		
		//The proposal with a different obs concept should have been skipped
		proposals = cs.getConceptProposals(cp.getFinalText());
		Assert.assertEquals(1, proposals.size());
		Assert.assertEquals(21, proposals.get(0).getObsConcept().getConceptId().intValue());
	}
	
	/**
	 * @see {@link ConceptProposalFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should work properly for country locales", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldWorkProperlyForCountryLocales() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-proposals.xml");
		
		ConceptService cs = Context.getConceptService();
		
		final Integer conceptproposalId = 5;
		ConceptProposal cp = cs.getConceptProposal(conceptproposalId);
		Concept conceptToMap = cs.getConcept(4);
		Locale locale = new Locale("en", "GB");
		
		Assert.assertFalse(conceptToMap.hasName(cp.getOriginalText(), locale));
		
		ConceptProposalFormController controller = (ConceptProposalFormController) applicationContext
		        .getBean("conceptProposalForm");
		controller.setApplicationContext(applicationContext);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new MockHttpSession(null));
		request.setMethod("POST");
		request.addParameter("conceptProposalId", conceptproposalId.toString());
		request.addParameter("finalText", cp.getOriginalText());
		request.addParameter("conceptId", conceptToMap.getConceptId().toString());
		request.addParameter("conceptNamelocale", locale.toString());
		request.addParameter("action", "");
		request.addParameter("actionToTake", "saveAsSynonym");
		
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mav = controller.handleRequest(request, response);
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
		
		Assert.assertEquals(cp.getOriginalText(), cp.getFinalText());
		Assert.assertTrue(conceptToMap.hasName(cp.getOriginalText(), locale));
	}
}
