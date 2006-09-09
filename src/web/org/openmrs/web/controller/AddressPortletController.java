package org.openmrs.web.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.address.AddressSupport;
import org.openmrs.address.AddressTemplate;
import org.openmrs.api.context.Context;

public class AddressPortletController extends PortletController {
	
	private static Log log = LogFactory.getLog(AddressPortletController.class);

	protected void populateModel(HttpServletRequest request, Context context, Map<String, Object> model) {

		AddressSupport as = AddressSupport.getInstance();
		
		AddressTemplate at = as.getDefaultAddressTemplate();
		if ( at == null ) {
			log.debug("Could not get default Address Template from AddressSupport");
		} else {
			log.debug("Able to Address Template: " + at.getDisplayName() + " from AddressSupport");
		}
		
		String templateName = (String)model.get("addressTemplate");
		if ( templateName != null ) {
			if ( as.getAddressTemplateByName(templateName) != null ) {
				at = as.getAddressTemplateByName(templateName);
			} else {
				log.debug("unable to get template by the name of " + templateName + ", using default");
			}
		}
		
		String divName = (String)model.get("model.addressDivName");
		if ( divName == null ) {
			model.put("model.addressDivName", "patientAddressPortlet");
		}
		
		model.put("addressTemplate", at);
	}

}
