package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class EncounterEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public EncounterEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		EncounterService es = Context.getEncounterService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(es.getEncounter(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Encounter not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		Encounter e = (Encounter)getValue();
		if (e == null) {
			return "";
		}
		else {
			return e.getEncounterId().toString();
		}
	}
	

}
