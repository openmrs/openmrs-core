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

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.security.OpenmrsAuthenticationToken;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Proves {@link OpenmrsSecurityContextFilter} still correctly bridges the
 * {@link jakarta.servlet.http.HttpSession} to the current thread, now that (as of 3.0.0)
 * {@code Context.setUserContext(...)}/{@code Context.clearUserContext()} - the two methods this
 * filter calls - are backed by {@link SecurityContextHolder} instead of a bespoke
 * {@code ThreadLocal}. This filter is now registered as part of {@link WebSecurityConfig}'s
 * {@code SecurityFilterChain} rather than a standalone {@code web.xml} filter (superseding
 * {@code org.openmrs.web.filter.OpenmrsFilter}, which did the same job from outside it) - the
 * {@code authorizeHttpRequests(...)} stage further down that same chain needs a populated
 * {@code SecurityContext} to already be in place by the time it runs, which is why
 * {@link WebSecurityConfig} adds this filter ahead of it. No changes to this filter's own logic
 * were needed to move it, since it already delegated to
 * {@code Context}/{@code SecurityContextHolder} rather than a filter-local field.
 */
class OpenmrsSecurityContextFilterTest {

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilter_shouldPopulateSecurityContextHolderDuringTheChainAndClearItAfter() throws Exception {
		OpenmrsSecurityContextFilter filter = new OpenmrsSecurityContextFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		CapturingServlet capturingServlet = new CapturingServlet();
		MockFilterChain chain = new MockFilterChain(capturingServlet);

		filter.doFilter(request, response, chain);

		// during the chain, the thread's SecurityContext must be populated by the same
		// UserContext this filter attached to the session
		assertInstanceOf(OpenmrsAuthenticationToken.class, capturingServlet.authenticationDuringChain);

		// after the filter returns, the thread must be clean again (same finally-block guarantee
		// Context.clearUserContext() has always provided)
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	private static final class CapturingServlet extends GenericServlet {

		private Object authenticationDuringChain;

		@Override
		public void service(ServletRequest req, ServletResponse res) {
			authenticationDuringChain = SecurityContextHolder.getContext().getAuthentication();
		}
	}
}
