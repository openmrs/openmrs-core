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

import org.openmrs.GlobalProperty;
import org.openmrs.aop.event.SaveServiceEvent;
import org.openmrs.api.db.event.SaveDbEvent;
import org.openmrs.event.AggregatedEntityEvent;
import org.openmrs.event.EntityEvent;
import org.openmrs.event.EventPublisher;
import org.openmrs.event.TransactionalEventAggregator;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Aggregates events <i>SaveServiceEvent&lt;GlobalProperty&gt;</i> and <i>SaveDbEvent&lt;GlobalProperty&gt;</i>
 * into {@link TestAggregatedEntityEvent}.
 */
@Component
public class TestTransactionalEventAggregator extends TransactionalEventAggregator {

	public TestTransactionalEventAggregator(EventPublisher eventPublisher) {
		super(eventPublisher);
	}

	@Override
	public boolean supportsEvent(EntityEvent<?> event) {
		ResolvableType resolvableType = event.getResolvableType();
		if (resolvableType != null) {
			return resolvableType.isAssignableFrom(ResolvableType.forClassWithGenerics(SaveServiceEvent.class, GlobalProperty.class)) 
				|| resolvableType.isAssignableFrom(ResolvableType.forClassWithGenerics(SaveDbEvent.class, GlobalProperty.class));
		}
		return false;
	}

	@Override
	public AggregatedEntityEvent newAggregatedEntityEvent(List<EntityEvent<?>> events) {
		return new TestAggregatedEntityEvent(events);
	}
}
