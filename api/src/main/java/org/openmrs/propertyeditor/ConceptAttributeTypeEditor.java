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

import org.openmrs.ConceptAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class ConceptAttributeTypeEditor extends PropertyEditorSupport {
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText() {
		ConceptAttributeType conceptAttributeType = (ConceptAttributeType) getValue();
		return conceptAttributeType == null ? null : conceptAttributeType.getId().toString();
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
	 *
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		ConceptService conceptService = Context.getConceptService();
		if (Context.isAuthenticated() && StringUtils.hasText(text)) {
			try {
				setValue(conceptService.getConceptAttributeType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				ConceptAttributeType conceptAttributeType = conceptService.getConceptAttributeTypeByUuid(text);
				setValue(conceptAttributeType);
				if (conceptAttributeType == null) {
					throw new IllegalArgumentException("ConceptAttributeType not found for " + text, ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
}
