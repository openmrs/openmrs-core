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
package org.openmrs.scheduler.web.controller;

import static org.junit.Assert.assertNotNull;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class SchedulerFormTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	private SchedulerFormController controller;
	
	/**
	 * See TRUNK-3970: Error when adding a task in version 1.9.3
	 * https://tickets.openmrs.org/browse/TRUNK-3970
	 */
	@Test
	public void addANewTaskShouldNotError() throws Exception {
		HttpServletRequest request = new MockHttpServletRequest("GET", "/openmrs/admin/scheduler/scheduler.form");
		ModelAndView mav = controller.handleRequest(request, new MockHttpServletResponse());
		assertNotNull(mav);
	}
}
