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

/**
 * Interface for listening to clinical data change events. AI modules and other consumers
 * implement this interface and register with the {@link ClinicalEventService} to receive
 * notifications when clinical data is created, updated, or voided.
 *
 * @since 3.0.0
 */
public interface ClinicalEventListener {
	
	/**
	 * Called when a clinical data change event occurs.
	 *
	 * @param event the clinical event containing details about what changed
	 */
	void onEvent(ClinicalEvent event);
	
	/**
	 * Returns whether this listener supports the given entity type. This allows listeners
	 * to filter which events they receive.
	 *
	 * @param entityType the class of the entity that changed
	 * @return true if this listener should receive events for the given entity type
	 */
	boolean supportsEntityType(Class<?> entityType);
}
