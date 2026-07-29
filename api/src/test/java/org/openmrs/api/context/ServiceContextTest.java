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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

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
import org.openmrs.api.ServiceNotFoundException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DatabaseUpdateException;
import org.openmrs.util.InputRequiredException;

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
	public void getService_shouldThrowServiceNotFoundExceptionForAnUnregisteredService() {
		assertFalse(serviceContext.isRefreshingContext());

		assertThrows(ServiceNotFoundException.class, () -> serviceContext.getService(UnregisteredService.class));
	}

	/**
	 * @see ServiceContext#getService(Class)
	 */
	@Test
	public void getService_shouldBlockUntilAnInProgressRefreshCompletes() throws Exception {
		AtomicReference<Object> retrievedService = new AtomicReference<>();
		AtomicReference<Throwable> thrown = new AtomicReference<>();
		CountDownLatch lookupComplete = new CountDownLatch(1);

		serviceContext.startRefreshingContext();

		Thread lookup = new Thread(() -> {
			try {
				retrievedService.set(serviceContext.getService(PatientService.class));
			} catch (Throwable t) {
				thrown.set(t);
			} finally {
				lookupComplete.countDown();
			}
		});
		lookup.start();

		try {
			// the lookup must not complete while the refresh is in progress; a broken (non-blocking)
			// implementation would return the service and count the latch down almost immediately
			assertFalse(lookupComplete.await(500, TimeUnit.MILLISECONDS));
		} finally {
			serviceContext.doneRefreshingContext();
		}

		// once the refresh finishes, the blocked lookup should complete
		assertTrue(lookupComplete.await(5, TimeUnit.SECONDS));
		assertNull(thrown.get());
		assertNotNull(retrievedService.get());
	}

	/**
	 * @see ServiceContext#setService(Class, Object)
	 */
	@Test
	public void setService_shouldMakeRegistrationsVisibleToOtherThreads() throws Exception {
		AtomicReference<Object> retrievedService = new AtomicReference<>();
		AtomicReference<Throwable> thrown = new AtomicReference<>();
		CountDownLatch readerRunning = new CountDownLatch(1);

		Thread reader = new Thread(() -> {
			readerRunning.countDown();
			long deadline = System.currentTimeMillis() + 5000;
			while (System.currentTimeMillis() < deadline) {
				try {
					retrievedService.set(serviceContext.getService(RegistryVisibilityService.class));
					return;
				} catch (ServiceNotFoundException ignored) {
					// not registered yet; keep racing the write
				} catch (Throwable t) {
					thrown.set(t);
					return;
				}
			}
		});
		reader.start();

		assertTrue(readerRunning.await(5, TimeUnit.SECONDS));
		serviceContext.setService(RegistryVisibilityService.class, new RegistryVisibilityServiceImpl());

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

	/**
	 * Service interface that is never registered, used to exercise the not-found path of getService.
	 */
	public interface UnregisteredService {}
}
