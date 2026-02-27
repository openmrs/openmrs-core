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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the {@link ClinicalEventService}.
 * <p>
 * Uses a thread-safe in-memory list of listeners. Events are dispatched synchronously
 * to all matching listeners. Listeners that throw exceptions are logged but do not
 * prevent other listeners from receiving the event.
 *
 * @since 3.0.0
 */
@Service("clinicalEventService")
public class ClinicalEventServiceImpl extends BaseOpenmrsService implements ClinicalEventService {
	
	private static final Logger log = LoggerFactory.getLogger(ClinicalEventServiceImpl.class);
	
	private final List<ClinicalEventListener> listeners = new CopyOnWriteArrayList<>();
	
	/**
	 * @see ClinicalEventService#publishEvent(ClinicalEvent)
	 */
	@Override
	public void publishEvent(ClinicalEvent event) {
		for (ClinicalEventListener listener : listeners) {
			try {
				if (listener.supportsEntityType(event.getEntityType())) {
					listener.onEvent(event);
				}
			}
			catch (Exception e) {
				log.error("Error dispatching clinical event to listener {}", listener.getClass().getName(), e);
			}
		}
	}
	
	/**
	 * @see ClinicalEventService#publishEvent(ClinicalEventType, Class, String, Patient)
	 */
	@Override
	public void publishEvent(ClinicalEventType eventType, Class<? extends OpenmrsObject> entityType, String entityUuid,
	        Patient patient) {
		publishEvent(new ClinicalEvent(eventType, entityType, entityUuid, patient));
	}
	
	/**
	 * @see ClinicalEventService#registerListener(ClinicalEventListener)
	 */
	@Override
	public void registerListener(ClinicalEventListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * @see ClinicalEventService#unregisterListener(ClinicalEventListener)
	 */
	@Override
	public void unregisterListener(ClinicalEventListener listener) {
		listeners.remove(listener);
	}
}
