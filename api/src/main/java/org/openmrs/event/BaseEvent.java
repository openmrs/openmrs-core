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

import java.util.HashSet;
import java.util.Set;

import org.openmrs.event.outbox.OutboxableEvent;
import org.springframework.context.ApplicationEvent;

/**
 * <b>For internal use only.</b>
 * <p>
 * Extend {@link EntityEvent} or {@link AggregatedEntityEvent} instead.
 *
 * @since 2.9.x
 */
class BaseEvent extends ApplicationEvent implements OutboxableEvent {

	protected final Set<String> tags;

	private String sessionId;

	public BaseEvent() {
		this(""); //empty for deserialization to work
	}

	public BaseEvent(Object source) {
		this(source, new HashSet<>());
	}

	public BaseEvent(Object source, Set<String> tags) {
		super(source);
		this.tags = tags;
	}

	public Set<String> getTags() {
		return tags;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
