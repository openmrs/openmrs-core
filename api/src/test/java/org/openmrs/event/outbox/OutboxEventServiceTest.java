/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.outbox;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class OutboxEventServiceTest extends BaseContextSensitiveTest {

	@Autowired
	private SessionFactory sessionFactory;

	private OutboxEventService service;

	@BeforeEach
	public void setUp() {
		// We inject a tiny 1-second timeout to make testing stuck events much easier
		service = new OutboxEventService(sessionFactory, 1, 4);
	}

	private OutboxEvent createTestEvent(OutboxEvent.Status status) {
		OutboxEvent event = new OutboxEvent();
		event.setUuid(UUID.randomUUID().toString());
		event.setEventType("TEST_EVENT");
		event.setPayload("{\"test\":\"data\"}");
		event.setStatus(status);
		return event;
	}

	@Test
	public void retryFailedOutboxEvent_shouldSetStatusToPendingIfFailed() {
		OutboxEvent event = createTestEvent(OutboxEvent.Status.FAILED);
		event.setErrorMessage("Old failure message");
		service.saveOutboxEvent(event);

		service.retryFailedOutboxEvent(event.getUuid());

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		OutboxEvent updated = sessionFactory.getCurrentSession().get(OutboxEvent.class, event.getId());
		Assert.assertEquals(OutboxEvent.Status.PENDING, updated.getStatus());
		Assert.assertTrue("Error count should be reset to 0", updated.getErrorCount() == 0);
		Assert.assertNull("Error message should be cleared", updated.getErrorMessage());
	}

	@Test
	public void resetStuckEvent_shouldResetProcessingEventsOlderThanTimeoutAndThrowException() {
		OutboxEvent event = createTestEvent(OutboxEvent.Status.PROCESSING);
		event.setErrorCount(2);
		service.saveOutboxEvent(event);

		// Backdate the dateChanged to bypass the 1-second timeout
		sessionFactory.getCurrentSession().createQuery("update OutboxEvent set dateChanged = :past where id = :id")
				.setParameter("past", Date.from(Instant.now().minusSeconds(5)))
				.setParameter("id", event.getId())
				.executeUpdate();

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		try {
			service.resetStuckEvent();
			Assert.fail("Expected OutboxException to be thrown when a stuck event is reset");
		} catch (OutboxException e) {
			// Expected behavior
		}

		OutboxEvent updated = sessionFactory.getCurrentSession().get(OutboxEvent.class, event.getId());
		Assert.assertEquals(OutboxEvent.Status.PENDING, updated.getStatus());
		Assert.assertTrue("Error count should be incremented", updated.getErrorCount() == 3); 
		Assert.assertNotNull(updated.getErrorMessage());
		Assert.assertTrue(updated.getErrorMessage().contains("Stuck in PROCESSING state"));
	}

	@Test
	public void warnOnTooManyPendingEvents_shouldExecuteWithoutErrors() {
		// This method just logs a warning, so we simply verify it executes completely without throwing an exception.
		service.warnOnTooManyPendingEvents();
	}

	@Test
	public void getProcessingAndPendingEvents_shouldReturnOnlyPendingAndProcessingEvents() {
		OutboxEvent pendingEvent = createTestEvent(OutboxEvent.Status.PENDING);
		service.saveOutboxEvent(pendingEvent);

		OutboxEvent processingEvent = createTestEvent(OutboxEvent.Status.PROCESSING);
		service.saveOutboxEvent(processingEvent);

		OutboxEvent failedEvent = createTestEvent(OutboxEvent.Status.FAILED);
		service.saveOutboxEvent(failedEvent);
		
		sessionFactory.getCurrentSession().flush();

		List<OutboxEvent> events = service.getProcessingAndPendingEvents();

		Assert.assertTrue(events.stream().anyMatch(e -> e.getId().equals(pendingEvent.getId())));
		Assert.assertTrue(events.stream().anyMatch(e -> e.getId().equals(processingEvent.getId())));
		Assert.assertFalse(events.stream().anyMatch(e -> e.getId().equals(failedEvent.getId())));
	}

	@Test
	public void getFailingEvents_shouldReturnOnlyFailedEvents() {
		OutboxEvent pendingEvent = createTestEvent(OutboxEvent.Status.PENDING);
		service.saveOutboxEvent(pendingEvent);

		OutboxEvent processingEvent = createTestEvent(OutboxEvent.Status.PROCESSING);
		service.saveOutboxEvent(processingEvent);

		OutboxEvent failedEvent = createTestEvent(OutboxEvent.Status.FAILED);
		service.saveOutboxEvent(failedEvent);
		
		sessionFactory.getCurrentSession().flush();

		List<OutboxEvent> events = service.getFailingEvents();
		
		assertThat(events, contains(failedEvent));
	}

	@Test
	public void lockEventForProcessing_shouldClaimPendingEventAndReturnTrue() {
		OutboxEvent event = createTestEvent(OutboxEvent.Status.PENDING);
		service.saveOutboxEvent(event);
		
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();

		boolean locked = service.lockEventForProcessing(event);
		Assert.assertTrue("Should successfully lock the PENDING event", locked);

		OutboxEvent updated = sessionFactory.getCurrentSession().get(OutboxEvent.class, event.getId());
		Assert.assertEquals(OutboxEvent.Status.PROCESSING, updated.getStatus());
	}

	@Test
	public void lockEventForProcessing_shouldReturnFalseIfEventAlreadyProcessing() {
		OutboxEvent event = createTestEvent(OutboxEvent.Status.PROCESSING);
		service.saveOutboxEvent(event);

		boolean locked = service.lockEventForProcessing(event);

		Assert.assertFalse("Should fail to lock an event that isn't PENDING", locked);
	}

	@Test
	public void saveOutboxEvent_shouldUpdateDateChanged() {
		OutboxEvent event = createTestEvent(OutboxEvent.Status.PENDING);
		// Ensure dateChanged is null before saving
		event.setDateChanged(null);
		Assert.assertNull(event.getDateChanged());

		service.saveOutboxEvent(event);

		Assert.assertNotNull("Entity should have an ID assigned", event.getId());
		Assert.assertNotNull("Date changed should be automatically populated", event.getDateChanged());
	}
}
