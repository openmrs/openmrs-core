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
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Property editor for {@link LocationTag}s
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @since 1.7
 */
public class LocationTagEditor extends PropertyEditorSupport {
	
	private static Log log = LogFactory.getLog(LocationTagEditor.class);
	
	public LocationTagEditor() {
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		LocationService ls = Context.getLocationService();
		if (Context.isAuthenticated() && StringUtils.hasText(text)) {
			try {
				setValue(ls.getLocationTag(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				LocationTag locationTag = ls.getLocationTagByUuid(text);
				setValue(locationTag);
				if (locationTag == null) {
					log.error("Error setting text: " + text, ex);
					throw new IllegalArgumentException("LocationTag not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText() {
		LocationTag t = (LocationTag) getValue();
		return t == null ? null : t.getLocationTagId().toString();
	}
	
}
