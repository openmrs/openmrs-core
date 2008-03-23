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
package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class DWRProgramWorkflowService {

	protected final Log log = LogFactory.getLog(getClass());
	DateFormat ymdDf = new SimpleDateFormat("yyyy-MM-dd");
	
	public PatientProgramItem getPatientProgram(Integer patientProgramId) {
		return new PatientProgramItem(Context.getProgramWorkflowService().getPatientProgram(patientProgramId));
	}
	
	public Vector<PatientStateItem> getPatientStates(Integer patientProgramId, Integer programWorkflowId) {
		Vector<PatientStateItem> ret = new Vector<PatientStateItem>();
		ProgramWorkflowService s = Context.getProgramWorkflowService();
		PatientProgram p = s.getPatientProgram(patientProgramId);
		ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
		for (PatientState st : p.statesInWorkflow(wf, false))
			ret.add(new PatientStateItem(st));
		return ret;
	}
	
	public Vector<ListItem> getWorkflowsByProgram(Integer programId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		Program program = Context.getProgramWorkflowService().getProgram(programId);
		Set<ProgramWorkflow> workflows = program.getWorkflows();
		if (workflows != null)
			for (ProgramWorkflow wf : workflows) {
				ListItem li = new ListItem();
				li.setId(wf.getProgramWorkflowId());
				li.setName(wf.getConcept().getName().getName());
				li.setDescription(wf.getConcept().getName().getDescription());
				ret.add(li);
			}
		return ret;
	}

	public Vector<ListItem> getStatesByWorkflow(Integer programWorkflowId) {
		log.debug("In getStatesByWorkflow with workflowID of " + programWorkflowId.toString());
		Vector<ListItem> ret = new Vector<ListItem>();
		
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getWorkflow(programWorkflowId);
		if ( workflow != null ) {
			Set<ProgramWorkflowState> states = workflow.getSortedStates();
			
			if ( states != null ) {
				log.debug("Got states of size " + states.size());
				for (ProgramWorkflowState state : states) {
					ListItem li = new ListItem();
					li.setId(state.getProgramWorkflowStateId());
					li.setName(state.getConcept().getName(Context.getLocale(), false).getName());
					ret.add(li);
				}
			} else {
				log.debug("States was null - there seem to be no states associated with this workflow");
			}
		} else {
			log.debug("Workflow was null, cannot get states");
		}

		if ( ret != null ) log.debug("Returning ret of size " + ret.size());
		else log.debug("Returning null ret");
		return ret;
	}
	
	/**
	 * Updates enrollment date and completion date for a PatientProgram.
	 * 
	 * Compares @param enrollmentDateYmd with {@link PatientProgram#getDateEnrolled()} and
	 * compares @param completionDateYmd with {@link PatientProgram#getDateCompleted()} .
	 * At least one of these comparisons must return true in order to update the PatientProgram.
	 * In other words, if neither the @param enrollmentDateYmd or the @param completionDateYmd match
	 * with the persisted object, then the PatientProgram will not be updated.
	 * 
	 * Also, if the enrollment date comes after the completion date, the PatientProgram will
	 * not be updated.
	 * 
	 * @param patientProgramId
	 * @param enrollmentDateYmd
	 * @param completionDateYmd
	 * @throws ParseException
	 */
	public void updatePatientProgram(Integer patientProgramId, String enrollmentDateYmd, String completionDateYmd) throws ParseException {
		PatientProgram pp = Context.getProgramWorkflowService().getPatientProgram(patientProgramId);
		Date dateEnrolled = null;
		Date dateCompleted = null;
		Date ppDateEnrolled = null; 
		Date ppDateCompleted = null;
		// If persisted date enrolled is not null then parse to ymdDf format.
		if (null != pp.getDateEnrolled()) {
			String enrolled = ymdDf.format(pp.getDateEnrolled());
			if (null != enrolled && enrolled.length() > 0)
				ppDateEnrolled = ymdDf.parse(enrolled);
		}
		// If persisted date enrolled is not null then parse to ymdDf format.
		if (null != pp.getDateCompleted()) {
			String completed = ymdDf.format(pp.getDateCompleted());
			if (null != completed && completed.length() > 0)
				ppDateCompleted = ymdDf.parse(completed);
		}
		// Parse parameter dates to ymdDf format.
		if (enrollmentDateYmd != null && enrollmentDateYmd.length() > 0)
			dateEnrolled = ymdDf.parse(enrollmentDateYmd);
		if (completionDateYmd != null && completionDateYmd.length() > 0)
			dateCompleted = ymdDf.parse(completionDateYmd);
		// If either either parameter and persisted instances 
		// of enrollment and completion dates are equal, then anyChange is true.
		boolean anyChange = OpenmrsUtil.nullSafeEquals(dateEnrolled, ppDateEnrolled);
		anyChange |= OpenmrsUtil.nullSafeEquals(dateCompleted, ppDateCompleted);
		// Do not update if the enrollment date is after the completion date.
		if (null != dateEnrolled && null != dateCompleted && dateCompleted.before(dateEnrolled)) {
			anyChange = false;
		}
		if (anyChange) {
			pp.setDateEnrolled(dateEnrolled);
			pp.setDateCompleted(dateCompleted);
			Context.getProgramWorkflowService().updatePatientProgram(pp);
		}
	}
	
	public void deletePatientProgram(Integer patientProgramId, String reason) {
		PatientProgram p = Context.getProgramWorkflowService().getPatientProgram(patientProgramId);
		Context.getProgramWorkflowService().voidPatientProgram(p, reason);
	}
	
	public Vector<ListItem> getPossibleNextStates(Integer patientProgramId, Integer programWorkflowId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		List<ProgramWorkflowState> states = Context.getProgramWorkflowService()
			.getPossibleNextStates(Context.getProgramWorkflowService().getPatientProgram(patientProgramId),
								   Context.getProgramWorkflowService().getWorkflow(programWorkflowId));
		for (ProgramWorkflowState state : states) {
			ListItem li = new ListItem();
			li.setId(state.getProgramWorkflowStateId());
			li.setName(state.getConcept().getName(Context.getLocale(), false).getName());
			ret.add(li);
		}
		return ret;
	}
	
	public void changeToState(Integer patientProgramId, Integer programWorkflowId,
		Integer programWorkflowStateId, String onDateDMY) throws ParseException {
		ProgramWorkflowService s = Context.getProgramWorkflowService(); 
		PatientProgram pp = s.getPatientProgram(patientProgramId);
		ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
		ProgramWorkflowState st = s.getState(programWorkflowStateId);
		Date onDate = null;
		if (onDateDMY != null && onDateDMY.length() > 0)
			onDate = ymdDf.parse(onDateDMY);
		s.changeToState(pp, wf, st, onDate);
	}
	
	public void voidLastState(Integer patientProgramId, Integer programWorkflowId, String voidReason) {
		ProgramWorkflowService s = Context.getProgramWorkflowService();
		PatientProgram pp = s.getPatientProgram(patientProgramId);
		ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
		s.voidLastState(pp, wf, voidReason);
	}
}
