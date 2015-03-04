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
