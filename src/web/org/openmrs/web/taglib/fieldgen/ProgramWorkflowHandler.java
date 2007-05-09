package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;

public class ProgramWorkflowHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "programWorkflow.field";
	
	public void run() {
		setUrl(defaultUrl);

		if ( fieldGenTag != null ) {
			String initialValue = "";
			checkEmptyVal((ProgramWorkflow)null);
			ProgramWorkflow pw = (ProgramWorkflow)this.fieldGenTag.getVal();
			if ( pw != null ) if ( pw.getProgramWorkflowId() != null ) initialValue = pw.getProgramWorkflowId().toString();
			String optionHeader = "";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if ( optionHeader == null ) optionHeader = "";
			String programPrefix = "false";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				programPrefix = "true".equalsIgnoreCase((String)(this.fieldGenTag.getParameterMap().get("programPrefix"))) ? "true" : "false";
			}

			List<ProgramWorkflow> workflows = new ArrayList<ProgramWorkflow>();
			
			ProgramWorkflowService pws = Context.getProgramWorkflowService();
			List<Program> programs = pws.getPrograms();
			if ( programs != null ) {
				for ( Program program : programs ) {
					Set<ProgramWorkflow> currFlows = program.getWorkflows();
					if ( currFlows != null ) workflows.addAll(currFlows);
				}
			}
			
			setParameter("programPrefix", programPrefix);
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("programWorkflows", workflows);
		}
	}
}
