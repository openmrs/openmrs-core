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
