package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class DrugEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public DrugEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		ConceptService es = Context.getConceptService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(es.getDrug(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Drug not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		Drug d = (Drug)getValue();
		if (d == null) {
			return "";
		}
		else {
			return d.getDrugId().toString();
		}
	}
	

}
