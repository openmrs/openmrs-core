/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class ServiceContextTest extends BaseContextSensitiveTest {

	private ServiceContext serviceContext;

	private ServiceContext spiedServiceContext;

	private boolean isUseSystemClassLoader;

	@BeforeEach
	public void setUp() throws InputRequiredException, DatabaseUpdateException {
		openMocks(this);
		serviceContext = Context.getServiceContext();
		spiedServiceContext = spy(serviceContext);
		isUseSystemClassLoader = serviceContext.isUseSystemClassLoader();
	}

	@AfterEach
	public void tearDown() {
		serviceContext.setUseSystemClassLoader(isUseSystemClassLoader);
	}

	@Test
	public void isInstantiated_shouldReturnTrueWhenSingletonExists() {
		assertTrue(ServiceContext.isInstantiated());
	}

	@Test
	public void getModuleOpenmrsServices_shouldRaiseApiExceptionWithNonExistentClass() {
		List<Object> params = new ArrayList<>();
		params.add("org.openmrs.module.webservices.rest.nonexistent.InvalidClass");
		params.add(Object.class);

		spiedServiceContext.setUseSystemClassLoader(false);
		APIException thrownException = assertThrows(APIException.class, () -> spiedServiceContext.setModuleService(params));

		assertNotNull(thrownException.getMessage());
		assertNotNull(thrownException.getCause());

		verify(spiedServiceContext, never()).getMessageService();
		verify(spiedServiceContext, never()).getMessageSourceService();
	}

	@Test
	public void getModuleOpenmrsServices_shouldRaiseApiExceptionWithNullClass() {
		List<Object> params = new ArrayList<>();
		params.add(null);
		params.add(Object.class);

		spiedServiceContext.setUseSystemClassLoader(false);
		APIException thrownException = assertThrows(APIException.class, () -> spiedServiceContext.setModuleService(params));

		assertNotNull(thrownException.getMessage());
		assertNull(thrownException.getCause());

		verify(spiedServiceContext, never()).getMessageService();
		verify(spiedServiceContext, never()).getMessageSourceService();
	}

	@Test
	public void getModuleOpenmrsServices_shouldRaiseApiExceptionWithNullClassInstance() {
		List<Object> params = new ArrayList<>();
		params.add("org.openmrs.module.webservices.rest.nonexistent.InvalidClass");
		params.add(null);

		spiedServiceContext.setUseSystemClassLoader(false);
		APIException thrownException = assertThrows(APIException.class, () -> spiedServiceContext.setModuleService(params));

		assertNotNull(thrownException.getMessage());
		assertNull(thrownException.getCause());

		verify(spiedServiceContext, never()).getMessageService();
		verify(spiedServiceContext, never()).getMessageSourceService();
	}

	/**
	 * @see ServiceContext#getService(Class)
	 */
	@Test
	public void getService_shouldReturnServiceOnTheFastPathWhenNoRefreshIsInProgress() {
		assertFalse(serviceContext.isRefreshingContext());

		assertNotNull(serviceContext.getService(PatientService.class));
	}

	/**
	 * @see ServiceContext#getService(Class)
	 */
	@Test
	public void getService_shouldBlockUntilAnInProgressRefreshCompletes() throws Exception {
		AtomicReference<Object> retrievedService = new AtomicReference<>();
		AtomicReference<Throwable> thrown = new AtomicReference<>();
		CountDownLatch lookupStarted = new CountDownLatch(1);

		serviceContext.startRefreshingContext();

		Thread lookup = new Thread(() -> {
			lookupStarted.countDown();
			try {
				retrievedService.set(serviceContext.getService(PatientService.class));
			} catch (Throwable t) {
				thrown.set(t);
			}
		});
		lookup.start();

		try {
			// wait for the lookup thread to reach getService, then give it time to block
			assertTrue(lookupStarted.await(5, TimeUnit.SECONDS));
			Thread.sleep(500);

			// the lookup must not have returned while the refresh is in progress
			assertNull(retrievedService.get());
			assertTrue(lookup.isAlive());
		} finally {
			serviceContext.doneRefreshingContext();
		}

		// once the refresh finishes, the blocked lookup should complete
		lookup.join(5000);
		assertFalse(lookup.isAlive());
		assertNull(thrown.get());
		assertNotNull(retrievedService.get());
	}

	/**
	 * @see ServiceContext#setService(Class, Object)
	 */
	@Test
	public void setService_shouldMakeRegistrationsVisibleToOtherThreads() throws Exception {
		serviceContext.setService(RegistryVisibilityService.class, new RegistryVisibilityServiceImpl());

		AtomicReference<Object> retrievedService = new AtomicReference<>();
		AtomicReference<Throwable> thrown = new AtomicReference<>();

		Thread reader = new Thread(() -> {
			try {
				retrievedService.set(serviceContext.getService(RegistryVisibilityService.class));
			} catch (Throwable t) {
				thrown.set(t);
			}
		});
		reader.start();
		reader.join(5000);

		assertNull(thrown.get());
		assertNotNull(retrievedService.get());
		assertTrue(retrievedService.get() instanceof RegistryVisibilityService);
	}

	/**
	 * Simple service interface used to verify that registrations are safely published across threads.
	 */
	public interface RegistryVisibilityService {}

	public static class RegistryVisibilityServiceImpl implements RegistryVisibilityService {}
}
