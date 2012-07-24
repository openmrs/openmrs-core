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
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass
 * an object back and forth through an html form or other medium. <br/>
 * <br/>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @see ConceptSource
 */
public class ConceptSourceEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptSourceEditor() {
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
				setValue(cs.getConceptSource(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				ConceptSource conceptSource = cs.getConceptSourceByUuid(text);
				setValue(conceptSource);
				if (conceptSource == null) {
					log.trace("ConceptSource not found by ID or UUID");
					throw new IllegalArgumentException("ConceptSource not found: " + text, ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		ConceptSource t = (ConceptSource) getValue();
		if (t == null) {
			return "";
		} else {
			return t.getConceptSourceId().toString();
		}
	}
	
}
