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
package org.openmrs.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;

/**
 * Parameters to specify:
 *    * 'propertyPrefix' will limit to only global properties starting with that prefix (e.g. "formentry.")
 *    * 'excludePrefix' will exclude global properties starting with that prefix (e.g. "formentry.started")
 *	  * 'hidePrefix' decides whether or not to trim the prefix on what is displayed (i.e. "formentry.infopath_url" -> "infopath_url") (default false)
 *	  * 'hideDescription' whether or not to show the global property's description (default: false)
 *    * 'title' will display that title
 *    * 'showHeader' whether or not to show a header row in the table (default true)
 * Values put in the model:
 *    * 'properties' -> List<GlobalProperty>
 */
public class GlobalPropertyPortletController extends PortletController {

	@Override
    protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		if (Context.isAuthenticated()) {
			String propertyPrefix = (String) model.get("propertyPrefix");
			if (propertyPrefix == null) {
				propertyPrefix = "";
				model.put("propertyPrefix", propertyPrefix);
			}
			String excludePrefix = (String) model.get("excludePrefix");
			if ("".equals(excludePrefix)) {
				excludePrefix = null;
				model.put("excludePrefix", excludePrefix);
			}
			List<GlobalProperty> properties = new ArrayList<GlobalProperty>();
			for (GlobalProperty p : Context.getAdministrationService().getAllGlobalProperties()) {
				if (p.getProperty().startsWith(propertyPrefix)
						&& (excludePrefix == null || !p.getProperty().startsWith(excludePrefix)) ) {
					properties.add(p);
				}
			}
			model.put("properties", properties);
			
			boolean showHeader = true;
			if ("false".equals(model.get("showHeader")))
				showHeader = false;
			model.put("showHeader", showHeader);
		}
    }

	
	
}
