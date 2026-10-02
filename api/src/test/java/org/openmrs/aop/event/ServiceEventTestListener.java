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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ServiceEventTestListener {

	private final List<MergePatientsServiceEvent> mergePatientsEvents = new CopyOnWriteArrayList<>();

	@EventListener
	public void onMergePatients(MergePatientsServiceEvent event) {
		mergePatientsEvents.add(event);
	}

	public List<MergePatientsServiceEvent> getMergePatientsEvents() {
		return mergePatientsEvents;
	}

	public void clearMergePatientsEvents() {
		mergePatientsEvents.clear();
	}
}
