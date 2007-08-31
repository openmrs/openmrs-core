package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptClassEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptClassEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("Setting text: " + text);
		ConceptService cs = Context.getConceptService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(cs.getConceptClass(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("ConceptClass not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		ConceptClass t = (ConceptClass) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getConceptClassId().toString();
		}
	}

}
