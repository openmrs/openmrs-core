/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search;

import org.springframework.context.ApplicationEvent;

/**
 * Represents an event object raised whenever a {@link org.hibernate.search.mapper.orm.Search} object is created.
 * Events are fired via the spring application event mechanism, listeners have to implement
 * {@link org.springframework.context.ApplicationListener} and set the Type parameter value to
 * {@link SearchCreatedEvent}, it also implies that listeners MUST be registered as spring beans in order
 * to be discovered.
 * 
 * @see SearchAndEntityCollection
 * @since 2.8.0
 */
public class SearchCreatedEvent extends ApplicationEvent {
	
	/**
	 * @see ApplicationEvent#ApplicationEvent(java.lang.Object)
	 */
	public SearchCreatedEvent(SearchAndEntityCollection searchAndEntityCollection) {
		super(searchAndEntityCollection);
	}
	
}
