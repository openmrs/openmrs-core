package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PatientIdentifierTypeEditor extends PropertyEditorSupport {

	Context context;
	
	public PatientIdentifierTypeEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			PatientService ps = context.getPatientService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getPatientIdentifierType(Integer.valueOf(text)));
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
		PatientIdentifierType t = (PatientIdentifierType) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getPatientIdentifierTypeId().toString();
		}
	}

}
