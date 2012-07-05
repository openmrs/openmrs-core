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
package org.openmrs.web.controller.patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManagePatientDashboardController implements MessageSourceAware {
	
	private MessageSource source;
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * render the patient dashboard model and direct to the view
	 */
	@RequestMapping("/admin/patients/managePatientDashboard.form")
	protected String renderDashboard(ModelMap map) throws Exception {
		map.put("overviewButtonLabel", source.getMessage("PatientDashboard.button.enable", null, Context.getLocale()));
		map.put("regimensButtonLabel", source.getMessage("PatientDashboard.button.disable", null, Context.getLocale()));
		map.put("visitsEncountersButtonLabel", source.getMessage("PatientDashboard.button.disable", null, Context.getLocale()));
		map.put("demographicsButtonLabel", source.getMessage("PatientDashboard.button.enable", null, Context.getLocale()));
		map.put("graphsButtonLabel", source.getMessage("PatientDashboard.button.disable", null, Context.getLocale()));
		map.put("formentryButtonLabel", source.getMessage("PatientDashboard.button.enable", null, Context.getLocale()));
		
		map.put("overviewStatus", source.getMessage("PatientDashboard.status.disabled", null, Context.getLocale()));
		map.put("regimensStatus", source.getMessage("PatientDashboard.status.enabled", null, Context.getLocale()));
		map.put("visitsEncountersStatus", source.getMessage("PatientDashboard.status.enabled", null, Context.getLocale()));
		map.put("demographicsStatus", source.getMessage("PatientDashboard.status.disabled", null, Context.getLocale()));
		map.put("graphsStatus", source.getMessage("PatientDashboard.status.enabled", null, Context.getLocale()));
		map.put("formentryStatus", source.getMessage("PatientDashboard.status.disabled", null, Context.getLocale()));
		
		return "/admin/patients/managePatientDashboardForm";
	}
	
	@Override
	public void setMessageSource(MessageSource messageSource) {
		source = messageSource;
	}
	
}
