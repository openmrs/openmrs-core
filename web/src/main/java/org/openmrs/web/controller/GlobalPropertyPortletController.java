/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;

/**
 * Parameters to specify: * 'propertyPrefix' will limit to only global properties starting with that
 * prefix (e.g. "formentry.") * 'excludePrefix' will exclude global properties starting with that
 * prefix (e.g. "formentry.started") * 'hidePrefix' decides whether or not to trim the prefix on
 * what is displayed (i.e. "formentry.infopath_url" -> "infopath_url") (default false) *
 * 'hideDescription' whether or not to show the global property's description (default: false) *
 * 'title' will display that title * 'showHeader' whether or not to show a header row in the table
 * (default true) Values put in the model: * 'properties' -> List<GlobalProperty>
 */
public class GlobalPropertyPortletController extends PortletController {
	
	/**
	 * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest,
	 *      java.util.Map)
	 * @should exclude multiple prefixes
	 */
	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		if (Context.isAuthenticated()) {
			setupModelForModule(model);
			
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
			
			String[] prefixes = new String[0];
			if (excludePrefix != null) {
				prefixes = excludePrefix.split(";");
			}
			
			List<GlobalProperty> properties = new ArrayList<GlobalProperty>();
			for (GlobalProperty p : Context.getAdministrationService().getAllGlobalProperties()) {
				if (p.getProperty().startsWith(propertyPrefix)) {
					boolean found = false;
					for (String prefix : prefixes) {
						if (p.getProperty().startsWith(prefix)) {
							found = true;
							break;
						}
					}
					if (!found) {
						properties.add(p);
					}
				}
			}
			model.put("properties", properties);
			
			boolean showHeader = true;
			if ("false".equals(model.get("showHeader"))) {
				showHeader = false;
			}
			model.put("showHeader", showHeader);
		}
	}
	
	/**
	 * Sets propertyPrefix to "${forModule}.", hidePrefix to "true" and excludePrefix to
	 * excludePrefix + "${forModule}.started;${forModule}.mandatory" if forModule parameter is
	 * present.
	 *
	 * @should change model if forModule is present
	 * @should not change mode if forModule is not present
	 * @should not override excludePrefix but concatenate
	 */
	protected void setupModelForModule(Map<String, Object> model) {
		if (Context.isAuthenticated()) {
			String forModule = (String) model.get("forModule");
			if ("".equals(forModule)) {
				forModule = null;
				model.put("forModule", forModule);
			}
			
			if (forModule != null) {
				String modulePrefix = forModule + ".";
				model.put("propertyPrefix", modulePrefix);
				model.put("hidePrefix", "true");
				
				String excludePrefix = (String) model.get("excludePrefix");
				if (excludePrefix == null) {
					excludePrefix = "";
				} else {
					excludePrefix += ";";
				}
				model.put("excludePrefix", excludePrefix + modulePrefix + "started;" + modulePrefix + "mandatory");
			}
		}
	}
	
}
