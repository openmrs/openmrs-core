package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ProgramWorkflowStateEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ProgramWorkflowStateEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		ProgramWorkflowService pws = Context.getProgramWorkflowService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(pws.getState(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text" + text, ex);
				throw new IllegalArgumentException("Program Workflow State not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		ProgramWorkflowState pws = (ProgramWorkflowState)getValue();
		if (pws == null) {
			return "";
		}
		else {
			return pws.getProgramWorkflowStateId().toString();
		}
	}

}
