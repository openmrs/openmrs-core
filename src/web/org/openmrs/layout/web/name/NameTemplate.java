package org.openmrs.layout.web.name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.layout.web.LayoutSupport;
import org.openmrs.layout.web.LayoutTemplate;

public class NameTemplate extends LayoutTemplate {
	private static Log log = LogFactory.getLog(NameTemplate.class);
	
	public String getLayoutToken() {
		return "IS_NAME_TOKEN";
	}
	
	public String getNonLayoutToken() {
		return "IS_NOT_NAME_TOKEN";
	}

	@Override
	public LayoutSupport getLayoutSupportInstance() {
		return NameSupport.getInstance();
	}
	
	
}
