package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class LocationEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	Context context;
	
	public LocationEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			PatientService ps = context.getPatientService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getLocation(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Location not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		Location t = (Location) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getLocationId().toString();
		}
	}

}
