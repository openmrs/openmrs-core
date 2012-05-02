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
package org.openmrs.web.controller.concept;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the {@link ConceptSourceFormController}
 */
public class ConceptSourceFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ConceptSourceListController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should retire concept source", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRetireConceptSource() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptSourceFormController controller = (ConceptSourceFormController) applicationContext
		        .getBean("conceptSourceForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("conceptSourceId", "3");
		mockRequest.setParameter("retireReason", "dummy reason for retirement");
		mockRequest.setParameter("retire", "dummy reason for retirement");
		
		controller.handleRequest(mockRequest, new MockHttpServletResponse());
		
		ConceptSource conceptSource = cs.getConceptSource(3);
		Assert.assertTrue(conceptSource.isRetired());
		Assert.assertEquals("dummy reason for retirement", conceptSource.getRetireReason());
	}
	
	/**
	 * @see {@link ConceptSourceListController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should delete concept source", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldDeleteConceptSource() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptSourceFormController controller = (ConceptSourceFormController) applicationContext
		        .getBean("conceptSourceForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("conceptSourceId", "3");
		mockRequest.setParameter("purge", "dummy reason for deletion");
		
		controller.handleRequest(mockRequest, new MockHttpServletResponse());
		
		ConceptSource nullConceptSource = cs.getConceptSource(3);
		Assert.assertNull(nullConceptSource);
	}
	
	/**
	 * @see {@link ConceptSourceListController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should restore retired concept source", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRestoreRetiredConceptSource() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptSourceFormController controller = (ConceptSourceFormController) applicationContext
		        .getBean("conceptSourceForm");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("conceptSourceId", "3");
		mockRequest.setParameter("retireReason", "dummy reason for retirement");
		mockRequest.setParameter("retire", "dummy reason for retirement");
		
		controller.handleRequest(mockRequest, new MockHttpServletResponse());
		
		ConceptSource conceptSource = cs.getConceptSource(3);
		Assert.assertTrue(conceptSource.isRetired());
		Assert.assertEquals("dummy reason for retirement", conceptSource.getRetireReason());
		
		MockHttpServletRequest restoreMockRequest = new MockHttpServletRequest();
		restoreMockRequest.setMethod("POST");
		restoreMockRequest.setParameter("conceptSourceId", "3");
		restoreMockRequest.setParameter("restore", "dummy reason for restoration");
		
		controller.handleRequest(restoreMockRequest, new MockHttpServletResponse());
		
		ConceptSource newConceptSource = cs.getConceptSource(3);
		Assert.assertNotNull("Error, Object is null", newConceptSource);
		Assert.assertTrue(!newConceptSource.isRetired());
	}
}
