/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void onSubmit_shouldDisplayAUserFriendlyErrorMessage() throws Exception {
		MockHttpServletRequest post = webTestHelper.newPOST("/admin/forms/fieldType.list");
		
		post.addParameter("fieldTypeId", "1");
		
		Response response = webTestHelper.handle(post);
		Assert.assertNotNull(response.session.getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	}
}
