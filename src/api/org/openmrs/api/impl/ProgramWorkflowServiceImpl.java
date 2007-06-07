package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

public class ProgramWorkflowServiceImpl implements ProgramWorkflowService {
	
	private Log log = LogFactory.getLog(this.getClass());

	private ProgramWorkflowDAO dao;
	
	public ProgramWorkflowServiceImpl() { }
	
	private ProgramWorkflowDAO getProgramWorkflowDAO() {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return dao;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#setProgramWorkflowDAO(org.openmrs.api.db.ProgramWorkflowDAO)
	 */
	public void setProgramWorkflowDAO(ProgramWorkflowDAO dao) {
		this.dao = dao;
	}
	
	// --- Program ---
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getPrograms()
	 */
	public List<Program> getPrograms() {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPrograms();
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#createOrUpdateProgram(org.openmrs.Program)
	 */
	public void createOrUpdateProgram(Program p) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		
		log.debug("Creating new program");
		if (p.getWorkflows() != null) {
			log.debug("\twith " + p.getWorkflows().size() + " workflows: " + p.getWorkflows());
		}
		
		Date now = new Date();
		if (p.getProgramId() == null) {
			if (p.getCreator() == null) {
				p.setCreator(Context.getAuthenticatedUser());
			}
			p.setDateCreated(now);
		} else {
			p.setChangedBy(Context.getAuthenticatedUser());
			p.setDateChanged(now);
		}
		
		if (p.getVoided()) {
			if (p.getDateVoided() == null) {
				p.setDateVoided(now);
			}
			if (p.getVoidedBy() == null) {
				p.setVoidedBy(Context.getAuthenticatedUser());
			}
		} else {
			p.setDateVoided(null);
			p.setVoidedBy(null);
			p.setVoidReason(null);
		}
		
		if (p.getWorkflows() != null) {
			for (ProgramWorkflow w : p.getWorkflows()) {
				if (w.getProgramWorkflowId() == null) {
					if (w.getCreator() == null) {
						w.setCreator(Context.getAuthenticatedUser());
					}
					w.setDateCreated(now);
				}
				if (w.getProgram() == null) {
					w.setProgram(p);
				} else if (!w.getProgram().getProgramId().equals(p.getProgramId())) {
					throw new IllegalArgumentException("This Program contains a ProgramWorkflow whose parent Program is already assigned to " + w.getProgram());
				}
			}
		}
		getProgramWorkflowDAO().createOrUpdateProgram(p);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getProgram(java.lang.Integer)
	 */
	public Program getProgram(Integer id) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getProgram(id);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getProgram(java.lang.String)
	 */
	public Program getProgram(String name) {
		// TODO: do this right
		//List<Program> progs = getPrograms();
		for (Program p : getPrograms())
			if (p.getConcept().getName(Context.getUserContext().getLocale(), false).getName().equals(name))
				return p;
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#retireProgram(org.openmrs.Program)
	 */
	public void retireProgram(Program p) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);

		p.setVoided(true);
		createOrUpdateProgram(p);
	}
	
	// --- ProgramWorkflow ---
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#createWorkflow(org.openmrs.ProgramWorkflow)
	 */
	public void createWorkflow(ProgramWorkflow w) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		
		getProgramWorkflowDAO().createWorkflow(w);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getWorkflow(java.lang.Integer)
	 */
	public ProgramWorkflow getWorkflow(Integer id) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getWorkflow(id);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getWorkflow(org.openmrs.Program, java.lang.String)
	 */
	public ProgramWorkflow getWorkflow(Program program, String name) {
		// TODO: fix this
		if (program.getWorkflows() == null)
			return null;
		for (ProgramWorkflow wf : program.getWorkflows())
			if (wf.getConcept().getName(Context.getUserContext().getLocale(), false).getName().equals(name))
				return wf;
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#updateWorkflow(org.openmrs.ProgramWorkflow)
	 */
	public void updateWorkflow(ProgramWorkflow w) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		if (w.getVoided()) {
			if (w.getVoidedBy() == null) {
				w.setVoidedBy(Context.getAuthenticatedUser());
			}
			if (w.getDateVoided() == null) {
				w.setDateVoided(new Date());
			}
		} else {
			w.setVoidedBy(null);
			w.setVoidReason(null);
			w.setDateVoided(null);
		}
		for (ProgramWorkflowState s : w.getStates()) {
			s.setProgramWorkflow(w);
			if (s.getCreator() == null) {
				s.setCreator(Context.getAuthenticatedUser());
			}
			if (s.getDateCreated() == null) {
				s.setDateCreated(new Date());
			}
		}
		getProgramWorkflowDAO().updateWorkflow(w);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#voidWorkflow(org.openmrs.ProgramWorkflow, java.lang.String)
	 */
	public void voidWorkflow(ProgramWorkflow w, String reason) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		w.setVoided(true);
		w.setVoidReason(reason);
		w.setVoidedBy(Context.getAuthenticatedUser());
		w.setDateVoided(new Date());
		getProgramWorkflowDAO().updateWorkflow(w);
	}

		
	// --- ProgramWorkflowState ---
		
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getState(java.lang.Integer)
	 */
	public ProgramWorkflowState getState(Integer id) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getState(id);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getState(org.openmrs.ProgramWorkflow, java.lang.String)
	 */
	public ProgramWorkflowState getState(ProgramWorkflow wf, String name) {
		// TODO: fix this
		for (ProgramWorkflowState st : wf.getStates())
			if (st.getConcept().getName(Context.getUserContext().getLocale(), false).equals(name))
				return st;
		return null;
	}
	
	// --- PatientProgram ---

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#createPatientProgram(org.openmrs.PatientProgram)
	 */
	public void createPatientProgram(PatientProgram p) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		Date now = new Date();
		if (p.getCreator() == null) {
			p.setCreator(Context.getAuthenticatedUser());
		}
		p.setDateCreated(now);
		for (PatientState s : p.getStates()) {
			if (s.getCreator() == null)
				s.setCreator(Context.getAuthenticatedUser());
			if (s.getDateCreated() == null)
				s.setDateCreated(now);
		}
		
		getProgramWorkflowDAO().createPatientProgram(p);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#updatePatientProgram(org.openmrs.PatientProgram)
	 */
	public void updatePatientProgram(PatientProgram p) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		Date now = new Date();
		p.setChangedBy(Context.getAuthenticatedUser());
		p.setDateChanged(now);
		for (PatientState state : p.getStates()) {
			if (state.getDateCreated() == null)
				state.setDateCreated(now);
			if (state.getCreator() == null)
				state.setCreator(Context.getAuthenticatedUser());
		}
		
		getProgramWorkflowDAO().updatePatientProgram(p);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getPatientProgram(java.lang.Integer)
	 */
	public PatientProgram getPatientProgram(Integer id) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPatientProgram(id);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getPatientPrograms(org.openmrs.Patient)
	 */
	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPatientPrograms(patient);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#enrollPatientInProgram(org.openmrs.Patient, org.openmrs.Program, java.util.Date, java.util.Date)
	 */
	public void enrollPatientInProgram(Patient patient, Program program, Date enrollmentDate, Date completionDate) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		// Should we add a boolean to the program title that says patients can be enrolled there twice simultaneously? 
		if (isInProgram(patient, program, enrollmentDate, completionDate))
			throw new IllegalArgumentException("patient is already in " + program +
					" sometime between " + enrollmentDate + " and " + completionDate);
		PatientProgram p = new PatientProgram();
		p.setPatient(patient);
		p.setProgram(program);
		p.setDateEnrolled(enrollmentDate);
		createPatientProgram(p);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#voidPatientProgram(org.openmrs.PatientProgram, java.lang.String)
	 */
	public void voidPatientProgram(PatientProgram p, String reason) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		if (!p.getVoided()) {
			p.setVoided(true);
			p.setDateVoided(new Date());
			p.setVoidedBy(Context.getAuthenticatedUser());
			p.setVoidReason(reason);
			updatePatientProgram(p);
		}
	}

