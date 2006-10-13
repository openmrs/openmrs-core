package org.openmrs.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.address.AddressSupport;
import org.openmrs.address.AddressTemplate;
import org.openmrs.api.context.Context;

public class AddressPortletController extends PortletController {
	
	private static Log log = LogFactory.getLog(AddressPortletController.class);

	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {

		AddressSupport as = AddressSupport.getInstance();
		
		AddressTemplate at = as.getDefaultAddressTemplate();
		if ( at == null ) {
			log.debug("Could not get default Address Template from AddressSupport");
		} 
		
		String templateName = (String)model.get("addressTemplate");
		if ( templateName != null ) {
			if ( as.getAddressTemplateByName(templateName) != null ) {
				at = as.getAddressTemplateByName(templateName);
			} else {
				log.debug("unable to get template by the name of " + templateName + ", using default");
			}
		}
		
		// Check global properties for defaults/overrides in the form of n=v,n1=v1, etc
		String customDefaults = Context.getAdministrationService().getGlobalProperty("address.defaults");
		if ( customDefaults != null ) {
			String[] tokens = customDefaults.split(",");
			Map<String,String> elementDefaults = at.getElementDefaults();

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

			at.setElementDefaults(elementDefaults);
		}
		
		String divName = (String)model.get("model.addressDivName");
		if ( divName == null ) {
			model.put("model.addressDivName", "patientAddressPortlet");
		}
		
		model.put("addressTemplate", at);
	}
}
