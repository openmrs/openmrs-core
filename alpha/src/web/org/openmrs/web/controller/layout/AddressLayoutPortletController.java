package org.openmrs.web.controller.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.address.AddressSupport;

public class AddressLayoutPortletController extends LayoutPortletController {
	
	private static Log log = LogFactory.getLog(AddressLayoutPortletController.class);
	
	protected String getDefaultsPropertyName() {
		return "layout.address.defaults";
	}
	
	protected String getDefaultDivId() {
		return "addressLayoutPortlet";
	}
	
	protected LayoutSupport getLayoutSupportInstance() {
		log.debug("Getting address layout instance");
		return AddressSupport.getInstance();
	}
}
