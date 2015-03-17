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

import org.apache.commons.lang.StringUtils;
import org.openmrs.GlobalProperty;

/**
 * Wrapper for {@link GlobalProperty} used in {@link SettingsForm}.
 */
public class SettingsProperty implements Comparable<SettingsProperty> {
	
	public static final String GENERAL = "General Settings";
	
	private GlobalProperty globalProperty;
	
	public SettingsProperty(GlobalProperty globalProperty) {
		this.globalProperty = globalProperty;
	}
	
	/**
	 * @return the section
	 */
	public String getSection() {
		String section = GENERAL;
		int sectionEnd = globalProperty.getProperty().indexOf(".");
		if (sectionEnd > 0) {
			section = globalProperty.getProperty().substring(0, sectionEnd);
			section = beautify(section);
		}
		
		return section;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		String name = globalProperty.getProperty();
		int sectionEnd = globalProperty.getProperty().indexOf(".");
		if (sectionEnd > 0) {
			name = globalProperty.getProperty().substring(sectionEnd + 1);
		}
		
		name = beautify(name);
		
		return name;
	}
	
	/**
	 * @return the globalProperty
	 */
	public GlobalProperty getGlobalProperty() {
		return globalProperty;
	}
	
	/**
	 * @param globalProperty the globalProperty to set
	 */
	public void setGlobalProperty(GlobalProperty globalProperty) {
		this.globalProperty = globalProperty;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SettingsProperty o) {
		return globalProperty.getProperty().compareTo(o.globalProperty.getProperty());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return globalProperty.hashCode();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SettingsProperty)) {
			return false;
		}
		SettingsProperty other = (SettingsProperty) obj;
		return compareTo(other) == 0;
	}
	
	/**
	 * Beautifies string
	 *
	 * @param section
	 * @return
	 */
	private String beautify(String section) {
		section = section.replace("_", " ");
		section = section.replace(".", " ");
		
		String[] sections = StringUtils.splitByCharacterTypeCamelCase(section);
		section = StringUtils.join(sections, " ");
		
		sections = StringUtils.split(section);
		for (int i = 0; i < sections.length; i++) {
			sections[i] = StringUtils.capitalize(sections[i]);
		}
		section = StringUtils.join(sections, " ");
		
		return section;
	}
	
}
