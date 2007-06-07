package org.openmrs.layout.address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.LayoutTemplate;

public class AddressTemplate extends LayoutTemplate {
	private static Log log = LogFactory.getLog(AddressTemplate.class);
	
	public String getLayoutToken() {
		return "IS_ADDR_TOKEN";
	}
	
	public String getNonLayoutToken() {
		return "IS_NOT_ADDR_TOKEN";
	}

	@Override
	public LayoutSupport getLayoutSupportInstance() {
		return AddressSupport.getInstance();
	}
	
}
