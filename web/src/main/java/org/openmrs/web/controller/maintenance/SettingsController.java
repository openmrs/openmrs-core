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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.directwebremoting.util.Logger;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.attribute.WebAttributeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Allows to manage settings.
 */
@Controller
public class SettingsController {
	
	protected final Logger log = Logger.getLogger(getClass());
	
	public static final String SETTINGS_PATH = "/admin/maintenance/settings";
	
	public static final String SETTINGS_FORM = "settingsForm";
	
	public static final String SHOW = "show";
	
	public static final String SECTIONS = "sections";
	
	@RequestMapping(value = SETTINGS_PATH, method = RequestMethod.GET)
	public void showSettings() {
	}
	
	@RequestMapping(value = SETTINGS_PATH, method = RequestMethod.POST)
	public void updateSettings(@ModelAttribute(SETTINGS_FORM) SettingsForm settingsForm, Errors errors,
	        HttpServletRequest request, HttpSession session) {
		
		List<GlobalProperty> toSave = new ArrayList<GlobalProperty>();
		try {
			for (int i = 0; i < settingsForm.getSettings().size(); ++i) {
				SettingsProperty property = settingsForm.getSettings().get(i);
				if (StringUtils.isNotEmpty(property.getGlobalProperty().getDatatypeClassname())) {
					// we need to handle the submitted value with the appropriate widget
					CustomDatatype dt = CustomDatatypeUtil.getDatatypeOrDefault(property.getGlobalProperty());
					CustomDatatypeHandler handler = CustomDatatypeUtil.getHandler(property.getGlobalProperty());
					if (handler != null) {
						try {
							Object value = WebAttributeUtil.getValue(request, dt, handler, "settings[" + i
							        + "].globalProperty.propertyValue");
							property.getGlobalProperty().setValue(value);
						}
						catch (Exception ex) {
							String originalValue = request.getParameter("originalValue[" + i + "]");
							property.getGlobalProperty().setPropertyValue(originalValue);
							errors.rejectValue("settings[" + i + "].globalProperty.propertyValue", "general.invalid");
						}
					}
				}
				toSave.add(property.getGlobalProperty());
			}
		}
		catch (Exception e) {
			log.error("Error saving global property", e);
			errors.reject("GlobalProperty.not.saved");
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
		}
		
		if (errors.hasErrors()) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "GlobalProperty.not.saved");
			
		} else {
			try {
				for (GlobalProperty gp : toSave) {
					getService().saveGlobalProperty(gp);
				}
				session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.saved");
			}
			catch (APIException e) {
				errors.reject("GlobalProperty.not.saved");
				session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			}
			// TODO: move this to a GlobalPropertyListener
			// refresh log level from global property(ies)
			OpenmrsUtil.applyLogLevels();
			
			OpenmrsUtil.setupLogAppenders();
		}
		
	}
	
	@ModelAttribute(SECTIONS)
	public List<String> getSections() {
		SortedSet<String> sortedSections = new TreeSet<String>();
		List<GlobalProperty> globalProperties = getService().getAllGlobalProperties();
		for (GlobalProperty globalProperty : globalProperties) {
			SettingsProperty property = new SettingsProperty(globalProperty);
			if (!isHidden(property)) {
				sortedSections.add(property.getSection());
			}
		}
		
		List<String> sections = new ArrayList<String>();
		if (sortedSections.remove(SettingsProperty.GENERAL)) {
			sections.add(SettingsProperty.GENERAL);
		}
		sections.addAll(sortedSections);
		
		return sections;
	}
	
	@ModelAttribute(SETTINGS_FORM)
	public SettingsForm getSettingsForm(@RequestParam(value = SHOW, required = false) String show) {
		SettingsForm settingsForm = new SettingsForm();
		if (show == null && settingsForm.getSection() == null) {
			show = SettingsProperty.GENERAL;
		}
		if (show != null) {
			settingsForm.setSection(show);
			settingsForm.setSettings(getSettings(show));
		}
		return settingsForm;
	}
	
	public List<SettingsProperty> getSettings(String section) {
		List<SettingsProperty> settings = new ArrayList<SettingsProperty>();
		
		List<GlobalProperty> globalProperties = getService().getAllGlobalProperties();
		for (GlobalProperty globalProperty : globalProperties) {
			SettingsProperty property = new SettingsProperty(globalProperty);
			
			if (section.equals(property.getSection()) && !isHidden(property)) {
				settings.add(property);
			}
		}
		
		Collections.sort(settings);
		
		return settings;
	}
	
	/**
	 * @param settingsProperty
	 * @return <code>true</code> if the property should be hidden from the user
	 */
	private boolean isHidden(SettingsProperty settingsProperty) {
		if (settingsProperty.getName().equals("Started")) {
			return true;
		} else if (settingsProperty.getName().equals("Mandatory")) {
			return true;
		} else if (settingsProperty.getName().equals("Database Version")) {
			return true;
		}
		return false;
	}
	
	private AdministrationService getService() {
		return Context.getAdministrationService();
	}
}
