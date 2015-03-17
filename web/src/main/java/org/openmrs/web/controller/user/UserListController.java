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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			
			Map<User, Set<Role>> userInheritanceLineMap = new HashMap<User, Set<Role>>(users.size());
			Set<Role> inheritanceLineRoles = new LinkedHashSet<Role>();
			List<Role> helpList = new ArrayList<Role>();
			
			for (User user : users) {
				Set<Role> roles = new LinkedHashSet<Role>();
				if (role != null && !user.getRoles().contains(role)) {
					// condition -> user has role only via inheritance
					inheritanceLineRoles.add(role);
					for (Role r : user.getRoles()) {
						if (r.getAllParentRoles().contains(role)) {
							// condition -> r = role that inherits from filtered role
							roles.add(r);
							helpList.addAll(role.getChildRoles());
							Role r2;
							for (int i = 0; i < helpList.size(); i++) {
								r2 = helpList.get(i);
								if (r2.getAllChildRoles().contains(r)) {
									// condition -> finding first child role that contains role, which inherits from filtered role
									inheritanceLineRoles.add(r2);
									helpList.clear();
									helpList.addAll(r2.getAllChildRoles());
									i = -1;
								} else if (r2.equals(r)) {
									inheritanceLineRoles.add(r2);
									break;
								}
							}
						}
					}
					userInheritanceLineMap.put(user, inheritanceLineRoles);
				} else if (role != null && user.getRoles().contains(role)) {
					// adding searched role on the first place for simplicity of dealing with it in JSTL
					roles.add(role);
				}
				
				roles.addAll(user.getRoles());
				userRolesMap.put(user, roles);
			}
			model.put("users", users);
			model.put("role", role);
			model.put("userInheritanceLineMap", userInheritanceLineMap);
			model.put("userRolesMap", userRolesMap);
		}
	}
	
	protected List<User> getUsers(String action, String name, Role role, Boolean includeDisabled) {
		// only do the search if there are search parameters or 
		if (action != null || StringUtils.hasText(name) || role != null) {
			if (includeDisabled == null) {
				includeDisabled = false;
			}
			List<Role> roles = null;
			if (role != null && StringUtils.hasText(role.getRole())) {
				roles = Collections.singletonList(role);
			}
			
			if (!StringUtils.hasText(name)) {
				name = null;
			}
			
			return Context.getUserService().getUsers(name, roles, includeDisabled);
		}
		
		return new ArrayList<User>();
		
	}
	
}
