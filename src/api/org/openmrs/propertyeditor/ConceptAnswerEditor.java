package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptAnswerEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptAnswerEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		ConceptService cs = Context.getConceptService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(cs.getConceptAnswer(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text" + text, ex);
				throw new IllegalArgumentException("Concept not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		ConceptAnswer c = (ConceptAnswer)getValue();
		if (c == null) {
			return "";
		}
		else {
			return c.getConceptAnswerId().toString();
		}
	}

}
