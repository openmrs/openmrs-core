/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller behind the webservices module's "settings.jsp" page.
 */
@Controller("webservices.rest.SettingsFormController")
@RequestMapping("/module/webservices/rest/settings")
public class SettingsFormController {
	
	@RequestMapping(method = RequestMethod.GET)
	public void showForm() {
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String handleSubmission(@ModelAttribute("globalPropertiesModel") GlobalPropertiesModel globalPropertiesModel,
	        Errors errors, WebRequest request) {
		globalPropertiesModel.validate(globalPropertiesModel, errors);
		if (errors.hasErrors())
			return null; // show the form again
			
		AdministrationService administrationService = Context.getAdministrationService();
		for (GlobalProperty p : globalPropertiesModel.getProperties()) {
			administrationService.saveGlobalProperty(p);
		}
		
		request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage("general.saved"),
		    RequestAttributes.SCOPE_SESSION);
		return "redirect:settings.form";
	}
	
	/**
	 * @return
	 */
	@ModelAttribute("globalPropertiesModel")
	public GlobalPropertiesModel getModel() {
		List<GlobalProperty> editableProps = new ArrayList<GlobalProperty>();
		
		Set<String> props = new LinkedHashSet<String>();
		props.add(RestConstants.URI_PREFIX_GLOBAL_PROPERTY_NAME);
		props.add(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME);
		props.add(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME);
		props.add(RestConstants.MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME);
		
		//remove the properties we dont want to edit
		for (GlobalProperty gp : Context.getAdministrationService().getGlobalPropertiesByPrefix(RestConstants.MODULE_ID)) {
			if (props.contains(gp.getProperty()))
				editableProps.add(gp);
		}
		
		return new GlobalPropertiesModel(editableProps);
	}
	
	/**
	 * Represents the model object for the form, which is typically used as a wrapper for the list
	 * of global properties list so that spring can bind the properties of the objects in the list.
	 * Also capable of validating itself
	 */
	public class GlobalPropertiesModel implements Validator {
		
		private List<GlobalProperty> properties;
		
		public GlobalPropertiesModel() {
		}
		
		public GlobalPropertiesModel(List<GlobalProperty> properties) {
			this.properties = properties;
		}
		
		/**
		 * @see org.springframework.validation.Validator#supports(java.lang.Class)
		 */
		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(getClass());
		}
		
		/**
		 * @see org.springframework.validation.Validator#validate(java.lang.Object,
		 *      org.springframework.validation.Errors)
		 */
		@Override
		public void validate(Object target, Errors errors) {
			GlobalPropertiesModel model = (GlobalPropertiesModel) target;
			for (int i = 0; i < model.getProperties().size(); ++i) {
				GlobalProperty gp = model.getProperties().get(i);
				if (gp.getProperty().equals(RestConstants.URI_PREFIX_GLOBAL_PROPERTY_NAME)) {
					// TODO validate legal uri prefix
				} else if (gp.getProperty().equals(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME)) {
					// TODO validate legal comma-separated IPv4 or IPv6 addresses, wildcards, etc
				} else if (gp.getProperty().equals(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME)) {
					boolean okay = false;
					try {
						Integer maxResultsAbsoluteVal = Integer.valueOf(model.getProperty(
						    RestConstants.MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME).getPropertyValue());
						if (Integer.valueOf(gp.getPropertyValue()) > 0
						        && Integer.valueOf(gp.getPropertyValue()) <= maxResultsAbsoluteVal) {
							okay = true;
						}
					}
					catch (Exception ex) {}
					if (!okay)
						errors.rejectValue("properties[" + i + "]", RestConstants.MODULE_ID
						        + ".maxResultsDefault.errorMessage");
				} else if (gp.getProperty().equals(RestConstants.MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME)) {
					boolean okay = false;
					try {
						okay = Integer.valueOf(gp.getPropertyValue()) > 0;
					}
					catch (Exception ex) {}
					if (!okay)
						errors.rejectValue("properties[" + i + "]", RestConstants.MODULE_ID
						        + ".maxResultsAbsolute.errorMessage");
				}
			}
		}
		
		/**
		 * Returns the global property for the given propertyName
		 * 
		 * @param propertyName
		 * @return
		 */
		public GlobalProperty getProperty(String propertyName) {
			GlobalProperty prop = null;
			for (GlobalProperty gp : getProperties()) {
				if (gp.getProperty().equals(propertyName)) {
					prop = gp;
					break;
				}
			}
			return prop;
		}
		
		/**
		 * @return
		 */
		public List<GlobalProperty> getProperties() {
			return properties;
		}
		
		/**
		 * @param properties
		 */
		public void setProperties(List<GlobalProperty> properties) {
			this.properties = properties;
		}
	}
	
}
