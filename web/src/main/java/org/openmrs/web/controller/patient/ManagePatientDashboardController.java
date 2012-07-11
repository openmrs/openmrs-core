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
import org.openmrs.web.WebConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class ManagePatientDashboardController implements MessageSourceAware {
	
	private MessageSource source;
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * render the patient dashboard model and direct to the view
	 */
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/patients/managePatientDashboard.form")
	protected String renderDashboard(ModelMap map) throws Exception {
		if (Context.isAuthenticated()) {
			AdministrationService as = Context.getAdministrationService();
			List<GlobalProperty> properties = as.getGlobalPropertiesByPrefix("ajax.dashboard");
			
			if (properties.isEmpty()) {
				List<GlobalProperty> newprops = new ArrayList<GlobalProperty>();
				newprops.add(new GlobalProperty("ajax.dashboard.overview", "Disabled"));
				newprops.add(new GlobalProperty("ajax.dashboard.regimens", "Onclick"));
				newprops.add(new GlobalProperty("ajax.dashboard.encountersvisits", "Preload"));
				newprops.add(new GlobalProperty("ajax.dashboard.demographics", "Onclick"));
				newprops.add(new GlobalProperty("ajax.dashboard.graphs", "Disabled"));
				newprops.add(new GlobalProperty("ajax.dashboard.formentry", "Preload"));
				as.saveGlobalProperties(newprops);
				properties = newprops;
			}
			for (GlobalProperty property : properties) {
				if (property.getProperty().equals("ajax.dashboard.overview")) {
					map.put("overviewStatus", property.getPropertyValue());
					map.put("overviewStatusLabel", getStatus(property.getPropertyValue()));
				} else if (property.getProperty().equals("ajax.dashboard.regimens")) {
					map.put("regimensStatus", property.getPropertyValue());
					map.put("regimensStatusLabel", getStatus(property.getPropertyValue()));
				} else if (property.getProperty().equals("ajax.dashboard.encountersvisits")) {
					map.put("visitsEncountersStatus", property.getPropertyValue());
					map.put("visitsEncountersStatusLabel", getStatus(property.getPropertyValue()));
				} else if (property.getProperty().equals("ajax.dashboard.demographics")) {
					map.put("demographicsStatus", property.getPropertyValue());
					map.put("demographicsStatusLabel", getStatus(property.getPropertyValue()));
				} else if (property.getProperty().equals("ajax.dashboard.graphs")) {
					map.put("graphsStatus", property.getPropertyValue());
					map.put("graphsStatusLabel", getStatus(property.getPropertyValue()));
				} else if (property.getProperty().equals("ajax.dashboard.formentry")) {
					map.put("formentryStatus", property.getPropertyValue());
					map.put("formentryStatusLabel", getStatus(property.getPropertyValue()));
				}
			}
		}
		return "/admin/patients/managePatientDashboardForm";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/patients/savePatientDashboard.form")
	protected String saveTabStatus(WebRequest request, @RequestParam(required = true, value = "tabId") String tabId,
	        @RequestParam(required = true, value = "status") String status) throws Exception {
		System.out.println("*****" + tabId + "*******" + status);
		if (Context.isAuthenticated()) {
			AdministrationService as = Context.getAdministrationService();
			GlobalProperty property = new GlobalProperty("ajax.dashboard." + tabId, status);
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Saved successfully", WebRequest.SCOPE_SESSION);
			as.saveGlobalProperty(property);
		}
		
		return "redirect:" + "/admin/patients/managePatientDashboard.form";
	}
	
	private String getStatus(String propertyValue) {
		String status = "";
		if (propertyValue.equals("Onclick")) {
			status = source.getMessage("PatientDashboard.status.onclick", null, Context.getLocale());
		} else if (propertyValue.equals("Preload")) {
			status = source.getMessage("PatientDashboard.status.preload", null, Context.getLocale());
		} else if (propertyValue.equals("Disabled")) {
			status = source.getMessage("PatientDashboard.status.disabled", null, Context.getLocale());
		}
		return status;
	}
	
	@Override
	public void setMessageSource(MessageSource messageSource) {
		source = messageSource;
	}
	
}
