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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.api.db.event.DeleteDbEvent;
import org.openmrs.api.db.event.SaveDbEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OutboxEventInterceptorTest {

	private OutboxEventService outboxEventService;

	private OutboxEventInterceptor interceptor;

	@BeforeEach
	public void setUp() {
		outboxEventService = mock(OutboxEventService.class);
		OutboxEventRegistry registry = mock(OutboxEventRegistry.class);
		when(registry.hasOutboxListeners(any())).thenReturn(true);
		interceptor = new OutboxEventInterceptor(outboxEventService, mock(ObjectMapper.class), registry);
	}

	@Test
	public void interceptAndSaveToOutbox_shouldNotSaveEventsAboutOutboxEvents() {
		interceptor.interceptAndSaveToOutbox(new SaveDbEvent<>(new OutboxEvent(), null, new Object[0], new String[0]));
		interceptor.interceptAndSaveToOutbox(new DeleteDbEvent<>(new OutboxEvent(), 1, new Object[0], new String[0]));

		verify(outboxEventService, never()).saveOutboxEvent(any());
	}

	@Test
	public void interceptAndSaveToOutbox_shouldSaveEventsAboutOtherEntities() {
		interceptor.interceptAndSaveToOutbox(new SaveDbEvent<>(new Location(), null, new Object[0], new String[0]));

		verify(outboxEventService).saveOutboxEvent(any(OutboxEvent.class));
	}
}
