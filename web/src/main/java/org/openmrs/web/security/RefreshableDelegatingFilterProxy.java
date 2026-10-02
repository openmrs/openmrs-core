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

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * A {@link DelegatingFilterProxy} that re-resolves its target bean from the current root
 * {@link WebApplicationContext} on every request, instead of resolving it once and caching it for
 * the life of the filter (the stock class's own behavior).
 * <p>
 * That caching is wrong for either {@code web.xml} seat this class currently occupies -
 * {@code springSecurityFilterChain} and {@link OpenmrsAuthorizationFilter}'s own
 * {@code openmrsAuthorizationFilter} seat alike: starting or stopping a module refreshes the same
 * root context both beans live in, in place ({@code WebModuleUtil.startModule}/{@code stopModule}
 * call {@code refreshWAC}, which re-initializes the dispatcher servlets but never touches either
 * filter), so a stock {@code DelegatingFilterProxy} would keep running whichever bean instance
 * existed <em>before</em> the refresh - for {@code openmrsAuthorizationFilter}, one built from
 * whichever {@link AuthorizedUrlMatchers} beans core and modules had registered at that point - for
 * the rest of the webapp's life. A module started, stopped, or upgraded afterward would have its
 * URL rules ignored, or left in force, silently. {@link AuthorizedUrlMatcher}'s own javadoc
 * promises a stale or missing rule "cannot persist past that refresh"; this class is what makes
 * that true for a seat a module refresh can actually reach.
 * <p>
 * Overrides {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} entirely rather than
 * {@link #initDelegate(WebApplicationContext)} - the latter is only ever consulted once, to
 * populate the superclass's own cached delegate field, so overriding it alone would not stop the
 * caching. {@link #findWebApplicationContext()} performs no caching of its own (a fresh
 * {@code ServletContext} attribute lookup every call), which is what actually makes a re-resolved
 * bean visible after a refresh.
 *
 * @since 3.0.0
 */
public class RefreshableDelegatingFilterProxy extends DelegatingFilterProxy {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
		WebApplicationContext webApplicationContext = findWebApplicationContext();
		if (webApplicationContext == null) {
			throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
		}

		Filter delegate = webApplicationContext.getBean(getTargetBeanName(), Filter.class);
		invokeDelegate(delegate, request, response, filterChain);
	}
}
