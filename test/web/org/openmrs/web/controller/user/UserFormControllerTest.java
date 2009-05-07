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
package org.openmrs.web.controller.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests the {@link UserFormController} class.
 */
public class UserFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link UserFormController#formBackingObject(HttpServletRequest)}
	 */
	// @Transactional annotation needed because the parent class is @Transactional and so screws propagates to this readOnly test
	@Transactional(readOnly = true)
	@Test
	@Verifies(value = "should get empty form with valid user", method = "formBackingObject(HttpServletRequest)")
	public void formBackingObject_shouldGetEmptyFormWithValidUser() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("userId", "1");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		UserFormController controller = (UserFormController) applicationContext.getBean("userForm");
		
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure there is a "userId" filled in on the concept
		User command = (User) modelAndView.getModel().get("user");
		Assert.assertNotNull(command.getUserId());
	}
	
}
