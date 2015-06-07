/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.SimpleTheme;

/**
 * A static messageSource theme that can be used as the parent theme source
 * 
 * @see OpenmrsCookieThemeResolver
 * @see org.springframework.ui.context.support.ResourceBundleThemeSource
 */
public class StaticThemeSource implements ThemeSource {
	
	private String themeName;
	
	/**
	 * @see org.springframework.ui.context.ThemeSource#getTheme(java.lang.String)
	 */
	@Override
	public Theme getTheme(String ignoredArgument) {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(this.themeName);
		return new SimpleTheme(themeName, messageSource);
	}
	
	/**
	 * @param themeName the themeName to set
	 */
	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}
	
}
