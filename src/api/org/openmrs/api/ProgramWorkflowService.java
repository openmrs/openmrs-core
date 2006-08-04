package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
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
		return getProgramWorkflowDAO().getPrograms();
	}
	
	public void createOrUpdateProgram(Program p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		
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
		return getProgramWorkflowDAO().getProgram(id);
	}
	
	public void retireProgram(Program p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);

		p.setRetired(true);
		createOrUpdateProgram(p);
	}
	
	// --- ProgramWorkflow ---
	
	public void createWorkflow(ProgramWorkflow w) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PROGRAMS);
		
		getProgramWorkflowDAO().createWorkflow(w);
	}
	
	public ProgramWorkflow getProgramWorkflow(Integer id) {
		return null;
	}
	
	public ProgramWorkflow findWorkflowByProgramAndConcept(Integer programId, Integer conceptId) {
		return getProgramWorkflowDAO().findWorkflowByProgramAndConcept(programId, conceptId);
	}
	
	public void voidProgramWorkflow(ProgramWorkflow w, String reason) {
	}
	
	// --- ProgramWorkflowState ---
	/*
	public void createProgramWorkflowState(ProgramWorkflowState s) {
	}
	
	public ProgramWorkflowState getProgramWorkflowState(Integer id) {
		return null;
	}
	
	public void voidProgramWorkflowState(ProgramWorkflowState s, String reason) {
	}
	*/
	// --- ProgramWorkflowTransition ---
	/*
	public void createProgramWorkflowTransition(ProgramWorkflowTransition t) {
	}
	
	public ProgramWorkflowTransition getProgramWorkflowTransition(Integer id) {
		return null;
	}
	
	public void voidProgramWorkflowTransition(ProgramWorkflowTransition t, String reason) {
	}
	*/
	// --- PatientProgram ---

	public void createPatientProgram(PatientProgram p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PATIENT_PROGRAMS);
		
		if (p.getCreator() == null) {
			p.setCreator(context.getAuthenticatedUser());
		}
		p.setDateCreated(new Date());
		
		getProgramWorkflowDAO().createPatientProgram(p);
	}
	
	public void updatePatientProgram(PatientProgram p) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_PATIENT_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_MANAGE_PATIENT_PROGRAMS);
		
		p.setChangedBy(context.getAuthenticatedUser());
		p.setDateChanged(new Date());
		
		getProgramWorkflowDAO().updatePatientProgram(p);
	}
	
	public PatientProgram getPatientProgram(Integer id) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPatientProgram(id);
	}
	
	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_VIEW_PROGRAMS);
		return getProgramWorkflowDAO().getPatientPrograms(patient);
	}
	
	public void enrollPatientInProgram(Patient patient, Program program, Date enrollmentDate) {
		// TODO: check whether the patient is already in the program at that point
		PatientProgram p = new PatientProgram();
		p.setPatient(patient);
		p.setProgram(program);
		p.setDateEnrolled(enrollmentDate);
		createPatientProgram(p);
	}
	
	public void voidPatientProgram(PatientProgram p, String reason) {
	}
	
	// --- PatientStatus ---
	/*
	public void createPatientStatus(PatientStatus s) {
	}
	
	public PatientStatus getPatientStatus(Integer id) {
		return null;
	}
	
	public void voidPatientStatus(PatientStatus s, String reason) {
	}
	*/

	// --- Actual useful methods ---
	/*
	public Concept getPatientStatus(Patient patient, ProgramWorkflow workflow, Date onDate) {
		return null;
	}
	
	public Map<ProgramWorkflow, Concept> getProgramStatuses(Patient patient, Program program, Date onDate) {
		return null;
	}
	
	public Collection<Patient> getPatients(Program program, Concept status, Date fromDate, Date toDate) {
		return null;
	}
	
	public void changeStatus(Patient patient, ProgramWorkflow workflow, Concept newStatus, Date onDate) throws APIException {	
	}
*/
	
	public Collection<Program> getCurrentPrograms(Patient patient, Date onDate) {
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

}
