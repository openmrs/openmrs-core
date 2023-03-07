/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper2_4;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.MockServerLogActionWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_4.ServerLogResource2_4;
import org.openmrs.module.webservices.rest.web.v1_0.controller.jupiter.MainResourceControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class ServerLogController2_4Test extends MainResourceControllerTest {

	@Autowired
	private RestService restService;

	private static final String LOG_1 = "INFO - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message";

	private static final String LOG_2 = "ERROR - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message";

	private final MockServerLogActionWrapper<ServerLogActionWrapper2_4> mockServerLogActionWrapper = new MockServerLogActionWrapper<ServerLogActionWrapper2_4>(
			new ServerLogActionWrapper2_4());

	@BeforeEach
	public void setUp() {
		ServerLogResource2_4 serverLogResource2_4 = (ServerLogResource2_4) restService
				.getResourceBySupportedClass(ServerLogActionWrapper.class);
		serverLogResource2_4.setServerLogActionWrapper(mockServerLogActionWrapper);
	}

	@Test
	public void save_shouldFailOnSave() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		assertThrows(Exception.class, () -> deserialize(handle(req)));
	}

	@Test
	public void delete_shouldFailOnDelete() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI());
		assertThrows(Exception.class, () -> deserialize(handle(req)));
	}

	@Test
	@Override
	public void shouldGetAll() throws Exception {
		//sanity check
		List<String[]> mockServerLogs = mockServerLogActionWrapper.getServerLogs();
		assertEquals(mockServerLogs.size(), 0);

		mockServerLogActionWrapper.mockMemoryAppenderBuffer.addAll(Arrays.asList(LOG_1, LOG_2));

		SimpleObject response = deserialize(handle(newGetRequest(getURI())));
		ArrayList<String[]> results = response.get("serverLog");
		assertNotNull(results);
		assertEquals(results.size(), getAllCount());

		assertEquals(mockServerLogActionWrapper.getServerLogs().size(), getAllCount());
	}

	@Test
	@Override
	public void shouldGetFullByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		assertThrows(Exception.class, () -> handle(req));
	}

	@Test
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		assertThrows(Exception.class, () -> handle(req));
	}

	@Test
	@Override
	public void shouldGetRefByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "ref");
		assertThrows(Exception.class, () -> handle(req));
	}

	@Override
	public String getURI() {
		return "serverlog";
	}

	@Override
	public String getUuid() {
		return "log1";
	}

	@Override
	public long getAllCount() {
		return mockServerLogActionWrapper.mockMemoryAppenderBuffer.size();
	}
}
