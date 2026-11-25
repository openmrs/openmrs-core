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

import jakarta.servlet.http.HttpSessionListener;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionTimeoutHttpSessionListenerContextTest extends BaseWebContextSensitiveTest {

	private static final String PROPERTY = OpenmrsConstants.SESSION_TIMEOUT_PROPERTY;

	private Properties originalRuntimeProperties;

	private String originalSystemProperty;

	@BeforeEach
	public void setUp() {
		originalRuntimeProperties = Context.getRuntimeProperties();
		originalSystemProperty = System.clearProperty(PROPERTY);
	}

	@AfterEach
	public void tearDown() {
		SessionTimeoutHttpSessionListener.setSessionTimeout(null);
		if (originalSystemProperty == null) {
			System.clearProperty(PROPERTY);
		} else {
			System.setProperty(PROPERTY, originalSystemProperty);
		}
		Context.setRuntimeProperties(originalRuntimeProperties);
	}

	private void setRuntimeProperty(String value) {
		Properties props = Context.getRuntimeProperties();
		props.setProperty(PROPERTY, value);
		Context.setRuntimeProperties(props);
	}

	@Test
	public void loadSessionTimeout_shouldReadTheGlobalProperty() {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(PROPERTY, "75"));

		SessionTimeoutHttpSessionListener.loadSessionTimeout();

		assertEquals(75 * 60, SessionTimeoutHttpSessionListenerTest.createSession());
	}

	@Test
	public void loadSessionTimeout_shouldPreferARuntimePropertyOverTheGlobalProperty() {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(PROPERTY, "75"));
		setRuntimeProperty("60");

		SessionTimeoutHttpSessionListener.loadSessionTimeout();

		assertEquals(60 * 60, SessionTimeoutHttpSessionListenerTest.createSession());
	}

	@Test
	public void loadSessionTimeout_shouldPreferASystemPropertyOverTheRuntimeProperty() {
		setRuntimeProperty("60");
		System.setProperty(PROPERTY, "30");

		SessionTimeoutHttpSessionListener.loadSessionTimeout();

		assertEquals(30 * 60, SessionTimeoutHttpSessionListenerTest.createSession());
	}

	@Test
	public void loadSessionTimeout_shouldKeepTheContainerDefaultWhenNotSet() {
		SessionTimeoutHttpSessionListener.setSessionTimeout("90");

		SessionTimeoutHttpSessionListener.loadSessionTimeout();

		assertEquals(SessionTimeoutHttpSessionListenerTest.DEFAULT_INTERVAL,
		    SessionTimeoutHttpSessionListenerTest.createSession());
	}

	@Test
	public void shouldBeRegisteredAsAnHttpSessionListener() {
		assertTrue(Context.getRegisteredComponents(HttpSessionListener.class).stream()
		        .anyMatch(l -> l instanceof SessionTimeoutHttpSessionListener));
	}
}
