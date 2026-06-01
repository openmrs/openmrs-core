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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 2.9.x
 */
@Component
public class OutboxEventInterceptor {

	private static final Logger log = LoggerFactory.getLogger(OutboxEventInterceptor.class);

	private final ObjectMapper objectMapper;

	private final OutboxEventRegistry outboxEventRegistry;

	private final OutboxEventService outboxEventService;

	public OutboxEventInterceptor(OutboxEventService outboxEventService, ObjectMapper objectMapper,
	    OutboxEventRegistry outboxEventRegistry) {
		this.outboxEventService = outboxEventService;
		this.objectMapper = objectMapper;
		this.outboxEventRegistry = outboxEventRegistry;
	}

	@EventListener
	public void interceptAndSaveToOutbox(OutboxableEvent event) throws OutboxException {
		if (!outboxEventRegistry.hasOutboxListeners(event)) {
			return;
		}
		saveToDatabase(event);
	}

	private void saveToDatabase(OutboxableEvent event) throws OutboxException {
		try {
			OutboxEvent item = new OutboxEvent();
			item.setEventType(event.getClass().getName());
			if (event instanceof OutboxEventPayload) {
				item.setPayload(((OutboxEventPayload) event).toPayload());
			} else {
				item.setPayload(objectMapper.writeValueAsString(event));
			}
			item.setDateCreated(new Date());
			item.setDateChanged(item.getDateCreated());
			item.setStatus(OutboxEvent.Status.PENDING);

			// Guaranteed to run within the existing OpenMRS wrapping transaction (if exists)
			outboxEventService.saveOutboxEvent(item);

			log.debug("Event {} saved to outbox", event.getClass().getSimpleName());
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize event for outbox", e);
			throw new OutboxException("Failed to serialize outbox event", e);
		}
	}
}
