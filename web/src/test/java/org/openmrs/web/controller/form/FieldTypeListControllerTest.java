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
package org.openmrs.web.controller.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.openmrs.web.test.WebTestHelper.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.validation.BindException;

public class FieldTypeListControllerTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	@Qualifier("webTestHelper")
	private WebTestHelper webTestHelper;
	
	/**
	 * @see FieldTypeListController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies display a user friendly error message
	 */
	@Test
	@NotTransactional
	public void onSubmit_shouldDisplayAUserFriendlyErrorMessage() throws Exception {
		MockHttpServletRequest post = webTestHelper.newPOST("/admin/forms/fieldType.list");
		
		post.addParameter("fieldTypeId", "1");
		
		Response response = webTestHelper.handle(post);
		Assert.assertNotNull(response.session.getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	}
}
