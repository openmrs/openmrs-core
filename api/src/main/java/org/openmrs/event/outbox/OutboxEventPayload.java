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

/**
 * It can be implemented for events to provide a custom serialization to
 * persist in the outbox. The implementing class must have a no-arg constructor.
 * 
 * @since 2.9.0
 */
public interface OutboxEventPayload {
	
	/**
	 * Creates a String representation of this object
	 * @return the String representation of this object
	 */
	String toPayload();
	
	/**
	 * Updates this object to values found in payload.
	 * 
	 * @param payload String representation of an object
	 */
	void fromPayload(String payload);
}
