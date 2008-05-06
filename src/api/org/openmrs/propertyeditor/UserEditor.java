/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class UserEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public UserEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		UserService ps = Context.getUserService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getUser(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("User not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
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
