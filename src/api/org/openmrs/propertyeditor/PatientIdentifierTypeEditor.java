package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PatientIdentifierTypeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public PatientIdentifierTypeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		PatientService ps = Context.getPatientService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getPatientIdentifierType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Identifier Type not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
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
