/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
