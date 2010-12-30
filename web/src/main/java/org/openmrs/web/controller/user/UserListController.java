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
package org.openmrs.web.controller.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserListController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @should get users just given action parameter
	 * @should get all users if no name given
	 * @should get users with a given role
	 * @should include disabled users if requested
	 */
	@RequestMapping(value = "/admin/users/users")
	public void displayUsers(ModelMap model, @RequestParam(value = "action", required = false) String action,
	                         @RequestParam(value = "name", required = false) String name,
	                         @RequestParam(value = "role", required = false) Role role,
	                         @RequestParam(value = "includeDisabled", required = false) Boolean includeDisabled)
	                                                                                                            throws Exception {
		
		if (Context.isAuthenticated()) {
			List<User> users = getUsers(action, name, role, includeDisabled);
			model.put("users", users);
		}
	}
	
	protected List<User> getUsers(String action, String name, Role role, Boolean includeDisabled) {
		// only do the search if there are search parameters or 
		if (action != null || StringUtils.hasText(name) || role != null) {
			if (includeDisabled == null)
				includeDisabled = false;
			List<Role> roles = null;
			if (role != null && StringUtils.hasText(role.getRole()))
				roles = Collections.singletonList(role);
			
			if (!StringUtils.hasText(name))
				name = null;
			
			return Context.getUserService().getUsers(name, roles, includeDisabled);
		}
		
		return new ArrayList<User>();
		
	}
	
}
