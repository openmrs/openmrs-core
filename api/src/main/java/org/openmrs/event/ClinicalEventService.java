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

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;

/**
 * Service for publishing and subscribing to clinical data change events.
 * <p>
 * This service enables a publish/subscribe pattern for clinical data mutations. AI modules
 * and other consumers register {@link ClinicalEventListener} instances to receive notifications
 * when clinical entities (encounters, observations, conditions, etc.) are created, updated,
 * or voided. This allows downstream systems to keep their indexes up to date in real time.
 *
 * @since 3.0.0
 */
public interface ClinicalEventService extends OpenmrsService {
	
	/**
	 * Publishes a clinical event to all registered listeners that support the event's entity type.
	 *
	 * @param event the clinical event to publish
	 */
	void publishEvent(ClinicalEvent event);
	
	/**
	 * Convenience method to create and publish a clinical event.
	 *
	 * @param eventType the type of change that occurred
	 * @param entityType the class of the entity that changed
	 * @param entityUuid the UUID of the entity that changed
	 * @param patient the patient associated with this event
	 */
	void publishEvent(ClinicalEventType eventType, Class<? extends OpenmrsObject> entityType, String entityUuid,
	        Patient patient);
	
	/**
	 * Registers a listener to receive clinical data change events.
	 *
	 * @param listener the listener to register
	 */
	void registerListener(ClinicalEventListener listener);
	
	/**
	 * Unregisters a previously registered listener.
	 *
	 * @param listener the listener to unregister
	 */
	void unregisterListener(ClinicalEventListener listener);
}
