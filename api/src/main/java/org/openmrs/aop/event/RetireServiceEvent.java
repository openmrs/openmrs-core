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

import org.openmrs.event.EntityEvent;

import java.util.Set;

/**
 * Published by {@link org.openmrs.aop.OpenmrsServiceEventAdvice}
 * 
 * @param <T> the changed entity
 *     
 * @since 2.9.0
 */
public class RetireServiceEvent<T> extends EntityEvent<T> {
	private static final long serialVersionUID = 1L;
	
	public RetireServiceEvent() {
	}
	
	public RetireServiceEvent(T entity) {
		super(entity);
	}

	public RetireServiceEvent(T entity, Set<String> tags) {
		super(entity, tags);
	}
}
