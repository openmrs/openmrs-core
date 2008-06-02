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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the ProgramWorkflow-related services class.
 * 
 * This method should not be invoked by itself.  Spring injection is used
 * to inject this implementation into the ServiceContext.  Which 
 * implementation is injected is determined by the spring application 
 * context file: /metadata/api/spring/applicationContext.xml
 * 
 * @see org.openmrs.api.ProgramWorkflowService
 */
@Transactional
public class ProgramWorkflowServiceImpl extends BaseOpenmrsService implements ProgramWorkflowService {
	
	protected final Log log = LogFactory.getLog(this.getClass());

	protected ProgramWorkflowDAO dao;
	
	public ProgramWorkflowServiceImpl() { }
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#setProgramWorkflowDAO(org.openmrs.api.db.ProgramWorkflowDAO)
	 */
	public void setProgramWorkflowDAO(ProgramWorkflowDAO dao) {
		this.dao = dao;
	}
	
	// **************************
	// PROGRAM
	// **************************
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#saveProgram(org.openmrs.Program)
	 */
    public Program saveProgram(Program program) throws APIException {
    	
    	User currentUser = Context.getAuthenticatedUser();
    	Date currentDate = new Date();
    	
    	// Program
		if (program.getConcept() == null) {
			throw new APIException("Program concept is required");
		}
    	if (program.getCreator() == null) {
    		program.setCreator(currentUser);
    	}
    	if (program.getDateCreated() == null) {
    		program.setDateCreated(currentDate);
    	}
		if (program.getProgramId() != null) {
			program.setChangedBy(currentUser);
			program.setDateChanged(currentDate);
	}
	
		// ProgramWorkflow
		for (ProgramWorkflow workflow : program.getWorkflows()) {
		
			if (workflow.getConcept() == null) {
				throw new APIException("ProgramWorkflow concept is required");
			}
	    	if (workflow.getCreator() == null) {
	    		workflow.setCreator(currentUser);
	    	}
	    	if (workflow.getDateCreated() == null) {
	    		workflow.setDateCreated(currentDate);
	    	}
			if (workflow.getProgramWorkflowId() != null) {
				workflow.setChangedBy(currentUser);
				workflow.setDateChanged(currentDate);
		}
			if (workflow.getProgram() == null) {
				workflow.setProgram(program);
			}
			else if (!workflow.getProgram().equals(program)) {
				throw new APIException("This Program contains a ProgramWorkflow whose parent Program is already assigned to " + workflow.getProgram());
		}
		
			// ProgramWorkflowState
			for (ProgramWorkflowState state : workflow.getStates()) {
				
				if (state.getConcept() == null || state.getInitial() == null || state.getTerminal() == null) {
					throw new APIException("ProgramWorkflowState concept, initial, terminal are required");
			}
		    	if (state.getCreator() == null) {
		    		state.setCreator(currentUser);
			}
		    	if (state.getDateCreated() == null) {
		    		state.setDateCreated(currentDate);
		}
				if (state.getProgramWorkflowStateId() != null) {
					state.setChangedBy(currentUser);
					state.setDateChanged(currentDate);
					}
				if (state.getProgramWorkflow() == null) {
					state.setProgramWorkflow(workflow);
				}
				else if (!state.getProgramWorkflow().equals(workflow)) {
					throw new APIException("This ProgramWorkflow contains a State whose parent ProgramWorkflow is already assigned to " + workflow.getProgram());
				}
			}
		}
		return dao.saveProgram(program);
	}
	
    /**
     * @see org.openmrs.api.ProgramWorkflowService#getProgram(java.lang.Integer)
	 */
    @Transactional(readOnly=true)
	public Program getProgram(Integer id) {
		return dao.getProgram(id);
	}