	// Utility methods
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#patientsInProgram(org.openmrs.Program, java.util.Date, java.util.Date)
	 */
	public Collection<Integer> patientsInProgram(Program program, Date fromDate, Date toDate) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().patientsInProgram(program, fromDate, toDate);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getCurrentPrograms(org.openmrs.Patient, java.util.Date)
	 */
	public Collection<PatientProgram> getCurrentPrograms(Patient patient, Date onDate) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		if (onDate == null) {
			onDate = new Date();
		}
		// date enrolled and date completed are actually java.sql.Timestamp, which can't be compared directly to a java.util.Date
		long atMs = onDate.getTime();
		
		Collection<PatientProgram> ret = new HashSet<PatientProgram>();
		for (PatientProgram pp : getPatientPrograms(patient)) {
			if ( (pp.getDateEnrolled() == null || pp.getDateEnrolled().getTime() <= atMs)
					&& (pp.getDateCompleted() == null || pp.getDateCompleted().getTime() >= atMs) ) {
				ret.add(pp);
			}
		}
		return ret;
	}
	
	// TODO: move this into Patient (probably make this a lazily-loaded hibernate mapping).
	// This is just a quick implementation without changing any hibernate mappings
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getLatestState(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow)
	 */
	public PatientState getLatestState(PatientProgram patientProgram, ProgramWorkflow workflow) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		PatientState ret = null;
		// treat null as the earliest date
		for (PatientState state : patientProgram.getStates()) {
			if (state.getState().getProgramWorkflow().equals(workflow))
				if (ret == null || ret.getStartDate() == null || (state.getStartDate() != null && state.getStartDate().compareTo(ret.getStartDate()) > 0))
					ret = state;
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#getPossibleNextStates(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow)
	 */
	public List<ProgramWorkflowState> getPossibleNextStates(PatientProgram patientProgram, ProgramWorkflow workflow) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		List<ProgramWorkflowState> ret = new ArrayList<ProgramWorkflowState>();
		PatientState currentState = getLatestState(patientProgram, workflow);
		for (ProgramWorkflowState st : workflow.getStates()) {
			if (isLegalTransition(currentState == null ? null : currentState.getState(), st))
				ret.add(st);
		}
		return ret;
	}
	
	// TODO: once we have a table of legal state transitions, then use that instead of this simple algorithm
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#isLegalTransition(org.openmrs.ProgramWorkflowState, org.openmrs.ProgramWorkflowState)
	 */
	public boolean isLegalTransition(ProgramWorkflowState fromState, ProgramWorkflowState toState) {
		if (fromState == null)
			return toState.getInitial();
		else if (fromState.equals(toState))
			return false;
		else
			return true;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#changeToState(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow, org.openmrs.ProgramWorkflowState, java.util.Date)
	 */
	public void changeToState(PatientProgram patientProgram, ProgramWorkflow wf, ProgramWorkflowState st, Date onDate) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		PatientState lastState = getLatestState(patientProgram, wf);
		if (lastState != null && onDate == null) {
			throw new IllegalArgumentException("You can't change from a non-null state without giving a change date");
		}
		if (lastState != null && lastState.getEndDate() != null) {
			throw new IllegalArgumentException("You can't change out of a state that has an end date already");
		}
		if (lastState != null)
			lastState.setEndDate(onDate);
		PatientState newState = new PatientState();
		newState.setPatientProgram(patientProgram);
		newState.setState(st);
		newState.setStartDate(onDate);
		patientProgram.getStates().add(newState);
		updatePatientProgram(patientProgram);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#voidLastState(org.openmrs.PatientProgram, org.openmrs.ProgramWorkflow, java.lang.String)
	 */
	public void voidLastState(PatientProgram patientProgram, ProgramWorkflow wf, String voidReason) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		List<PatientState> states = patientProgram.statesInWorkflow(wf, false);
		PatientState last = null;
		PatientState nextToLast = null;
		if (states.size() > 0)
			last = states.get(states.size() - 1);
		if (states.size() > 1)
			nextToLast = states.get(states.size() - 2);
		if (last != null) {
			last.setVoided(true);
			last.setVoidReason(voidReason);
		}
		if (nextToLast != null && nextToLast.getEndDate() != null) {
			nextToLast.setEndDate(null);
			nextToLast.setDateChanged(new Date());
			nextToLast.setChangedBy(Context.getAuthenticatedUser());
		}
		updatePatientProgram(patientProgram);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.api.impl.ProgramWorkflowService#isInProgram(org.openmrs.Patient, org.openmrs.Program, java.util.Date, java.util.Date)
	 */
	public boolean isInProgram(Patient patient, Program program, Date fromDate, Date toDate) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return patientsInProgram(program, fromDate, toDate).contains(patient.getPatientId());
	}

	public void terminatePatientProgram(PatientProgram patProg, ProgramWorkflowState finalState, Date terminatedOn) {
		this.changeToState(patProg, finalState.getProgramWorkflow(), finalState, terminatedOn);
	}
	
	public void createConceptStateConversion(ConceptStateConversion csc) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		getProgramWorkflowDAO().createConceptStateConversion(csc);
	}

	public void updateConceptStateConversion(ConceptStateConversion csc) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		getProgramWorkflowDAO().updateConceptStateConversion(csc);
	}

	public void deleteConceptStateConversion(ConceptStateConversion csc) {
		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		getProgramWorkflowDAO().deleteConceptStateConversion(csc);
	}

	@Transactional(readOnly=true)
	public ConceptStateConversion getConceptStateConversion(Integer id) {
		log.debug("In getcsc with id of " + id.toString());
		ConceptStateConversion ret = null;

		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		
		ret = getProgramWorkflowDAO().getConceptStateConversion(id);
		
		return ret;
	}
	
	@Transactional(readOnly=true)
	public List<ConceptStateConversion> getAllConversions() {
		log.debug("In getAllConversions");
		List<ConceptStateConversion> ret = null;

		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		
		ret = getProgramWorkflowDAO().getAllConversions();
		
		return ret;
	}

	public Set<ProgramWorkflow> getCurrentWorkflowsByPatient(Patient patient) {
		log.debug("Getting workflows with patient: " + patient);
		Set<ProgramWorkflow> ret = null;
		
		List<PatientProgram> programs = (List<PatientProgram>)this.getPatientPrograms(patient);
		if ( programs != null ) {
			for ( PatientProgram program : programs ) {
				Set<ProgramWorkflow> workflows = this.getCurrentWorkflowsByPatientProgram(program);
				if ( workflows != null ) {
					if ( ret == null ) ret = new HashSet<ProgramWorkflow>();
					ret.addAll(workflows);					
				}
			}
		}

		if ( ret == null ) log.debug("Ret is null, leaving the method");
		else log.debug("Ret is size: " + ret.size());
		
		return ret;
	}

	public Set<ProgramWorkflow> getCurrentWorkflowsByPatientProgram(PatientProgram program) {
		log.debug("Getting workflows with program: " + program);
		Set<ProgramWorkflow> ret = null;
		
		if ( program != null ) {
			Set<PatientState> states = program.getStates();
			if ( states != null ) {
				for ( PatientState state : states ) {
					if ( ret == null ) ret = new HashSet<ProgramWorkflow>();
					ret.add(state.getState().getProgramWorkflow());
				}
			}
		}

		if ( ret == null ) log.debug("Ret is null, leaving the method");
		else log.debug("Ret is size: " + ret.size());
		
		return ret;
	}
	
	public void triggerStateConversion(Patient patient, Concept trigger, Date dateConverted) {
		log.debug("in triggerConversion with patient: " + patient + ", trigger " + trigger + ", and date: " + dateConverted);
		if ( patient != null && trigger != null && dateConverted !=  null ) {
			
			// first, we need to find out what worklows we're dealing with - a little roundabout because of the way the tables are set up
			List<PatientProgram> programs = (List<PatientProgram>)this.getPatientPrograms(patient);
			if ( programs != null ) {
				for ( PatientProgram program : programs ) {
				
					Set<ProgramWorkflow> workflows = this.getCurrentWorkflowsByPatientProgram(program);
					if ( workflows != null ) {
						for ( ProgramWorkflow workflow : workflows ) {
							ConceptStateConversion conversion = this.getConceptStateConversion(workflow, trigger);
							if ( conversion != null ) {
								// that means that there is a conversion to make for this workflow/trigger - let's try to change state
								log.debug("Found conversion: " + conversion);
								ProgramWorkflowState resultingState = conversion.getProgramWorkflowState();
								
								// this is the place to add logic about what conditions we'd want to actually convert for
								if ( program.getActive(dateConverted) || !program.getCurrentState().getState().getTerminal() ) {
									log.debug("Changing patient " + patient + " to state " + resultingState + " in workflow " + workflow);
									this.changeToState(program, workflow, resultingState, dateConverted);									
								} else {
									if ( !program.getActive(dateConverted) ) log.debug("was about to change state, but failed because program not active");
									if ( program.getCurrentState().getState().getTerminal() ) log.debug("was about to change state, but failed because current state is already terminal");
								}
							}
						}
					}

					
				}				
			}
		} else {
			if ( patient == null ) throw new APIException("Attempting to convert state of an invalid patient");
			if ( trigger == null ) throw new APIException("Attempting to convert state for a patient without a valid trigger concept");
			if ( dateConverted == null ) throw new APIException("Invalid date for converting patient state");
		}
	}

	public ConceptStateConversion getConceptStateConversion(ProgramWorkflow workflow, Concept trigger) {
		log.debug("In getcsc with workflow: " + workflow + ", and trigger concept: " + trigger);
		ConceptStateConversion ret = null;

		if (!Context.getUserContext().hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		
		ret = getProgramWorkflowDAO().getConceptStateConversion(workflow, trigger);
		
		return ret;
	}

}