/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	        @RequestParam(value = "includeDisabled", required = false) Boolean includeDisabled) throws Exception {
		
		if (Context.isAuthenticated()) {
			List<User> users = getUsers(action, name, role, includeDisabled);
			Map<User, Set<Role>> userRolesMap = new HashMap<User, Set<Role>>(users.size());
			for (User user : users) {
				Set<Role> roles = null;
				//only show the searched on role if it is inherited
				if (role != null && !user.getRoles().contains(role)) {
					roles = new LinkedHashSet<Role>();
					roles.add(role);//inherited role should be displayed first
					roles.addAll(user.getAllRoles());
				} else {
					roles = new HashSet<Role>(user.getAllRoles());
					roles.remove(role);//don't display the searched on role
				}
				userRolesMap.put(user, roles);
			}
			model.put("users", users);
			model.put("role", role);
			model.put("userRolesMap", userRolesMap);
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
