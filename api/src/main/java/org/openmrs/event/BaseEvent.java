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

import org.openmrs.event.outbox.OutboxableEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * See {@link EntityEvent} or {@link AggregatedEntityEvent} instead.
 * 
 * @since 2.9.0
 */
public abstract class BaseEvent implements OutboxableEvent {
	private static final long serialVersionUID = 1L;
	
	protected final Set<String> tags;
	
	protected BaseEvent() {
		this.tags = new HashSet<>();
	}
	
	protected BaseEvent(Set<String> tags) {
		this.tags = tags;
	}
	
	public Set<String> getTags() {
		return tags;
	}
}
