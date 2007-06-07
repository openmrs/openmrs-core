package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class FormEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public FormEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		FormService ps = Context.getFormService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getForm(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Form not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		Form t = (Form) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getFormId().toString();
		}
	}

}
