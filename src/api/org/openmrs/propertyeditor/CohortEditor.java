package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class CohortEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public CohortEditor() { }
	
	public void setAsText(String text) throws IllegalArgumentException { 
		if (StringUtils.hasText(text)) {
			try {
				setValue(Context.getCohortService().getCohort(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Cohort not found: " + ex.getMessage());
			}
		} else {
			setValue(null);
		}
	}

	public String getAsText() {
		Cohort c = (Cohort) getValue();
		if (c == null) {
			return "";
		}
		else {
			return c.getCohortId().toString();
		}
	}
	
}
