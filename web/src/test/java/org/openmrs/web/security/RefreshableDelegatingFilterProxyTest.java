/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Proves {@link RefreshableDelegatingFilterProxy} re-resolves its delegate from the current root
 * {@link WebApplicationContext} on every request, unlike the stock {@code DelegatingFilterProxy} it
 * replaces in {@code web.xml} - the module-refresh problem documented on the class itself.
 * {@link AnnotationConfigWebApplicationContext} is used rather than a plain
 * {@code GenericWebApplicationContext} specifically because it supports being {@code refresh()}ed
 * more than once, the same way {@code WebModuleUtil.refreshWAC} refreshes the real root context in
 * place when a module starts or stops.
 */
class RefreshableDelegatingFilterProxyTest {

	// deliberately not "springSecurityFilterChain" - VersionedFilterConfig below is a @Configuration
	// class, and this test suite's Spring contexts component-scan broadly enough to pick up nested
	// test @Configuration/@Service classes like it (see PreAuthorizeTestService,
	// CombinedAnnotationTestService elsewhere in this package); reusing the real production bean
	// name here would let this bean silently replace the actual security filter chain in any other
	// test's context built from the same scan
	private static final String TARGET_BEAN_NAME = "refreshableDelegatingFilterProxyTestFilterChain";

	@Test
	void doFilter_shouldResolveTheCurrentBeanEvenAfterTheContextWasRefreshedInPlace() throws Exception {
		MockServletContext servletContext = new MockServletContext();
		AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
		webApplicationContext.setServletContext(servletContext);
		webApplicationContext.register(VersionedFilterConfig.class);
		webApplicationContext.refresh();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);

		RefreshableDelegatingFilterProxy proxy = new RefreshableDelegatingFilterProxy();
		proxy.setTargetBeanName(TARGET_BEAN_NAME);
		proxy.init(new MockFilterConfig(servletContext, TARGET_BEAN_NAME));

		assertEquals("v1", chainVersionSeenBy(proxy));

		// Simulate a module start/stop: WebModuleUtil.refreshWAC() refreshes this same root context
		// in place, rebuilding its beans (here, standing in for a new SecurityFilterChain built from
		// a different set of AuthorizedUrlMatchers) without ever touching this filter.
		VersionedFilterConfig.version = "v2";
		webApplicationContext.refresh();

		assertEquals("v2", chainVersionSeenBy(proxy));
	}

	@Test
	void doFilter_shouldReuseTheResolvedBeanBetweenRefreshes() throws Exception {
		// the delegate is cached between refreshes rather than looked up per request. A listener on
		// ContextRefreshedEvent cannot do this job - a filter can only register one after the context
		// has started, and prepareRefresh() resets the listener set, so it would never fire - hence the
		// startup-date key, which this test pins by counting how often the bean is actually created.
		MockServletContext servletContext = new MockServletContext();
		AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
		webApplicationContext.setServletContext(servletContext);
		webApplicationContext.register(VersionedFilterConfig.class);
		VersionedFilterConfig.creations.set(0);
		webApplicationContext.refresh();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);

		RefreshableDelegatingFilterProxy proxy = new RefreshableDelegatingFilterProxy();
		proxy.setTargetBeanName(TARGET_BEAN_NAME);
		proxy.init(new MockFilterConfig(servletContext, TARGET_BEAN_NAME));

		int afterContextStarted = VersionedFilterConfig.creations.get();
		for (int i = 0; i < 5; i++) {
			chainVersionSeenBy(proxy);
		}

		assertEquals(afterContextStarted, VersionedFilterConfig.creations.get(),
		    "five requests should not have rebuilt the bean");
	}

	@Test
	void doFilter_shouldAnswerServiceUnavailableWhileThereIsNoContextToResolveAgainst() throws Exception {
		// mid-refresh there is no usable context. Continuing down the chain would serve the request
		// with no UserContext installed and no URL rule enforced, so it has to stop here - but as a
		// recoverable 503 rather than the stock class's IllegalStateException, which would surface as
		// a 500 on every request for as long as the refresh took.
		MockServletContext servletContext = new MockServletContext();
		RefreshableDelegatingFilterProxy proxy = new RefreshableDelegatingFilterProxy();
		proxy.setTargetBeanName(TARGET_BEAN_NAME);
		proxy.init(new MockFilterConfig(servletContext, TARGET_BEAN_NAME));

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();
		proxy.doFilter(new MockHttpServletRequest(), response, chain);

		assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.getStatus());
		assertNull(chain.getRequest(), "the request must not have been passed down the chain");
	}

	private String chainVersionSeenBy(RefreshableDelegatingFilterProxy proxy) throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		proxy.doFilter(request, response, new MockFilterChain());
		return response.getHeader("Chain-Version");
	}

	@Configuration
	static class VersionedFilterConfig {

		static volatile String version = "v1";

		static final AtomicInteger creations = new AtomicInteger();

		@Bean(name = TARGET_BEAN_NAME)
		Filter testFilterChain() {
			creations.incrementAndGet();
			String versionAtConstructionTime = version;
			return (request, response, chain) -> ((jakarta.servlet.http.HttpServletResponse) response)
			        .setHeader("Chain-Version", versionAtConstructionTime);
		}
	}
}
