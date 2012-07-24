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
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing a Patient object to a string so that Spring knows how to
 * pass a Person back and forth through an html form or other medium
 * <br/>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 *
 * @see Patient
 */
public class PatientEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @should set using id
	 * @should set using uuid
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		PatientService ps = Context.getPatientService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getPatient(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				Patient patient = ps.getPatientByUuid(text);
				setValue(patient);
				if (patient == null) {
					log.error("Error setting text: " + text, ex);
					throw new IllegalArgumentException("Patient not found: " + ex.getMessage());
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
		Patient t = (Patient) getValue();
		if (t == null) {
			return "";
		} else {
			return t.getPatientId().toString();
		}
	}
	
}
