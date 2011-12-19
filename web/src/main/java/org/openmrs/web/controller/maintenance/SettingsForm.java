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

import java.util.List;

/**
 * Form used by {@link SettingsController}.
 */
public class SettingsForm {
	
	private String section;
	
	private List<SettingsProperty> settings;
	
	/**
	 * @return the section
	 */
	public String getSection() {
		return section;
	}
	
	/**
	 * @param section the section to set
	 */
	public void setSection(String section) {
		this.section = section;
	}
	
	/**
	 * @return the settings
	 */
	public List<SettingsProperty> getSettings() {
		return settings;
	}
	
	/**
	 * @param settings the settings to set
	 */
	public void setSettings(List<SettingsProperty> settings) {
		this.settings = settings;
	}
	
}
