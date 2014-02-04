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
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptReferenceTermEditor extends PropertyEditorSupport {
	
	private final static Log log = LogFactory.getLog(ConceptReferenceTermEditor.class);
	
	public ConceptReferenceTermEditor() {
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("Setting text: " + text);
		if (StringUtils.hasText(text)) {
			try {
				setValue(Context.getConceptService().getConceptReferenceTerm(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("ConceptReferenceTerm not found: " + text, ex);
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		ConceptReferenceTerm term = (ConceptReferenceTerm) getValue();
		if (term == null || term.getConceptReferenceTermId() == null) {
			return "";
		}
		
		return term.getConceptReferenceTermId().toString();
	}
}
