package org.openmrs.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.util.OpenmrsConstants;

public class ProgramWorkflowService {
	
	private Log log = LogFactory.getLog(this.getClass());

	private Context context;
	private DAOContext daoContext;
	
	public ProgramWorkflowService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private ProgramWorkflowDAO getProgramWorkflowDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return daoContext.getProgramWorkflowDAO();
	}
	
	// --- Program ---
	
	public List<Program> getPrograms() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPrograms();
	}
	
	public void createOrUpdateProgram(Program p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		
		log.debug("Creating new program");
		if (p.getWorkflows() != null) {
			log.debug("\twith " + p.getWorkflows().size() + " workflows: " + p.getWorkflows());
		}
		
		Date now = new Date();
		if (p.getProgramId() == null) {
			if (p.getCreator() == null) {
				p.setCreator(context.getAuthenticatedUser());
			}
			p.setDateCreated(now);
		} else {
			p.setChangedBy(context.getAuthenticatedUser());
			p.setDateChanged(now);
		}
		
		if (p.getVoided()) {
			if (p.getDateVoided() == null) {
				p.setDateVoided(now);
			}
			if (p.getVoidedBy() == null) {
				p.setVoidedBy(context.getAuthenticatedUser());
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
						w.setCreator(context.getAuthenticatedUser());
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
	
	public Program getProgram(Integer id) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getProgram(id);
	}
	
	public void retireProgram(Program p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);

		p.setVoided(true);
		createOrUpdateProgram(p);
	}
	
	// --- ProgramWorkflow ---
	
	public void createWorkflow(ProgramWorkflow w) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		
		getProgramWorkflowDAO().createWorkflow(w);
	}
	
	public ProgramWorkflow getWorkflow(Integer id) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getWorkflow(id);
	}
	
	public void updateWorkflow(ProgramWorkflow w) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		if (w.getVoided()) {
			if (w.getVoidedBy() == null) {
				w.setVoidedBy(context.getAuthenticatedUser());
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
				s.setCreator(context.getAuthenticatedUser());
			}
			if (s.getDateCreated() == null) {
				s.setDateCreated(new Date());
			}
		}
		getProgramWorkflowDAO().updateWorkflow(w);
	}
	
	public void voidWorkflow(ProgramWorkflow w, String reason) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		w.setVoided(true);
		w.setVoidReason(reason);
		w.setVoidedBy(context.getAuthenticatedUser());
		w.setDateVoided(new Date());
		getProgramWorkflowDAO().updateWorkflow(w);
	}

		
	// --- ProgramWorkflowState ---
		
	public ProgramWorkflowState getState(Integer id) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getState(id);
	}
	
	// --- PatientProgram ---

	public void createPatientProgram(PatientProgram p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		if (p.getCreator() == null) {
			p.setCreator(context.getAuthenticatedUser());
		}
		p.setDateCreated(new Date());
		
		getProgramWorkflowDAO().createPatientProgram(p);
	}
	
	public void updatePatientProgram(PatientProgram p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		
		Date now = new Date();
		p.setChangedBy(context.getAuthenticatedUser());
		p.setDateChanged(now);
		for (PatientState state : p.getStates()) {
			if (state.getDateCreated() == null)
				state.setDateCreated(now);
			if (state.getCreator() == null)
				state.setCreator(context.getAuthenticatedUser());
		}
		
		getProgramWorkflowDAO().updatePatientProgram(p);
	}
	
	public PatientProgram getPatientProgram(Integer id) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPatientProgram(id);
	}
	
	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPatientPrograms(patient);
	}
	
	public void enrollPatientInProgram(Patient patient, Program program, Date enrollmentDate, Date completionDate) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
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
	
	public void voidPatientProgram(PatientProgram p, String reason) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS);
		if (!p.getVoided()) {
			p.setVoided(true);
			p.setDateVoided(new Date());
			p.setVoidedBy(context.getAuthenticatedUser());
			p.setVoidReason(reason);
			updatePatientProgram(p);
		}
	}

	// Utility methods
	
	/**
	 * @return patientIds of all patients who are enrolled in _program_ between _fromDate_ and _toDate_ 
	 */
	public Collection<Integer> patientsInProgram(Program program, Date fromDate, Date toDate) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().patientsInProgram(program, fromDate, toDate);
	}

	public Collection<Program> getCurrentPrograms(Patient patient, Date onDate) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		if (onDate == null) {
			onDate = new Date();
		}
		// date enrolled and date completed are actually java.sql.Timestamp, which can't be compared directly to a java.util.Date
		long atMs = onDate.getTime();
		
		Collection<Program> ret = new HashSet<Program>();
		for (PatientProgram pp : getPatientPrograms(patient)) {
			if ( (pp.getDateEnrolled() == null || pp.getDateEnrolled().getTime() <= atMs)
					&& (pp.getDateCompleted() == null || pp.getDateCompleted().getTime() >= atMs) ) {
				ret.add(pp.getProgram());
			}
		}
		return ret;
	}
	
	// TODO: move this into Patient (probably make this a lazily-loaded hibernate mapping).
	// This is just a quick implementation without changing any hibernate mappings
	public PatientState getLatestState(PatientProgram patientProgram, ProgramWorkflow workflow) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
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

	public List<ProgramWorkflowState> getPossibleNextStates(PatientProgram patientProgram, ProgramWorkflow workflow) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
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
	public boolean isLegalTransition(ProgramWorkflowState fromState, ProgramWorkflowState toState) {
		if (fromState == null)
			return toState.getInitial();
		else if (fromState.equals(toState))
			return false;
		else
			return true;
	}

	public void changeToState(PatientProgram patientProgram, ProgramWorkflow wf, ProgramWorkflowState st, Date onDate) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
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
	
	/**
	 * Voids the last unvoided state in the given workflow, and clears the endDate of the next-to-last unvoided state.  
	 * @param patientProgram
	 * @param wf
	 * @param voidReason
	 */
	public void voidLastState(PatientProgram patientProgram, ProgramWorkflow wf, String voidReason) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_PATIENT_PROGRAMS))
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
			nextToLast.setChangedBy(context.getAuthenticatedUser());
		}
		updatePatientProgram(patientProgram);
	}

	/**
	 * @return Returns true if _patient_ is enrolled in _program_ anytime between _fromDate_ and _toDate_. (null values for those dates mean beginning-of-time and end-of-time)
	 */
	public boolean isInProgram(Patient patient, Program program, Date fromDate, Date toDate) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return patientsInProgram(program, fromDate, toDate).contains(patient.getPatientId());
	}

}