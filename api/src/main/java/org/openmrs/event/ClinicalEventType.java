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
 * Defines the types of clinical data change events that can be published.
 *
 * @since 3.0.0
 */
public enum ClinicalEventType {
	
	/** A new clinical entity was created */
	CREATED,
	
	/** An existing clinical entity was updated */
	UPDATED,
	
	/** A clinical entity was voided (soft-deleted) */
	VOIDED,
	
	/** A voided clinical entity was unvoided (restored) */
	UNVOIDED
}
