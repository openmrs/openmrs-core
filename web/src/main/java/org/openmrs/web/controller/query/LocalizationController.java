/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.query;

import java.util.Locale;

import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ajax-accessible queries related to localization
 */
@Controller
public class LocalizationController {
	
	@RequestMapping("/q/message")
	@ResponseBody
	public String getMessage(@RequestParam("key") String key, @RequestParam(required = false, value = "locale") Locale locale) {
		String ret;
		if (locale != null) {
			ret = Context.getMessageSourceService().getMessage(key);
		} else {
			ret = Context.getMessageSourceService().getMessage(key, null, locale);
		}
		return (ret == null || key.equals(ret)) ? "" : ret;
	}
}
