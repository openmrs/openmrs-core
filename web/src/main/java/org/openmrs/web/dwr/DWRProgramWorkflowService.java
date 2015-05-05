/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.Concept;
import org.openmrs.Location;
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
	
	public Vector<ListItem> getPossibleOutcomes(Integer programId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		List<Concept> possibleOutcomes = Context.getProgramWorkflowService().getPossibleOutcomes(programId);
		for (Concept possibleOutcome : possibleOutcomes) {
			ListItem li = new ListItem();
			li.setName(possibleOutcome.getName().getName());
			li.setId(possibleOutcome.getConceptId());
			ret.add(li);
		}
		return ret;
	}
	
	public Vector<PatientStateItem> getPatientStates(Integer patientProgramId, Integer programWorkflowId) {
		Vector<PatientStateItem> ret = new Vector<PatientStateItem>();
		ProgramWorkflowService s = Context.getProgramWorkflowService();
		PatientProgram p = s.getPatientProgram(patientProgramId);
		ProgramWorkflow wf = p.getProgram().getWorkflow(programWorkflowId);
		for (PatientState st : p.statesInWorkflow(wf, false)) {
			ret.add(new PatientStateItem(st));
		}
		return ret;
	}
	
	public Vector<ListItem> getWorkflowsByProgram(Integer programId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		Program program = Context.getProgramWorkflowService().getProgram(programId);
		Set<ProgramWorkflow> workflows = program.getWorkflows();
		if (workflows != null) {
			for (ProgramWorkflow wf : workflows) {
				ListItem li = new ListItem();
				li.setId(wf.getProgramWorkflowId());
				try {
					li.setName(wf.getConcept().getName().getName());
				}
				catch (NullPointerException ex) {}
				try {
					li.setDescription(wf.getConcept().getDescription().getDescription());
				}
				catch (NullPointerException ex) {}
				ret.add(li);
			}
		}
		return ret;
	}
	
	public Vector<ListItem> getStatesByWorkflow(Integer programWorkflowId) {
		log.debug("In getStatesByWorkflow with workflowID of " + programWorkflowId);
		Vector<ListItem> ret = new Vector<ListItem>();
		
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getWorkflow(programWorkflowId);
		if (workflow != null) {
			Set<ProgramWorkflowState> states = workflow.getSortedStates();
			
			if (states != null) {
				log.debug("Got states of size " + states.size());
				for (ProgramWorkflowState state : states) {
					ListItem li = new ListItem();
					li.setId(state.getProgramWorkflowStateId());
					try {
						li.setName(state.getConcept().getName(Context.getLocale(), false).getName());
					}
					catch (NullPointerException ex) {}
					ret.add(li);
				}
			} else {
				log.debug("States was null - there seem to be no states associated with this workflow");
			}
		} else {
			log.debug("Workflow was null, cannot get states");
		}
		
		log.debug("Returning ret of size " + ret.size());
		return ret;
	}
	
	/**
	 * Updates enrollment date, completion date, and location for a PatientProgram. Compares @param
	 * enrollmentDateYmd with {@link PatientProgram#getDateEnrolled()} compares @param
	 * completionDateYmd with {@link PatientProgram#getDateCompleted()}, compares @param locationId
	 * with {@link PatientProgram#getLocation()}, compares @param outcomeId with {@link org.openmrs.PatientProgram#getOutcome()}.
	 * At least one of these comparisons must indicate a change in order to update the PatientProgram. In other words, if neither the @param
	 * enrollmentDateYmd, the @param completionDateYmd, or the @param locationId or the @param outcomeId
	 * match with the persisted object, then the PatientProgram will not be updated.
	 * <p>Also, if the enrollment date comes after the completion date, the PatientProgram will not be updated.</p>
	 *
	 * @param patientProgramId
	 * @param enrollmentDateYmd
	 * @param completionDateYmd
	 * @param locationId
	 * @param outcomeId
	 * @throws ParseException
	 */
	public void updatePatientProgram(Integer patientProgramId, String enrollmentDateYmd, String completionDateYmd,
	        Integer locationId, Integer outcomeId) throws ParseException {
		PatientProgram pp = Context.getProgramWorkflowService().getPatientProgram(patientProgramId);
		Location loc = null;
		if (locationId != null) {
			loc = Context.getLocationService().getLocation(locationId);
		}
		Concept outcomeConcept = null;
		if (outcomeId != null) {
			outcomeConcept = Context.getConceptService().getConcept(outcomeId);
		}
		Date dateEnrolled = null;
		Date dateCompleted = null;
		Date ppDateEnrolled = null;
		Date ppDateCompleted = null;
		Location ppLocation = pp.getLocation();
		Concept ppOutcome = pp.getOutcome();
		// If persisted date enrolled is not null then parse to ymdDf format.
		if (null != pp.getDateEnrolled()) {
			String enrolled = ymdDf.format(pp.getDateEnrolled());
			if (null != enrolled && enrolled.length() > 0) {
				ppDateEnrolled = ymdDf.parse(enrolled);
			}
		}
		// If persisted date enrolled is not null then parse to ymdDf format.
		if (null != pp.getDateCompleted()) {
			String completed = ymdDf.format(pp.getDateCompleted());
			if (null != completed && completed.length() > 0) {
				ppDateCompleted = ymdDf.parse(completed);
			}
		}
		// Parse parameter dates to ymdDf format.
		if (enrollmentDateYmd != null && enrollmentDateYmd.length() > 0) {
			dateEnrolled = ymdDf.parse(enrollmentDateYmd);
		}
		if (completionDateYmd != null && completionDateYmd.length() > 0) {
			dateCompleted = ymdDf.parse(completionDateYmd);
		}
		// If either either parameter and persisted instances 
		// of enrollment and completion dates are equal, then anyChange is true.
		boolean anyChange = OpenmrsUtil.nullSafeEquals(dateEnrolled, ppDateEnrolled);
		anyChange |= OpenmrsUtil.nullSafeEquals(dateCompleted, ppDateCompleted);
		anyChange |= OpenmrsUtil.nullSafeEquals(loc, ppLocation);
		anyChange |= OpenmrsUtil.nullSafeEquals(outcomeConcept, ppOutcome);
		// Do not update if the enrollment date is after the completion date.
		if (null != dateEnrolled && null != dateCompleted && dateCompleted.before(dateEnrolled)) {
			anyChange = false;
		}
		if (anyChange) {
			pp.setDateEnrolled(dateEnrolled);
			pp.setDateCompleted(dateCompleted);
			pp.setLocation(loc);
			pp.setOutcome(outcomeConcept);
			Context.getProgramWorkflowService().savePatientProgram(pp);
		}
	}
	
	public void deletePatientProgram(Integer patientProgramId, String reason) {
		PatientProgram p = Context.getProgramWorkflowService().getPatientProgram(patientProgramId);
		Context.getProgramWorkflowService().voidPatientProgram(p, reason);
	}
	
	/**
	 * @should return a list consisting of active, not retired, states.
	 */
	public Vector<ListItem> getPossibleNextStates(Integer patientProgramId, Integer programWorkflowId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		PatientProgram pp = Context.getProgramWorkflowService().getPatientProgram(patientProgramId);
		ProgramWorkflow pw = pp.getProgram().getWorkflow(programWorkflowId);
		List<ProgramWorkflowState> states = pw.getPossibleNextStates(pp);
		for (ProgramWorkflowState state : states) {
			ListItem li = new ListItem();
			li.setId(state.getProgramWorkflowStateId());
			li.setName(state.getConcept().getName(Context.getLocale(), false).getName());
			if (!state.isRetired() && !state.getConcept().isRetired()) {
				ret.add(li);
			}
		}
		return ret;
	}
	
	//TODO there doesn't seem to be any way in the administrator interface to retire a state, just a concept.
	
	public void changeToState(Integer patientProgramId, Integer programWorkflowId, Integer programWorkflowStateId,
	        String onDateDMY) throws ParseException {
		ProgramWorkflowService s = Context.getProgramWorkflowService();
		PatientProgram pp = s.getPatientProgram(patientProgramId);
		ProgramWorkflowState st = pp.getProgram().getWorkflow(programWorkflowId).getState(programWorkflowStateId);
		Date onDate = null;
		if (onDateDMY != null && onDateDMY.length() > 0) {
			onDate = ymdDf.parse(onDateDMY);
		}
		pp.transitionToState(st, onDate);
		s.savePatientProgram(pp);
	}
	
	public void voidLastState(Integer patientProgramId, Integer programWorkflowId, String voidReason) {
		ProgramWorkflowService s = Context.getProgramWorkflowService();
		PatientProgram pp = s.getPatientProgram(patientProgramId);
		ProgramWorkflow wf = s.getWorkflow(programWorkflowId);
		pp.voidLastState(wf, Context.getAuthenticatedUser(), new Date(), voidReason);
		Context.getProgramWorkflowService().savePatientProgram(pp);
	}
}
