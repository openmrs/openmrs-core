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
import java.util.List;
import java.util.Set;

/**
 * Extend to create an event for your grouped entity events that you can listen to.
 * <p>
 * It needs to be used together with {@link TransactionalEventAggregator}.
 *
 * @since 2.9.x
 */
public abstract class AggregatedEntityEvent extends BaseEvent {

	private List<EntityEvent<?>> events;

	public AggregatedEntityEvent() {
	}

	public AggregatedEntityEvent(List<EntityEvent<?>> events) {
		this(events, new HashSet<>());
	}

	public AggregatedEntityEvent(List<EntityEvent<?>> events, Set<String> tags) {
		super(events, tags);
		this.events = events;
	}

	public List<EntityEvent<?>> getEvents() {
		return events;
	}

	public void setEvents(List<EntityEvent<?>> events) {
		this.events = events;
		this.source = events;
	}
}
