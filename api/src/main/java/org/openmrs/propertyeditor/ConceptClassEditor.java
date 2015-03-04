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
import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass
 * an object back and forth through an html form or other medium. <br/>
 * <br/>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @see ConceptClass
 */
public class ConceptClassEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptClassEditor() {
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("Setting text: " + text);
		ConceptService cs = Context.getConceptService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(cs.getConceptClass(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				ConceptClass conceptClass = cs.getConceptClassByUuid(text);
				setValue(conceptClass);
				if (conceptClass == null) {
					throw new IllegalArgumentException("ConceptClass not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		ConceptClass t = (ConceptClass) getValue();
		if (t == null) {
			return "";
		} else {
			return t.getConceptClassId().toString();
		}
	}
	
}
