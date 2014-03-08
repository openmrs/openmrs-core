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

import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.theme.CookieThemeResolver;

/**
 * Customization of the spring theme resolver so that an admin can set a global property for the
 * default locale for the system
 *
 * This class will not throw an exception even if there is a problem retrieving that global property, since that
 * prevent even an error page from being displayed properly
 */
public class OpenmrsCookieThemeResolver extends CookieThemeResolver {
	
	private final String GP_THEME_NAME = OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME;
	
	/**
	 * Return the name of the default theme.
	 */
	@Override
	public String getDefaultThemeName() {
		
		// check the global properties for the current theme
		String themeName = null;
		boolean openedSession = false;
		try {
			if (!Context.isSessionOpen()) {
				// only try to open a session if one isn't open for us already
				openedSession = true;
				Context.openSession();
			}
			
			// check the admin-set global property for the theme
			themeName = Context.getAdministrationService().getGlobalProperty(GP_THEME_NAME);
		}
		catch (Exception ex) {
			// We must not throw an exception here, since this code is called from every page including the
			// for uncaught exceptions. Therefore we pass, and fall back to default behavior.
		}
		finally {
			// only close the session if we opened it
			if (openedSession) {
				Context.closeSession();
			}
		}
		
		if (StringUtils.hasText(themeName)) {
			return themeName;
		} else {
			return super.getDefaultThemeName();
		}
	}
}
