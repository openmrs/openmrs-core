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
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class WorkflowCollectionEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public WorkflowCollectionEditor() {	}
	
	/**
	 * Takes a "program_id:list"
	 * where program_id is the id of the program that this collection is for (or not present, if it's a new program)
	 * and list is a space-separated list of concept ids.
	 * This class is a bit of a hack, because I don't know a better way to do this. -DJ
	 * The purpose is to void and unvoid workflows where possible rather than deleting and creating them.  
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			ConceptService cs = Context.getConceptService();
			ProgramWorkflowService pws = Context.getProgramWorkflowService();
			Program program = null;
			try {
				int ind = text.indexOf(":");
				String progIdStr = text.substring(0, ind);
				text = text.substring(ind + 1);
				program = pws.getProgram(Integer.valueOf(progIdStr));
			} catch (Exception ex) { }
			
			String[] conceptIds = text.split(" ");
			Set<ProgramWorkflow> oldSet = program == null ? new HashSet<ProgramWorkflow>() : program.getWorkflows();
			Set<Integer> newConceptIds = new HashSet<Integer>();
			
			for (String id : conceptIds) {
				if (id.trim().length() == 0)
					continue;
				log.debug("trying " + id);
				newConceptIds.add(Integer.valueOf(id.trim()));
			}
			
			// go through oldSet and see what we need to keep and what we need to unvoid
			Set<Integer> alreadyDone = new HashSet<Integer>();
			Set<ProgramWorkflow> newSet = new HashSet<ProgramWorkflow>();
			for (ProgramWorkflow pw : oldSet) {
				if (!newConceptIds.contains(pw.getConcept().getConceptId())) {
					pw.setVoided(true);
				} else if (pw.getVoided()) { // && newConceptIds.contains(pw...)
					pw.setVoided(false);
				}
				newSet.add(pw);
				alreadyDone.add(pw.getConcept().getConceptId());
			}
			
			// now add any new ones
			newConceptIds.removeAll(alreadyDone);
			for (Integer conceptId : newConceptIds) {
				ProgramWorkflow pw = new ProgramWorkflow();
				pw.setProgram(program);
				pw.setConcept(cs.getConcept(conceptId));
				newSet.add(pw);
			}

			setValue(newSet);
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		Collection<ProgramWorkflow> pws = (Collection<ProgramWorkflow>) getValue();
		if (pws == null || pws.size() == 0) {
			return ":";
		} else {
			Integer progId = null;
			for (ProgramWorkflow pw : pws) {
				if (pw.getProgram() != null && pw.getProgram().getProgramId() != null) {
					progId = pw.getProgram().getProgramId();
					break;
				}
			}
			StringBuilder ret = new StringBuilder();
			if (progId != null) {
				ret.append(progId);
			}
			ret.append(":");
			for (ProgramWorkflow pw : pws) {
				ret.append(pw.getConcept().getConceptId()).append(" ");
			}
			return ret.toString().trim();
		}
	}

}
