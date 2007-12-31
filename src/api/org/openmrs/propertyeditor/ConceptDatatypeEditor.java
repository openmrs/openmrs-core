package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptDatatypeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptDatatypeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("setting text: " + text);
		ConceptService cs = Context.getConceptService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(cs.getConceptDatatype(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("ConceptDatatype not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		ConceptDatatype t = (ConceptDatatype) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getConceptDatatypeId().toString();
		}
	}

}
