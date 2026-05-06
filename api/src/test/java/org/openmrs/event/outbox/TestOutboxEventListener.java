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

import org.openmrs.event.AggregatedEntityEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TestOutboxEventListener {
	
	private final List<TestEvent> capturedEvents = new CopyOnWriteArrayList<>();
	
	public class TestEvent {
		private final String method;
		private final Object event;
		
		public TestEvent(String method, Object event) {
			this.method = method;
			this.event = event;
		}

		public String getMethod() {
			return method;
		}

		public Object getEvent() {
			return event;
		}
	}
	
	@Order(1)
	@OutboxEventListener
	public void onPatientCreated(OutboxEventIT.PatientCreatedEvent event) {
		capturedEvents.add(new TestEvent("onPatientCreated", event));
	}
	
	@Order(2)
	@OutboxEventListener
	public void onInvalid(OutboxEventIT.NonSerializableEvent event) {
		capturedEvents.add(new TestEvent("onInvalid", event));
	}

	@Order(4)
	@OutboxEventListener
	public void onPatientCreatedAfterFailing(OutboxEventIT.PatientCreatedEvent event) {
		capturedEvents.add(new TestEvent("onPatientCreatedAfterFailing", event));
	}

	@Order(3)
	@OutboxEventListener
	public void onPatientCreatedFailing(OutboxEventIT.PatientCreatedEvent event) {
		long retriesCount = capturedEvents.stream().filter(e -> e.getMethod().equals("onPatientCreatedFailingFailed")).count();
		if (retriesCount < 4) {
			capturedEvents.add(new TestEvent("onPatientCreatedFailingFailed", event));
			throw new RuntimeException("Failing listener");
		}
		capturedEvents.add(new TestEvent("onPatientCreatedFailingSucceeded", event));
	}

	@Order(2)
	@OutboxEventListener
	public void onAggregatedExact(TestAggregatedEntityEvent event) {
		capturedEvents.add(new TestEvent("onAggregatedExact", event));
	}
	
	@Order(1)
	@OutboxEventListener
	public void onAggregatedGeneric(AggregatedEntityEvent event) {
		capturedEvents.add(new TestEvent("onAggregatedGeneric", event));
	}
	
	@Order(3)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onAfterCommitAggregated(TestAggregatedEntityEvent event) {
		capturedEvents.add(new TestEvent("onAfterCommitAggregated", event));
	}
	
	public List<TestEvent> getCapturedEvents() {
		return capturedEvents;
	}
	
	public void clearCapturedEvents() {
		capturedEvents.clear();
	}
}
