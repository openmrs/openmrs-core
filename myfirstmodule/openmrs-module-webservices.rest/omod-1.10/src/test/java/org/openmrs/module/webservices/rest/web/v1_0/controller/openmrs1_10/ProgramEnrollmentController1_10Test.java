/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_10;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ProgramEnrollmentController1_10Test extends MainResourceControllerTest {
	
	private ProgramWorkflowService service;
	
	private PatientService patientService;
	
	@Before
	public void before() {
		this.service = Context.getProgramWorkflowService();
		this.patientService = Context.getPatientService();
	}
	
	/**
	 * @see MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "programenrollment";
	}
	
	/**
	 * @see MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 0;
	}
	
	/**
	 * @see MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PATIENT_PROGRAM_UUID;
	}
	
	@Test
	@Override
	public void shouldGetAll() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.setParameter("patient", RestTestConstants1_8.PATIENT_IN_A_PROGRAM_UUID);
		SimpleObject result = deserialize(handle(req));
		
		Patient patient = patientService.getPatientByUuid(RestTestConstants1_8.PATIENT_IN_A_PROGRAM_UUID);
		List<PatientProgram> patientPrograms = service.getPatientPrograms(patient, null, null, null, null, null, false);
		Assert.assertEquals(patientPrograms.size(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldExcludeVoided() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.setParameter("patient", RestTestConstants1_8.PATIENT_WITH_VOIDED_PROGRAM_UUID);
		SimpleObject result = deserialize(handle(req));
		
		Patient patient = patientService.getPatientByUuid(RestTestConstants1_8.PATIENT_WITH_VOIDED_PROGRAM_UUID);
		List<PatientProgram> patientPrograms = service.getPatientPrograms(patient, null, null, null, null, null, false);
		Assert.assertEquals(patientPrograms.size(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldTransitPatientState() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String stateStartDate = "2015-08-04";
		String json = "{ \"states\": [{ \"state\": {\"uuid\" : \"" + RestTestConstants1_8.STATE_UUID
		        + "\"}, \"startDate\": \"" + stateStartDate + "\"}]}";
		
		PatientProgram patientProgram = service.getPatientProgramByUuid(getUuid());
		Assert.assertEquals(1, patientProgram.getStates().size());
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), SimpleObject.parseJson(json));
		SimpleObject result = deserialize(handle(req));
		
		patientProgram = service.getPatientProgramByUuid(getUuid());
		Assert.assertNotNull(result);
		List<PatientState> states = new ArrayList<PatientState>(patientProgram.getStates());
		Assert.assertEquals(2, states.size());
		sortPatientStatesBasedOnStartDate(states);
		Assert.assertEquals(RestTestConstants1_8.STATE_UUID, states.get(1).getState().getUuid());
		String existingStateEndDate = dateFormat.format(states.get(0).getEndDate());
		Assert.assertEquals(stateStartDate, existingStateEndDate);
	}
	
	@Test
	public void shouldUpdateExistingState() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String existingStartDate = "2008-08-08";
		String stateStartDate = "2015-08-04";
		
		PatientProgram patientProgram = service.getPatientProgramByUuid(getUuid());
		Set<PatientState> existingStates = patientProgram.getStates();
		Assert.assertEquals(1, existingStates.size());
		PatientState existingPatientState = existingStates.iterator().next();
		Assert.assertEquals(existingStartDate, dateFormat.format(existingPatientState.getStartDate()));
		
		String json = "{ \"states\": [{ \"uuid\": \"" + existingPatientState.getUuid() + "\", \"startDate\": \""
		        + stateStartDate + "\"}]}";
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), SimpleObject.parseJson(json));
		SimpleObject result = deserialize(handle(req));
		
		patientProgram = service.getPatientProgramByUuid(getUuid());
		Assert.assertNotNull(result);
		Set<PatientState> actualPatientStates = patientProgram.getStates();
		Assert.assertEquals(1, actualPatientStates.size());
		Assert.assertEquals(stateStartDate, dateFormat.format(actualPatientStates.iterator().next().getStartDate()));
	}
	
	@Test
	public void shouldVoidPatientState() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		PatientProgram patientProgram = service.getPatientProgramByUuid(getUuid());
		Assert.assertEquals(1, patientProgram.getStates().size());
		
		//Transit the existing patient state to new state
		String stateStartDate = "2015-08-04";
		String json = "{ \"states\": [{ \"state\": {\"uuid\" : \"" + RestTestConstants1_8.STATE_UUID
		        + "\"}, \"startDate\": \"" + stateStartDate + "\"}]}";
		
		MockHttpServletRequest req = newPostRequest(getURI() + "/" + getUuid(), SimpleObject.parseJson(json));
		SimpleObject result = deserialize(handle(req));
		
		patientProgram = service.getPatientProgramByUuid(getUuid());
		Assert.assertNotNull(result);
		List<PatientState> states = new ArrayList<PatientState>(patientProgram.getStates());
		sortPatientStatesBasedOnStartDate(states);
		Assert.assertEquals(2, states.size());
		
		PatientState transitedPatientState = states.get(1);
		PatientState existingPatientState = states.get(0);
		String existingStateEndDate = dateFormat.format(existingPatientState.getEndDate());
		Assert.assertEquals(stateStartDate, existingStateEndDate);
		Assert.assertFalse(existingPatientState.getVoided());
		Assert.assertFalse(transitedPatientState.getVoided());
		
		//Delete the last patient state
		req = newDeleteRequest(getURI() + "/" + getUuid() + "/state/" + transitedPatientState.getUuid(), new Parameter(
		        "!purge", ""), new Parameter("reason", "none"));
		handle(req);
		
		patientProgram = service.getPatientProgramByUuid(getUuid());
		
		states = new ArrayList<PatientState>(patientProgram.getStates());
		sortPatientStatesBasedOnStartDate(states);
		PatientState voidedPatientState = states.get(1);
		existingPatientState = states.get(0);
		
		Assert.assertTrue(voidedPatientState.getVoided());
		Assert.assertFalse(existingPatientState.getVoided());
		Assert.assertNull(existingPatientState.getEndDate());
		
	}
	
	private static void sortPatientStatesBasedOnStartDate(List<PatientState> patientStates) {
		Collections.sort(patientStates, new Comparator<PatientState>() {
			
			@Override
			public int compare(PatientState o1, PatientState o2) {
				return OpenmrsUtil.compareWithNullAsLatest(o1.getStartDate(), o2.getStartDate());
			}
		});
	}
	
}
