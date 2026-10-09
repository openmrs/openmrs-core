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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * A {@link DelegatingFilterProxy} that becomes a transparent pass-through when its target bean is
 * absent, so an opt-in filter can be declared unconditionally in {@code web.xml} while the backing
 * bean ({@code springSessionRepositoryFilter}) is only created when
 * {@code session.distributed=true}.
 * <p>
 * It also re-resolves its target after the root {@link WebApplicationContext} is refreshed, the way
 * {@link org.openmrs.web.security.RefreshableDelegatingFilterProxy} does: starting or stopping a
 * module refreshes that context in place ({@code WebModuleUtil.refreshWAC}), so a stock proxy would
 * keep running the pre-refresh delegate for the rest of the webapp's life. It is a separate class
 * rather than a reuse of {@code RefreshableDelegatingFilterProxy} because this seat is legitimately
 * <em>absent</em> whenever the feature is off, whereas that class requires its target bean to exist
 * and answers {@code 503} when it cannot resolve one.
 *
 * @since 3.0.0
 */
public class OptionalDelegatingFilterProxy extends DelegatingFilterProxy {

	private volatile Long lastContextStartup;

	private volatile Boolean targetBeanPresent;

	/**
	 * The container calls {@code init} before any request is filtered, and the inherited implementation
	 * resolves the target bean eagerly, failing startup when it is absent. Since the target only exists
	 * when {@code session.distributed=true}, that resolution failure is expected and leaves this filter
	 * as a pass-through.
	 * <p>
	 * Any other failure (a target bean that exists but is not a {@code Filter}, or one that fails to
	 * start) is still propagated, because it is a genuine misconfiguration rather than the opt-in
	 * feature simply being off.
	 */
	@Override
	protected void initFilterBean() throws ServletException {
		try {
			super.initFilterBean();
		} catch (NoSuchBeanDefinitionException e) {
			logger.debug("Filter '" + getFilterName() + "' has no target bean, so it is a pass-through");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
		if (isTargetBeanPresent()) {
			super.doFilter(request, response, filterChain);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	/**
	 * Cached presence check with context refresh detection. When OpenMRS refreshes the web application
	 * context (e.g. module start/stop), a previously present bean may be destroyed or replaced; we must
	 * re-resolve in that case.
	 */
	private boolean isTargetBeanPresent() {
		WebApplicationContext wac;
		try {
			wac = findWebApplicationContext();
		} catch (IllegalStateException e) {
			// No ServletContext yet (the filter has not been initialized); nothing to delegate to.
			logger.debug("Filter '" + getFilterName() + "' is not initialized yet, so it is a pass-through", e);
			return false;
		}
		long currentStartup = wac == null ? 0L : wac.getStartupDate();
		if (lastContextStartup == null || lastContextStartup.longValue() != currentStartup) {
			targetBeanPresent = null;
			lastContextStartup = Long.valueOf(currentStartup);
		}
		Boolean cached = targetBeanPresent;
		if (cached == null) {
			String targetBeanName = getTargetBeanName();
			if (targetBeanName == null || wac == null) {
				cached = false;
			} else {
				try {
					cached = wac.containsBean(targetBeanName);
				} catch (RuntimeException e) {
					// Mid-refresh the context is closed; treat the delegate as absent and pass through rather
					// than failing the request.
					logger.debug("Could not resolve target bean '" + targetBeanName + "' during a context refresh", e);
					cached = false;
				}
			}
			targetBeanPresent = cached;
		}
		return cached;
	}


}
