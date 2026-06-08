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

import java.util.Set;

/**
 * See {@link EntityEvent} or {@link AggregatedEntityEvent}.
 *
 * @since 2.9.0
 */
public abstract class BaseSessionEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;

	protected String sessionId;

	/**
	 * Default constructor for deserialization.
	 */
	public BaseSessionEvent() {
	}

	public BaseSessionEvent(Set<String> tags) {
		super(tags);
	}

	public BaseSessionEvent(String sessionId, Set<String> tags) {
		super(tags);
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
