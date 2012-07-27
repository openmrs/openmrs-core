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
