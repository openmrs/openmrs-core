package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class FormEditor extends PropertyEditorSupport {

	Context context;
	
	public FormEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			FormService ps = context.getFormService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getForm(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Form not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
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
