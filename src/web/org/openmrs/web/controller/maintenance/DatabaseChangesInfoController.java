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

import org.openmrs.util.DatabaseUpdater;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This backs the maintenance/databaseChangesInfo.jsp page that lists off all changes that have been
 * run by liquibase
 * 
 * @see DatabaseUpdater
 */
@Controller
public class DatabaseChangesInfoController {
	
	/**
	 * Called for GET requests only on the databaseChangesInfo page. POST page requests are invalid
	 * and ignored.
	 * 
	 * @param model the key value pair that will be accessible from the jsp page
	 * @throws Exception if there is trouble getting the database changes from liquibase
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/admin/maintenance/databaseChangesInfo")
	public String showPage(ModelMap model) throws Exception {
		model.addAttribute("databaseChanges", DatabaseUpdater.getDatabaseChanges());
		
		// where Spring can find the jsp.  /WEB-INF/view is prepended, and ".jsp" is appended
		return "/admin/maintenance/databaseChangesInfo";
	}
	
}
