package org.openmrs.layout.address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;

public class AddressSupport extends LayoutSupport<AddressTemplate> {

	private static AddressSupport singleton;
	
	static Log log = LogFactory.getLog(AddressSupport.class);
	
	public AddressSupport() {
		if (singleton == null)
			singleton = this;
		log.debug("Setting singleton: " + singleton);
	}
	
	public static AddressSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else {
			log.debug("Returning singleton: " + singleton);
			return singleton;			
		}
	}
	
	public String getDefaultLayoutFormat() {
			String ret = Context.getAdministrationService().getGlobalProperty("layout.address.format");
			return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
		}
}
