package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PersonAttributeTypeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());

	public void setAsText(String text) throws IllegalArgumentException {
		PersonService ps = Context.getPersonService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getPersonAttributeType(Integer.valueOf(text)));
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
		PersonAttributeType t = (PersonAttributeType) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getPersonAttributeTypeId().toString();
		}
	}
	
}
