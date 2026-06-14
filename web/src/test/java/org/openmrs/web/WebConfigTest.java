/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebConfigTest {

	/**
	 * Without this mapping, the {@code *.htm} servlet mapping in web.xml routes the welcome file
	 * {@code /index.htm} to the openmrs DispatcherServlet, which has no core handler for it - producing
	 * a 404 on {@code /openmrs/} when no UI module is installed.
	 */
	@Test
	public void indexHtmFallbackMapping_shouldRegisterDefaultServletHandlerForIndexHtm() {
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getNamedDispatcher("default")).thenReturn(mock(RequestDispatcher.class));

		SimpleUrlHandlerMapping mapping = new WebConfig().indexHtmFallbackMapping(servletContext);

		assertNotNull(mapping.getUrlMap().get("/index.htm"));
		assertInstanceOf(DefaultServletHttpRequestHandler.class, mapping.getUrlMap().get("/index.htm"));
	}

	/**
	 * The fallback must have lower precedence than legacyui's {@code legacyUiUrlMapping} (order 100) so
	 * legacyui's {@code /**\/*.htm} handler still wins for {@code /index.htm} when installed.
	 */
	@Test
	public void indexHtmFallbackMapping_shouldHaveLowestPrecedenceOrder() {
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getNamedDispatcher("default")).thenReturn(mock(RequestDispatcher.class));

		SimpleUrlHandlerMapping mapping = new WebConfig().indexHtmFallbackMapping(servletContext);

		assertEquals(Integer.MAX_VALUE - 1, mapping.getOrder());
	}

	/**
	 * Guards against a regression where the {@code ServletContext} is not propagated to the handler
	 * (e.g. someone removes the explicit {@code setServletContext} call). Without it,
	 * {@link DefaultServletHttpRequestHandler#handleRequest} throws {@code IllegalStateException} at
	 * runtime instead of forwarding to the container's default servlet, breaking {@code /openmrs/}.
	 */
	@Test
	public void indexHtmFallbackMapping_shouldForwardToContainerDefaultServlet() throws Exception {
		ServletContext servletContext = mock(ServletContext.class);
		RequestDispatcher defaultDispatcher = mock(RequestDispatcher.class);
		when(servletContext.getNamedDispatcher("default")).thenReturn(defaultDispatcher);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		SimpleUrlHandlerMapping mapping = new WebConfig().indexHtmFallbackMapping(servletContext);
		HttpRequestHandler handler = (HttpRequestHandler) mapping.getUrlMap().get("/index.htm");
		handler.handleRequest(request, response);

		verify(defaultDispatcher).forward(request, response);
	}
}
