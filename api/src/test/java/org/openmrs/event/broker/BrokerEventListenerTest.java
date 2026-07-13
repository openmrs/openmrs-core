/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.broker;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

@Import(BrokerEventListenerTest.TestListener.class)
public class BrokerEventListenerTest extends BaseContextSensitiveTest {

	@Autowired
	BrokerEventListenerFactory listenerFactory;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	TestListener testListener;

	@BeforeEach
	public void setUp() {
		testListener.clear();
	}

	@Test
	public void shouldBeRegisteredWithListenerFactory() {
		assertThat(listenerFactory.getListeners(), hasItem(allOf(hasProperty("source", equalTo("my-test-source")))));
	}

	@Test
	public void shouldReceiveEventWhenSourceAndBrokerMatch() {
		BrokerIncomingEvent event = new BrokerIncomingEvent();
		event.setSource("test-source");
		event.setBroker("test-broker");

		eventPublisher.publishEvent(event);

		assertThat(testListener.getReceivedEvents(), hasItem(event));
	}

	@Test
	public void shouldFilterOutEventWhenSourceDoesNotMatch() {
		BrokerIncomingEvent event = new BrokerIncomingEvent();
		event.setSource("wrong-source");
		event.setBroker("test-broker");

		eventPublisher.publishEvent(event);

		assertThat(testListener.getReceivedEvents(), is(empty()));
	}

	@Test
	public void shouldFilterOutEventWhenBrokerDoesNotMatch() {
		BrokerIncomingEvent event = new BrokerIncomingEvent();
		event.setSource("test-source");
		event.setBroker("wrong-broker");

		eventPublisher.publishEvent(event);

		assertThat(testListener.getReceivedEvents(), is(empty()));
	}

	@Component
	public static class TestListener {

		private final List<BrokerIncomingEvent> receivedEvents = new ArrayList<>();

		@BrokerEventListener(value = "test-source", broker = "test-broker")
		public void handleEvent(BrokerIncomingEvent<String> event) {
			receivedEvents.add(event);
		}

		public List<BrokerIncomingEvent> getReceivedEvents() {
			return receivedEvents;
		}

		public void clear() {
			receivedEvents.clear();
		}
	}
}
