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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebConfigTest {

	/**
	 * Builds the resolver list the way {@code @EnableWebMvc} does, then lets Spring Security's own
	 * {@code AuthorizationProxyWebConfiguration} contribute to it - so this exercises the real
	 * resolver, and a rename upstream fails here rather than silently restoring the bare 403 page that
	 * {@link WebConfig#removeRethrowingAccessDeniedExceptionResolver(ObjectProvider)} exists to stop.
	 */
	private static HandlerExceptionResolverComposite compositeWithSpringSecurityResolver() throws Exception {
		List<HandlerExceptionResolver> resolvers = new ArrayList<>(
		        List.of(new ExceptionHandlerExceptionResolver(), new DefaultHandlerExceptionResolver()));

		Class<?> configuration = Class.forName(
		    "org.springframework.security.config.annotation.method.configuration." + "AuthorizationProxyWebConfiguration");
		Constructor<?> constructor = configuration.getDeclaredConstructor();
		constructor.setAccessible(true);
		Method extend = configuration.getMethod("extendHandlerExceptionResolvers", List.class);
		extend.setAccessible(true);
		extend.invoke(constructor.newInstance(), resolvers);

		HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
		composite.setExceptionResolvers(resolvers);
		return composite;
	}

	private static void runRemoval(HandlerExceptionResolverComposite composite) {
		ObjectProvider<HandlerExceptionResolverComposite> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(composite);

		new WebConfig().removeRethrowingAccessDeniedExceptionResolver(provider).afterSingletonsInstantiated();
	}

	/**
	 * Proves the hole this removal closes is real before asserting it is closed: as Spring Security
	 * leaves the composite, a denial is rethrown instead of being resolved, so
	 * {@code simpleMappingExceptionResolver} (a separate bean, consulted only after this composite)
	 * never maps it to {@code uncaughtException}.
	 */
	@Test
	public void springSecurityResolver_shouldRethrowADenialBeforeRemoval() throws Exception {
		HandlerExceptionResolverComposite composite = compositeWithSpringSecurityResolver();
		AccessDeniedException denial = new AccessDeniedException("Privileges required: Get Providers");

		AccessDeniedException thrown = assertThrows(AccessDeniedException.class,
		    () -> composite.resolveException(new MockHttpServletRequest(), new MockHttpServletResponse(), null, denial));

		assertSame(denial, thrown);
	}

	@Test
	public void removeRethrowingAccessDeniedExceptionResolver_shouldDropItFromTheComposite() throws Exception {
		HandlerExceptionResolverComposite composite = compositeWithSpringSecurityResolver();
		int before = composite.getExceptionResolvers().size();

		runRemoval(composite);

		assertEquals(before - 1, composite.getExceptionResolvers().size());
		assertTrue(
		    composite.getExceptionResolvers().stream()
		            .noneMatch(resolver -> resolver.getClass().getName().contains("AccessDeniedExceptionResolver")),
		    composite.getExceptionResolvers().toString());
	}

	/**
	 * The point of the removal: the composite declines a denial instead of rethrowing it, which is what
	 * lets the DispatcherServlet fall through to {@code simpleMappingExceptionResolver}.
	 */
	@Test
	public void removeRethrowingAccessDeniedExceptionResolver_shouldLeaveADenialUnresolved() throws Exception {
		HandlerExceptionResolverComposite composite = compositeWithSpringSecurityResolver();
		runRemoval(composite);

		assertNull(composite.resolveException(new MockHttpServletRequest(), new MockHttpServletResponse(), null,
		    new AccessDeniedException("denied")));
	}

	/**
	 * The resolver the denial now reaches, and the view it lands on - the same one
	 * {@code APIAuthenticationException} reaches on master, where a UI module's
	 * {@code authorizationHandlerInclude.jsp} turns it into the login redirect.
	 */
	@Test
	public void simpleMappingExceptionResolver_shouldMapADenialToUncaughtException() {
		SimpleMappingExceptionResolver resolver = new WebConfig().simpleMappingExceptionResolver();

		ModelAndView resolved = resolver.resolveException(new MockHttpServletRequest(), new MockHttpServletResponse(), null,
		    new AccessDeniedException("denied"));

		assertNotNull(resolved);
		assertEquals("uncaughtException", resolved.getViewName());
	}

	/**
	 * Keeps the removal from being pointless: it only matters because this resolver is a separate bean
	 * ordered after the {@code @EnableWebMvc} composite (order 0), so anything the composite resolves -
	 * or rethrows - never reaches here.
	 */
	@Test
	public void simpleMappingExceptionResolver_shouldBeOrderedAfterTheWebMvcComposite() {
		assertEquals(100, new WebConfig().simpleMappingExceptionResolver().getOrder());
	}

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
