/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Tests for the {@link LocationFormController} which handles the location.form page.
 */
public class LocationFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link LocationFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not retire location if reason is empty", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotRetireLocationIfReasonIsEmpty() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("locationId", "1");
		request.setParameter("retireReason", "");
		request.setParameter("retired", "true");
		request.setParameter("retireLocation", "true");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		LocationFormController controller = (LocationFormController) applicationContext.getBean("locationForm");
		
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure an error is returned because of the empty retire reason
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) modelAndView.getModel().get(
		    "org.springframework.validation.BindingResult.location");
		Assert.assertTrue(bindingResult.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see {@link LocationFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should retire location", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRetireLocation() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("locationId", "1");
		request.setParameter("retireReason", "some non-null reason");
		request.setParameter("retireLocation", "true");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		((SimpleFormController) applicationContext.getBean("locationForm")).handleRequest(request, response);
		
		Location retiredLocation = Context.getLocationService().getLocation(1);
		Assert.assertTrue(retiredLocation.isRetired());
	}
	
	/**
	 * @see {@link LocationFormController#formBackingObject(HttpServletRequest)}
	 */
	@Test
	@Verifies(value = "should return valid location given valid locationId", method = "formBackingObject(HttpServletRequest)")
	public void formBackingObject_shouldReturnValidLocationGivenValidLocationId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("locationId", "1");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		LocationFormController controller = (LocationFormController) applicationContext.getBean("locationForm");
		
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure there is an "locationId" filled in on the concept
		Location command = (Location) modelAndView.getModel().get("location");
		Assert.assertNotNull(command.getLocationId());
	}
}
