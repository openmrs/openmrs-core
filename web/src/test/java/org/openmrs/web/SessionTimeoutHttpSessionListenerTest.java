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

import jakarta.servlet.http.HttpSessionEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SessionTimeoutHttpSessionListenerTest {

	static final int DEFAULT_INTERVAL = 1800;

	@AfterEach
	public void reset() {
		SessionTimeoutHttpSessionListener.setSessionTimeout(null);
	}

	static int createSession() {
		MockHttpSession session = new MockHttpSession();
		session.setMaxInactiveInterval(DEFAULT_INTERVAL);
		new SessionTimeoutHttpSessionListener().sessionCreated(new HttpSessionEvent(session));
		return session.getMaxInactiveInterval();
	}

	private int createSessionWithTimeout(String value) {
		SessionTimeoutHttpSessionListener.setSessionTimeout(value);
		return createSession();
	}

	@Test
	public void sessionCreated_shouldApplyTheSessionTimeout() {
		assertEquals(90 * 60, createSessionWithTimeout("90"));
	}

	@Test
	public void sessionCreated_shouldTrimTheSessionTimeout() {
		assertEquals(45 * 60, createSessionWithTimeout(" 45 "));
	}

	@Test
	public void sessionCreated_shouldKeepTheContainerDefaultWhenTheTimeoutIsNotSet() {
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(null));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(""));
	}

	@Test
	public void sessionCreated_shouldKeepTheContainerDefaultWhenTheTimeoutIsInvalid() {
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("ninety"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("0"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("-15"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(String.valueOf(Integer.MAX_VALUE)));
	}

	@Test
	public void sessionCreated_shouldAcceptTheLargestTimeoutThatFitsInSeconds() {
		int max = Integer.MAX_VALUE / 60;
		assertEquals(max * 60, createSessionWithTimeout(String.valueOf(max)));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(String.valueOf(max + 1)));
	}

	@Test
	public void sessionCreated_shouldReplaceAValidTimeoutWithTheDefaultWhenGivenAnInvalidOne() {
		assertEquals(90 * 60, createSessionWithTimeout("90"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("ninety"));
	}

	@Test
	public void sessionCreated_shouldRevertToTheContainerDefaultWhenTheTimeoutIsCleared() {
		assertEquals(90 * 60, createSessionWithTimeout("90"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(null));
	}
}
