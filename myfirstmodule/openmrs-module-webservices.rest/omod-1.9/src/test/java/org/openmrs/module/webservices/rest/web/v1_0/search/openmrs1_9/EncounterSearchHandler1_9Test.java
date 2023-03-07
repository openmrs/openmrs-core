/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_9;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class EncounterSearchHandler1_9Test extends RestControllerTestUtils {
	
	private final String CODED_FOOD_ASSISTANCE_QUESTION = "59800cf7-c1a9-11e8-9554-54ee75ef41c2";
	
	private final String ANSWER_YES = "b055abd8-a420-4a11-8b98-02ee170a7b54";
	
	private final String ANSWER_NO = "b98a6ed4-77e7-4cee-aae2-81957fcd7f48";
	
	protected String getURI() {
		return "encounter";
	}
	
	/**
	 * @verifies returns encounters having observations with matching numeric values
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test
	public void shouldReturnEncountersWithNumericObsValues() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// CD4 count concept
		req.addParameter("obsConcept", RestTestConstants1_8.CONCEPT_NUMERIC_UUID);
		req.addParameter("obsValues", "150,175");
		
		SimpleObject result = deserialize(handle(req));
		List<Encounter> encounters = result.get("results");
		Assert.assertEquals(2, encounters.size());
	}
	
	/**
	 * @verifies returns encounters having observations with matching text values
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test
	public void shouldReturnEncountersWithTextObsValues() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// favorite food concept
		req.addParameter("obsConcept", RestTestConstants1_9.CONCEPT_TEXT_UUID);
		req.addParameter("obsValues", "PB and J");
		
		SimpleObject result = deserialize(handle(req));
		List<Encounter> encounters = result.get("results");
		Assert.assertEquals(1, encounters.size());
	}
	
	/**
	 * @verifies returns encounters having observations with matching coded values
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test
	public void shouldReturnEncountersWithCodedObsValues() throws Exception {
		executeDataSet("encounterWithCodedObs1_9.xml");
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// food assistance question
		req.addParameter("obsConcept", CODED_FOOD_ASSISTANCE_QUESTION);
		// "yes" answer
		req.addParameter("obsValues", ANSWER_YES);
		
		SimpleObject result = deserialize(handle(req));
		List<Encounter> encounters = result.get("results");
		Assert.assertEquals(1, encounters.size());
	}
	
	/**
	 * @verifies returns encounters having observations with matching concept
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test
	public void shouldReturnEncountersForConcept() throws Exception {
		executeDataSet("encounterWithCodedObs1_9.xml");
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// food assistance question
		req.addParameter("obsConcept", CODED_FOOD_ASSISTANCE_QUESTION);
		
		SimpleObject result = deserialize(handle(req));
		List<Encounter> encounters = result.get("results");
		Assert.assertEquals(1, encounters.size());
	}
	
	/**
	 * @verifies does not return encounters without matching coded observations
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test
	public void shouldNotReturnEncountersWithCodedObsValue() throws Exception {
		executeDataSet("encounterWithCodedObs1_9.xml");
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// food assistance question
		req.addParameter("obsConcept", CODED_FOOD_ASSISTANCE_QUESTION);
		// "no" answer
		req.addParameter("obsValues", ANSWER_NO);
		
		SimpleObject result = deserialize(handle(req));
		List<Encounter> encounters = result.get("results");
		Assert.assertEquals(0, encounters.size());
	}
	
	/**
	 * @verifies does not return encounters without matching coded observations
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void shouldThrowExceptionForInvalidConcept() throws Exception {
		executeDataSet("encounterWithCodedObs1_9.xml");
		
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// food assistance question
		req.addParameter("obsConcept", "FAKE_CONCEPT_123_UUID");
		// "no" answer
		req.addParameter("obsValues", ANSWER_NO);
		
		handle(req);
	}
	
	/**
	 * @verifies does not return encounters without matching coded observations
	 * @see EncounterSearchHandler1_9#search(RequestContext)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionForInvalidNumericConcept() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("s", "byObs");
		req.addParameter("patient", RestTestConstants1_9.PATIENT_WITH_OBS_UUID);
		// CD4 count concept
		req.addParameter("obsConcept", RestTestConstants1_8.CONCEPT_NUMERIC_UUID);
		req.addParameter("obsValues", "abc,175");
		
		handle(req);
	}
}
