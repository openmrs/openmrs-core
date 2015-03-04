/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;

import java.util.Arrays;

public class UserListItem extends PersonListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer userId;
	
	private String systemId;
	
	private String username;
	
	private String[] roles = new String[0];
	
	private Boolean retired = Boolean.FALSE;
	
	public UserListItem() {
	}
	
	public UserListItem(User user) {
		super((user == null) ? null : user.getPerson());
		
		if (user != null) {
			userId = user.getUserId();
			systemId = user.getSystemId();
			username = user.getUsername();
			int i = 0;
			roles = new String[user.getRoles().size()];
			for (Role r : user.getRoles()) {
				roles[i++] = r.getRole();
			}
			this.retired = user.isRetired();
			setVoided(retired); // so the parent PersonListItem class works is someone tries to use .voided
		}
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public String getSystemId() {
		return systemId;
	}
	
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	
	public String[] getRoles() {
		return roles;
	}
	
	public void setRoles(String[] roles) {
		if (roles == null) {
			this.roles = new String[0];
		} else {
			this.roles = Arrays.copyOf(roles, roles.length);
		}
		
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Boolean getRetired() {
		return retired;
	}
	
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
}
