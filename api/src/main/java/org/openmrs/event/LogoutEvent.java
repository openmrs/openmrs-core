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

import org.openmrs.User;
import org.springframework.context.ApplicationEvent;

/**
 * Published by {@link org.openmrs.api.context.Context#logout()} whenever an authenticated user
 * explicitly logs out of the system.
 *
 * <p>This event is NOT published on session timeout — that is handled separately by an
 * {@code HttpSessionListener} in the consuming module.
 *
 * @since 2.7.0
 */
public class LogoutEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final String username;
	
	private final Integer userId;
	
	public LogoutEvent(Object source, User user) {
		super(source);
		this.username = (user != null) ? user.getUsername() : null;
		this.userId = (user != null) ? user.getUserId() : null;
	}
	
	public String getUsername() {
		return username;
	}
	
	public Integer getUserId() {
		return userId;
	}
}
