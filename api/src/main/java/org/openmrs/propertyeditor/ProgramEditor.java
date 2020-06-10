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

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass
 * an object back and forth through an html form or other medium. <br>
 * If string value starts with "concept.", then the text after the dot is treated as a concept_id or uuid
 * The name of the concept associated with that id is treated as the name of the program to fetch.
 * <br>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 *
 * @see Program
 */
public class ProgramEditor extends PropertyEditorSupport {
	
	private static final Logger log = LoggerFactory.getLogger(ProgramEditor.class);
	
	public ProgramEditor() {
	}
	
	/**
	 * <strong>Should</strong> set using concept id
	 * <strong>Should</strong> set using concept uuid
	 * <strong>Should</strong> set using program id
	 * <strong>Should</strong> set using program uuid
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				if (text.startsWith("concept.")) {
					Integer conceptId = Integer.valueOf(text.substring(text.indexOf('.') + 1));
					Concept c = Context.getConceptService().getConcept(conceptId);
					setValue(Context.getProgramWorkflowService().getProgramByName(c.getName().getName()));
				} else {
					Integer programId = Integer.valueOf(text);
					setValue(Context.getProgramWorkflowService().getProgram(programId));
				}
			}
			catch (Exception ex) {
				Program p;
				if (text.startsWith("concept.")) {
					Concept c = Context.getConceptService().getConceptByUuid(text.substring(text.indexOf('.') + 1));
					p = Context.getProgramWorkflowService().getProgramByName(c.getName().getName());
				} else {
					p = Context.getProgramWorkflowService().getProgramByUuid(text);
				}
				
				setValue(p);
				if (p == null) {
					log.error("Error setting text: " + text, ex);
					throw new IllegalArgumentException("Program not found: " + text, ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		Program p = (Program) getValue();
		if (p == null) {
			return "";
		} else {
			return p.getProgramId().toString();
		}
	}
	
}
