/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.web.controller;

import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7Util;
import org.openmrs.hl7.Hl7InArchivesMigrateThread;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Processes requests for the page for managing the hl7InArchive migration
 */
@Controller
public class Hl7InArchiveMigrationController {
	
	/**
	 * Adds data to the modelAndView object to be rendered in hl7 archive migrate page
	 * 
	 * @return the modelAndView
	 */
	@RequestMapping(value = "/admin/hl7/hl7InArchiveMigration.htm")
	public ModelAndView renderMigratePage() {
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/hl7/hl7InArchiveMigration");
		modelAndView.addObject("time_out", HL7Constants.THREAD_SLEEP_PERIOD);
		modelAndView.addObject("hl7_archives_dir", HL7Util.getHl7ArchivesDirectory().getAbsolutePath());
		modelAndView.addObject("migration_status", Hl7InArchivesMigrateThread.getTransferStatus().toString());
		modelAndView.addObject("isMigrationRunning", Hl7InArchivesMigrateThread.isActive());
		
		return modelAndView;
	}
	
}
