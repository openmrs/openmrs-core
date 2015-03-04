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
