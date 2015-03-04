/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

/**
 * Class to convert the "programid: workflowoneid workflow2id" strings to actual workflows on a
 * program
 */
public class WorkflowCollectionEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public WorkflowCollectionEditor() {
	}
	
	private Program program = null;
	
	/**
	 * @param program
	 */
	public WorkflowCollectionEditor(Program program) {
		this.program = program;
	}
	
	/**
	 * Takes a "program_id:list" where program_id is the id of the program that this collection is
	 * for (or not present, if it's a new program) and list is a space-separated list of concept
	 * ids. This class is a bit of a hack, because I don't know a better way to do this. -DJ The
	 * purpose is to retire and un-retire workflows where possible rather than deleting and creating
	 * them.
	 * 
	 * @should update workflows in program
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			ConceptService cs = Context.getConceptService();
			ProgramWorkflowService pws = Context.getProgramWorkflowService();
			try {
				int ind = text.indexOf(":");
				String progIdStr = text.substring(0, ind);
				text = text.substring(ind + 1);
				if (program == null) {
					// if a program wasn't passed in, try to look it up now
					program = pws.getProgram(Integer.valueOf(progIdStr));
				}
			}
			catch (Exception ex) {}
			
			String[] conceptIds = text.split(" ");
			Set<ProgramWorkflow> oldSet = program == null ? new HashSet<ProgramWorkflow>() : program.getAllWorkflows();
			Set<Integer> newConceptIds = new HashSet<Integer>();
			
			for (String id : conceptIds) {
				if (id.trim().length() == 0) {
					continue;
				}
				log.debug("trying " + id);
				newConceptIds.add(Integer.valueOf(id.trim()));
			}
			
			// go through oldSet and see what we need to keep and what we need to unvoid
			Set<Integer> alreadyDone = new HashSet<Integer>();
			for (ProgramWorkflow pw : oldSet) {
				if (!newConceptIds.contains(pw.getConcept().getConceptId())) {
					pw.setRetired(true);
				} else if (newConceptIds.contains(pw.getConcept().getConceptId()) && pw.isRetired()) {
					pw.setRetired(false);
				}
				alreadyDone.add(pw.getConcept().getConceptId());
			}
			
			// now add any new ones
			newConceptIds.removeAll(alreadyDone);
			for (Integer conceptId : newConceptIds) {
				ProgramWorkflow pw = new ProgramWorkflow();
				pw.setProgram(program);
				pw.setConcept(cs.getConcept(conceptId));
				oldSet.add(pw);
			}
			
			setValue(oldSet);
		} else {
			setValue(null);
		}
	}
	
	/**
	 * Convert this program's workflows into "id: wkflowid wkflowid wkflowid"
	 * 
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	@SuppressWarnings("unchecked")
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
