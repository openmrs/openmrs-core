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
