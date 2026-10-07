/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import jakarta.servlet.http.MappingMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrailingSlashFilterTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new LocationController()).addFilters(new TrailingSlashFilter()).build();
	}

	@Test
	void shouldHandleGetWithTrailingSlashAsPathWithoutSlash() throws Exception {
		mockMvc.perform(onWsServlet(get("/openmrs/ws/rest/v1/location"))).andExpect(status().isOk())
		        .andExpect(content().string("all locations"));
		mockMvc.perform(onWsServlet(get("/openmrs/ws/rest/v1/location/"))).andExpect(status().isOk())
		        .andExpect(content().string("all locations"));
	}

	@Test
	void shouldHandlePostWithTrailingSlashAsPostToPathWithoutSlash() throws Exception {
		mockMvc.perform(onWsServlet(post("/openmrs/ws/rest/v1/location/")).content("Kampala")).andExpect(status().isOk())
		        .andExpect(content().string("created Kampala"));
	}

	private static MockHttpServletRequestBuilder onWsServlet(MockHttpServletRequestBuilder builder) {
		return builder.contextPath("/openmrs").servletPath("/ws").with(request -> {
			request.setHttpServletMapping(new MockHttpServletMapping("", "/ws/*", "openmrs", MappingMatch.PATH));
			return request;
		});
	}

	@RestController
	static class LocationController {

		@GetMapping("/rest/v1/location")
		String getAll() {
			return "all locations";
		}

		@PostMapping("/rest/v1/location")
		String create(@RequestBody String name) {
			return "created " + name;
		}
	}
}
