package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class RoleEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public RoleEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		UserService es = Context.getUserService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(es.getRole(text));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Role not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		Role r = (Role) getValue();
		if (r == null)
			return "";
		else
			return r.getRole();
	}

}
