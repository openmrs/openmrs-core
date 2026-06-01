/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.outbox.tasks;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.api.context.Context;
import org.openmrs.event.EventPublisher;
import org.openmrs.event.outbox.OutboxEvent;
import org.openmrs.event.outbox.OutboxEventPayload;
import org.openmrs.event.outbox.OutboxEventRegistry;
import org.openmrs.event.outbox.OutboxEventService;
import org.openmrs.event.outbox.OutboxException;
import org.openmrs.event.outbox.OutboxExceptionEvent;
import org.openmrs.scheduler.TaskContext;
import org.openmrs.scheduler.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 2.9.x
 */
@Component
public class OutboxPollingTaskHandler implements TaskHandler<OutboxPollingTaskData> {

	private static final Logger log = LoggerFactory.getLogger(OutboxPollingTaskHandler.class);

	private final OutboxEventRegistry registry;

	private final ObjectMapper objectMapper;

	private final EventPublisher eventPublisher;

	private final OutboxEventService outboxEventService;

	public OutboxPollingTaskHandler(OutboxEventRegistry registry, ObjectMapper objectMapper, EventPublisher eventPublisher,
	    OutboxEventService outboxEventService) {
		this.registry = registry;
		this.objectMapper = objectMapper;
		this.eventPublisher = eventPublisher;
		this.outboxEventService = outboxEventService;
	}

	/**
	 * Processes pending outbox events. Only one thread is processing events at a time to guarantee
	 * strict chronological ordering.
	 * <p>
	 * In case an event listener does not complete in {@code outboxevent.listener.timeout} (120s by
	 * default) it is considered stale and the event can be picked up by another thread for processing.
	 * It is signaled by throwing an OutboxException, which can be viewed in scheduler UI.
	 * <p>
	 * This method does not complete until outbox is empty, thus it can run indefinitely regardless of
	 * the schedule.
	 *
	 * @param taskData task data
	 * @param taskContext task context
	 * @throws Exception
	 */
	@Override
	public void execute(OutboxPollingTaskData taskData, TaskContext taskContext) throws Exception {
		outboxEventService.resetStuckEvent();

		while (true) {
			List<OutboxEvent> pendingItems = outboxEventService.getProcessingAndPendingEvents();

			if (pendingItems.isEmpty()) {
				return;
			}

			outboxEventService.warnOnTooManyPendingEvents();

			log.debug("Found {} pending/processing outbox items to process", pendingItems.size());

			for (OutboxEvent item : pendingItems) {
				if (OutboxEvent.Status.PROCESSING.equals(item.getStatus())) {
					// An event is currently being processed by another handler.
					// Stop here to guarantee strict chronological ordering.
					return;
				}

				if (!outboxEventService.lockEventForProcessing(item)) {
					// Another scheduled task handler just grabbed it and is processing it right now.
					// Stop processing subsequent events to maintain strict ordering.
					return;
				}

				Set<String> completedListeners = new LinkedHashSet<>();
				try {
					Class<?> eventClass = Class.forName(item.getEventType());
					Object event;
					if (OutboxEventPayload.class.isAssignableFrom(eventClass)) {
						event = eventClass.getDeclaredConstructor().newInstance();
						((OutboxEventPayload) event).fromPayload(item.getPayload());
					} else {
						event = objectMapper.readValue(item.getPayload(), eventClass);
					}

					if (item.getCompletedListeners() != null && !item.getCompletedListeners().isEmpty()) {
						completedListeners.addAll(Arrays.asList(item.getCompletedListeners().split(",")));
					}

					// Dispatch ONLY to registered outbox listeners, bypassing the ApplicationEventPublisher
					// If one of listeners fails, the event will be re-tried only for the failing listener and continue
					// with the remaining listeners.
					registry.dispatchOutboxEvent(event, completedListeners, () -> {
						// Update processing so that it is not considered stuck after each listener completes
						item.setCompletedListeners(String.join(",", completedListeners));
						log.debug("Successfully dispatched outbox event {} to {}", event.getClass(),
						    item.getCompletedListeners());
						outboxEventService.saveOutboxEvent(item);
					});

					item.setStatus(OutboxEvent.Status.COMPLETED);
					log.debug("Completed dispatching outbox event {} to {}", event.getClass(), item.getCompletedListeners());
					outboxEventService.saveOutboxEvent(item);
				} catch (Exception e) {
					OutboxException outboxException = new OutboxException(
					        "Failed to process outbox item ID: " + item.getId(), e);
					eventPublisher.publishEvent(new OutboxExceptionEvent(outboxException));

					int errorCount = item.getErrorCount() == null ? 1 : item.getErrorCount() + 1;

					// Extract stacktrace
					String errorMessage = ExceptionUtils.getStackTrace(e);
					if (errorMessage.length() > 1024) {
						errorMessage = errorMessage.substring(0, 1024);
					}

					// Revert to PENDING (to retry)
					item.setStatus(OutboxEvent.Status.PENDING);
					item.setErrorCount(errorCount);
					item.setErrorMessage(errorMessage);
					outboxEventService.saveOutboxEvent(item);

					// Stop processing subsequent events to ensure strict ordering
					throw outboxException;
				}
			}
			Context.clearSession(); // free up memory
		}
	}
}
