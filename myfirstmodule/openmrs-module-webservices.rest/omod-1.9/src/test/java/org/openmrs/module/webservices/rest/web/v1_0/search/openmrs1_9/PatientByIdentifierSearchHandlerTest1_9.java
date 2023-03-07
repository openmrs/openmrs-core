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

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_8.LocationSearchHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class PatientByIdentifierSearchHandlerTest1_9 extends MainResourceControllerTest {
	
	/**
	 * @see MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "patient";
	}
	
	/**
	 * @see MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return Context.getPatientService().getAllPatients(false).size();
	}
	
	/**
	 * @see MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_8.PATIENT_UUID;
	}
	
	/**
	 * @verifies return location by tag uuid
	 * @see LocationSearchHandler#getSearchConfig()
	 */
	@Test
	public void getSearchConfig_shouldReturnPatientByIdentifier() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("identifier", "7TU-8");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(Context.getPatientService().getPatient(8).getUuid(),
		    PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void getSearchConfig_shouldReturnPatientByIdentifier_matchExact() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("identifier", "7TU-8");
		req.addParameter("searchType", "exact");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(Context.getPatientService().getPatient(8).getUuid(),
		    PropertyUtils.getProperty(hits.get(0), "uuid"));
	}
	
	@Test
	public void getSearchConfig_shouldReturnPatientByIdentifier_matchStart() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("identifier", "7T");
		req.addParameter("searchType", "start");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> hits = (List<Object>) result.get("results");
		Assert.assertEquals(1, hits.size());
	}
}
