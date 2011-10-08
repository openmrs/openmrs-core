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
