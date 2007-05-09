package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class EncounterTypeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public EncounterTypeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		EncounterService ps = Context.getEncounterService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getEncounterType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Encounter Type not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		EncounterType t = (EncounterType) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getEncounterTypeId().toString();
		}
	}

}
