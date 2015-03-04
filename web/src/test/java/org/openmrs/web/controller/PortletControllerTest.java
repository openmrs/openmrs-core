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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests methods on the {@link PortletController} class
 */
public class PortletControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * Convenience method to get the "model" from the controller's handleRequest method
	 * 
	 * @param patientId the patient id to fetch
	 * @return the Map from string to object of everything in the generated "model"
	 * @throws Exception
	 */
	private Map<String, Object> getModelFromController(Integer patientId) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setAttribute(WebConstants.INIT_REQ_UNIQUE_ID, "1");
		request.getSession().setAttribute(WebConstants.OPENMRS_PORTLET_LAST_REQ_ID, "0");
		request.setAttribute("javax.servlet.include.servlet_path", "testPortlet");
		request.setAttribute("org.openmrs.portlet.parameters", new HashMap());
		request.setAttribute("org.openmrs.portlet.patientId", patientId);
		
		ModelAndView modelAndView = new PortletController().handleRequest(request, response);
		
		return (Map<String, Object>) modelAndView.getModel().get("model");
	}
	
	/**
	 * @see {@link PortletController#handleRequest(HttpServletRequest,HttpServletResponse)}
	 */
	@Test
	@Verifies(value = "should calculate bmi into patientBmiAsString", method = "handleRequest(HttpServletRequest,HttpServletResponse)")
	public void handleRequest_shouldCalculateBmiIntoPatientBmiAsString() throws Exception {
		executeDataSet("org/openmrs/web/controller/include/PortletControllerTest-bmi.xml");
		Map<String, Object> modelmap = getModelFromController(7);
		Assert.assertEquals("61.7", modelmap.get("patientBmiAsString"));
	}
	
	/**
	 * @see {@link PortletController#handleRequest(HttpServletRequest,HttpServletResponse)}
	 */
	@Test
	@Verifies(value = "should not fail with empty height and weight properties", method = "handleRequest(HttpServletRequest,HttpServletResponse)")
	public void handleRequest_shouldNotFailWithEmptyHeightAndWeightProperties() throws Exception {
		Map<String, Object> modelmap = getModelFromController(7);
		Assert.assertEquals("?", modelmap.get("patientBmiAsString"));
	}
}
