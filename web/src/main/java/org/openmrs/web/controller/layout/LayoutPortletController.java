/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.layout;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.LayoutTemplate;
import org.openmrs.web.controller.PortletController;

public abstract class LayoutPortletController extends PortletController {
	
	private static Log log = LogFactory.getLog(LayoutPortletController.class);
	
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		// TODO: this only cached the first name or address template that comes through. I need to cache one of each. 
		String templateName = (String) model.get("layoutTemplateName");
		String thisLayoutName = getDefaultDivId() + "." + templateName;
		if (!thisLayoutName.equals(model.get("cachedLayoutName"))) {
			LayoutSupport layoutSupport = getLayoutSupportInstance();
			LayoutTemplate layoutTemplate = layoutSupport.getDefaultLayoutTemplate();
			
			if (layoutTemplate == null) {
				log.debug("Could not get default LayoutTemplate from " + layoutSupport.getClass());
			}
			
			if (templateName != null) {
				if (layoutSupport.getLayoutTemplateByName(templateName) != null) {
					layoutTemplate = layoutSupport.getLayoutTemplateByName(templateName);
				} else {
					log.debug("unable to get template by the name of " + templateName + ", using default");
				}
			}
			
			// Check global properties for defaults/overrides in the form of n=v,n1=v1, etc
			String customDefaults = Context.getAdministrationService().getGlobalProperty("layout.address.defaults");
			if (customDefaults != null) {
				String[] tokens = customDefaults.split(",");
				Map<String, String> elementDefaults = layoutTemplate.getElementDefaults();
				
				for (String token : tokens) {
					String[] pair = token.split("=");
					if (pair.length == 2) {
						String name = pair[0];
						String val = pair[1];
						
						if (elementDefaults == null) {
							elementDefaults = new HashMap<String, String>();
						}
						elementDefaults.put(name, val);
					} else {
						log.debug("Found invalid token while parsing GlobalProperty address format defaults");
					}
				}
				
				layoutTemplate.setElementDefaults(elementDefaults);
			}
			
			String divName = (String) model.get("portletDivId");
			if (divName == null) {
				model.put("portletDivId", getDefaultDivId());
			}
			
			model.put("layoutTemplate", layoutTemplate);
			model.put("layoutTemplateName", templateName);
			model.put("cachedLayoutName", thisLayoutName);
		}
	}
	
	protected String getDefaultsPropertyName() {
		return "layout.defaults";
	}
	
	protected String getDefaultDivId() {
		return "layoutPortlet";
	}
	
	protected abstract LayoutSupport getLayoutSupportInstance();
	
}
