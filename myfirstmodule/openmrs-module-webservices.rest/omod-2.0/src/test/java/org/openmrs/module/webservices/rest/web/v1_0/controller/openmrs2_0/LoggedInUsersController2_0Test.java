/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.openmrs.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LoggedInUsersController2_0Test extends RestControllerTestUtils {

	@Autowired
	ApplicationContext context;

	private String getURI() {
		return "loggedinusers";
	}

	@Test
	public void shouldGetLoggedInUsers() throws Exception {
		// prepare mock session
		MockHttpSession session = new MockHttpSession();
		Map<String, String> users = new HashMap<>();
		users.put("id", "username");
		session.getServletContext().setAttribute(WebConstants.CURRENT_USERS, users);

		// make GET request
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/rest/" + getNamespace() + "/" + getURI());
		req.setSession(session);
		MockHttpServletResponse response = handle(req);

		// assert result
		List<String> result = Arrays.asList(new ObjectMapper().readValue(response.getContentAsString(), String[].class));

		assertEquals(1, result.size());
		assertEquals("username", result.get(0));
	}

}
