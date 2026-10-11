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

import org.openmrs.web.security.RefreshableDelegatingFilterProxy;
import org.openmrs.web.session.DistributedHttpSessionCondition;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * An opt-in variant of {@link RefreshableDelegatingFilterProxy} for the seat that only exists when
 * {@code session.distributed=true}: {@code web.xml} declares the filter unconditionally, while the
 * backing bean ({@code springSessionRepositoryFilter}) is contributed by
 * {@link org.openmrs.web.session.DistributedHttpSessionConfig} only when the feature is on.
 * <p>
 * Keyed on the same flag the config uses: feature off - pass-through (the bean is legitimately
 * absent); feature on - delegate, refresh-aware like the parent; feature on but the bean
 * unavailable (mid-refresh, startup) - the parent answers {@code 503}, because running the request
 * without its Spring Session wrapper would let a container session replace the browser's
 * distributed-session cookie and log the user out.
 *
 * @since 3.0.0
 */
public class OptionalDelegatingFilterProxy extends RefreshableDelegatingFilterProxy {

	/**
	 * The inherited init resolves the target bean eagerly and fails startup when it is absent; that
	 * resolution failure is expected with the feature off. Any other failure is still propagated.
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
		if (!DistributedHttpSessionCondition.isEnabled()) {
			// the feature is off: the bean is legitimately absent, so the seat is a pass-through
			filterChain.doFilter(request, response);
			return;
		}
		// feature on: delegate - the parent answers 503 while the bean is unavailable
		super.doFilter(request, response, filterChain);
	}
}
