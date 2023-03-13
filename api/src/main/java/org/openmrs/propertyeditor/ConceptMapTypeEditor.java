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

import org.openmrs.ConceptMapType;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ConceptMapTypeEditor extends PropertyEditorSupport {
	
	private static final Logger log = LoggerFactory.getLogger(ConceptMapTypeEditor.class);
	
	public ConceptMapTypeEditor() {
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("Setting text: " + text);
		if (StringUtils.hasText(text)) {
			try {
				setValue(Context.getConceptService().getConceptMapType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				ConceptMapType value = Context.getConceptService().getConceptMapTypeByUuid(text);
				setValue(value);
				if (value == null) {
					throw new IllegalArgumentException("ConceptMapType not found: " + text, ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		ConceptMapType mapType = (ConceptMapType) getValue();
		if (mapType == null || mapType.getConceptMapTypeId() == null) {
			return "";
		}
		
		return mapType.getConceptMapTypeId().toString();
	}
	
}
