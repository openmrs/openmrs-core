/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.LocaleAndThemeConfiguration;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/localeandthemeconfiguration")
public class LocaleAndThemeConfigurationController2_0 extends BaseRestController {

	@RequestMapping(method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Object getCurrentConfiguration() {
		AdministrationService administrationService = Context.getAdministrationService();
		String theme = administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME);
		String locale = administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);

		LocaleAndThemeConfiguration configuration = new LocaleAndThemeConfiguration();
		configuration.setDefaultTheme(theme);
		configuration.setDefaultLocale(locale);

		return ConversionUtil.convertToRepresentation(configuration, Representation.FULL);
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void updateCurrentConfiguration(@RequestBody LocaleAndThemeConfiguration newConfiguration) {
		AdministrationService administrationService = Context.getAdministrationService();
		String theme = newConfiguration.getDefaultTheme();
		String locale = newConfiguration.getDefaultLocale();

		administrationService.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_THEME, theme);
		administrationService.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, locale);
	}
}
