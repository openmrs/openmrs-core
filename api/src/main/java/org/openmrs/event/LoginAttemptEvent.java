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

import org.springframework.context.ApplicationEvent;

/**
 * Published by {@link org.openmrs.api.db.hibernate.HibernateContextDAO} whenever a login
 * attempt is made — successful, failed, or resulting in account lockout.
 *
 * <p>Listeners (e.g. audit modules) can subscribe to this event to record security events
 * without coupling to openmrs-core internals.
 * 
 * @since 2.7.0
 */
public class LoginAttemptEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final String username;
	
	private final Integer userId;
	
	private final boolean success;
	
	private final String failureReason; // "INVALID_CREDENTIALS" | "ACCOUNT_LOCKED" | null on success
	
	private final boolean accountLocked;
	
	public LoginAttemptEvent(Object source, String username, Integer userId, boolean success, String failureReason,
	    boolean accountLocked) {
		super(source);
		this.username = username;
		this.userId = userId;
		this.success = success;
		this.failureReason = failureReason;
		this.accountLocked = accountLocked;
	}
	
	public String getUsername() {
		return username;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public String getFailureReason() {
		return failureReason;
	}
	
	public boolean isAccountLocked() {
		return accountLocked;
	}
}
