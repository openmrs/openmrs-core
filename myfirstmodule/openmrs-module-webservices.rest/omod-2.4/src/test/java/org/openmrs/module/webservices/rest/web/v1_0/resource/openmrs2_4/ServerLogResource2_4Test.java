/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_4;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;

import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper2_4;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.MockServerLogActionWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Integration tests for the ServerLogResource2_4 class
 */
public class ServerLogResource2_4Test extends BaseModuleWebContextSensitiveTest {

	private final MockServerLogActionWrapper<ServerLogActionWrapper2_4> mockServerLogActionWrapper = new MockServerLogActionWrapper<>(
			new ServerLogActionWrapper2_4());

	@Autowired
	RestService restService;

	@Autowired
	private MainResourceController mainResourceController;

	public String getURI() {
		return "serverlog";
	}

	@Test
	public void testGetAll() {
		ServerLogResource2_4 serverLogResource = (ServerLogResource2_4) restService
				.getResourceBySupportedClass(ServerLogActionWrapper.class);
		serverLogResource.setServerLogActionWrapper(mockServerLogActionWrapper);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();
		SimpleObject result = mainResourceController.get(getURI(), request, response);

		ArrayList<String[]> serverLog = result.get("serverLog");
		assertEquals(serverLog.size(), 0);

		String mockLogLine1 = "INFO - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message";
		// Add some mock log lines to mockMemoryAppenderBuffer
		mockServerLogActionWrapper.mockMemoryAppenderBuffer.add(mockLogLine1);
		result = mainResourceController.get(getURI(), request, response);
		serverLog = result.get("serverLog");
		assertNotEquals(serverLog.size(), 0);

		String[] logLine1 = serverLog.get(0);
		assertNotEquals(logLine1[0], null);
		assertNotEquals(logLine1[1], null);
		assertNotEquals(logLine1[2], null);
		assertNotEquals(logLine1[3], null);
	}

	@Test
	public void shouldThrowExceptionWhenRequestGetDefaultByUuid() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertThrows(Exception.class, () -> mainResourceController.get(getURI() + "/" + getUuid(), request, response));

	}

	public String getUuid() {
		return "log1";
	}
}
