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
