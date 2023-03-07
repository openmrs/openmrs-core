/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.api.OrderService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ActiveListTypeResource1_8Test extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private MainResourceController mainResourceController;
	
	@Mock
	private OrderService orderService;
	
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void testResourceDoesNotSupportOperationException() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleObject activeListTypes = mainResourceController.get("activelisttype", new MockHttpServletRequest(), response);
	}
}
