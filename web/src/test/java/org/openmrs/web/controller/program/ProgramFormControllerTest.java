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
package org.openmrs.web.controller.program;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

/**
 * Tests the {@link ProgramFormController} class.
 */
public class ProgramFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ProgramFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Transactional(readOnly = true)
	@Verifies(value = "should save workflows with program", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldSaveWorkflowsWithProgram() throws Exception {
		
		// sanity check to make sure that program #3 doesn't have any workflows already:
		Assert.assertEquals(0, Context.getProgramWorkflowService().getProgram(3).getAllWorkflows().size());
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("programId", "3");
		request.setParameter("allWorkflows", ":2"); // set one workflow on this program
		
		ProgramFormController controller = (ProgramFormController) applicationContext.getBean("programForm");
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertNotSame(0, Context.getProgramWorkflowService().getProgram(3).getAllWorkflows().size());
	}
}
