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

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.web.WebConstants;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenmrsFilterTest {

	@Test
	void shouldNotRewriteUnchangedSessionAttributesForAnonymousUser() throws Exception {
		// arrange
		UserContext userContext = new UserContext(Context.getAuthenticationScheme());
		Locale locale = userContext.getLocale();

		MockHttpSession originalSession = new MockHttpSession();
		originalSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		originalSession.setAttribute("username", "-anonymous user-");
		originalSession.setAttribute("locale", locale);

		MockHttpSession session = spy(originalSession);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		OpenmrsFilter filter = new OpenmrsFilter();

		// act
		filter.doFilterInternal(request, response, chain);

		// assert
		verify(session, never()).setAttribute("username", "-anonymous user-");
		verify(session, never()).setAttribute("locale", locale);
	}

	@Test
	void shouldUpdateUsernameWhenAuthenticatedUserChanges() throws Exception {
		UserContext userContext = mock(UserContext.class);
		User user = new User();
		user.setUsername("admin");

		when(userContext.getAuthenticatedUser()).thenReturn(user);
		when(userContext.getLocale()).thenReturn(Locale.ENGLISH);

		MockHttpSession session = new MockHttpSession();
		session.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		session.setAttribute("username", "-anonymous user-");
		session.setAttribute("locale", Locale.ENGLISH);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		MockHttpServletResponse response = new MockHttpServletResponse();

		new OpenmrsFilter().doFilter(request, response, new MockFilterChain());

		assertEquals("admin", session.getAttribute("username"));
	}

	@Test
	void shouldNotUpdateUsernameWhenUsernameHasNotChanged() throws Exception {
		UserContext userContext = mock(UserContext.class);
		User user = new User();
		user.setUsername("admin");

		when(userContext.getAuthenticatedUser()).thenReturn(user);
		when(userContext.getLocale()).thenReturn(Locale.ENGLISH);

		MockHttpSession originalSession = new MockHttpSession();
		originalSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		originalSession.setAttribute("username", "admin");
		originalSession.setAttribute("locale", Locale.ENGLISH);

		MockHttpSession session = spy(originalSession);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		MockHttpServletResponse response = new MockHttpServletResponse();

		new OpenmrsFilter().doFilter(request, response, new MockFilterChain());

		verify(session, never()).setAttribute("username", "admin");
	}

	@Test
	void shouldUpdateLocaleWhenLocaleChanges() throws Exception {
		UserContext userContext = mock(UserContext.class);

		when(userContext.getLocale()).thenReturn(Locale.ENGLISH);

		MockHttpSession session = new MockHttpSession();
		session.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		session.setAttribute("locale", Locale.FRENCH);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		MockHttpServletResponse response = new MockHttpServletResponse();

		new OpenmrsFilter().doFilter(request, response, new MockFilterChain());

		assertEquals(Locale.ENGLISH, session.getAttribute("locale"));
	}
}
