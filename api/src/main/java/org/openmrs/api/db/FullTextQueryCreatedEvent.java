/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.springframework.context.ApplicationEvent;

/**
 * Represents an event object raised whenever a {@link org.hibernate.search.FullTextQuery} object is
 * created. Events are fired via the spring application event mechanism, listeners have to implement
 * {@link org.springframework.context.ApplicationListener} and set the Type parameter value to
 * FullTextQueryCreatedEvent, it also implies that listeners MUST be registered as spring beans in
 * order to be discovered.
 * 
 * @see FullTextQueryAndEntityClass
 * @since 2.3.0
 */
public class FullTextQueryCreatedEvent extends ApplicationEvent {
	
	/**
	 * @see ApplicationEvent#ApplicationEvent(java.lang.Object)
	 */
	public FullTextQueryCreatedEvent(FullTextQueryAndEntityClass queryAndClass) {
		super(queryAndClass);
	}
	
}
