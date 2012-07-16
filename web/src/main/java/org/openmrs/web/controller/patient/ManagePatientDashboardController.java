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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				newprops.add(new GlobalProperty("ajax.dashboard.Overview", "Preload"));
				newprops.add(new GlobalProperty("ajax.dashboard.Regimens", "Preload"));
				newprops.add(new GlobalProperty("ajax.dashboard.VisitsEncounters", "Preload"));
				newprops.add(new GlobalProperty("ajax.dashboard.Demographics", "Preload"));
				newprops.add(new GlobalProperty("ajax.dashboard.Graphs", "Preload"));
				newprops.add(new GlobalProperty("ajax.dashboard.FormEntry", "Preload"));
				as.saveGlobalProperties(newprops);
				properties = newprops;
			}
			Map<String, String> ajaxProperties = new HashMap<String, String>();
			Map<String, String> ajaxLabelProperties = new HashMap<String, String>();
			for (GlobalProperty property : properties) {
				String key = property.getProperty().replace(".dashboard.", "");
				ajaxProperties.put(key, property.getPropertyValue());
				ajaxLabelProperties.put(key, getStatus(property.getPropertyValue()));
			}
			map.put("ajaxProperties", ajaxProperties);
			map.put("ajaxLabelProperties", ajaxLabelProperties);
		}
		return "/admin/patients/managePatientDashboardForm";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/patients/savePatientDashboard.form")
	protected String saveTabStatus(WebRequest request, @RequestParam(required = true, value = "tabId") String tabId,
	        @RequestParam(required = true, value = "status") String status) throws Exception {
		if (Context.isAuthenticated()) {
			AdministrationService as = Context.getAdministrationService();
			GlobalProperty property = new GlobalProperty(tabId.replace("ajax", "ajax.dashboard."), status);
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, source.getMessage("PatientDashboard.saved.success", null,
			    Context.getLocale()), WebRequest.SCOPE_SESSION);
			as.saveGlobalProperty(property);
		}
		
		return "redirect:" + "/admin/patients/managePatientDashboard.form";
	}
	
	private String getStatus(String propertyValue) {
		String status = "";
		if (propertyValue.equals("Onclick")) {
			status = source.getMessage("PatientDashboard.status.onclick", null, Context.getLocale());
		} else if (propertyValue.equals("Background")) {
			status = source.getMessage("PatientDashboard.status.background", null, Context.getLocale());
		} else if (propertyValue.equals("Preload")) {
			status = source.getMessage("PatientDashboard.status.preload", null, Context.getLocale());
		}
		return status;
	}
	
	@Override
	public void setMessageSource(MessageSource messageSource) {
		source = messageSource;
	}
	
}