    /**
     * @see org.openmrs.api.ProgramWorkflowService#getProgram(java.lang.String)
	 */
    @Transactional(readOnly=true)
	public Program getProgram(String name) {
    	return getProgramByName(name);
    }

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getProgram(java.lang.String)
	 */
    @Transactional(readOnly=true)
	public Program getProgramByName(String name) {
		for (Program p : getAllPrograms()) {
			if (p.getConcept().isNamed(name)) {
				return p;
			}
		}
		return null;
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#getAllPrograms()
	 */
    @Transactional(readOnly=true)
    public List<Program> getAllPrograms() throws APIException {
    	return getAllPrograms(true);
    }

	/**
     * @see org.openmrs.api.ProgramWorkflowService#getAllPrograms(boolean)
     */
    @Transactional(readOnly=true)
    public List<Program> getAllPrograms(boolean includeRetired) throws APIException {
    	return dao.getAllPrograms(includeRetired);
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#findPrograms(java.lang.String)
     */
    @Transactional(readOnly=true)
    public List<Program> getPrograms(String nameFragment) throws APIException {
    	return dao.findPrograms(nameFragment);
    }
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#purgeProgram(org.openmrs.Program)
	 */
    public void purgeProgram(Program program) throws APIException {
    	purgeProgram(program, false);
	    
    }
		
	/**
     * @see org.openmrs.api.ProgramWorkflowService#purgeProgram(org.openmrs.Program, boolean)
     */
    public void purgeProgram(Program program, boolean cascade) throws APIException {
    	if (cascade && !program.getWorkflows().isEmpty()) {
	    	throw new APIException("Cascade purging of Programs is not implemented yet");
	    }
    	dao.deleteProgram(program);
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#retireProgram(org.openmrs.Program)
	 */
    public Program retireProgram(Program program) throws APIException {
    	program.setRetired(true);
    	for (ProgramWorkflow workflow : program.getWorkflows()) {
    		workflow.setRetired(true);
    		for (ProgramWorkflowState state : workflow.getStates()) {
    			state.setRetired(true);
    		}
    	}
    	return saveProgram(program);
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#retireProgram(org.openmrs.Program)
	 */
    public Program unRetireProgram(Program program) throws APIException {
    	Date lastModifiedDate = program.getDateChanged();
    	program.setRetired(false);
    	for (ProgramWorkflow workflow : program.getWorkflows()) {
    		if (lastModifiedDate != null && lastModifiedDate.equals(workflow.getDateChanged())) {
    			workflow.setRetired(false);
	    		for (ProgramWorkflowState state : workflow.getStates()) {
	    			if (lastModifiedDate != null && lastModifiedDate.equals(state.getDateChanged())) {
	    				state.setRetired(false);
	    			}
	    		}
    		}
    	}
    	return saveProgram(program);
	}
	
	// **************************
	// PATIENT PROGRAM 
	// **************************
    
	/**
     * @see org.openmrs.api.ProgramWorkflowService#savePatientProgram(org.openmrs.PatientProgram)
	 */
    public PatientProgram savePatientProgram(PatientProgram patientProgram) throws APIException {
    	User currentUser = Context.getAuthenticatedUser();
    	Date currentDate = new Date();
    	
		if (patientProgram.getPatient() == null || patientProgram.getProgram() == null) {
			throw new APIException("PatientProgram requires a Patient and a Program");
		}
    	// Program
    	if (patientProgram.getCreator() == null) {
    		patientProgram.setCreator(currentUser);
    	}
    	if (patientProgram.getDateCreated() == null) {
    		patientProgram.setDateCreated(currentDate);
    	}
		if (patientProgram.getPatientProgramId() != null) {
			patientProgram.setChangedBy(currentUser);
			patientProgram.setDateChanged(currentDate);
		}
		if (patientProgram.getVoided()) {
			if (patientProgram.getVoidedBy() == null) {
				patientProgram.setVoidedBy(currentUser);
			}
			if (patientProgram.getDateVoided() == null) {
				patientProgram.setDateVoided(currentDate);
			}
		}
		else {
			patientProgram.setVoidedBy(null);
			patientProgram.setVoidReason(null);
			patientProgram.setDateVoided(null);
		}
		
		// Patient State
		for (PatientState state : patientProgram.getStates()) {
			if (state.getState() == null) {
				throw new APIException("PatientState requires a State");
			}
			if (state.getPatientProgram() == null) {
				state.setPatientProgram(patientProgram);
			}
			else if (!state.getPatientProgram().equals(patientProgram)) {
				throw new APIException("This PatientProgram contains a ProgramWorkflowState whose parent is already assigned to " + state.getPatientProgram());
			}
	    	if (state.getCreator() == null) {
	    		state.setCreator(currentUser);
	    	}
	    	if (state.getDateCreated() == null) {
	    		state.setDateCreated(currentDate);
	    	}
			if (state.getPatientStateId() != null) {
				state.setChangedBy(currentUser);
				state.setDateChanged(currentDate);
			}
			if (patientProgram.getVoided() || state.getVoided()) {
				state.setVoided(true);
				if (state.getVoidedBy() == null) {
					state.setVoidedBy(currentUser);
				}
				if (state.getDateVoided() == null) {
					state.setDateVoided(currentDate);
			}
				if (state.getVoidReason() == null && patientProgram.getVoidReason() != null) {
					state.setVoidReason(patientProgram.getVoidReason());
			}
		}
			else {
				state.setVoidedBy(null);
				state.setVoidReason(null);
				state.setDateVoided(null);
			}
		}
		return dao.savePatientProgram(patientProgram);
		}
    
    /**
     * @see org.openmrs.api.ProgramWorkflowService#getPatientProgram(java.lang.Integer)
     */
    @Transactional(readOnly=true)
	public PatientProgram getPatientProgram(Integer patientProgramId) {
		return dao.getPatientProgram(patientProgramId);
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#getPatientPrograms(org.openmrs.Patient, org.openmrs.Program, java.util.Date, java.util.Date, java.util.Date, java.util.Date)
	 */
    @Transactional(readOnly=true)
    public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate, Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided) throws APIException {
    	return dao.getPatientPrograms(patient, program, minEnrollmentDate, maxEnrollmentDate, minCompletionDate, maxCompletionDate, includeVoided);
	}

    /**
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getPatientPrograms(Cohort, Collection<Program>)
	 */
	public List<PatientProgram> getPatientPrograms(Cohort cohort, Collection<Program> programs) {
		if (cohort.getMemberIds().size() < 1)
			return dao.getPatientPrograms(null, programs);
		else
			return dao.getPatientPrograms(cohort, programs);
	}
		
	/**
     * @see org.openmrs.api.ProgramWorkflowService#purgePatientProgram(org.openmrs.PatientProgram)
     */
    public void purgePatientProgram(PatientProgram patientProgram) throws APIException {
	    purgePatientProgram(patientProgram, false);
	
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#purgePatientProgram(org.openmrs.PatientProgram, boolean)
     */
    public void purgePatientProgram(PatientProgram patientProgram, boolean cascade) throws APIException {
    	if (cascade && !patientProgram.getStates().isEmpty()) {
	    	throw new APIException("Cascade purging of PatientPrograms is not implemented yet");
	    }
    	dao.deletePatientProgram(patientProgram);
	}

    /**
     * @see org.openmrs.api.ProgramWorkflowService#voidPatientProgram(org.openmrs.PatientProgram, java.lang.String)
	 */
	public PatientProgram voidPatientProgram(PatientProgram patientProgram, String reason) {
		patientProgram.setVoided(true);
		patientProgram.setVoidReason(reason);
		return savePatientProgram(patientProgram); // The savePatientProgram method handles all of the voiding defaults and cascades
	}
	
    /**
     * @see org.openmrs.api.ProgramWorkflowService#voidPatientProgram(org.openmrs.PatientProgram, java.lang.String)
	 */
	public PatientProgram unvoidPatientProgram(PatientProgram patientProgram) {
		Date voidDate = patientProgram.getDateVoided();
		patientProgram.setVoided(false);
		for (PatientState state : patientProgram.getStates()) {
			if (voidDate != null && voidDate.equals(state.getDateVoided())) {
				state.setVoided(false);
				state.setVoidedBy(null);
				state.setDateVoided(null);
				state.setVoidReason(null);
			}
		}
		return savePatientProgram(patientProgram); // The savePatientProgram method handles all of the unvoiding defaults
	}
	

	// **************************
	// CONCEPT STATE CONVERSION 
	// **************************

	/**
     * @see org.openmrs.api.ProgramWorkflowService#saveConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
    public ConceptStateConversion saveConceptStateConversion(ConceptStateConversion csc) throws APIException {
    	if (csc.getConcept() == null || csc.getProgramWorkflow() == null || csc.getProgramWorkflowState() == null) {
    		throw new APIException("ConceptStateConversion requires a Concept, ProgramWorkflow, and ProgramWorkflowState");
		}
    	return dao.saveConceptStateConversion(csc);
		}
		
    /**
     * @see org.openmrs.api.ProgramWorkflowService#getConceptStateConversion(java.lang.Integer)
     */
    @Transactional(readOnly=true)
	public ConceptStateConversion getConceptStateConversion(Integer id) {
		return dao.getConceptStateConversion(id);
	}
	
	/**
     * @see org.openmrs.api.ProgramWorkflowService#getAllConceptStateConversions()
	 */
    @Transactional(readOnly=true)
    public List<ConceptStateConversion> getAllConceptStateConversions() throws APIException {
    	return dao.getAllConceptStateConversions();
    }
		
	/**
     * @see org.openmrs.api.ProgramWorkflowService#purgeConceptStateConversion(org.openmrs.ConceptStateConversion)
     */
    public void purgeConceptStateConversion(ConceptStateConversion conceptStateConversion) throws APIException {
    	purgeConceptStateConversion(conceptStateConversion, false);
		}
		
	/**
     * @see org.openmrs.api.ProgramWorkflowService#purgeConceptStateConversion(org.openmrs.ConceptStateConversion, boolean)
     */
    public void purgeConceptStateConversion(ConceptStateConversion conceptStateConversion, boolean cascade) throws APIException {
    	dao.deleteConceptStateConversion(conceptStateConversion);
	}
	
    /**
     * @see org.openmrs.api.ProgramWorkflowService#triggerStateConversion(org.openmrs.Patient, org.openmrs.Concept, java.util.Date)
	 */
	public void triggerStateConversion(Patient patient, Concept trigger, Date dateConverted) {

		// Check input parameters
		if ( patient == null ) throw new APIException("Attempting to convert state of an invalid patient");
		if ( trigger == null ) throw new APIException("Attempting to convert state for a patient without a valid trigger concept");
		if ( dateConverted == null ) throw new APIException("Invalid date for converting patient state");

		for (PatientProgram patientProgram : getPatientPrograms(patient, null, null, null, null, null, false)) {
			Set<ProgramWorkflow> workflows = patientProgram.getProgram().getWorkflows();
			for (ProgramWorkflow workflow : workflows) {
				if (!workflow.isRetired()) {
					ProgramWorkflowState currentState = patientProgram.getCurrentState(workflow).getState();
					ProgramWorkflowState transitionState = workflow.getState(trigger);
					if (transitionState != null && workflow.isLegalTransition(currentState, transitionState)) {
						patientProgram.transitionToState(transitionState, dateConverted);
						log.info("State Conversion Triggered: patientProgram=" + patientProgram + " transition from " + currentState + " to " + transitionState + " on " + dateConverted);
					}
				}
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getConceptStateConversion(org.openmrs.ProgramWorkflow, org.openmrs.Concept)
	 */
	@Transactional(readOnly=true)
	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) {
		return dao.getConceptStateConversion(workflow, trigger);
	}
	
	// **************************
	// DEPRECATED PROGRAM
	// **************************
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#createOrUpdateProgram(org.openmrs.Program)
	 * @deprecated
	 */
	public void createOrUpdateProgram(Program program) {
		saveProgram(program);
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getPrograms()
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public List<Program> getPrograms() {
		return getAllPrograms();
		}

	// **************************
	// DEPRECATED PROGRAM WORKFLOW
	// **************************
	
	/**
	 * @see org.pih.api.ProgramWorkflowService#createWorkflow(ProgramWorkflow)
	 * @deprecated
	 */
	public void createWorkflow(ProgramWorkflow w) {
		updateWorkflow(w);
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#updateWorkflow(org.openmrs.ProgramWorkflow)
	 * @deprecated
	 */
	public void updateWorkflow(ProgramWorkflow w) {
		if (w.getProgram() == null) {
			throw new APIException("ProgramWorkflow requires a Program");
		}
		saveProgram(w.getProgram());
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getWorkflow(java.lang.Integer)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public ProgramWorkflow getWorkflow(Integer id) {
		for (Program p : getAllPrograms()) {
			for (ProgramWorkflow w : p.getWorkflows()) {
				if (w.getProgramWorkflowId().equals(id)) {
					return w;
				}
			}
		}
		return null;
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getWorkflow(org.openmrs.Program, java.lang.String)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public ProgramWorkflow getWorkflow(Program program, String name) {
		return program.getWorkflowByName(name);
		}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#voidWorkflow(org.openmrs.ProgramWorkflow, java.lang.String)
	 * @deprecated
	 */
	public void voidWorkflow(ProgramWorkflow w, String reason) {
		w.setRetired(true);
		saveProgram(w.getProgram());
	}
	
	// **************************
	// DEPRECATED PROGRAM WORKFLOW STATE
	// **************************
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getStates()
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public List<ProgramWorkflowState> getStates() {
		return getStates(true);
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getStates(boolean)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public List<ProgramWorkflowState> getStates(boolean includeRetired) {
		List<ProgramWorkflowState> ret = new ArrayList<ProgramWorkflowState>();
		for (Program p : getAllPrograms()) {
			for (ProgramWorkflow w : p.getWorkflows()) {
				for (ProgramWorkflowState s : w.getStates()) {
					if (includeRetired || !s.isRetired()) {
						ret.add(s);
					}
		}
		}
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getState(java.lang.Integer)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public ProgramWorkflowState getState(Integer id) {
		for (ProgramWorkflowState s : getStates()) {
			if (s.getProgramWorkflowStateId().equals(id)) {
				return s;
		}
		}
		return null;
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getState(org.openmrs.ProgramWorkflow, java.lang.String)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public ProgramWorkflowState getState(ProgramWorkflow programWorkflow, String name) {
		return programWorkflow.getStateByName(name);
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getPossibleNextStates(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public List<ProgramWorkflowState> getPossibleNextStates(PatientProgram patientProgram, ProgramWorkflow workflow) {
		return workflow.getPossibleNextStates(patientProgram);
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#isLegalTransition(org.openmrs.ProgramWorkflowState, org.openmrs.ProgramWorkflowState)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public boolean isLegalTransition(ProgramWorkflowState fromState, ProgramWorkflowState toState) {
		return fromState.getProgramWorkflow().isLegalTransition(fromState, toState);
	}
		

	// **************************
	// DEPRECATED PATIENT PROGRAM 
	// **************************
		
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#createPatientProgram(org.openmrs.PatientProgram)
	 * @deprecated
	 */
	public void createPatientProgram(PatientProgram patientProgram) {
		savePatientProgram(patientProgram);
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#updatePatientProgram(org.openmrs.PatientProgram)
	 * @deprecated
	 */
	public void updatePatientProgram(PatientProgram patientProgram) {
		savePatientProgram(patientProgram);
	}
		
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#enrollPatientInProgram(org.openmrs.Patient, org.openmrs.Program, java.util.Date, java.util.Date, org.openmrs.User)
	 * @deprecated
	 */
	public void enrollPatientInProgram(Patient patient, Program program, Date enrollmentDate, Date completionDate, User creator) {
		PatientProgram p = new PatientProgram();
		p.setPatient(patient);
		p.setProgram(program);
		p.setDateEnrolled(enrollmentDate);
		p.setDateCompleted(completionDate);
		p.setCreator(creator);
		savePatientProgram(p);
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getPatientPrograms(org.openmrs.Patient)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		return getPatientPrograms(patient, null, null, null, null, null, false);
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#patientsInProgram(org.openmrs.Program, java.util.Date, java.util.Date)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public Collection<Integer> patientsInProgram(Program program, Date fromDate, Date toDate) {
		List<Integer> ret = new ArrayList<Integer>();
	    Collection<PatientProgram> programs = getPatientPrograms(null, program, null, toDate, fromDate, null, false);
	    for (PatientProgram patProgram : programs) {
	    	ret.add(patProgram.getPatient().getPatientId());
	    }
		return ret;
	}
		
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getCurrentPrograms(org.openmrs.Patient, java.util.Date)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public Collection<PatientProgram> getCurrentPrograms(Patient patient, Date onDate) {
		List<PatientProgram> ret = new ArrayList<PatientProgram>();
		for (PatientProgram pp : getPatientPrograms(patient)) {
			if (pp.getActive(onDate == null ? new Date() : onDate)) {
				ret.add(pp);
			}
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#isInProgram(org.openmrs.Patient, org.openmrs.Program, java.util.Date, java.util.Date)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public boolean isInProgram(Patient patient, Program program, Date fromDate, Date toDate) {
		return !getPatientPrograms(patient, program, null, toDate, fromDate, null, false).isEmpty();
	}

	// **************************
	// DEPRECATED PATIENT STATE 
	// **************************
		
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getPatientState(java.lang.Integer)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public PatientState getPatientState(Integer patientStateId) {
		for (PatientProgram p : getPatientPrograms(null, null, null, null, null, null, false)) {
			PatientState state = p.getPatientState(patientStateId);
			if (state != null) {
				return state;
				}
			}
		return null;
		}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getLatestState(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public PatientState getLatestState(PatientProgram patientProgram, ProgramWorkflow workflow) {
		return patientProgram.getCurrentState(workflow);
	}
		
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getCurrentWorkflowsByPatient(org.openmrs.Patient)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public Set<ProgramWorkflow> getCurrentWorkflowsByPatient(Patient patient) {
		Set<ProgramWorkflow> ret = new HashSet<ProgramWorkflow>();
		for (PatientProgram patientProgram : getPatientPrograms(patient)) {
			ret.addAll(getCurrentWorkflowsByPatientProgram(patientProgram));
		}
		return ret;
	}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getCurrentWorkflowsByPatientProgram(org.openmrs.PatientProgram)
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public Set<ProgramWorkflow> getCurrentWorkflowsByPatientProgram(PatientProgram patientProgram) {
		Set<ProgramWorkflow> ret = new HashSet<ProgramWorkflow>();
		if (patientProgram != null) {
			for (PatientState state : patientProgram.getStates()) {
					ret.add(state.getState().getProgramWorkflow());
				}
			}
		return ret;
		}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#changeToState(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow, org.openmrs.ProgramWorkflowState, java.util.Date)
	 * @deprecated
	 */
	public void changeToState(PatientProgram patientProgram, ProgramWorkflow workflow, ProgramWorkflowState state, Date onDate) {
		patientProgram.transitionToState(state, onDate);
		savePatientProgram(patientProgram);
	}
		
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#voidLastState(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow, java.lang.String)
	 * @deprecated
	 */
	public void voidLastState(PatientProgram patientProgram, ProgramWorkflow workflow, String voidReason) {
		patientProgram.voidLastState(workflow, Context.getAuthenticatedUser(), new Date(), voidReason);
	}
	
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#terminatePatientProgram(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflowState, java.util.Date)
	 * @deprecated
	 */
	public void terminatePatientProgram(PatientProgram patientProgram, ProgramWorkflowState finalState, Date terminatedOn) {
		changeToState(patientProgram, finalState.getProgramWorkflow(), finalState, terminatedOn);
	}
			
	// **************************
	// DEPRECATED CONCEPT STATE CONVERSION
	// **************************
				
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#createConceptStateConversion(org.openmrs.ConceptStateConversion)
	 * @deprecated
	 */
	public void createConceptStateConversion(ConceptStateConversion csc) {
		saveConceptStateConversion(csc);
								}
								
	/**
	 * @see org.openmrs.api.ProgramWorkflowService#updateConceptStateConversion(org.openmrs.ConceptStateConversion)
	 * @deprecated
	 */
	public void updateConceptStateConversion(ConceptStateConversion csc) {
		saveConceptStateConversion(csc);
					}

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#getAllConversions()
	 * @deprecated
	 */
	@Transactional(readOnly=true)
	public List<ConceptStateConversion> getAllConversions() {
		return getAllConceptStateConversions();
				}				

	/**
	 * @see org.openmrs.api.ProgramWorkflowService#deleteConceptStateConversion(org.openmrs.ConceptStateConversion)
	 * @deprecated
	 */
	public void deleteConceptStateConversion(ConceptStateConversion csc) {
		purgeConceptStateConversion(csc);
	}

}