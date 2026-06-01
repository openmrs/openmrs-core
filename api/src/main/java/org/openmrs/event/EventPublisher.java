/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * OpenMRS custom {@link ApplicationEventPublisher}, which populates sessionId in order to be able
 * to group events happening in the same session.
 * <p>
 * It can be autowired as {@link EventPublisher} or Spring's {@link ApplicationEventPublisher}.
 *
 * @since 2.9.x
 */
@Primary
@Component
public class EventPublisher implements ApplicationEventPublisher {

	private final ApplicationEventPublisher delegate;

	private final SessionFactory sessionFactory;

	public EventPublisher(ApplicationEventPublisher delegate, SessionFactory sessionFactory) {
		this.delegate = delegate;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void publishEvent(@NonNull Object event) {
		if (event instanceof BaseEvent) {
			String sessionId = ((SessionImplementor) sessionFactory.getCurrentSession()).getSessionIdentifier().toString();
			BaseEvent baseEvent = (BaseEvent) event;
			baseEvent.setSessionId(sessionId);
		}
		delegate.publishEvent(event);
	}

}
