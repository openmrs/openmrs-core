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

import java.util.List;

import jakarta.servlet.http.HttpSessionListener;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListenerTest extends BaseWebContextSensitiveTest {

	private Listener listener;

	@BeforeEach
	public void beforeEach() {
		listener = new Listener();
		Listener.clearHttpSessionListeners();
	}

	@AfterEach
	public void afterEach() {
		Listener.clearHttpSessionListeners();
	}

	/**
	 * @see Listener#getHttpSessionListeners()
	 */
	@Test
	public void getHttpSessionListeners_shouldCacheListeners() {
		List<HttpSessionListener> first = listener.getHttpSessionListeners();
		List<HttpSessionListener> second = listener.getHttpSessionListeners();

		assertSame(first, second);
	}

	/**
	 * @see Listener#getHttpSessionListeners()
	 */
	@Test
	public void getHttpSessionListeners_shouldCacheEmptyListeners() {
		List<HttpSessionListener> first = listener.getHttpSessionListeners();
		List<HttpSessionListener> second = listener.getHttpSessionListeners();

		assertTrue(first.isEmpty());
		assertSame(first, second);
	}

	/**
	 * @see RegisteredHttpSessionListenerCache#onApplicationEvent(ContextRefreshedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldClearHttpSessionListeners() {
		RegisteredHttpSessionListenerCache cache = applicationContext.getBean(RegisteredHttpSessionListenerCache.class);

		cache.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

		// The event handler should invalidate the Listener cache.
		assertNotNull(cache);
	}

}
