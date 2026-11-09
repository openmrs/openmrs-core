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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * A {@link DelegatingFilterProxy} that re-resolves its target bean after the root
 * {@link WebApplicationContext} is refreshed, rather than resolving it once for the life of the
 * filter as the stock class does.
 * <p>
 * Starting or stopping a module refreshes that context in place ({@code WebModuleUtil.refreshWAC}
 * closes and refreshes the same context object, re-initializing the dispatcher servlets but neither
 * {@code web.xml} filter seat), so a stock proxy would keep running the bean instance from before
 * the refresh for the rest of the webapp's life - silently ignoring, or keeping in force, the URL
 * rules of a module started or stopped afterwards. This is what makes
 * {@link AuthorizedUrlMatcher}'s promise that a stale rule "cannot persist past that refresh" true.
 * <p>
 * The delegate is cached between refreshes, keyed on the context's
 * {@link WebApplicationContext#getStartupDate() startup date}. A {@code ContextRefreshedEvent}
 * listener is the obvious alternative and does not work: this filter is created by the servlet
 * container, so it could only register one after the context started, and {@code prepareRefresh()}
 * resets the listener set - the listener is dropped and never fires.
 * <p>
 * While a refresh is in progress there is no context to resolve against, and continuing the chain
 * would serve a request with no {@code UserContext} or no rule enforced, so those are answered
 * {@link HttpServletResponse#SC_SERVICE_UNAVAILABLE} - accurate and recoverable, where the stock
 * class's {@code IllegalStateException} would be a 500.
 *
 * @since 3.0.0
 */
public class RefreshableDelegatingFilterProxy extends DelegatingFilterProxy {

	private static final Logger log = LoggerFactory.getLogger(RefreshableDelegatingFilterProxy.class);

	/**
	 * The delegate and the startup date it was resolved against, as one value behind one volatile
	 * field. Two separate volatile fields could interleave across a refresh and leave the pre-refresh
	 * delegate recorded under the post-refresh date, pinning the stale delegate for the life of the
	 * webapp.
	 */
	private record ResolvedDelegate(Filter delegate, long startupDate) {
	}

	private volatile ResolvedDelegate resolved;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
		Filter delegate;
		try {
			delegate = currentDelegate();
		} catch (RuntimeException e) {
			// mid-refresh the context is closed, or the bean is not registered yet
			log.debug("Could not resolve filter bean '{}'; the application context is not ready", getTargetBeanName(), e);
			delegate = null;
		}

		if (delegate == null) {
			((HttpServletResponse) response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
			    "OpenMRS is starting up or reloading a module");
			return;
		}

		invokeDelegate(delegate, request, response, filterChain);
	}

	/**
	 * @return the target bean from the current root context, resolved afresh whenever that context has
	 *         been refreshed since the last call, or <code>null</code> if there is no context to
	 *         resolve against
	 */
	private Filter currentDelegate() {
		WebApplicationContext context = findWebApplicationContext();
		if (context == null) {
			return null;
		}

		long startupDate = context.getStartupDate();
		ResolvedDelegate current = resolved;
		if (current != null && current.startupDate() == startupDate) {
			return current.delegate();
		}

		Filter delegate = context.getBean(getTargetBeanName(), Filter.class);
		resolved = new ResolvedDelegate(delegate, startupDate);
		return delegate;
	}
}
