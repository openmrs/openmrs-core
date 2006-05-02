package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	Context context;
	
	public ConceptEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			ConceptService cs = context.getConceptService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(cs.getConcept(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					log.error(ex);
					throw new IllegalArgumentException("Concept not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		Concept c = (Concept) getValue();
		if (c == null) {
			return "";
		}
		else {
			return c.getConceptId().toString();
		}
	}

}
