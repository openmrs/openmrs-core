package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PersonAttributeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());

	public void setAsText(String text) throws IllegalArgumentException {
		PersonService ps = Context.getPersonService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getPersonAttribute(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Person Attribute Type not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		PersonAttribute t = (PersonAttribute)getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getPersonAttributeId().toString();
		}
	}
	
}
