package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PatientEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public PatientEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		PatientService ps = Context.getPatientService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getPatient(Integer.valueOf(text)));
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
		Patient t = (Patient) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getPatientId().toString();
		}
	}
	

}
