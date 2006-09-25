package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class DrugEditor extends PropertyEditorSupport {

	Context context;
	
	public DrugEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			ConceptService es = context.getConceptService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(es.getDrug(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Drug not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		Drug d = (Drug)getValue();
		if (d == null) {
			return "";
		}
		else {
			return d.getDrugId().toString();
		}
	}
	

}
