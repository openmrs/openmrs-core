package org.openmrs.web.controller.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.name.NameSupport;

public class NameLayoutPortletController extends LayoutPortletController {
	
	private static Log log = LogFactory.getLog(NameLayoutPortletController.class);
	
	protected String getDefaultsPropertyName() {
		return "layout.name.defaults";
	}
	
	protected String getDefaultDivId() {
		return "nameLayoutPortlet";
	}
	
	protected LayoutSupport getLayoutSupportInstance() {
		log.debug("Getting name layout instance");
		return NameSupport.getInstance();
	}
}
