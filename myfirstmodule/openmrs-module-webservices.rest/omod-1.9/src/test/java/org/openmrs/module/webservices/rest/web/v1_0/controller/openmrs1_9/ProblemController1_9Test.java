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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class ProblemController1_9Test extends MainResourceControllerTest {
	
	private static final String ACTIVE_LIST_INITIAL_XML = "customActiveListTest.xml";
	
	private PatientService patientService;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "problem";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PROBLEM_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return patientService.getProblems(patientService.getPatient(2)).size();
	}
	
	private String getPatientUuid() {
		return RestTestConstants1_8.PATIENT_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Before
	public void init() throws Exception {
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		patientService = Context.getPatientService();
	}
	
	@Test
	public void getProblem_shouldGetADefaultRepresentationOfAProblem() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		Assert.assertNotNull(result);
		Util.log("Problem fetched (default)", result);
		Assert.assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "links"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "person"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "problem"));
		Assert.assertNotNull(PropertyUtils.getProperty(result, "sortWeight"));
		Assert.assertNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void getProblem_shouldGetAFullRepresentationOfAProblem() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI() + "/" + getUuid(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL));
		SimpleObject result = deserialize(handle(req));
		Assert.assertNotNull(result);
		Util.log("Problem fetched (default)", result);
		assertEquals(getUuid(), PropertyUtils.getProperty(result, "uuid"));
		assertNotNull(PropertyUtils.getProperty(result, "links"));
		assertNotNull(PropertyUtils.getProperty(result, "person"));
		assertNotNull(PropertyUtils.getProperty(result, "problem"));
		assertNotNull(PropertyUtils.getProperty(result, "sortWeight"));
		assertNotNull(PropertyUtils.getProperty(result, "auditInfo"));
	}
	
	@Test
	public void shouldAddANewProblemToAPatient() throws Exception {
		
		long originalCount = getAllCount();
		SimpleObject problem = new SimpleObject();
		problem.add("problem", RestTestConstants1_8.CONCEPT_UUID);
		problem.add("person", getPatientUuid());
		problem.add("startDate", "2013-01-01");
		String json = new ObjectMapper().writeValueAsString(problem);
		
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		
		SimpleObject newPatientIdentifierType = deserialize(handle(req));
		
		assertNotNull(PropertyUtils.getProperty(newPatientIdentifierType, "uuid"));
		assertEquals(originalCount + 1, getAllCount());
		
	}
	
	@Test
	public void shouldEditAProblem() throws Exception {
		final int id = 2;
		final ProblemModifier newModifier = ProblemModifier.HISTORY_OF;
		Problem problem = patientService.getProblem(id);
		assertEquals(false, newModifier.equals(problem.getModifier()));
		SimpleObject p = new SimpleObject();
		p.add("modifier", newModifier);
		String json = new ObjectMapper().writeValueAsString(p);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + problem.getUuid());
		req.setContent(json.getBytes());
		handle(req);
		
		problem = patientService.getProblem(id);
		assertEquals(newModifier, problem.getModifier());
	}
	
	@Test
	public void voidProblem_shouldVoidAProblem() throws Exception {
		final int id = 2;
		Problem problem = patientService.getProblem(id);
		assertEquals(false, problem.isVoided());
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + problem.getUuid());
		req.addParameter("!purge", "");
		final String reason = "none";
		req.addParameter("reason", reason);
		handle(req);
		
		problem = patientService.getProblem(id);
		assertEquals(true, problem.isVoided());
		assertEquals(reason, problem.getVoidReason());
	}
	
	@Test
	public void searchByPatient_shouldGetProblemsForAPatient() throws Exception {
		MockHttpServletRequest req = newGetRequest(getURI(), new Parameter("patient", getPatientUuid()));
		SimpleObject result = deserialize(handle(req));
		List<Object> results = Util.getResultsList(result);
		assertEquals(1, results.size());
		assertEquals(getUuid(), PropertyUtils.getProperty(results.get(0), "uuid"));
	}
	
	@Test
	public void getProblem_shouldGetProblemByConceptMappings() throws Exception {
		String json = "{\"person\":\"" + RestTestConstants1_8.PERSON_UUID
		        + "\", \"problem\":\"SNOMED CT:2332523\", \"startDate\":\"2013-12-09\"}";
		Object newObs = deserialize(handle(newPostRequest(getURI(), json)));
		Assert.assertNotNull(PropertyUtils.getProperty(newObs, "problem"));
	}
}
