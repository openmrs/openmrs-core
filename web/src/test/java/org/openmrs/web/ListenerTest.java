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

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpSessionListener;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

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
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			contextMock.when(() -> Context.getRegisteredComponents(HttpSessionListener.class))
			        .thenReturn(Collections.singletonList(Mockito.mock(HttpSessionListener.class)));

			Listener.setOpenmrsStarted(true);

			List<HttpSessionListener> first = listener.getHttpSessionListeners();
			List<HttpSessionListener> second = listener.getHttpSessionListeners();

			assertSame(first, second);

			contextMock.verify(() -> Context.getRegisteredComponents(HttpSessionListener.class), times(1));
		} finally {
			Listener.setOpenmrsStarted(false);
		}
	}

	/**
	 * @see Listener#getHttpSessionListeners()
	 */
	@Test
	public void getHttpSessionListeners_shouldCacheEmptyListeners() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			contextMock.when(() -> Context.getRegisteredComponents(HttpSessionListener.class))
			        .thenReturn(Collections.emptyList());

			Listener.setOpenmrsStarted(true);

			List<HttpSessionListener> first = listener.getHttpSessionListeners();
			List<HttpSessionListener> second = listener.getHttpSessionListeners();

			assertTrue(first.isEmpty());
			assertSame(first, second);

			contextMock.verify(() -> Context.getRegisteredComponents(HttpSessionListener.class), times(1));
		} finally {
			Listener.setOpenmrsStarted(false);
		}
	}

	/**
	 * @see RegisteredHttpSessionListenerCache#onApplicationEvent(ContextRefreshedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldClearHttpSessionListeners() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			contextMock.when(() -> Context.getRegisteredComponents(HttpSessionListener.class))
			        .thenReturn(Collections.emptyList());

			Listener.setOpenmrsStarted(true);

			listener.getHttpSessionListeners();

			RegisteredHttpSessionListenerCache cache = applicationContext.getBean(RegisteredHttpSessionListenerCache.class);
			cache.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

			listener.getHttpSessionListeners();

			// The refresh invalidates the cached listeners and causes a new lookup.
			contextMock.verify(() -> Context.getRegisteredComponents(HttpSessionListener.class), times(2));
		} finally {
			Listener.setOpenmrsStarted(false);
		}
	}

	/**
	 * @see RegisteredHttpSessionListenerCache#onApplicationEvent(ContextRefreshedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldInvalidateHttpSessionListenerCache() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			contextMock.when(() -> Context.getRegisteredComponents(HttpSessionListener.class))
			        .thenReturn(Collections.emptyList());

			Listener.setOpenmrsStarted(true);

			List<HttpSessionListener> first = listener.getHttpSessionListeners();

			RegisteredHttpSessionListenerCache cache = applicationContext.getBean(RegisteredHttpSessionListenerCache.class);
			cache.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

			List<HttpSessionListener> second = listener.getHttpSessionListeners();

			// Clearing the cache should cause the next lookup to return a new list.
			assertNotSame(first, second);
		} finally {
			Listener.setOpenmrsStarted(false);
		}
	}

}
