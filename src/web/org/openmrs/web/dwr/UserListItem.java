/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;

public class UserListItem extends PersonListItem {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Integer userId;
	
	private String systemId;
	
	private String username;
	
	private String[] roles = new String[0];
	
	public UserListItem() {
	}
	
	public UserListItem(User user) {
		super(user);
		
		if (user != null) {
			userId = user.getUserId();
			systemId = user.getSystemId();
			username = user.getUsername();
			int i = 0;
			roles = new String[user.getRoles().size()];
			for (Role r : user.getRoles()) {
				roles[i++] = r.getRole();
			}
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
		this.roles = roles;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
}
