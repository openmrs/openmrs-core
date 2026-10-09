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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.MappingMatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.server.RequestPath;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ServletRequestPathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepeatedSlashFilterTest {

	private final RepeatedSlashFilter filter = new RepeatedSlashFilter();

	private final MockFilterChain chain = new MockFilterChain();

	@Test
	void doFilter_shouldCollapseRepeatedSlashesInTheRequestUri() throws Exception {
		filter.doFilter(wsRequest("/openmrs//ws///rest/v1//session"), new MockHttpServletResponse(), chain);

		HttpServletRequest filtered = filtered();
		assertEquals("/openmrs/ws/rest/v1/session", filtered.getRequestURI());
		// the container already normalized these when it chose the servlet
		assertEquals("/ws", filtered.getServletPath());
		assertEquals("/rest/v1/session", filtered.getPathInfo());
	}

	@Test
	void doFilter_shouldCollapseRepeatedSlashesInTheRequestUrlButNotAfterItsScheme() throws Exception {
		filter.doFilter(wsRequest("/openmrs//ws/rest/v1/session"), new MockHttpServletResponse(), chain);

		assertEquals("http://localhost/openmrs/ws/rest/v1/session", filtered().getRequestURL().toString());
	}

	@Test
	void doFilter_shouldCollapseRepeatedSlashesAheadOfTheContextPath() throws Exception {
		// Tomcat skips leading slashes when it works out the context path, so the request arrives with a
		// context path of /openmrs while the URI keeps them
		filter.doFilter(wsRequest("//openmrs/ws/rest/v1/session"), new MockHttpServletResponse(), chain);

		assertEquals("/openmrs/ws/rest/v1/session", filtered().getRequestURI());
	}

	@Test
	void doFilter_shouldCollapseRepeatedSlashesInsideTheContextPath() throws Exception {
		// for a context path of several segments, Tomcat returns them as the URI spelled them
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/a//b/x");
		request.setContextPath("/a//b");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertEquals("/a/b", filtered().getContextPath());
		assertEquals("/a/b/x", filtered().getRequestURI());
	}

	@Test
	void doFilter_shouldLeaveTheQueryStringAndEncodedSlashesAlone() throws Exception {
		MockHttpServletRequest request = wsRequest("/openmrs//ws/rest/v1/%2F%2Fsession");
		request.setQueryString("q=//x");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertEquals("/openmrs/ws/rest/v1/%2F%2Fsession", filtered().getRequestURI());
		assertEquals("q=//x", filtered().getQueryString());
	}

	@Test
	void doFilter_shouldPassARequestWithoutRepeatedSlashesOnUnwrapped() throws Exception {
		MockHttpServletRequest request = wsRequest("/openmrs/ws/rest/v1/session");
		request.setQueryString("q=//x");

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		assertSame(request, chain.getRequest());
	}

	/**
	 * What failed these requests with a 500: Spring Security's {@code ServletRequestPathFilter} parses
	 * the path with this utility before {@code FilterChainProxy}'s firewall is reached, and it requires
	 * the context path plus the {@code /ws} servlet path to start the raw request URI.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "/openmrs//ws/rest/v1/session", "/openmrs///ws/rest/v1/session",
	        "//openmrs/ws/rest/v1/session" })
	void doFilter_shouldLetSpringParseThePathOfARequestItFailedToParseRaw(String requestUri) throws Exception {
		MockHttpServletRequest raw = wsRequest(requestUri);
		assertThrows(IllegalArgumentException.class, () -> ServletRequestPathUtils.parseAndCache(raw));

		filter.doFilter(raw, new MockHttpServletResponse(), chain);
		RequestPath path = ServletRequestPathUtils.parseAndCache(filtered());

		assertEquals("/openmrs/ws/rest/v1/session", path.value());
	}

	private HttpServletRequest filtered() {
		return (HttpServletRequest) chain.getRequest();
	}

	/**
	 * @return a request for the {@code openmrs} {@code DispatcherServlet}'s {@code /ws/*} mapping as
	 *         Tomcat presents one: the servlet path and path info come from the normalized path it
	 *         dispatched on, while the request URI stays as the client sent it
	 */
	private static MockHttpServletRequest wsRequest(String requestUri) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
		request.setContextPath("/openmrs");
		request.setServletPath("/ws");
		request.setPathInfo("/rest/v1/session");
		request.setHttpServletMapping(new MockHttpServletMapping("rest/v1/session", "/ws/*", "openmrs", MappingMatch.PATH));
		return request;
	}
}
