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
package org.openmrs.web.controller.layout;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.LayoutSupport;
import org.openmrs.layout.web.LayoutTemplate;
import org.openmrs.web.controller.PortletController;

public abstract class LayoutPortletController extends PortletController {
	
	private static Log log = LogFactory.getLog(LayoutPortletController.class);

	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		// TODO: this only cached the first name or address template that comes through. I need to cache one of each. 
		String templateName = (String)model.get("layoutTemplateName");
		String thisLayoutName = getDefaultDivId() + "." + templateName;
		if (!thisLayoutName.equals(model.get("cachedLayoutName"))) {
			LayoutSupport layoutSupport = getLayoutSupportInstance();
			LayoutTemplate layoutTemplate = layoutSupport.getDefaultLayoutTemplate();
			
			if ( layoutTemplate == null ) {
				log.debug("Could not get default LayoutTemplate from " + layoutSupport.getClass());
			}
			
			if ( templateName != null ) {
				if ( layoutSupport.getLayoutTemplateByName(templateName) != null ) {
					layoutTemplate = layoutSupport.getLayoutTemplateByName(templateName);
				} else {
					log.debug("unable to get template by the name of " + templateName + ", using default");
				}
			}
			
			// Check global properties for defaults/overrides in the form of n=v,n1=v1, etc
			String customDefaults = Context.getAdministrationService().getGlobalProperty("layout.address.defaults");
			if ( customDefaults != null ) {
				String[] tokens = customDefaults.split(",");
				Map<String,String> elementDefaults = layoutTemplate.getElementDefaults();
	
				for ( String token : tokens ) {
					String[] pair = token.split("=");
					if ( pair.length == 2 ) {
						String name = pair[0];
						String val = pair[1];
						
						if ( elementDefaults == null ) elementDefaults = new HashMap<String,String>();
						elementDefaults.put(name, val);
					} else {
						log.debug("Found invalid token while parsing GlobalProperty address format defaults");
					}
				}
	
				layoutTemplate.setElementDefaults(elementDefaults);
			}
			
			String divName = (String)model.get("portletDivId");
			if ( divName == null ) {
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
