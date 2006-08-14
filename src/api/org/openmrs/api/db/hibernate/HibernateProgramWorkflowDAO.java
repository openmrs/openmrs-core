package org.openmrs.api.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProgramWorkflowDAO;

public class HibernateProgramWorkflowDAO implements ProgramWorkflowDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	private HibernateProgramWorkflowDAO() { }
	
	public HibernateProgramWorkflowDAO(Context context) {
		this.context = context;
	}

	public void createOrUpdateProgram(Program program) throws DAOException {
		log.debug("Creating or updating program " + program);
		if (program.getWorkflows() != null) {
			log.debug("\twith " + program.getWorkflows().size() + " workflows: " + program.getWorkflows());
		}
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			if (program.getProgramId() == null) {
				session.save(program);
			} else {
				session.merge(program);
			}
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to create or update program", e);
			HibernateUtil.rollbackTransaction();
		}
	}

	public Program getProgram(Integer id) throws DAOException {
		Session session = HibernateUtil.currentSession();
		return (Program) session.get(Program.class, id);
	}

	public List<Program> getPrograms() throws DAOException {
		Session session = HibernateUtil.currentSession();
		List<Program> programs = new ArrayList<Program>();
		programs.addAll(session.createQuery("from Program").list());
		return programs;
	}

	public ProgramWorkflow findWorkflowByProgramAndConcept(Integer programId, Integer conceptId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		ProgramWorkflow w = null;
		try {
			HibernateUtil.beginTransaction();
			w = (ProgramWorkflow) session.createQuery("from ProgramWorkflow w where w.program.programid = :programId and w.concept.conceptId = :conceptId")
				.setInteger("programId", programId)
				.setInteger("conceptId", conceptId)
				.uniqueResult();
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return w;
	}

	public void createPatientProgram(PatientProgram p) throws DAOException {
		if (p.getPatient() == null || p.getPatient().getPatientId() == null
				|| p.getProgram() == null || p.getProgram().getProgramId() == null) {
			throw new DAOException("can't create a PatientProgram without already-persisted patient and program");
		}
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.save(p);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to create PatientProgram", e);
			HibernateUtil.rollbackTransaction();
		}
	}
	
	public void updatePatientProgram(PatientProgram p) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.update(p);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to update PatientProgram", e);
			HibernateUtil.rollbackTransaction();
		}
	}

	public PatientProgram getPatientProgram(Integer id) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			PatientProgram program = (PatientProgram) session.get(PatientProgram.class, id);
			HibernateUtil.commitTransaction();
			return program;
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return null;
	}

	public Collection<PatientProgram> getPatientPrograms(Patient patient) {
		List<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			Criteria criteria = session.createCriteria(PatientProgram.class);
			criteria.add(Restrictions.eq("patient", patient));
			criteria.addOrder(org.hibernate.criterion.Order.desc("dateEnrolled"));
			criteria.addOrder(org.hibernate.criterion.Order.desc("dateCompleted"));
			patientPrograms.addAll(criteria.list());
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error(e); 
			HibernateUtil.rollbackTransaction();
		}
		
		return patientPrograms;
	}
	
	public ProgramWorkflow getWorkflow(Integer id) {
		Session session = HibernateUtil.currentSession();
		return (ProgramWorkflow) session.get(ProgramWorkflow.class, id);
	}

	public void createWorkflow(ProgramWorkflow w) {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.save(w);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to create ProgramWorkflow", e);
			HibernateUtil.rollbackTransaction();
		}
	}
	
	public void updateWorkflow(ProgramWorkflow w) {
		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.update(w);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Failed to update ProgramWorkflow", e);
			HibernateUtil.rollbackTransaction();
		}		
	}

	public ProgramWorkflowState getState(Integer id) {
		Session session = HibernateUtil.currentSession();
		return (ProgramWorkflowState) session.get(ProgramWorkflowState.class, id);
	}
	
}
