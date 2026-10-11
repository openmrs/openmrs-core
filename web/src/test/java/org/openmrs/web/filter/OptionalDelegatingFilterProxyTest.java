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

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers {@link OptionalDelegatingFilterProxy}, the filter that lets {@code web.xml} declare the
 * Spring Session repository filter unconditionally while it is only a real filter when
 * {@code session.distributed=true}.
 */
class OptionalDelegatingFilterProxyTest {

	/**
	 * Mirrors the {@code <filter-name>} used in {@code web.xml}, which is also the bean name
	 * {@code DelegatingFilterProxy} falls back to when no target bean name has been set.
	 */
	private static final String FILTER_NAME = "springSessionRepositoryFilter";

	private final MockHttpServletRequest request = new MockHttpServletRequest();

	private final MockHttpServletResponse response = new MockHttpServletResponse();

	private final RecordingFilterChain chain = new RecordingFilterChain();

	@Test
	void init_shouldNotFailWhenTheTargetBeanIsAbsent() {
		MockServletContext servletContext = servletContextWith(webApplicationContext(null, null));

		OptionalDelegatingFilterProxy proxy = new OptionalDelegatingFilterProxy();

		assertDoesNotThrow(() -> proxy.init(filterConfig(servletContext)));
	}

	@Test
	void doFilter_shouldDelegateToTheTargetBeanWhenItIsPresent() throws ServletException, IOException {
		RecordingFilter target = new RecordingFilter();
		MockServletContext servletContext = servletContextWith(webApplicationContext(FILTER_NAME, target));
		OptionalDelegatingFilterProxy proxy = new OptionalDelegatingFilterProxy();
		proxy.init(filterConfig(servletContext));

		proxy.doFilter(request, response, chain);

		assertEquals(1, target.invocations);
		assertEquals(0, chain.invocations);
	}

	@Test
	void doFilter_shouldPassThroughWhenTheTargetBeanIsAbsent() throws ServletException, IOException {
		MockServletContext servletContext = servletContextWith(webApplicationContext(null, null));
		OptionalDelegatingFilterProxy proxy = new OptionalDelegatingFilterProxy();
		proxy.init(filterConfig(servletContext));

		proxy.doFilter(request, response, chain);

		assertEquals(1, chain.invocations);
	}

	@Test
	void doFilter_shouldPassThroughWhenNoTargetBeanNameIsResolvable() throws ServletException, IOException {
		OptionalDelegatingFilterProxy proxy = new OptionalDelegatingFilterProxy();

		proxy.doFilter(request, response, chain);

		assertEquals(1, chain.invocations);
	}

	@Test
	void doFilter_shouldPassThroughWhenNoWebApplicationContextIsAvailable() throws ServletException, IOException {
		OptionalDelegatingFilterProxy proxy = new ContextLessProxy();
		proxy.init(filterConfig(new MockServletContext()));

		proxy.doFilter(request, response, chain);

		assertEquals(1, chain.invocations);
	}

	private static MockFilterConfig filterConfig(MockServletContext servletContext) {
		MockFilterConfig filterConfig = new MockFilterConfig(servletContext, FILTER_NAME);
		filterConfig.addInitParameter("targetBeanName", FILTER_NAME);
		return filterConfig;
	}

	private static MockServletContext servletContextWith(WebApplicationContext wac) {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		return servletContext;
	}

	private static StaticWebApplicationContext webApplicationContext(String beanName, Filter bean) {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		if (beanName != null) {
			wac.getBeanFactory().registerSingleton(beanName, bean);
		}
		wac.refresh();
		return wac;
	}

	/**
	 * Simulates a filter that runs before any {@code ContextLoaderListener} has published the root web
	 * application context.
	 */
	private static class ContextLessProxy extends OptionalDelegatingFilterProxy {

		@Override
		protected WebApplicationContext findWebApplicationContext() {
			return null;
		}
	}

	private static class RecordingFilter implements Filter {

		private int invocations;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
			invocations++;
		}
	}

	private static class RecordingFilterChain implements FilterChain {

		private int invocations;

		@Override
		public void doFilter(ServletRequest request, ServletResponse response) {
			invocations++;
		}
	}
}
