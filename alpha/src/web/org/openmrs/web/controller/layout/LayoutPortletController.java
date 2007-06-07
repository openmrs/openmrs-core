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
		
		LayoutSupport layoutSupport = getLayoutSupportInstance();
		LayoutTemplate layoutTemplate = layoutSupport.getDefaultLayoutTemplate();
		
		if ( layoutTemplate == null ) {
			log.debug("Could not get default LayoutTemplate from " + layoutSupport.getClass());
		}
		
		String templateName = (String)model.get("layoutTemplate");
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
	}
	
	protected String getDefaultsPropertyName() {
		return "layout.defaults";
	}
	
	protected String getDefaultDivId() {
		return "layoutPortlet";
	}
	
	protected abstract LayoutSupport getLayoutSupportInstance();
	
}
