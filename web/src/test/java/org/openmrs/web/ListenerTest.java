/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of this file was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the default session timeout behavior driven by the "session.timeout" runtime property, as
 * requested in TRUNK-6868.
 */
public class ListenerTest {

	private static final String CONTAINER_DEFAULT_TIMEOUT = "30";

	private Properties runtimeProperties(String timeoutValue) {
		Properties props = new Properties();
		if (timeoutValue != null) {
			props.setProperty(OpenmrsConstants.SESSION_TIMEOUT_RUNTIME_PROPERTY, timeoutValue);
		}
		return props;
	}

	/**
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldApplyTheRuntimePropertyTimeoutToTheServletContext() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, runtimeProperties("90"));

		assertEquals(90, servletContext.getSessionTimeout());
	}

	/**
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldKeepTheContainerDefaultWhenThePropertyIsNotSet() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, runtimeProperties(null));
		assertEquals(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT), servletContext.getSessionTimeout());

		Listener.applySessionTimeout(servletContext, new Properties());
		assertEquals(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT), servletContext.getSessionTimeout());
	}

	/**
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldIgnoreAndKeepTheContainerDefaultOnANonNumericValue() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, runtimeProperties("ninety"));

		assertEquals(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT), servletContext.getSessionTimeout());
	}

	/**
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldIgnoreValuesThatAreNotAPositiveNumberOfMinutes() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, runtimeProperties("0"));

		assertEquals(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT), servletContext.getSessionTimeout());

		Listener.applySessionTimeout(servletContext, runtimeProperties("-15"));

		assertEquals(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT), servletContext.getSessionTimeout());
	}

	/**
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldTolerateASurroundingWhitespaceOnTheValue() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, runtimeProperties("  45 "));

		assertEquals(45, servletContext.getSessionTimeout());
	}

	/**
	 * On the very first boot the installation wizard builds the runtime properties from the
	 * installation settings after the servlet context was initialized, so the property reaches this
	 * helper only mid-flight, mixed among the install keys. It must still be applied.
	 *
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldApplyTheTimeoutForInstallScriptStyleProperties() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Properties installationProperties = new Properties();
		installationProperties.setProperty("connection.url", "jdbc:mysql://localhost:3306/openmrs?autoReconnect=true");
		installationProperties.setProperty("connection.username", "openmrs_user");
		installationProperties.setProperty("auto_update_database", "true");
		installationProperties.setProperty(OpenmrsConstants.SESSION_TIMEOUT_RUNTIME_PROPERTY, "90");

		Listener.applySessionTimeout(servletContext, installationProperties);

		assertEquals(90, servletContext.getSessionTimeout());
	}

	/**
	 * The timeout is applied at webapp startup and again when OpenMRS is started on the same context;
	 * the second, idempotent application must keep the value.
	 *
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldKeepTheSameTimeoutWhenAppliedTwiceOnTheSameContext() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, runtimeProperties("90"));
		Listener.applySessionTimeout(servletContext, runtimeProperties("90"));

		assertEquals(90, servletContext.getSessionTimeout());
	}

	/**
	 * @see Listener#applySessionTimeout
	 */
	@Test
	public void applySessionTimeout_shouldDoNothingWhenThereAreNoRuntimeProperties() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.setSessionTimeout(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT));

		Listener.applySessionTimeout(servletContext, null);

		assertEquals(Integer.parseInt(CONTAINER_DEFAULT_TIMEOUT), servletContext.getSessionTimeout());
	}
}
