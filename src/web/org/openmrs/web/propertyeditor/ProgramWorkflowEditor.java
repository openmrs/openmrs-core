package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ProgramWorkflowEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ProgramWorkflowEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		ProgramWorkflowService pws = Context.getProgramWorkflowService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(pws.getWorkflow(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text" + text, ex);
				throw new IllegalArgumentException("Program Workflow not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		ProgramWorkflow pw = (ProgramWorkflow)getValue();
		if (pw == null) {
			return "";
		}
		else {
			return pw.getProgramWorkflowId().toString();
		}
	}

}
