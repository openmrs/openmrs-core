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
