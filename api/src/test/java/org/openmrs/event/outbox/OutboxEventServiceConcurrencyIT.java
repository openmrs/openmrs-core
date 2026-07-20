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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the transactional outbox against a stale-read race: a poller reads a pending event
 * (populating the Hibernate first-level cache) before claiming it, while another poller partially
 * processes and releases the same event in between. The claiming poller must resume from the
 * committed progress rather than its stale cached read, otherwise it re-runs listeners that were
 * already recorded as completed.
 */
public class OutboxEventServiceConcurrencyIT extends BaseContextSensitiveNonTransactionalTest {

	@Autowired
	private OutboxEventService service;

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void lockEventForProcessing_shouldObserveProgressCommittedByAnotherHandlerAfterThisHandlersRead()
	        throws Exception {
		// Given a PENDING event with no recorded progress
		OutboxEvent seed = new OutboxEvent();
		seed.setUuid(UUID.randomUUID().toString());
		seed.setEventType("TEST_EVENT");
		seed.setPayload("{\"test\":\"data\"}");
		seed.setStatus(OutboxEvent.Status.PENDING);
		service.saveOutboxEvent(seed);
		final Integer id = seed.getId();
		Context.clearSession();

		// And this handler has already read it before claiming it (exactly as the poller does via
		// getProcessingAndPendingEvents()), which puts the null-progress state into the first-level cache
		OutboxEvent handle = sessionFactory.getCurrentSession().get(OutboxEvent.class, id);
		assertNull(handle.getCompletedListeners());

		// When another handler commits progress on the same row from a separate session
		final String progress = "testOutboxEventListener.onSomething";
		runInSeparateSession(() -> {
			OutboxEvent inOtherSession = sessionFactory.getCurrentSession().get(OutboxEvent.class, id);
			inOtherSession.setCompletedListeners(progress);
			service.saveOutboxEvent(inOtherSession);
		});

		// Then claiming the event must reflect the committed progress, not this handler's stale cached read
		boolean claimed = service.lockEventForProcessing(handle);
		assertTrue(claimed, "the PENDING event should be claimable");
		assertEquals(progress, handle.getCompletedListeners(),
		    "lockEventForProcessing must refresh the committed progress, not return the stale cached read");
	}

	private void runInSeparateSession(Runnable action) throws InterruptedException {
		List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
		Thread thread = new Thread(() -> {
			Context.openSession();
			try {
				Context.authenticate(getCredentials());
				action.run();
			} catch (Throwable t) {
				errors.add(t);
			} finally {
				Context.closeSession();
			}
		});
		thread.start();
		thread.join();
		if (!errors.isEmpty()) {
			throw new RuntimeException("action in separate session failed", errors.get(0));
		}
	}
}
