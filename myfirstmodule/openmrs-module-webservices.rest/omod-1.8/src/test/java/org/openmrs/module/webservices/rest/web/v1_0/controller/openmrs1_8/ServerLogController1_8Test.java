/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper;
import org.openmrs.module.webservices.helper.ServerLogActionWrapper1_8;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.MockServerLogActionWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ServerLogResource1_8;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerLogController1_8Test extends MainResourceControllerTest {

	@Autowired
	private RestService restService;

	private static final String LOG_1 = "INFO - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message";

	private static final String LOG_2 = "ERROR - Simple.appender(115) |2018-03-03 15:44:54,834| Info Message";

	private final MockServerLogActionWrapper<ServerLogActionWrapper1_8> mockServerLogActionWrapper = new MockServerLogActionWrapper<ServerLogActionWrapper1_8>(
			new ServerLogActionWrapper1_8());

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		ServerLogResource1_8 serverLogResource1_8 = (ServerLogResource1_8) restService
				.getResourceBySupportedClass(ServerLogActionWrapper.class);
		serverLogResource1_8.setServerLogActionWrapper(mockServerLogActionWrapper);
	}

	@Test(expected = Exception.class)
	public void save_shouldFailOnSave() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		SimpleObject resultLogs = deserialize(handle(req));
	}

	@Test(expected = Exception.class)
	public void delete_shouldFailOnDelete() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI());
		SimpleObject resultLogs = deserialize(handle(req));
	}

	@Test
	@Override
	public void shouldGetAll() throws Exception {
		//sanity check
		List<String[]> mockServerLogs = mockServerLogActionWrapper.getServerLogs();
		Assert.assertEquals(0, mockServerLogs.size());

		mockServerLogActionWrapper.mockMemoryAppenderBuffer.addAll(Arrays.asList(LOG_1, LOG_2));

		SimpleObject response = deserialize(handle(newGetRequest(getURI())));
		ArrayList<String[]> results = response.get("serverLog");
		Assert.assertNotNull(results);
		Assert.assertEquals(results.size(), getAllCount());

		Assert.assertEquals(mockServerLogActionWrapper.getServerLogs().size(), getAllCount());
	}

	@Test(expected = Exception.class)
	@Override
	public void shouldGetFullByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		handle(req);
	}

	@Test(expected = Exception.class)
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "full");
		handle(req);
	}

	@Test(expected = Exception.class)
	@Override
	public void shouldGetRefByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
		req.addParameter("v", "ref");
		handle(req);
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
