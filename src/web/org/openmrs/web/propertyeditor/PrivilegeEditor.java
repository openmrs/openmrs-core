package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class PrivilegeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public PrivilegeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		UserService es = Context.getUserService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(es.getPrivilege(text));
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
		Privilege p = (Privilege) getValue();
		if (p == null)
			return "";
		else
			return p.getPrivilege();
	}

}
