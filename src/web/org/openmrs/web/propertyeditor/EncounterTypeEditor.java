package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class EncounterTypeEditor extends PropertyEditorSupport {

	Context context;
	
	public EncounterTypeEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			EncounterService ps = context.getEncounterService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getEncounterType(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Encounter Type not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
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
