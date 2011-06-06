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
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing an object to a string so that Spring knows how to pass
 * an object back and forth through an html form or other medium. <br/>
 * If string value starts with "concept.", then the text after the dot is treated as a concept_id or uuid
 * The name of the concept associated with that id is treated as the name of the program to fetch.
 * <br/>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @see Program
 */
public class ProgramEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public ProgramEditor() {
	}
	
	/**
	 * @should set using concept id
	 * @should set using concept uuid
	 * @should set using program id
	 * @should set using program uuid
	 */
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
				} else
					p = Context.getProgramWorkflowService().getProgramByUuid(text);
				
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
	
	public String getAsText() {
		Program p = (Program) getValue();
		if (p == null) {
			return "";
		} else {
			return p.getProgramId().toString();
		}
	}
	
}
