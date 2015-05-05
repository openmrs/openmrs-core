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

import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.OpenmrsUtil;
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
		model.addAttribute("updateLogFile", OpenmrsUtil.getApplicationDataDirectory()
		        + DatabaseUpdater.DATABASE_UPDATES_LOG_FILE);
		
		// where Spring can find the jsp.  /WEB-INF/view is prepended, and ".jsp" is appended
		return "/admin/maintenance/databaseChangesInfo";
	}
	
}
