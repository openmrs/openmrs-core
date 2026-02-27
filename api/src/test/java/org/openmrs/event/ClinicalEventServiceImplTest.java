/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link ClinicalEventServiceImpl}.
 */
public class ClinicalEventServiceImplTest extends BaseContextSensitiveTest {
	
	@Autowired
	private ClinicalEventService clinicalEventService;
	
	@Test
	public void publishEvent_shouldNotifyRegisteredListener() {
		AtomicReference<ClinicalEvent> received = new AtomicReference<>();
		
		ClinicalEventListener listener = new ClinicalEventListener() {
			@Override
			public void onEvent(ClinicalEvent event) {
				received.set(event);
			}
			
			@Override
			public boolean supportsEntityType(Class<?> entityType) {
				return Obs.class.equals(entityType);
			}
		};
		
		clinicalEventService.registerListener(listener);
		try {
			Patient patient = new Patient(1);
			clinicalEventService.publishEvent(ClinicalEventType.CREATED, Obs.class, "test-uuid", patient);
			
			assertThat(received.get().getEventType(), is(ClinicalEventType.CREATED));
			assertThat(received.get().getEntityUuid(), is("test-uuid"));
			assertThat(received.get().getEntityType().equals(Obs.class), is(true));
		} finally {
			clinicalEventService.unregisterListener(listener);
		}
	}
	
	@Test
	public void publishEvent_shouldNotNotifyListenerForUnsupportedEntityType() {
		AtomicInteger callCount = new AtomicInteger(0);
		
		ClinicalEventListener listener = new ClinicalEventListener() {
			@Override
			public void onEvent(ClinicalEvent event) {
				callCount.incrementAndGet();
			}
			
			@Override
			public boolean supportsEntityType(Class<?> entityType) {
				return Obs.class.equals(entityType);
			}
		};
		
		clinicalEventService.registerListener(listener);
		try {
			Patient patient = new Patient(1);
			clinicalEventService.publishEvent(ClinicalEventType.CREATED, Encounter.class, "test-uuid", patient);
			
			assertThat(callCount.get(), is(0));
		} finally {
			clinicalEventService.unregisterListener(listener);
		}
	}
	
	@Test
	public void publishEvent_shouldContinueIfListenerThrowsException() {
		AtomicInteger callCount = new AtomicInteger(0);
		
		ClinicalEventListener failingListener = new ClinicalEventListener() {
			@Override
			public void onEvent(ClinicalEvent event) {
				throw new RuntimeException("Test failure");
			}
			
			@Override
			public boolean supportsEntityType(Class<?> entityType) {
				return true;
			}
		};
		
		ClinicalEventListener successListener = new ClinicalEventListener() {
			@Override
			public void onEvent(ClinicalEvent event) {
				callCount.incrementAndGet();
			}
			
			@Override
			public boolean supportsEntityType(Class<?> entityType) {
				return true;
			}
		};
		
		clinicalEventService.registerListener(failingListener);
		clinicalEventService.registerListener(successListener);
		try {
			Patient patient = new Patient(1);
			assertDoesNotThrow(() -> clinicalEventService.publishEvent(ClinicalEventType.CREATED, Obs.class, "test-uuid", patient));
			assertThat(callCount.get(), is(1));
		} finally {
			clinicalEventService.unregisterListener(failingListener);
			clinicalEventService.unregisterListener(successListener);
		}
	}
	
	@Test
	public void unregisterListener_shouldStopReceivingEvents() {
		AtomicInteger callCount = new AtomicInteger(0);
		
		ClinicalEventListener listener = new ClinicalEventListener() {
			@Override
			public void onEvent(ClinicalEvent event) {
				callCount.incrementAndGet();
			}
			
			@Override
			public boolean supportsEntityType(Class<?> entityType) {
				return true;
			}
		};
		
		clinicalEventService.registerListener(listener);
		
		Patient patient = new Patient(1);
		clinicalEventService.publishEvent(ClinicalEventType.CREATED, Obs.class, "test-uuid", patient);
		assertThat(callCount.get(), is(1));
		
		clinicalEventService.unregisterListener(listener);
		clinicalEventService.publishEvent(ClinicalEventType.UPDATED, Obs.class, "test-uuid-2", patient);
		assertThat(callCount.get(), is(1));
	}
}
