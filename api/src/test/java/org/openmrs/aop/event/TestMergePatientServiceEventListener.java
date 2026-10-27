/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Test listener used to assert that {@link MergePatientServiceEvent} is published.
 */
@Component
public class TestMergePatientServiceEventListener {

	private final List<MergePatientServiceEvent> events = new ArrayList<>();

	@EventListener
	public void onMergePatientServiceEvent(MergePatientServiceEvent event) {
		events.add(event);
	}

	public List<MergePatientServiceEvent> getEvents() {
		return events;
	}

	public void clear() {
		events.clear();
	}
}
