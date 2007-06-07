package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ProgramEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ProgramEditor() { }
	
	public void setAsText(String text) throws IllegalArgumentException { 
		if (StringUtils.hasText(text)) {
			try {
				if (text.startsWith("concept.")) {
					Integer conceptId = Integer.valueOf(text.substring(text.indexOf('.') + 1));
					Concept c = Context.getConceptService().getConcept(conceptId);
					setValue(Context.getProgramWorkflowService().getProgram(c.getName().getName()));
				} else {
					Integer programId = Integer.valueOf(text);
					setValue(Context.getProgramWorkflowService().getProgram(programId));
				}
			} catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Program not found: " + text, ex);
			}
		} else {
			setValue(null);
		}
	}

	public String getAsText() {
		Program p = (Program) getValue();
		if (p == null) {
			return "";
		}
		else {
			return p.getProgramId().toString();
		}
	}

}
