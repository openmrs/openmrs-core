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
	
	private static final String GP_THEME_NAME = OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME;
	
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
