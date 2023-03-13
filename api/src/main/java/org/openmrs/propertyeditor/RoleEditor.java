/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass
 * an object back and forth through an html form or other medium. <br>
 * <br>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 *
 * @see Role
 */
public class RoleEditor extends PropertyEditorSupport {
	
	private static final Logger log = LoggerFactory.getLogger(RoleEditor.class);
	
	public RoleEditor() {
	}
	
	/**
	 * <strong>Should</strong> set using name
	 * <strong>Should</strong> set using uuid
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		UserService es = Context.getUserService();
		if (StringUtils.hasText(text)) {
			try {
				Role r = es.getRole(text);
				setValue(r);
				//when a role is not found, no exception is generated. throw one to execute the catch block
				if (r == null) {
					throw new Exception();
				}
			}
			catch (Exception ex) {
				Role r = es.getRoleByUuid(text);
				setValue(r);
				if (r == null) {
					log.error("Error setting text: " + text, ex);
					throw new IllegalArgumentException("Role not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		Role r = (Role) getValue();
		if (r == null) {
			return "";
		} else {
			return r.getRole();
		}
	}
	
}
