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
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * {@link PropertyEditor} for {@link Visit}
 *
 * @since 1.9
 */
public class VisitEditor extends PropertyEditorSupport {
	
	private static final Log log = LogFactory.getLog(VisitEditor.class);
	
	public VisitEditor() {
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		VisitService vs = Context.getVisitService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(vs.getVisit(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				Visit v = vs.getVisitByUuid(text);
				setValue(v);
				if (v == null) {
					throw new IllegalArgumentException("Visit not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		Visit v = (Visit) getValue();
		if (v == null) {
			return "";
		} else {
			return v.getVisitId().toString();
		}
	}
	
}
