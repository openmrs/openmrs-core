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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests the behavior of {@link OpenmrsFilter}, particularly its handling of username and locale
 * session attributes for anonymous and authenticated requests.
 *
 * @see OpenmrsFilter
 */
public class OpenmrsFilterTest extends BaseWebContextSensitiveTest {

	protected static final String FILTER_INITIAL_DATA_XML = "OpenmrsFilterTest-initial.xml";

	private static final String USERNAME_ATTRIBUTE = "username";

	private static final String LOCALE_ATTRIBUTE = "locale";

	private static final String ANONYMOUS_USERNAME = "-anonymous user-";

	@AfterEach
	public void logout() {
		Context.logout();
	}

	/**
	 * @see OpenmrsFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
	 */
	@Test
	public void doFilterInternal_shouldSetAnonymousUsernameAndLocale() throws Exception {
		// Create a session and request for an anonymous user.
		RecordingHttpSession session = new RecordingHttpSession();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		// Process the request through the filter.
		OpenmrsFilter filter = new OpenmrsFilter();
		filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

		UserContext userContext = (UserContext) session.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);

		// Verify that the expected username and locale are stored in the session.
		assertThat(session.getAttribute(USERNAME_ATTRIBUTE), is(ANONYMOUS_USERNAME));
		assertThat(session.getAttribute(LOCALE_ATTRIBUTE), is(userContext.getLocale()));

		// Verify that each session attribute was written once for the initial request.
		assertEquals(1, session.countWrites(USERNAME_ATTRIBUTE));
		assertEquals(1, session.countWrites(LOCALE_ATTRIBUTE));
	}

	/**
	 * @see OpenmrsFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
	 */
	@Test
	public void doFilterInternal_shouldNotRewriteUnchangedAnonymousSessionAttributes() throws Exception {
		// Create a session and request for an anonymous user.
		RecordingHttpSession session = new RecordingHttpSession();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		// Process the initial request to populate the session attributes.
		OpenmrsFilter filter = new OpenmrsFilter();
		filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

		// Clear the initial writes so only the writes from the second request are measured.
		session.clearWrites();

		// Process a second request using the same session and unchanged values.
		MockHttpServletRequest secondRequest = new MockHttpServletRequest();
		secondRequest.setSession(session);
		filter.doFilterInternal(secondRequest, new MockHttpServletResponse(), mock(FilterChain.class));

		UserContext userContext = (UserContext) session.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);

		// Verify that the session attributes retain their expected values.
		assertThat(session.getAttribute(USERNAME_ATTRIBUTE), is(ANONYMOUS_USERNAME));
		assertThat(session.getAttribute(LOCALE_ATTRIBUTE), is(userContext.getLocale()));

		// Verify that unchanged session attributes were not written again.
		assertEquals(0, session.countWrites(USERNAME_ATTRIBUTE));
		assertEquals(0, session.countWrites(LOCALE_ATTRIBUTE));
	}

	/**
	 * @see OpenmrsFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
	 */
	@Test
	public void doFilterInternal_shouldSetAuthenticatedUsernameAndLocale() throws Exception {
		// Load the test user and authenticate the current context.
		executeDataSet(FILTER_INITIAL_DATA_XML);
		Context.authenticate(getTestUserCredentials());

		UserContext userContext = Context.getUserContext();

		// Create a session containing the authenticated UserContext.
		RecordingHttpSession session = new RecordingHttpSession();
		session.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		// Process the authenticated request through the filter.
		OpenmrsFilter filter = new OpenmrsFilter();
		filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

		// Verify that the authenticated username and locale are stored in the session.
		assertThat(session.getAttribute(USERNAME_ATTRIBUTE), is("test_user"));
		assertThat(session.getAttribute(LOCALE_ATTRIBUTE), is(userContext.getLocale()));

		// Verify that the authenticated username is written directly, without an intermediate anonymous value.
		assertEquals(1, session.countWrites(USERNAME_ATTRIBUTE));
		assertEquals(1, session.countWrites(LOCALE_ATTRIBUTE));
	}

	/**
	 * @see OpenmrsFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
	 */
	@Test
	public void doFilterInternal_shouldNotRewriteUnchangedAuthenticatedSessionAttributes() throws Exception {
		// Load the test user and authenticate the current context.
		executeDataSet(FILTER_INITIAL_DATA_XML);
		Context.authenticate(getTestUserCredentials());

		UserContext userContext = Context.getUserContext();

		// Create a session containing the authenticated UserContext.
		RecordingHttpSession session = new RecordingHttpSession();
		session.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		// Process the initial authenticated request to populate the session attributes.
		OpenmrsFilter filter = new OpenmrsFilter();
		filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

		// Clear the initial writes so only the writes from the second request are measured.
		session.clearWrites();

		// Process a second request using the same session and unchanged values.
		MockHttpServletRequest secondRequest = new MockHttpServletRequest();
		secondRequest.setSession(session);
		filter.doFilterInternal(secondRequest, new MockHttpServletResponse(), mock(FilterChain.class));

		// Verify that the session attributes retain their expected values.
		assertThat(session.getAttribute(USERNAME_ATTRIBUTE), is("test_user"));
		assertThat(session.getAttribute(LOCALE_ATTRIBUTE), is(userContext.getLocale()));

		// Verify that unchanged session attributes were not written again.
		assertEquals(0, session.countWrites(USERNAME_ATTRIBUTE));
		assertEquals(0, session.countWrites(LOCALE_ATTRIBUTE));
	}

	/**
	 * @see OpenmrsFilter#doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
	 */
	@Test
	public void doFilterInternal_shouldUpdateChangedSessionAttributes() throws Exception {
		// Create a session with values that differ from those the filter should set.
		RecordingHttpSession session = new RecordingHttpSession();
		session.setAttribute(USERNAME_ATTRIBUTE, "old_username");
		session.setAttribute(LOCALE_ATTRIBUTE, Locale.ENGLISH);

		// Clear the setup writes so only writes performed by the filter are counted.
		session.clearWrites();

		// Create a request using the prepared session.
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(session);

		// Process the request through the filter.
		OpenmrsFilter filter = new OpenmrsFilter();
		filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

		UserContext userContext = (UserContext) session.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);

		// Verify that the changed session attributes now contain the expected values.
		assertThat(session.getAttribute(USERNAME_ATTRIBUTE), is(ANONYMOUS_USERNAME));
		assertThat(session.getAttribute(LOCALE_ATTRIBUTE), is(userContext.getLocale()));

		// Verify that each changed attribute was written exactly once.
		assertEquals(1, session.countWrites(USERNAME_ATTRIBUTE));
		assertEquals(1, session.countWrites(LOCALE_ATTRIBUTE));
	}

	/**
	 * Gets the credentials of the test_user to be authenticated.
	 *
	 * @return test_user credentials
	 */
	private Credentials getTestUserCredentials() {
		return new UsernamePasswordCredentials("test_user", "test");
	}

	private static class RecordingHttpSession extends MockHttpSession {

		private final List<String> attributeWrites = new ArrayList<>();

		@Override
		public void setAttribute(@NonNull String name, Object value) {
			attributeWrites.add(name);
			super.setAttribute(name, value);
		}

		private long countWrites(String attributeName) {
			return attributeWrites.stream().filter(attributeName::equals).count();
		}

		private void clearWrites() {
			attributeWrites.clear();
		}
	}
}
