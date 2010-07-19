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
package org.openmrs.web.controller.maintenance;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.servlet.LoginServlet;
import org.openmrs.web.user.CurrentUsers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Display the current users logged in the system.
 * 
 * @see CurrentUsers
 * @see LoginServlet
 * @see SessionListener
 */
@Controller
public class CurrentUsersController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Lists current users.
	 * 
	 * @param request
	 * @param modelMap
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/admin/maintenance/currentUsers.list")
	public void listCurrentUsers(HttpServletRequest request, ModelMap modelMap) {
		log.debug("List current users");
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_USERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_USERS);
		
		modelMap.put("currentUsers", CurrentUsers.getCurrentUsernames(request.getSession()));
	}
	
}
