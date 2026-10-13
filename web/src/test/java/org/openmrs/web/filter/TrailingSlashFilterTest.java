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

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TrailingSlashFilterTest {

	@Test
	void doFilter_shouldTrimTheTrailingSlashFromAWsRequest() throws Exception {
		MockHttpServletRequest request = request("/openmrs/ws/rest/v1/session/", "/ws", "/rest/v1/session/");
		request.setQueryString("v=full");

		HttpServletRequest filtered = filter(request);

		assertEquals("/openmrs/ws/rest/v1/session", filtered.getRequestURI());
		assertEquals("http://localhost/openmrs/ws/rest/v1/session", filtered.getRequestURL().toString());
		assertEquals("/openmrs", filtered.getContextPath());
		assertEquals("/ws", filtered.getServletPath());
		assertEquals("/rest/v1/session", filtered.getPathInfo());
		assertEquals("v=full", filtered.getQueryString());
	}

	@Test
	void doFilter_shouldTrimTheTrailingSlashInARootDeployment() throws Exception {
		HttpServletRequest filtered = filter(request("/ws/rest/v1/session/", "", "/ws", "/rest/v1/session/"));

		assertEquals("/ws/rest/v1/session", filtered.getRequestURI());
		assertEquals("/rest/v1/session", filtered.getPathInfo());
	}

	@Test
	void doFilter_shouldLeaveAWsRequestWithoutATrailingSlashAlone() throws Exception {
		MockHttpServletRequest request = request("/openmrs/ws/rest/v1/session", "/ws", "/rest/v1/session");

		assertSame(request, filter(request));
	}

	@Test
	void doFilter_shouldLeaveTheWsRootAlone() throws Exception {
		// legacyui's index page answers /openmrs/ws/, and /openmrs/ws has no handler. The filter's own
		// guard lets this through; UrlHandlerFilter is what leaves the servlet's root path alone
		MockHttpServletRequest request = request("/openmrs/ws/", "/ws", "/");

		assertSame(request, filter(request));
	}

	@Test
	void doFilter_shouldLeaveARequestToAnotherServletAlone() throws Exception {
		MockHttpServletRequest request = request("/openmrs/ms/legacyui/x/", "/ms", "/legacyui/x/");

		assertSame(request, filter(request));
	}

	@Test
	void doFilter_shouldLeaveTheWsMappingWithoutPathInfoAlone() throws Exception {
		MockHttpServletRequest request = request("/openmrs/ws", "/ws", null);

		assertSame(request, filter(request));
	}

	@Test
	void doFilter_shouldLeaveARequestWhoseUriTheContainerDidNotMapVerbatimAlone() throws Exception {
		// Tomcat collapses // when it picks the servlet but keeps it in the request URI, which
		// UrlHandlerFilter cannot parse
		MockHttpServletRequest request = request("/openmrs//ws/rest/v1/session/", "/ws", "/rest/v1/session/");

		assertSame(request, filter(request));
	}

	@Test
	void doFilter_shouldLeaveAWsRequestWhoseUriEndsInTwoSlashesAlone() throws Exception {
		// Tomcat collapses // in the path info but keeps it in the request URI
		MockHttpServletRequest request = request("/openmrs/ws/rest/v1/session//", "/ws", "/rest/v1/session/");

		assertSame(request, filter(request));
	}

	@Test
	void doFilter_shouldLeaveARequestMappedToAnotherServletAloneWhateverItsUriStartsWith() throws Exception {
		// the container resolves the dot segment before it picks the servlet; the URI keeps it
		MockHttpServletRequest request = request("/openmrs/ws/../scripts/x/", "/scripts", "/x/");

		assertSame(request, filter(request));
	}

	private static MockHttpServletRequest request(String requestUri, String servletPath, String pathInfo) {
		return request(requestUri, "/openmrs", servletPath, pathInfo);
	}

	private static MockHttpServletRequest request(String requestUri, String contextPath, String servletPath,
	        String pathInfo) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
		request.setContextPath(contextPath);
		request.setServletPath(servletPath);
		request.setPathInfo(pathInfo);
		return request;
	}

	private static HttpServletRequest filter(MockHttpServletRequest request) throws Exception {
		MockFilterChain chain = new MockFilterChain();
		new TrailingSlashFilter().doFilter(request, new MockHttpServletResponse(), chain);
		return (HttpServletRequest) chain.getRequest();
	}
}
