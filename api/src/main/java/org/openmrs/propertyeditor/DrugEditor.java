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
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass
 * an object back and forth through an html form or other medium. <br/>
 * <br/>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @see Drug
 */
public class DrugEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public DrugEditor() {
	}
	
	/**
	 * Sets the value of the property editor given the drug identifier.
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 * @should set value to the drug with the specified identifier
	 * @should set value to null if given empty string
	 * @should set value to null if given null value
	 * @should set using uuid
	 * @should fail if drug does not exist with non-empty identifier
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		ConceptService es = Context.getConceptService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(es.getDrug(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				Drug drug = es.getDrugByUuid(text);
				setValue(drug);
				if (drug == null) {
					log.error("Error setting text: " + text, ex);
					throw new IllegalArgumentException("Drug not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	/**
	 * Gets the drug identifier associated with this property editor.
	 * 
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 * @should return drug identifier as string when editor has a value
	 * @should return empty string when editor has a null value
	 */
	public String getAsText() {
		Drug d = (Drug) getValue();
		if (d == null) {
			return "";
		} else {
			return d.getDrugId().toString();
		}
	}
	
}
