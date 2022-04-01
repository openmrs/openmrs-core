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

import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Backs the localesAndThemes.jsp page to let the admin change the default locale, default theme,
 * etc
 */
@Controller
public class LocalesAndThemesFormController {
	
	/**
	 * Called for GET requests only on the databaseChangesInfo page. POST page requests are invalid
	 * and ignored.
	 * 
	 * @param model the key value pair that will be accessible from the jsp page
	 * @throws Exception if there is trouble getting the database changes from liquibase
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/admin/maintenance/localesAndThemes")
	public void showPage(ModelMap model) throws Exception {
		String theme = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME);
		model.addAttribute("theme", theme);
		
		String locale = Context.getAdministrationService()
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
		model.addAttribute("locale", locale);
		
		String allowedLocales = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST);
		model.addAttribute("allowedLocales", allowedLocales);
	}
	
	/**
	 * Called upon save of the page
	 * 
	 * @param theme the theme name to save
	 * @param locale the locale to save (en, en_GB, es, etc)
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/maintenance/localesAndThemes")
	public String saveDefaults(@RequestParam("theme") String theme, @RequestParam("locale") String locale) throws Exception {
		
		// save the theme
		GlobalProperty themeGP = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME);
		themeGP.setPropertyValue(theme);
		Context.getAdministrationService().saveGlobalProperty(themeGP);
		
		// save the locale
		GlobalProperty localeGP = Context.getAdministrationService().getGlobalPropertyObject(
		    OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
		localeGP.setPropertyValue(locale);
		Context.getAdministrationService().saveGlobalProperty(localeGP);
		
		return "redirect:/admin/maintenance/localesAndThemes.form";
	}
	
}