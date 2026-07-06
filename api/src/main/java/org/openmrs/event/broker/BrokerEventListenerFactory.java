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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Adds support for {@link BrokerEventListener}.
 * <p>
 * Brokers may call {@link BrokerEventListenerFactory#getListeners()} to setup listeners.
 *
 * @since 2.9.0
 */
@Component
public class BrokerEventListenerFactory implements EventListenerFactory, Ordered {

	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

	@Override
	public boolean supportsMethod(@NonNull Method method) {
		return AnnotatedElementUtils.hasAnnotation(method, BrokerEventListener.class);
	}

	@Override
	public ApplicationListener<?> createApplicationListener(@NonNull String beanName, @NonNull Class<?> type,
	        @NonNull Method method) {
		BrokerEventListener annotation = AnnotatedElementUtils.findMergedAnnotation(method, BrokerEventListener.class);

		if (annotation != null) {
			// Extract payload type from the first method parameter
			if (method.getParameterCount() > 0) {
				MethodParameter methodParameter = new MethodParameter(method, 0);
				ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
				Class<?> payloadType = resolvableType.resolve();

				// If the parameter is wrapped in a generic BrokerIncomingEvent<?>, extract the inner generic type
				if (payloadType != null && BrokerIncomingEvent.class.isAssignableFrom(payloadType)) {
					Class<?> genericType = resolvableType.getGeneric(0).resolve();
					if (genericType == null) {
						genericType = Object.class;
					}

					payloadType = genericType;
					listeners.addIfAbsent(new Listener(annotation.value(), annotation.broker(), payloadType));
					return new BrokerApplicationListenerMethodAdapter(beanName, type, method);
				}
			}
		}

		throw new IllegalArgumentException("BrokerEventListener must have BrokerIncomingEvent as the first parameter");
	}

	@Override
	public int getOrder() {
		// Needs to run before the standard DefaultEventListenerFactory
		return 50;
	}

	/**
	 * Lists registered listeners.
	 *
	 * @return the listeners
	 */
	public List<Listener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}

	public static class Listener {

		private final String source;

		private final String broker;

		private final Class<?> payloadType;

		public Listener(String source, String broker, Class<?> payloadType) {
			this.source = source;
			this.broker = broker;
			this.payloadType = payloadType;
		}

		public String getSource() {
			return source;
		}

		public String getBroker() {
			return broker;
		}

		public Class<?> getPayloadType() {
			return payloadType;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Listener listener = (Listener) o;
			return Objects.equals(source, listener.source) && Objects.equals(broker, listener.broker)
			        && Objects.equals(payloadType, listener.payloadType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(source, broker, payloadType);
		}
	}
}
