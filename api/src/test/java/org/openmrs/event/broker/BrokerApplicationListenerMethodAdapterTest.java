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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrokerApplicationListenerMethodAdapterTest {

	private boolean methodInvoked;

	private Method listenerWithBrokerMethod;

	private Method listenerWithoutBrokerMethod;

	@BeforeEach
	public void setup() {
		methodInvoked = false;
		listenerWithBrokerMethod = ReflectionUtils.findMethod(TestBean.class, "listenerWithBroker",
			BrokerIncomingEvent.class);
		listenerWithoutBrokerMethod = ReflectionUtils.findMethod(TestBean.class, "listenerWithoutBroker",
			BrokerIncomingEvent.class);
	}

	@Test
	public void processEvent_shouldInvokeMethodWhenSourceAndBrokerMatch() {
		// given
		TestAdapter adapter = new TestAdapter("testBean", TestBean.class, listenerWithBrokerMethod);
		BrokerIncomingEvent<String> eventPayload = new BrokerIncomingEvent<>("payload", "source-1", "broker-1");
		PayloadApplicationEvent<BrokerIncomingEvent<String>> event = new PayloadApplicationEvent<>(new Object(), eventPayload);

		// when
		adapter.processEvent(event);

		// then
		assertTrue(methodInvoked);
	}

	@Test
	public void processEvent_shouldNotInvokeMethodWhenSourceDoesNotMatch() {
		// given
		TestAdapter adapter = new TestAdapter("testBean", TestBean.class, listenerWithBrokerMethod);
		BrokerIncomingEvent<String> eventPayload = new BrokerIncomingEvent<>("payload", "source-WRONG", "broker-1");
		PayloadApplicationEvent<BrokerIncomingEvent<String>> event = new PayloadApplicationEvent<>(new Object(), eventPayload);

		// when
		adapter.processEvent(event);

		// then
		assertFalse(methodInvoked);
	}

	@Test
	public void processEvent_shouldNotInvokeMethodWhenBrokerDoesNotMatch() {
		// given
		TestAdapter adapter = new TestAdapter("testBean", TestBean.class, listenerWithBrokerMethod);
		BrokerIncomingEvent<String> eventPayload = new BrokerIncomingEvent<>("payload", "source-1", "broker-WRONG");
		PayloadApplicationEvent<BrokerIncomingEvent<String>> event = new PayloadApplicationEvent<>(new Object(), eventPayload);

		// when
		adapter.processEvent(event);

		// then
		assertFalse(methodInvoked);
	}

	@Test
	public void processEvent_shouldInvokeMethodWhenBrokerIsEmptyInAnnotation() {
		// given
		TestAdapter adapter = new TestAdapter("testBean", TestBean.class, listenerWithoutBrokerMethod);
		BrokerIncomingEvent<String> eventPayload = new BrokerIncomingEvent<>("payload", "source-2", "any-broker");
		PayloadApplicationEvent<BrokerIncomingEvent<String>> event = new PayloadApplicationEvent<>(new Object(), eventPayload);

		// when
		adapter.processEvent(event);

		// then
		assertTrue(methodInvoked);
	}

	@Test
	public void processEvent_shouldNotInvokeMethodForNonBrokerIncomingEventPayload() {
		// given
		TestAdapter adapter = new TestAdapter("testBean", TestBean.class, listenerWithBrokerMethod);
		PayloadApplicationEvent<String> event = new PayloadApplicationEvent<>(new Object(), "some other payload");

		// when
		adapter.processEvent(event);

		// then
		assertFalse(methodInvoked);
	}

	@Test
	public void processEvent_shouldNotInvokeMethodForNonPayloadApplicationEvent() {
		// given
		TestAdapter adapter = new TestAdapter("testBean", TestBean.class, listenerWithBrokerMethod);
		ApplicationEvent event = new ApplicationEvent(new Object()) {
		};

		// when
		adapter.processEvent(event);

		// then
		assertFalse(methodInvoked);
	}

	/** Test adapter that flags invocation instead of executing the real method */
	class TestAdapter extends BrokerApplicationListenerMethodAdapter {

		public TestAdapter(String beanName, Class<?> targetClass, Method method) {
			super(beanName, targetClass, method);
		}

		@Override
		protected Object doInvoke(Object... args) {
			methodInvoked = true;
			return null;
		}
	}

	static class TestBean {

		@BrokerEventListener(value = "source-1", broker = "broker-1")
		public void listenerWithBroker(BrokerIncomingEvent<String> event) {}

		@BrokerEventListener("source-2")
		public void listenerWithoutBroker(BrokerIncomingEvent<String> event) {}
	}
}
