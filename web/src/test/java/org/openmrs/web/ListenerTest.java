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

import java.util.Properties;

import jakarta.servlet.http.HttpSessionEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListenerTest {

	private static final int DEFAULT_INTERVAL = 1800;

	@AfterEach
	public void resetSessionTimeout() {
		Listener.loadSessionTimeout(null);
	}

	private int createSessionWithTimeout(String timeoutValue) {
		Properties props = new Properties();
		if (timeoutValue != null) {
			props.setProperty(OpenmrsConstants.SESSION_TIMEOUT_RUNTIME_PROPERTY, timeoutValue);
		}
		Listener.loadSessionTimeout(props);

		MockHttpSession session = new MockHttpSession();
		session.setMaxInactiveInterval(DEFAULT_INTERVAL);
		new Listener().sessionCreated(new HttpSessionEvent(session));
		return session.getMaxInactiveInterval();
	}

	@Test
	public void sessionCreated_shouldApplyTheSessionTimeoutRuntimeProperty() {
		assertEquals(90 * 60, createSessionWithTimeout("90"));
	}

	@Test
	public void sessionCreated_shouldTrimTheSessionTimeoutRuntimeProperty() {
		assertEquals(45 * 60, createSessionWithTimeout(" 45 "));
	}

	@Test
	public void sessionCreated_shouldKeepTheContainerDefaultWhenThePropertyIsNotSet() {
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(null));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(""));
	}

	@Test
	public void sessionCreated_shouldKeepTheContainerDefaultWhenThePropertyIsInvalid() {
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("ninety"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("0"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout("-15"));
		assertEquals(DEFAULT_INTERVAL, createSessionWithTimeout(String.valueOf(Integer.MAX_VALUE)));
	}

	@Test
	public void sessionCreated_shouldKeepTheContainerDefaultWhenThereAreNoRuntimeProperties() {
		createSessionWithTimeout("90");
		Listener.loadSessionTimeout(null);

		MockHttpSession session = new MockHttpSession();
		session.setMaxInactiveInterval(DEFAULT_INTERVAL);
		new Listener().sessionCreated(new HttpSessionEvent(session));
		assertEquals(DEFAULT_INTERVAL, session.getMaxInactiveInterval());
	}
}
