/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.validator.DrugOrderValidator;
import org.openmrs.validator.OrderValidator;
import org.openmrs.validator.PatientValidator;
import org.openmrs.validator.PersonValidator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.validation.Validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the methods in {@link HandlerUtil}
 */
public class HandlerUtilTest extends BaseContextSensitiveTest {

	/**
	 * The handler cache is static and outlives garbage collection, so anything this class leaves in it
	 * would be served to the rest of the fork.
	 */
	@AfterEach
	public void clearHandlerCache() {
		HandlerUtil.clearCachedHandlers();
	}

	/**
	 * @see HandlerUtil#getHandlerForType(Class, Class)
	 */
	@Test
	public void getHandlersForType_shouldReturnAListOfAllClassesThatCanHandleThePassedType() {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		assertEquals(1, l.size());
		assertEquals(OrderValidator.class, l.iterator().next().getClass());
		l = HandlerUtil.getHandlersForType(Validator.class, DrugOrder.class);
		assertEquals(2, l.size());
	}

	/**
	 * @see HandlerUtil#getHandlerForType(Class, Class)
	 */
	@Test
	public void getHandlersForType_shouldReturnAnEmptyListIfNoClassesCanHandleThePassedType() {
		List<Validator> l = HandlerUtil.getHandlersForType(Validator.class, PatientValidator.class);
		assertNotNull(l);
		assertEquals(0, l.size());
	}

	/**
	 * @see HandlerUtil#getPreferredHandler(Class, Class)
	 */
	@Test
	public void getPreferredHandler_shouldReturnThePreferredHandlerForThePassedHandlerAndType() {
		Validator v = HandlerUtil.getPreferredHandler(Validator.class, DrugOrder.class);
		assertEquals(DrugOrderValidator.class, v.getClass());
	}

