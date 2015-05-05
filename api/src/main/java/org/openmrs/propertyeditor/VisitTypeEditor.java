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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * {@link PropertyEditor} for {@link VisitType}
 * 
 * @since 1.9
 */
public class VisitTypeEditor extends PropertyEditorSupport {
	
	private static final Log log = LogFactory.getLog(VisitTypeEditor.class);
	
	public VisitTypeEditor() {
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		VisitService vs = Context.getVisitService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(vs.getVisitType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				VisitType v = vs.getVisitTypeByUuid(text);
				setValue(v);
				if (v == null) {
					throw new IllegalArgumentException("Visit Type not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		VisitType vt = (VisitType) getValue();
		if (vt == null) {
			return "";
		} else {
			return vt.getVisitTypeId().toString();
		}
	}
	
}
