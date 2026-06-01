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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.MapMaker;

/**
 * It is used to discover any {@link OutboxEventListener} listeners and dispatch events to them.
 *
 * @since 2.9.x
 */
@Component
public class OutboxEventRegistry implements SmartInitializingSingleton {

	private static final Logger log = LoggerFactory.getLogger(OutboxEventRegistry.class);

	private final ApplicationContext applicationContext;

	private final List<ListenerMethod> registry;

	private final ConcurrentMap<ResolvableType, Boolean> hasOutboxListenersCache;

	private final boolean enabled;

	public OutboxEventRegistry(ApplicationContext applicationContext,
	    @Value("${outboxevent.enabled:true}") boolean enabled) {
		this.applicationContext = applicationContext;
		this.registry = new ArrayList<>();
		this.hasOutboxListenersCache = new MapMaker().weakKeys().weakValues().makeMap();
		this.enabled = enabled;
	}

	@Override
	public void afterSingletonsInstantiated() {
		if (!enabled) {
			return;
		}

		for (String beanName : applicationContext.getBeanDefinitionNames()) {
			Class<?> type = applicationContext.getType(beanName);
			if (type != null) {
				// Extract original class just in case it is wrapped by CGLIB proxy (e.g., @Transactional beans)
				Class<?> userClass = ClassUtils.getUserClass(type);
				ReflectionUtils.doWithMethods(userClass, method -> {
					if (AnnotatedElementUtils.hasAnnotation(method, OutboxEventListener.class)) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length == 1) {
							ResolvableType eventType = ResolvableType.forMethodParameter(method, 0);
							Order orderAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, Order.class);
							int order = orderAnnotation != null ? orderAnnotation.value() : Ordered.LOWEST_PRECEDENCE;
							registry.add(new ListenerMethod(eventType, beanName, method, order));
						}
					}
				});
			}
		}

		// Sort all listeners once at application startup
		Collections.sort(registry);
	}

	public boolean hasOutboxListeners() {
		return !registry.isEmpty();
	}

	public boolean hasOutboxListeners(Object event) {
		if (!hasOutboxListeners()) {
			return false;
		}

		ResolvableType resolvableType = ResolvableType.forInstance(event);
		Boolean result = hasOutboxListenersCache.get(resolvableType);
		if (result != null) {
			return result;
		}

		// Checks if the published event type (or its subclass) is registered for the outbox
		result = registry.stream().anyMatch(listener -> listener.supports(resolvableType));

		hasOutboxListenersCache.put(resolvableType, result);
		return result;
	}

	public void dispatchOutboxEvent(Object event, Set<String> completedListeners, Runnable listenerCallback) {
		ResolvableType resolvableType = ResolvableType.forInstance(event);

		for (ListenerMethod listener : registry) {
			if (listener.supports(resolvableType) && !completedListeners.contains(listener.getListenerId())) {
				listener.invoke(applicationContext, event);
				completedListeners.add(listener.getListenerId());
				listenerCallback.run();
			}
		}
	}

	private static class ListenerMethod implements Comparable<ListenerMethod> {

		private final ResolvableType targetEventType;

		private final String beanName;

		private final Method method;

		private final int order;

		private final String listenerId;

		public ListenerMethod(ResolvableType targetEventType, String beanName, Method method, int order) {
			this.targetEventType = targetEventType;
			this.beanName = beanName;
			this.method = method;
			this.order = order;
			this.listenerId = beanName + "." + method.getName();
		}

		public boolean supports(ResolvableType eventType) {
			return this.targetEventType.isAssignableFrom(eventType);
		}

		public String getListenerId() {
			return listenerId;
		}

		public void invoke(ApplicationContext context, Object event) {
			Object bean = context.getBean(beanName);
			// Ensure we invoke on the proxy if the bean is wrapped (e.g., @Transactional)
			Method methodToInvoke = ClassUtils.getMostSpecificMethod(method, bean.getClass());
			ReflectionUtils.makeAccessible(methodToInvoke);
			ReflectionUtils.invokeMethod(methodToInvoke, bean, event);
		}

		@Override
		public int compareTo(ListenerMethod other) {
			return Integer.compare(this.order, other.order);
		}
	}
}