	/**
	 * @see HandlerUtil#getPreferredHandler(Class, Class)
	 */
	@Test
	public void getPreferredHandler_shouldThrowAAPIExceptionExceptionIfNoHandlerIsFound() {

		APIException exception = assertThrows(APIException.class,
		    () -> HandlerUtil.getPreferredHandler(Validator.class, Integer.class));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("handler.type.not.found",
		    new Object[] { Validator.class.toString(), Integer.class }, null)));
	}

	@Test
	public void getPreferredHandler_shouldReturnPatientValidatorForPatient() {
		Validator handler = HandlerUtil.getPreferredHandler(Validator.class, Patient.class);

		assertThat(handler, is(instanceOf(PatientValidator.class)));
	}

	@Test
	public void getPreferredHandler_shouldReturnPersonValidatorForPerson() {
		Validator handler = HandlerUtil.getPreferredHandler(Validator.class, Person.class);

		assertThat(handler, is(instanceOf(PersonValidator.class)));
	}

	@Test
	public void getHandlersForType_shouldServeRepeatedLookupsFromTheCache() {
		HandlerUtil.clearCachedHandlers();

		List<Validator> first = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		List<Validator> second = HandlerUtil.getHandlersForType(Validator.class, Order.class);

		assertSame(first, second);
	}

	@Test
	public void getHandlersForType_shouldKeepCachedHandlersAcrossGarbageCollection() {
		HandlerUtil.clearCachedHandlers();
		List<Validator> cached = HandlerUtil.getHandlersForType(Validator.class, Order.class);

		// TRUNK-6697: the cache keys are built inside the lookup and are reachable from nothing but the
		// cache, so weakly held entries are collectible here. System.gc() is only a hint, so this can
		// pass without proving anything - it cannot, however, pass on an entry that was collected
		System.gc();

		assertSame(cached, HandlerUtil.getHandlersForType(Validator.class, Order.class));
	}

	@Test
	public void getHandlersForType_shouldCacheHandlersForANullTypeSeparately() {
		HandlerUtil.clearCachedHandlers();

		List<Validator> anyType = HandlerUtil.getHandlersForType(Validator.class, null);
		List<Validator> orderOnly = HandlerUtil.getHandlersForType(Validator.class, Order.class);

		assertNotSame(anyType, orderOnly);
		assertThat(anyType.size(), greaterThan(orderOnly.size()));
		assertSame(anyType, HandlerUtil.getHandlersForType(Validator.class, null));
	}

	@Test
	public void getHandlersForType_shouldReturnAnUnmodifiableList() {
		HandlerUtil.clearCachedHandlers();

		List<Validator> handlers = HandlerUtil.getHandlersForType(Validator.class, Order.class);

		assertThrows(UnsupportedOperationException.class, () -> handlers.add(null));
	}

	@Test
	public void getHandlersForType_shouldNotPublishHandlersScannedBeforeAnInvalidation() {
		ServiceContext serviceContext = ServiceContext.getInstance();
		ApplicationContext realContext = serviceContext.getApplicationContext();
		ApplicationContext contextRefreshingMidScan = mock(ApplicationContext.class);
		when(contextRefreshingMidScan.getBeansOfType(Validator.class)).thenAnswer(invocation -> {
			// a module starting, and so a context refresh, while the scan is still running
			HandlerUtil.clearCachedHandlers();
			return realContext.getBeansOfType(Validator.class);
		});

		List<Validator> scannedBeforeInvalidation;
		try {
			serviceContext.setApplicationContext(contextRefreshingMidScan);
			HandlerUtil.clearCachedHandlers();
			scannedBeforeInvalidation = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		} finally {
			serviceContext.setApplicationContext(realContext);
		}

		// the invalidated lookup must have written into the map it read from, not into the live cache
		assertNotSame(scannedBeforeInvalidation, HandlerUtil.getHandlersForType(Validator.class, Order.class));
	}

	@Test
	public void getHandlersForType_shouldReturnTheSameHandlerListToConcurrentCallers() throws Exception {
		HandlerUtil.clearCachedHandlers();

		int callers = 8;
		ExecutorService executor = Executors.newFixedThreadPool(callers);
		try {
			// release every caller at once so they race on the empty cache. They call Context from pool
			// threads, which works because looking up registered components needs no user context
			CountDownLatch startLine = new CountDownLatch(1);
			List<Future<List<Validator>>> results = new ArrayList<>(callers);
			for (int i = 0; i < callers; i++) {
				results.add(executor.submit(() -> {
					startLine.await();
					return HandlerUtil.getHandlersForType(Validator.class, DrugOrder.class);
				}));
			}
			startLine.countDown();

			// whoever won the race, the losers are handed the winner's list rather than their own copy,
			// so all eight callers and every later lookup share one instance
			List<Validator> expected = null;
			for (Future<List<Validator>> result : results) {
				List<Validator> handlers = result.get(30, TimeUnit.SECONDS);
				assertEquals(2, handlers.size());
				if (expected == null) {
					expected = handlers;
				} else {
					assertSame(expected, handlers);
				}
			}

			assertSame(expected, HandlerUtil.getHandlersForType(Validator.class, DrugOrder.class));
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void clearCachedHandlers_shouldForceHandlersToBeLookedUpAgain() {
		List<Validator> before = HandlerUtil.getHandlersForType(Validator.class, Order.class);

		HandlerUtil.clearCachedHandlers();

		List<Validator> after = HandlerUtil.getHandlersForType(Validator.class, Order.class);
		assertNotSame(before, after);
		assertEquals(before, after);
	}

	/**
	 * Calling onApplicationEvent directly would prove only that the method body clears the cache. What
	 * has to hold is that Spring delivers the event at all, which it decides from the type argument of
	 * the ApplicationListener the bean implements.
	 */
	@Test
	public void onApplicationEvent_shouldBeDeliveredBySpringOnRefreshButNotOnClose() {
		GenericApplicationContext listenerContext = new GenericApplicationContext();
		listenerContext.registerBeanDefinition("handlerUtil", new RootBeanDefinition(HandlerUtil.class));
		try {
			List<Validator> beforeRefresh = HandlerUtil.getHandlersForType(Validator.class, Order.class);

			listenerContext.refresh();

			List<Validator> afterRefresh = HandlerUtil.getHandlersForType(Validator.class, Order.class);
			assertNotSame(beforeRefresh, afterRefresh);

			// closing must leave the cache alone: between a close and the refresh that follows it there
			// is no application context to rescan, so a miss there would fail rather than serve handlers
			listenerContext.close();

			assertSame(afterRefresh, HandlerUtil.getHandlersForType(Validator.class, Order.class));
		} finally {
			listenerContext.close();
		}
	}
}
