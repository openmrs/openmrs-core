package org.openmrs.layout.name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.layout.LayoutSupport;

public class NameSupport extends LayoutSupport<NameTemplate> {

	private static NameSupport singleton;
	
	static Log log = LogFactory.getLog(NameSupport.class);
	
	public NameSupport() {
		if (singleton == null)
			singleton = this;
	}
	
	public static NameSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else {
			return singleton;			
		}
	}
	
	public String getDefaultLayoutFormat() {
			String ret = Context.getAdministrationService().getGlobalProperty("layout.name.format");
			return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
		}
}
