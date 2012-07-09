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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
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
		if (Context.isAuthenticated()) {
			AdministrationService as = Context.getAdministrationService();
			List<GlobalProperty> properties = as.getGlobalPropertiesByPrefix("ajax.dashboard");
			for (GlobalProperty property : properties) {
				if (property.getProperty().equals("ajax.dashboard.overview")) {
					map.put("overviewStatus", getStatus(property.getPropertyValue())[0]);
					map.put("overviewButtonLabel", getStatus(property.getPropertyValue())[1]);
				} else if (property.getProperty().equals("ajax.dashboard.regimens")) {
					map.put("regimensStatus", getStatus(property.getPropertyValue())[0]);
					map.put("regimensButtonLabel", getStatus(property.getPropertyValue())[1]);
				} else if (property.getProperty().equals("ajax.dashboard.encountersvisits")) {
					map.put("visitsEncountersStatus", getStatus(property.getPropertyValue())[0]);
					map.put("visitsEncountersButtonLabel", getStatus(property.getPropertyValue())[1]);
				} else if (property.getProperty().equals("ajax.dashboard.demographics")) {
					map.put("demographicsStatus", getStatus(property.getPropertyValue())[0]);
					map.put("demographicsButtonLabel", getStatus(property.getPropertyValue())[1]);
				} else if (property.getProperty().equals("ajax.dashboard.graphs")) {
					map.put("graphsStatus", getStatus(property.getPropertyValue())[0]);
					map.put("graphsButtonLabel", getStatus(property.getPropertyValue())[1]);
				} else if (property.getProperty().equals("ajax.dashboard.formentry")) {
					map.put("formentryStatus", getStatus(property.getPropertyValue())[0]);
					map.put("formentryButtonLabel", getStatus(property.getPropertyValue())[1]);
				}
			}
		}
		return "/admin/patients/managePatientDashboardForm";
	}
	
	private String[] getStatus(String propertyValue) {
		String[] status = { "", "" };
		if (propertyValue.equals("enabled")) {
			status[0] = source.getMessage("PatientDashboard.status.enabled", null, Context.getLocale());
			status[1] = source.getMessage("PatientDashboard.button.disable", null, Context.getLocale());
		} else if (propertyValue.equals("disabled")) {
			status[0] = source.getMessage("PatientDashboard.status.disabled", null, Context.getLocale());
			status[1] = source.getMessage("PatientDashboard.button.enable", null, Context.getLocale());
		}
		return status;
	}
	
	@Override
	public void setMessageSource(MessageSource messageSource) {
		source = messageSource;
	}
	
}
