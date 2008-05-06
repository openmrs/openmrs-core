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
import org.openmrs.Tribe;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class TribeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public TribeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		PatientService ps = Context.getPatientService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getTribe(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Tribe not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		Tribe t = (Tribe) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getTribeId().toString();
		}
	}

}
