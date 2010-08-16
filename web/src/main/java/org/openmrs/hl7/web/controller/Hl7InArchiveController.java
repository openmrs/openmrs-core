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

package org.openmrs.hl7.web.controller;

import org.openmrs.api.context.Context;
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
public class Hl7InArchiveController {
	
	/**
	 * Adds data to the modelAndView object to be rendered in hl7 archive migrate page
	 * 
	 * @return the modelAndView
	 */
	@RequestMapping(value = "/admin/hl7/hl7InArchive.htm")
	public ModelAndView renderMigratePage() {
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/hl7/hl7InArchive");
		modelAndView.addObject("time_out", HL7Constants.THREAD_SLEEP_PERIOD);
		modelAndView.addObject("hl7_archives_dir", HL7Util.getHl7ArchivesDirectory().getAbsolutePath());
		modelAndView.addObject("migration_status", Hl7InArchivesMigrateThread.getTransferStatus().toString());
		modelAndView.addObject("isMigrationRequired", Context.getHL7Service().isArchiveMigrationRequired());
		
		return modelAndView;
	}
	
}
