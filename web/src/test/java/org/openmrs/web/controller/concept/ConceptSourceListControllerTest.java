/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
 * Tests the {@link ConceptSourceListController}
 */
public class ConceptSourceListControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ConceptSourceListController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should retire concept source", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldRetireConceptSource() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptSourceListController controller = (ConceptSourceListController) applicationContext
		        .getBean("conceptSourceList");
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setMethod("POST");
		mockRequest.setParameter("conceptSourceId", "3");
		mockRequest.setParameter("retireReason", "dummy reason for retirement");
		
		controller.handleRequest(mockRequest, new MockHttpServletResponse());
		
		ConceptSource conceptSource = cs.getConceptSource(3);
		Assert.assertTrue(conceptSource.isRetired());
		Assert.assertEquals("dummy reason for retirement", conceptSource.getRetireReason());
	}
}
