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
