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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

/**
 * Used to evaluate source and broker.
 * 
 * @since 2.9.0
 */
public class BrokerApplicationListenerMethodAdapter extends ApplicationListenerMethodAdapter {

	private final String expectedSource;
	private final String expectedBroker;

	public BrokerApplicationListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
		super(beanName, targetClass, method);
		BrokerEventListener ann = AnnotatedElementUtils.findMergedAnnotation(method, BrokerEventListener.class);
		this.expectedSource = (ann != null) ? ann.value() : "";
		this.expectedBroker = (ann != null) ? ann.broker() : "";
	}

	@Override
	public void processEvent(@NonNull ApplicationEvent event) {
		Object payload = event;
		
		// Spring automatically wraps POJO events in PayloadApplicationEvent
		if (event instanceof PayloadApplicationEvent) {
			payload = ((PayloadApplicationEvent<?>) event).getPayload();
		}

		if (payload instanceof BrokerIncomingEvent) {
			BrokerIncomingEvent<?> brokerEvent = (BrokerIncomingEvent<?>) payload;
			
			boolean matchesSource = expectedSource.equals(brokerEvent.getSource());
			boolean matchesBroker = expectedBroker.isEmpty() || expectedBroker.equals(brokerEvent.getBroker());

			if (!matchesSource || !matchesBroker) {
				// Ignore the event because it doesn't match the required conditions
				return;
			}
		} else {
			// Ignore the event because it doesn't contain BrokerIncomingEvent
			return;
		}
		
		// Execute the standard listener method
		super.processEvent(event);
	}
}
