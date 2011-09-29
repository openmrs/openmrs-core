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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.directwebremoting.util.Logger;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Allows to manage settings.
 */
@Controller
@SessionAttributes(value = { SettingsController.Model.SECTIONS })
public class SettingsController {
	
	protected final Logger log = Logger.getLogger(getClass());
	
	public static final class Path {
		
		public static final String SETTINGS = "/admin/maintenance/settings";
	}
	
	public static final class Model {
		
		public static final String SETTINGS_FORM = "settingsForm";
		
		public static final String SHOW = "show";
		
		public static final String SECTIONS = "sections";
	}
	
	@RequestMapping(value = Path.SETTINGS, method = RequestMethod.GET)
	public void showSettings() {
	}
	
	@RequestMapping(value = Path.SETTINGS, method = RequestMethod.POST)
	public void updateSettings(@ModelAttribute(Model.SETTINGS_FORM) SettingsForm settingsForm, HttpSession session) {
		try {
			for (SettingsProperty property : settingsForm.getSettings()) {
				getService().saveGlobalProperty(property.getGlobalProperty());
			}
		}
		catch (Exception e) {
			log.error("Error saving global property", e);
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.not.saved");
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, e.getMessage());
		}
		
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.saved");
		
		// refresh log level from global property(ies)
		OpenmrsUtil.applyLogLevels();
	}
	
	@ModelAttribute(Model.SECTIONS)
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
	
	@ModelAttribute(Model.SETTINGS_FORM)
	public SettingsForm getSettingsForm(@RequestParam(value = Model.SHOW, required = false) String show,
	        @ModelAttribute(Model.SECTIONS) List<String> sections) {
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
