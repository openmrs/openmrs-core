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
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrokerEventListenerFactoryTest {

	private BrokerEventListenerFactory factory;

	@BeforeEach
	public void setup() {
		factory = new BrokerEventListenerFactory();
	}

	@Test
	public void supportsMethod_shouldReturnTrueForAnnotatedMethod() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "validListener", BrokerIncomingEvent.class);
		assertTrue(factory.supportsMethod(method));
	}

	@Test
	public void supportsMethod_shouldReturnFalseForNonAnnotatedMethod() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "notAListener");
		assertFalse(factory.supportsMethod(method));
	}

	@Test
	public void createApplicationListener_shouldCreateListenerAndRegisterMetadata() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "validListener", BrokerIncomingEvent.class);
		
		ApplicationListener<?> listener = factory.createApplicationListener("testBean", TestBean.class, method);
		
		assertNotNull(listener);
		assertTrue(listener instanceof BrokerApplicationListenerMethodAdapter);

		List<BrokerEventListenerFactory.Listener> registeredListeners = factory.getListeners();
		assertEquals(1, registeredListeners.size());
		
		BrokerEventListenerFactory.Listener meta = registeredListeners.get(0);
		assertEquals("my-source", meta.getSource());
		assertEquals("my-broker", meta.getBroker());
		assertEquals(String.class, meta.getPayloadType());
	}

	@Test
	public void createApplicationListener_shouldAddOnlyUniqueListeners() {
		Method method1 = ReflectionUtils.findMethod(TestBean.class, "validListener", BrokerIncomingEvent.class);
		Method method2 = ReflectionUtils.findMethod(TestBean.class, "duplicateListener", BrokerIncomingEvent.class);
		Method method3 = ReflectionUtils.findMethod(TestBean.class, "distinctListener", BrokerIncomingEvent.class);
		
		factory.createApplicationListener("testBean", TestBean.class, method1);
		factory.createApplicationListener("testBean", TestBean.class, method2);
		factory.createApplicationListener("testBean", TestBean.class, method3);
		
		List<BrokerEventListenerFactory.Listener> registeredListeners = factory.getListeners();
		assertEquals(2, registeredListeners.size());
	}

	@Test
	public void createApplicationListener_shouldThrowExceptionIfNoParameters() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "noParamsListener");
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			factory.createApplicationListener("testBean", TestBean.class, method);
		});
		
		assertEquals("BrokerEventListener must have BrokerIncomingEvent as the first parameter", exception.getMessage());
	}

	@Test
	public void createApplicationListener_shouldThrowExceptionIfWrongParameterType() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "wrongParamsListener", String.class);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			factory.createApplicationListener("testBean", TestBean.class, method);
		});
		
		assertEquals("BrokerEventListener must have BrokerIncomingEvent as the first parameter", exception.getMessage());
	}

	@Test
	public void createApplicationListener_shouldNotThrowExceptionIfRawBrokerIncomingEvent() {
		Method method = ReflectionUtils.findMethod(TestBean.class, "rawListener", BrokerIncomingEvent.class);
		
		factory.createApplicationListener("testBean", TestBean.class, method);

		List<BrokerEventListenerFactory.Listener> registeredListeners = factory.getListeners();
		assertEquals(1, registeredListeners.size());
	}

	@Test
	public void getOrder_shouldReturn50() {
		assertEquals(50, factory.getOrder());
	}

	@Test
	public void getListeners_shouldReturnUnmodifiableList() {
		assertThrows(UnsupportedOperationException.class, () -> {
			factory.getListeners().add(new BrokerEventListenerFactory.Listener("s", "b", String.class));
		});
	}

	static class TestBean {
		@BrokerEventListener(value = "my-source", broker = "my-broker")
		public void validListener(BrokerIncomingEvent<String> event) {}

		@BrokerEventListener(value = "my-source", broker = "my-broker")
		public void duplicateListener(BrokerIncomingEvent<String> event) {}

		@BrokerEventListener(value = "distinct-source", broker = "my-broker")
		public void distinctListener(BrokerIncomingEvent<String> event) {}

		@BrokerEventListener("my-source")
		public void noParamsListener() {}

		@BrokerEventListener("my-source")
		public void wrongParamsListener(String event) {}
		
		@SuppressWarnings("rawtypes")
		@BrokerEventListener("my-source")
		public void rawListener(BrokerIncomingEvent event) {}

		public void notAListener() {}
	}
}
