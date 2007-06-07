package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Tribe;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class TribeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public TribeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		PatientService ps = Context.getPatientService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getTribe(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Tribe not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
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
