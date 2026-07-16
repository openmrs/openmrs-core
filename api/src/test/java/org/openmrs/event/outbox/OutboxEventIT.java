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
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.event.outbox.tasks.OutboxTaskSchedulerInitializer;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OutboxEventIT extends BaseContextSensitiveNonTransactionalTest {

	@Autowired
	private TestEventPublisherService testEventPublisherService;

	@Autowired
	private TestOutboxEventListener testOutboxEventListener;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private OutboxTaskSchedulerInitializer outboxTaskSchedulerInitializer;

	@BeforeEach
	public void setUp() throws Exception {
		// Clean the outbox table before each test to ensure isolation
		adminService.saveGlobalProperty(new GlobalProperty("eventPublished", "false"));
		testEventPublisherService.clearOutbox();
		testOutboxEventListener.clearCapturedEvents();
		outboxTaskSchedulerInitializer.schedule();
	}

	@Test
	public void interceptAndSaveToOutbox_shouldSaveEventWhenListenerExists() throws InterruptedException {
		// Arrange: A patient event is created, and the registry has a listener.
		PatientCreatedEvent event = new PatientCreatedEvent(UUID.randomUUID().toString());

		// Act: The event is published within a transaction.
		testEventPublisherService.publishEventInTransaction(event);

		// Assert: the transaction was committed
		assertThat(adminService.getGlobalProperty("eventPublished", "false"), equalTo("true"));

		// Assert: The interceptor should have created an OutboxEvent in the database.
		List<OutboxEvent> outboxEvents = (List<OutboxEvent>) sessionFactory.getCurrentSession()
		        .createQuery("from OutboxEvent").list();

		assertThat(outboxEvents,
		    contains(
		        allOf(
		            hasProperty("payload",
		                is("{\"@class\":\"org.openmrs.event.outbox.OutboxEventIT$PatientCreatedEvent\",\"uuid\":\""
		                        + event.getUuid() + "\"}")),
		            hasProperty("status", is(OutboxEvent.Status.PENDING)),
		            hasProperty("eventType", is(PatientCreatedEvent.class.getName())),
		            hasProperty("completedListeners", is(nullValue()))),
		        allOf(hasProperty("status", is(OutboxEvent.Status.PENDING)),
		            hasProperty("eventType", is(TestAggregatedEntityEvent.class.getName())),
		            hasProperty("completedListeners", is(nullValue())))));

		waitForCapturedEvents(10);

		// Check for onAfterCommitAggregated which is a TransactionalEventListener executed in a different thread than
		// OutboxEventListeners thus no guarantee of ordering between them
		List<TestOutboxEventListener.TestEvent> capturedEvents = new ArrayList<>(
		        testOutboxEventListener.getCapturedEvents());
		TestOutboxEventListener.TestEvent afterCommitEvent = capturedEvents.stream()
		        .filter(e -> "onAfterCommitAggregated".equals(e.getMethod())).findFirst()
		        .orElseThrow(() -> new AssertionError("onAfterCommitAggregated not found"));

		assertThat(afterCommitEvent, hasProperty("event", hasProperty("events", hasSize(2))));
		capturedEvents.remove(afterCommitEvent);

		//Assert the ordering of captured outbox events is correct
		assertThat(capturedEvents, contains(
		    allOf(hasProperty("method", is("onPatientCreated")),
		        hasProperty("event", hasProperty("uuid", equalTo(event.getUuid())))),
		    hasProperty("method", is("onPatientCreatedFailingFailed")),
		    hasProperty("method", is("onPatientCreatedFailingFailed")),
		    hasProperty("method", is("onPatientCreatedFailingFailed")),
		    hasProperty("method", is("onPatientCreatedFailingFailed")),
		    hasProperty("method", is("onPatientCreatedFailingSucceeded")),
		    allOf(hasProperty("method", is("onPatientCreatedAfterFailing")),
		        hasProperty("event", hasProperty("uuid", equalTo(event.getUuid())))),
		    allOf(hasProperty("method", is("onAggregatedGeneric")), hasProperty("event", hasProperty("events", hasSize(2)))),
		    allOf(hasProperty("method", is("onAggregatedExact")), hasProperty("event", hasProperty("events", hasSize(2))))));

		waitForOutboxCompleted();

		Context.clearSession(); // To get the latest state of the database
		outboxEvents = (List<OutboxEvent>) sessionFactory.getCurrentSession().createQuery("from OutboxEvent order by id")
		        .list();

		assertThat(outboxEvents,
		    contains(
		        allOf(
		            hasProperty("payload",
		                is("{\"@class\":\"org.openmrs.event.outbox.OutboxEventIT$PatientCreatedEvent\",\"uuid\":\""
		                        + event.getUuid() + "\"}")),
		            hasProperty("status", is(OutboxEvent.Status.COMPLETED)),
		            hasProperty("eventType", is(PatientCreatedEvent.class.getName())),
		            hasProperty("completedListeners",
		                is("testOutboxEventListener.onPatientCreated,testOutboxEventListener.onPatientCreatedFailing,"
		                        + "testOutboxEventListener.onPatientCreatedAfterFailing"))),
		        allOf(hasProperty("status", is(OutboxEvent.Status.COMPLETED)),
		            hasProperty("eventType", is(TestAggregatedEntityEvent.class.getName())),
		            hasProperty("completedListeners",
		                is("testOutboxEventListener.onAggregatedGeneric,testOutboxEventListener.onAggregatedExact")))));
	}

	@Test
	public void interceptAndSaveToOutbox_shouldNotSaveEventWhenNoListenerExists() {
		// Arrange: A concept event is created, and the registry has NO listeners.
		ConceptCreatedEvent event = new ConceptCreatedEvent(UUID.randomUUID().toString());

		// Act: The event is published within a transaction.
		testEventPublisherService.publishEventInTransaction(event);

		// Assert: the transaction was committed
		assertThat(adminService.getGlobalProperty("eventPublished", "false"), equalTo("true"));

		// Assert: The outbox table should have just one event
		List<OutboxEvent> outboxEvents = (List<OutboxEvent>) sessionFactory.getCurrentSession()
		        .createQuery("from OutboxEvent").list();

		assertThat(outboxEvents, contains(allOf(hasProperty("status", is(OutboxEvent.Status.PENDING)),
		    hasProperty("eventType", is(TestAggregatedEntityEvent.class.getName())))));
	}

	@Test
	public void interceptAndSaveToOutbox_shouldSaveEventWhenNoTransaction() {
		// Arrange: A patient event is created, and there's no transaction
		PatientCreatedEvent event = new PatientCreatedEvent(UUID.randomUUID().toString());

		// Act: The event is published without transaction.
		testEventPublisherService.publishEventWithoutTransaction(event);

		// Assert: the saveGlobalProperty transaction was committed
		assertThat(adminService.getGlobalProperty("eventPublished", "false"), equalTo("true"));

		// Assert: The outbox table should have just one event
		List<OutboxEvent> outboxEvents = (List<OutboxEvent>) sessionFactory.getCurrentSession()
		        .createQuery("from OutboxEvent").list();

		assertThat(outboxEvents,
		    contains(
		        allOf(hasProperty("status", is(OutboxEvent.Status.PENDING)),
		            hasProperty("eventType", is(TestAggregatedEntityEvent.class.getName()))),
		        allOf(hasProperty("status", is(OutboxEvent.Status.PENDING)),
		            hasProperty("eventType", is(PatientCreatedEvent.class.getName())))));
	}

	@Test
	public void interceptAndSaveToOutbox_shouldThrowRuntimeExceptionOnSerializationFailure() {
		// Arrange: An event is created, and we inject a faulty ObjectMapper that will fail.
		NonSerializableEvent event = new NonSerializableEvent();

		// Act: Publish the event. The interceptor will try to serialize, fail, and throw an exception.
		assertThrows(RuntimeException.class, () -> testEventPublisherService.publishEventInTransaction(event));

		// Assert: the transaction was rolled back
		assertThat(adminService.getGlobalProperty("eventPublished", "false"), equalTo("false"));

		// Assert: The outbox table should remain empty.
		List<OutboxEvent> outboxEvents = (List<OutboxEvent>) sessionFactory.getCurrentSession()
		        .createQuery("from OutboxEvent").list();

		assertEquals(0, outboxEvents.size());
	}

	// Simple event classes for testing purposes
	public static class PatientCreatedEvent implements OutboxableEvent {

		private String uuid;

		public PatientCreatedEvent() {
		}

		public PatientCreatedEvent(String uuid) {
			this.uuid = uuid;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}

	public static class ConceptCreatedEvent implements OutboxableEvent {

		private String uuid;

		public ConceptCreatedEvent() {
		}

		public ConceptCreatedEvent(String uuid) {
			this.uuid = uuid;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}

	public static class NonSerializableEvent implements OutboxableEvent {

		@JsonProperty
		public Object getNonSerializableObject() {
			throw new IllegalStateException("This object is not serializable");
		}
	}

	private void waitForCapturedEvents(int count) throws InterruptedException {
		long start = System.currentTimeMillis();
		int capturedCount = 0;
		while (System.currentTimeMillis() - start < 180000) {
			capturedCount = testOutboxEventListener.getCapturedEvents().size();
			if (capturedCount >= count) {
				return;
			}
			Thread.sleep(500);
		}
		throw new RuntimeException("Captured " + capturedCount + " events (expected " + count + ") in 180s");
	}

	private void waitForOutboxCompleted() throws InterruptedException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 120000) {
			Long pendingCount = (Long) sessionFactory.getCurrentSession()
			        .createQuery("select count(id) from OutboxEvent where status != 'COMPLETED'").uniqueResult();

			if (pendingCount != null && pendingCount == 0L) {
				Context.clearSession();
				return;
			}

			Thread.sleep(500);
		}
		throw new RuntimeException("Outbox events not marked as COMPLETED in 120s");
	}
}
