package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Tribe;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class TribeEditor extends PropertyEditorSupport {

	Context context;
	
	public TribeEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			PatientService ps = context.getPatientService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getTribe(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Tribe not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		Tribe t = (Tribe) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getTribeId().toString();
		}
	}

}
