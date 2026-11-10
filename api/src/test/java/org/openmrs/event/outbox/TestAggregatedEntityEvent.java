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

import java.util.List;
import java.util.Set;

import org.openmrs.event.AggregatedEntityEvent;
import org.openmrs.event.EntityEvent;

public class TestAggregatedEntityEvent extends AggregatedEntityEvent {

	/**
	 * Default constructor for deserialization.
	 */
	public TestAggregatedEntityEvent() {
	}

	public TestAggregatedEntityEvent(List<EntityEvent<?>> events) {
		super(events);
	}

	public TestAggregatedEntityEvent(List<EntityEvent<?>> events, Set<String> tags) {
		super(events, tags);
	}
}
