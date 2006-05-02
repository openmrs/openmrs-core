package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class UserEditor extends PropertyEditorSupport {

	Context context;
	
	public UserEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			UserService ps = context.getUserService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getUser(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("User not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		User t = (User) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getUserId().toString();
		}
	}

}
