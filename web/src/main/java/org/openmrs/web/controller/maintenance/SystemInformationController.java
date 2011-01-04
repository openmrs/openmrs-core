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

import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This backs the maintenance/systemInfo.jsp page that lists off all the system information.
 */
@Controller
public class SystemInformationController {
	
	/**
     * Default constructor used by spring MVC
     */
	public SystemInformationController() {
	}
	
	/**
	 * Called for GET requests only on the systemInfo page.
	 * 
	 * @param model map
	 * @should add openmrs information attribute to the model map
	 * @should add java runtime information attribute to the model map
	 * @should add database information attribute to the model map
	 * @should add memory information attribute to the model map
	 * @should add module information attribute to the model map
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/admin/maintenance/systemInfo")
	public String showPage(ModelMap model) {
		model.addAttribute("systemInfo", Context.getAdministrationService().getSystemInformation());
		return "/admin/maintenance/systemInfo";
	}
	
}
