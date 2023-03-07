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

import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper1_8;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.MockServerLogActionWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;

/**
 * Integration tests for the ServerLogResource class
 */
public class ServerLogResource1_8Test extends BaseModuleWebContextSensitiveTest {

	@Autowired
	RestService restService;

	@Autowired
	private MainResourceController mainResourceController;

	private final MockServerLogActionWrapper<ServerLogActionWrapper1_8> mockServerLogActionWrapper = new MockServerLogActionWrapper<ServerLogActionWrapper1_8>(
			new ServerLogActionWrapper1_8());

	public String getURI() {
		return "serverlog";
	}

	@Test
	public void testGetAll() {
		ServerLogResource1_8 serverLogResource = (ServerLogResource1_8) restService
				.getResourceBySupportedClass(ServerLogActionWrapper.class);
		serverLogResource.setServerLogActionWrapper(mockServerLogActionWrapper);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleObject result = mainResourceController.get(getURI(), request, response);

		ArrayList<String[]> serverLog = result.get("serverLog");
		Assert.assertEquals(0, serverLog.size());

		String mockLogLine1 = "INFO - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message";
		// Add some mock log lines to mockMemoryAppenderBuffer
		mockServerLogActionWrapper.mockMemoryAppenderBuffer.add(mockLogLine1);
		result = mainResourceController.get(getURI(), request, response);
		serverLog = result.get("serverLog");
		Assert.assertNotEquals(serverLog.size(), 0);

		String[] logLine1 = serverLog.get(0);
		Assert.assertNotEquals(logLine1[0], null);
		Assert.assertNotEquals(logLine1[1], null);
		Assert.assertNotEquals(logLine1[2], null);
		Assert.assertNotEquals(logLine1[3], null);
	}

	@Test(expected = Exception.class)
	public void shouldThrowExceptionWhenRequestGetDefaultByUuid() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleObject result = mainResourceController.get(getURI() + "/" + getUuid(), request, response);

		ArrayList<String[]> serverLog = result.get("serverLog");
	}

	public String getUuid() {
		return "log1";
	}
}
