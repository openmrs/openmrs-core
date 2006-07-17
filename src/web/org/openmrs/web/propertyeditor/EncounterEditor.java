package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class EncounterEditor extends PropertyEditorSupport {

	Context context;
	
	public EncounterEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			EncounterService es = context.getEncounterService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(es.getEncounter(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Encounter not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
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
