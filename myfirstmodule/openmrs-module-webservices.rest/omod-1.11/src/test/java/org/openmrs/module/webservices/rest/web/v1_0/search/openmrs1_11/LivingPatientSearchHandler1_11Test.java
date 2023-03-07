/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_11;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_11;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class LivingPatientSearchHandler1_11Test extends MainResourceControllerTest {
	
	private PatientService patientService;
	
	@Before
	public void init() throws Exception {
		patientService = Context.getPatientService();
		executeDataSet(RestTestConstants1_11.LIVING_PATIENT_SEARCH_DATASET);
	}
	
	@Override
	public String getURI() {
		return "patient";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_11.PATIENT_UUID;
	}
	
	@Override
	public long getAllCount() {
		return patientService.getAllPatients().size();
	}
	
	@Test
	public void shouldNotReturnPatientsIfQParamValueIsEmpty() throws Exception {
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(0, patients.size());
	}
	
	@Test
	public void shouldNotReturnPatientsIfQParamIsNotSet() throws Exception {
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("includeDead", "True");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(0, patients.size());
	}
	
	@Test
	public void shouldReturnBothDeadAndLivingPatientsIfIncludeDeadIsSetToTrue() throws Exception {
		patientService.getPatientByUuid(getUuid()).setDead(true);
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Moz");
		req.addParameter("includeDead", "True");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(3, patients.size());
	}
	
	@Test
	public void shouldReturnOnlyLivingPatientsBydefaultIfIncludeDeadIsNotSet() throws Exception {
		patientService.getPatientByUuid(getUuid()).setDead(true);
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Moz");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(2, patients.size());
	}
	
	@Test
	public void shouldReturnOnlyLivingPatientsOnWrongIncludeDeadParamValue() throws Exception {
		patientService.getPatientByUuid(getUuid()).setDead(true);
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Moz");
		req.addParameter("includeDead", "wrongx");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(2, patients.size());
	}
	
	@Test
	public void shouldReturnOnlyLivingPatientsIfIncludeDeadParamIsSetToFalse() throws Exception {
		patientService.getPatientByUuid(getUuid()).setDead(true);
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "Moz");
		req.addParameter("includeDead", "False");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(2, patients.size());
	}
	
	@Test
	public void shouldNotReturnPatientsIfNoPatientMatchesQParam() throws Exception {
		patientService.getPatientByUuid(getUuid()).setDead(true);
		Assert.assertEquals(8, getAllCount());
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("q", "www");
		req.addParameter("includeDead", "True");
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		Assert.assertEquals(0, patients.size());
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
}
