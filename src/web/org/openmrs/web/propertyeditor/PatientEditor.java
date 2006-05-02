package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PatientEditor extends PropertyEditorSupport {

	Context context;
	
	public PatientEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			PatientService ps = context.getPatientService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getPatient(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Identifier Type not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		Patient t = (Patient) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getPatientId().toString();
		}
	}
	

}
