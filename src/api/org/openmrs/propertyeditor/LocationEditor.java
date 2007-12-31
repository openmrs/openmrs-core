package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class LocationEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public LocationEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		EncounterService es = Context.getEncounterService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(es.getLocation(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Location not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
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
